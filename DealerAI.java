import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.System.*;

public class DealerAI {
  private static String currentPlayer = "Dealer1";
  private static Boolean playerSoft = false;
  private static Boolean dealerSoft = false;
  private static ArrayList<Integer> playerCards = new ArrayList<Integer>();
  private static ArrayList<Integer> dealerCards = new ArrayList<Integer>();
  private static ArrayList<String> gameState = new ArrayList<String>();
  private static String host = "localhost";
  private static int port = 9897;
  private static Socket listener;
  private static PrintWriter out;
  private static BufferedReader in;
  private static String msg;
  
  
  /* This method returns hit or no hit
   * Assumging dealer stops at a soft 17
   */
  public static String nextMove() {
    int playerTotal = getDealTotal();
    System.out.println("player total is : " + playerTotal);
    if (playerTotal == 21) {
       return "Stay";
    } else if (playerSoft) {
      if ((playerTotal >= 8) && (playerTotal <= 11)) {
         return "Stay";
      } else if (playerTotal < 17) {
        return "Hit";
      } else {
        return "Stay"; 
      }
    } else {
      if (playerTotal < 17) {
        return "Hit";
      } else {
        return "Stay";
      }
    }
  }
  
  public static void resetCards(){
    playerCards.clear();
    dealerCards.clear();
    gameState.clear();
  }
  
  
  /* sum total of the players cards
   */
  public static int getPlayTotal(){
    int total = 0;
    for (int i=0; i < playerCards.size(); ++i) {
      if (playerCards.get(i) == 1) {
        playerSoft = true;
      }
      total = total + playerCards.get(i);
    }
    return total;
  }
  
  
  /* sum total of the dealers cards
   */ 
  public static int getDealTotal(){
    int total = 0;
    for (int i=0; i < dealerCards.size(); ++i) {
      if (dealerCards.get(i) == 1) {
        dealerSoft = true;
      }
      total = total + dealerCards.get(i);
    }
    return total;
  }
  
  
  /* checks if a string is a number
   */
  public static Boolean isNum (String str) {
    try {
      int d = Integer.parseInt(str);
    } catch (NumberFormatException n) {
      return false;
    }
    return true;
  }
  
  public static void printState(){
     for (String s: gameState) {
         System.out.println(s);
     }
     System.out.println();
  }
  
  
  /* Parses the blackjack game state, and puts the current players
   * and dealers cards into an arraylist 
   */ 
  public static void parseGameState (){
    String[] splitLine;
    for (int i=2; i < gameState.size(); ++i) {
      if (currentPlayer.contains(gameState.get(i))) {
        for ( ++i;i<gameState.size();++i){
          splitLine = gameState.get(i).split(" ");
          if (!isNum(splitLine[0])) {
            break;
          } else {
            dealerCards.add (Integer.parseInt(splitLine[0]));
          }
        }
      }
    } 
  }
  
  public static void run() throws IOException {
    try {
      msg = in.readLine();
      if (msg.contains("Enter your name")) {
        System.out.println(msg);
        out.println("Dealer");
      }
      while (true) {
        msg = in.readLine();
        if (msg == null) {
           break;
        }
        if (msg.contains("How many players")) {
          System.out.println("Number of players");
          out.println(1);
        } else {
          while (true) {
            if (msg.contains("End")) {
               break;
            }
            if (!msg.equals("")){
              gameState.add(msg);
            }
            msg = in.readLine();
          } 
          printState();
          parseGameState();
          String move = nextMove();
          System.out.println(move + "\n");
          out.flush();
          out.println(move);
          resetCards();
        }
       // out.flush();
       // out.println("Ready");
      }
    } finally {
      listener.close();
      out.close();
      in.close();
    }
  }

  
  
  public static void main (String[] args) {
    try
    {
      listener = new Socket (host, port);
      out = new PrintWriter (listener.getOutputStream(), true);
      in = new BufferedReader (new InputStreamReader (listener.getInputStream()));
      run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
