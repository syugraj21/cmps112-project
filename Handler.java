/** Secondary class which will handle each player connecting to a socket,
including reading and writing for each Handler. **/

import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.*;

class Handler extends Thread{

    // Fields for each Handler
    public String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Constructor
    public Handler(Socket socket){
        this.socket = socket;
    }
    
    // Run method to start thread.
    public void run(){
        try{
            // Create input and output streams with the socket connection.
            in = new BufferedReader(new InputStreamReader(
                 socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // Infinite loop that asks player name, checks if it exists
            // in players list, & if not, then adds it, along with a 
            // counter based on number of players of that language.
            while(true){
                out.println("Enter your name.");
                this.name = in.readLine();
                if(this.name == null){
                    return;
                }
                if(!GameServer.players.contains(this.name)){
                    out.println("How many players of " + this.name + " ?");
                    String numberPlayers = "";
                    while(true){
                        numberPlayers = in.readLine();
                        System.out.println(numberPlayers);
                        if(numberPlayers != "") break;
                    }
                    for(int i = 1; i <= Integer.parseInt(numberPlayers); i++){
                        GameServer.players.add(this.name + Integer.toString(i));
                    }
                    break;
                }else{
                    out.println("This language player already exists.");
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Method to ask this player their move.
    public String askMove(){
        int currentHandler = 0;
        String result = "";
        long startTime = System.nanoTime();
        try{
            out.flush();
            out.println(GameServer.gameState);
            result = (in.readLine());
            System.out.println(result);
        }catch (IOException e) {
            e.printStackTrace();
        }
        long estimatedTime = System.nanoTime() - startTime;

        // Find the current language handler so you can add to his move times.
        for(int i = 0; i < GameServer.handlers.size(); i++){
            if(this.name.equals(GameServer.handlers.get(i).name)){
                currentHandler = i;
                break;
            }
        }
        // Add his estimated move time to the list of his language
        // move times, only if his message isn't "Ready".
        if(!result.equals("Ready")){
            GameServer.moveTimes.get(currentHandler).add(estimatedTime);
        }
        return result;
    }
  
}