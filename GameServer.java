/** Main GameServer that creates decks, creates games, deals out cards
to players, receives responses, keeps games going, & keeps statistics. **/

import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;

public class GameServer{

    // Fields: Deck, players, player queue, gameState, client handlers, W/L/B/D stats, and avg times
    public static Deck deck = new Deck();
    public static List<String> players = new ArrayList<String>();
    public static List<String> playerQueue = new ArrayList<String>();
    public static String gameState;
    public static List<Handler> handlers = new ArrayList<Handler>();
    public static List<int[]> statistics = new ArrayList<int[]>();
    public static List<List<Long>> moveTimes = new ArrayList<List<Long>>();
    
    public static void main(String [] args){
        // Basic messages. Also asks how many games should be played.
        System.out.println("The Game Server is now running.");
        System.out.println("How many games would you like to play?");

        
        // Get number of games based on stdin.
        Scanner console = new Scanner(System.in);
        int numberGames = Integer.parseInt(console.nextLine());

        // Find out how many languages will be connecting.
        System.out.println("How many languages will be connecting?");
        int numberClients = Integer.parseInt(console.nextLine());
        
        // Connect players via sockets, decide number of decks based on
        // number of players, and add this info to gameState.
        connectPlayers(numberClients);
        int numberDecks = (int)Math.ceil(players.size()/3.0);
        
        // Loop which carries out game numberGames times. 
        for(int i = 0; i < numberGames; i++){
            gameState = numberDecks + "\n";
            deck.initialize(numberDecks);
            if(i > 0){
                resetPlayers();
            }
            shufflePlayerOrder();
            if(deal()){
                askMove();
            }  
            calculateWinners();
            calculateTimes();
            System.out.println(gameState + '\n');
        }
        benchmark();
    }

    public static void connectPlayers(int numberClients){
        int numberConnections = 0;
        try{
            // Opening a socket and working with a socket must always 
            // be within a try block, to catch a possible IO exception.
            ServerSocket listener = new ServerSocket(9897);
            while(numberConnections < numberClients){
                // Only allow 4 connections, 1 for each language + dealer.
                handlers.add(new Handler(listener.accept()));
                handlers.get(numberConnections).run();
                // Create entries in moveTimes and in statistics, for EACH handler added.
                statistics.add(new int[4]);
                moveTimes.add(new ArrayList<Long>());
                numberConnections++;
            }
            listener.close();
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }
    
    public static void resetPlayers(){
        // For every iteration after the 1st, put everything from
        // playerQueue into players, so that it can be shuffled again.
        for(int i = 0; i < playerQueue.size(); i++){
            players.add(playerQueue.get(i));
        }
        playerQueue.clear();
    }
    
    public static void shufflePlayerOrder(){
        int playerTurn;
        // Create all the players.
        // players.add("Java");
        // players.add("Haskell");
        // players.add("Python");
        // One by one, insert players into player queue in random order.
        // Grabs players from players list, and puts it into playerQueue
        // in random order. Also removes them from players.
        players.remove("Dealer1");
        int psize = players.size();
        for(int i = 0; i <psize; i++){
            playerTurn = (int)(Math.random() * players.size());
            playerQueue.add(players.get(playerTurn));
            players.remove(playerTurn);
        }
        // Once player queue is created, add Dealer at the end of queue.
        playerQueue.add("Dealer1");
    }
    
    public static boolean deal(){
        for(int j = 0; j < playerQueue.size(); j++){
            gameState += (playerQueue.get(j) + "\n");
            for(int i = 0; i < 2; i++){
                Deck.Card card = randomCard();
                gameState += cardInfo(card);
            }
        }
        gameState += "End\n\n";
        int dealerCards = calculateHand("Dealer1");
        if(dealerCards == 21) return false;
        else return true;
    }
    
    public static void askMove(){
        Handler currentHandler = null;
        String playerName = "";
        String buffer = "";
        int cardSum = 0;
        for(int p = 0; p < playerQueue.size(); p++){
            playerName = playerQueue.get(p);
            buffer = playerName;
            gameState = playerName + "\n" + gameState;
            cardSum = calculateHand(buffer);
            // While the last character in playerName is a digit...
            // remove the last character.
            while(Character.isDigit(playerName.charAt(playerName.length()-1))){
                playerName = playerName.substring(0, playerName.length()-1);
            }
            for(int i = 0; i < handlers.size(); i++){
                if (playerName.equals(handlers.get(i).name)){
                    currentHandler = handlers.get(i);
                    break;
                }
            }
            while(true){
                String move = currentHandler.askMove();
                if(move.equals("Stay")) break;
                else if(move.equals("Hit")){
                    Deck.Card hitCard = randomCard();
                //    System.out.println(cardInfo(hitCard));
                    addParse(hitCard, buffer);
                    cardSum = calculateHand(buffer);
                  //  System.out.println(cardSum);
                    if(cardSum > 21) break;
                }     
            }
            int indexOfNewline = gameState.indexOf('\n');
            gameState = gameState.substring(indexOfNewline + 1);
        }
    }
    
    public static void calculateWinners(){
        gameState = "Dealer1\n" + gameState;
        int dealerTotal = calculateHand("Dealer1");
        int indexOfNewline = gameState.indexOf('\n');
        gameState = gameState.substring(indexOfNewline + 1);

        int playerTotal = 0;
        int currentPlayer = 0;
        boolean dealerBust = false;
        String playerName;

        if (dealerTotal > 21) dealerBust = true;
        
        for(int i = 0; i < playerQueue.size() - 1; i++){
            playerName = playerQueue.get(i);
            gameState = playerName + "\n" + gameState;
            playerTotal = calculateHand(playerName);
            while(Character.isDigit(playerName.charAt(playerName.length()-1))){
                playerName = playerName.substring(0, playerName.length()-1);
            }
            for(int j = 0; j < handlers.size(); j++){
                if (playerName.equals(handlers.get(j).name)){
                    currentPlayer = j;
                    break;
                }
            }    
            if(playerTotal > 21){
                statistics.get(currentPlayer)[1]++;
                statistics.get(currentPlayer)[2]++;
            }
            else if(playerTotal <= 21 && dealerBust){
                 statistics.get(currentPlayer)[0]++;
            }
            else if(playerTotal < dealerTotal && !dealerBust){
                statistics.get(currentPlayer)[1]++;
            }
            else if(playerTotal <= 21 && playerTotal > dealerTotal){
                statistics.get(currentPlayer)[0]++;
            }
            else if(playerTotal == dealerTotal && !dealerBust){
                statistics.get(currentPlayer)[3]++;
            }
            indexOfNewline = gameState.indexOf('\n');
            gameState = gameState.substring(indexOfNewline + 1);
        }
        
    }

    public static void calculateTimes(){
        long sum = 0;
        long average = 0;
        int length = 0;

        for(int i = 0; i < moveTimes.size(); i++){
            length = moveTimes.get(i).size();
            for(int j = 0; j < moveTimes.get(i).size(); j++){
                sum += moveTimes.get(i).get(j);
            }
            moveTimes.get(i).clear();
            average = sum/length;
            moveTimes.get(i).add(average);
        }
    }

    public static void benchmark(){
         for(int i = 0; i < handlers.size(); i++){
            if(handlers.get(i).name.equals("Dealer")) continue;
            System.out.println(handlers.get(i).name);
            for(int j = 0; j < 4; j++){
                switch (j){
                    case 0 : System.out.print("W: "); 
                             break;
                    case 1 : System.out.print("L: ");
                             break;
                    case 2 : System.out.print("B: ");
                             break;
                    case 3 : System.out.print("D: ");
                             break; 
                }
                System.out.println(statistics.get(i)[j]);
            }
            System.out.print("Average Move Time: ");
            System.out.print(moveTimes.get(i).get(0) / 1000000.0);
            System.out.println(" ms\n");
         }
    }

    /** Adds card to certain player's hand. Finds occurence of player within 
    gameState string, then finds occurence of the very next newline. Then puts back
    substring upto the newline, then adds the string representation of the new card, 
    and then adds the remaining substring back to gameState. **/
    public static void addParse(Deck.Card card, String currentPlayer){
        int indexOfPlayer = gameState.indexOf(currentPlayer);
        indexOfPlayer = gameState.indexOf(currentPlayer, indexOfPlayer+  1);
        int indexOfNewline = gameState.indexOf('\n', indexOfPlayer);
        gameState = gameState.substring(0, indexOfNewline + 1) + cardInfo(card) + gameState.substring(indexOfNewline + 1);
    }
    
    // Function to generate random card from the deck and return it.
    private static Deck.Card randomCard(){
        int randomCard = (int)(Math.random() * (deck.length() - 2));
        Deck.Card card = deck.drawCard(randomCard);
        return card;
    }
    
    // Function to take in card, read its info, & output all its info as a string.
    private static String cardInfo(Deck.Card card){
        return(String.valueOf(card.value) + " " + card.symbol + " " + card.suit + "\n");
    }
    
    private static int calculateHand(String currentPlayer){
        int indexOfPlayer = gameState.indexOf(currentPlayer);
        indexOfPlayer = gameState.indexOf(currentPlayer, indexOfPlayer+  1);
        int indexOfNewLine = gameState.indexOf('\n', indexOfPlayer);
        String value = "";
        int sum = 0;
        boolean aces = false;
        while(true){
            indexOfNewLine += 1;
            value += gameState.charAt(indexOfNewLine);
            if(!Character.isDigit(value.charAt(0))) break;
            if(value.equals("1")) aces = true;
            if(gameState.charAt(indexOfNewLine + 1) == '0')
                value += "0";
            sum += Integer.parseInt(value);
            indexOfNewLine = gameState.indexOf("\n", indexOfNewLine);
            value = "";
        }
        // Check whether this person wants to make his Ace, or one of his
        // Aces, an 11 or a 1.
        if(aces){
            if(sum <= 11) sum += 10;
        }
        return sum;
    }
}