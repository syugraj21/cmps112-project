import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.System.*;

public class javaAI {
  private static String currentPlayer;
  private static Boolean playerSoft = false;
  private static Boolean dealerSoft = false;
  private static ArrayList<Integer> playerCards = new ArrayList<Integer>();
  private static ArrayList<Integer> dealerCards = new ArrayList<Integer>();
  private static ArrayList<String> gameState = new ArrayList<String>();
  private static ArrayList<Integer> otherCards = new ArrayList<Integer>();
  private static String host = "localhost";
  private static int port = 9897;
  private static int deckSize = 52;
  private static int deckNum = 1;
  private static int highCardNum = 0;
  private static int lowCardNum = 0;
  private static Socket listener;
  private static PrintWriter out;
  private static BufferedReader in;
  private static String msg;
  
  
  /* This method returns hit or no hit
   * Assumging dealer stops at a soft 17
   */
  public static String nextMove() {
    int playerTotal = getPlayTotal();
    int dealerTotal = getDealTotal();
    String nxtCard = nextCard();
    System.out.println("player total is "+ playerTotal);
    if (playerTotal == 21) {
       return "Stay";
    } else if (playerSoft) {
       if ((playerTotal >= 8) && (playerTotal <= 11)) {
         return "Stay";
       } else {
         if (playerTotal < 17 ){
           return "Hit";        
         } else {
           return "Stay";
         }
       }
    } else {
      if (playerTotal < 12) {
        return "Hit"; 
      } else {
        if ( (playerTotal <= 15) ) {
           return "Hit";
        } else {
           return "Stay";
        }
      }
   }
 }
  
  /*
    returns low if the probability of the next is low,
    or return high if the probability of the next card high
  */
  public static String nextCard() {
    if ((highCardNum >= lowCardNum) ){
       return "low";
    } else {
       return "high";
    }
  }
  
  public static void resetCards(){
    playerCards.clear();
    dealerCards.clear();
    otherCards.clear();
    gameState.clear();
    playerSoft = false;
    dealerSoft = false;
    highCardNum = 0;
    lowCardNum = 0;
    deckSize = 52;
    deckNum = 1;
  }
  
  public static void countCards(){
  	for (int i =0; i< otherCards.size(); ++i) {
  	  if (otherCards.get(i) > 7 ) {
  	    ++highCardNum;
  	  } else {
  	  	++lowCardNum;
  	  }
  	}
  }
  
  
  /* sum total of the players cards
   */
  public static int getPlayTotal(){
    int total = 0;
     //System.out.println("player size is " + dealerCards.size());
    for (int i=0; i < playerCards.size(); ++i) {
      if (playerCards.get(i) == 1) {
        playerSoft = true;
      }
      if (playerCards.get(i) > 7) {
         ++highCardNum; 
      } else {
         ++lowCardNum;
      }
      total = total + playerCards.get(i);
    }
    return total;
  }
  
  
  /* sum total of the dealers cards
   */ 
  public static int getDealTotal(){
    int total = 0;
    //System.out.println("dealer size is " + dealerCards.size());
    for (int i=0; i < dealerCards.size(); ++i) {
      if (dealerCards.get(i) == 1) {
        dealerSoft = true;
      }
      if (playerCards.get(i) > 7) {
         ++highCardNum; 
      } else {
         ++lowCardNum;
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
    String dealer = "Dealer";
    currentPlayer = gameState.get(0);
    String deck = gameState.get(1);
    deckNum = Integer.parseInt(deck);
    //System.out.println ("\n the current player is " + currentPlayer);
    //System.out.println ("the size is " + gameState.size() + "\n");
    for (int i=2; i < gameState.size(); ++i) {
      if (currentPlayer.contains(gameState.get(i))) {
        for ( ++i;i<gameState.size();++i){
          splitLine = gameState.get(i).split(" ");
          if (!isNum(splitLine[0])) {
            break;
          } else {
            playerCards.add (Integer.parseInt(splitLine[0]));
          }
        }
      } 
      if ( gameState.get(i).contains(dealer)) {
        for ( ++i;i<gameState.size();++i){
          splitLine = gameState.get(i).split(" ");
          if (!isNum(splitLine[0])) {
            break;
          } else {
            dealerCards.add (Integer.parseInt(splitLine[0]));
            break;
          }
        }
        break;
      } 
    }
  }
  
  public static void run() throws IOException {
    try {
      msg = in.readLine();
      if (msg.contains("Enter your name")) {
        System.out.println(msg);
        out.println("Java");
      }
      while (true) {
        msg = in.readLine();
        if (msg == null) {
           break;
        }
        if (msg.contains("How many players")) {
          System.out.println("Number of players");
          Scanner getNumPlayer = new Scanner(System.in);
          String numPly = getNumPlayer.nextLine();
          out.println(numPly);
          getNumPlayer.close();
        } else {
          while (true) {
            if (msg.contains("End")) {
               break;
            }
            if (!(msg.equals(""))){
              gameState.add(msg);
            }
            msg = in.readLine();
          } 
          printState();
          parseGameState();
          String move = nextMove();
          resetCards();
          System.out.println(move+"\n");
          out.flush();
          out.println(move);
        }
      //  out.flush();
      //  out.println("Ready");
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
