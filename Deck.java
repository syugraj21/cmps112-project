// Deck implementation using singly linked list with cards as elements.

import java.io.*;
import java.util.*;
import java.lang.*;

class Deck{

    public class Card{
        // Fields
        int value;
        String symbol;
        String suit;
        Card next;
        // Constructor
        Card (int value, String symbol, String suit){
            this.value = value;
            this.symbol = symbol;
            this.suit = suit;
            this.next = null;
        }
        public String toString(){
            return (String.valueOf(value) + " " + symbol + " " + suit);}
   }
   
    // Fields
    public Card top;
    public String [] suits = {"Hearts", "Spades", "Clubs", "Diamonds"};
    public String [] faceCards = {"Jack", "Queen", "King"};
    
    // Constructor
    Deck(){top = null;}
   
    // Access Methods
    int length(){
        int count = 0;
        Card temp = this.top;
        while(temp != null){
            count++;
            temp = temp.next;}
        return count;
    }
    
    Card drawCard(int index){
        Card temp;
        if(index == 0){
            temp = this.top;
            this.top = this.top.next;
            return temp;
        }
        temp = this.top;
        Card result;
        for(int i = 1; i < index; i++){
            temp = temp.next;
        }
        result = temp.next;
        temp.next = temp.next.next;
        return result;
    }
    
    // Manipulation Methods
    void insert(int value, String symbol, String suit){
        Card newCard = new Card (value, symbol, suit);
        newCard.next = this.top;
        this.top = newCard;
    }
    
    void initialize(int numberDecks){
        /** First clear the top to delete deck, then initialize top
        as joker so you never have to worry about inserting to the 
        beginning. Later, Joker can be ignored. **/
        this.top = null;
        this.top = new Card(0, "0", "Joker");
        
        // Based on how many decks you want, start adding cards:
        for(int n = 0; n < numberDecks; n++){
        
            // Insert cards 2-10, one time for each suit.
            for(int i = 2; i <= 10; i++){
                for(int j = 0; j < 4; j++){
                    insert(i, String.valueOf(i), this.suits[j]);
                }
            }

            // Insert Aces, one time for each suit.
            for(int k = 0; k < 4; k++){
                insert(1, "Ace", this.suits[k]);
            }
            
            // Insert all face cards, one time for each suit.
            for(int z = 0; z < 3; z++){
                for(int x = 0; x < 4; x++){
                    insert(10, this.faceCards[z], this.suits[x]);
                }
            }
        }    
        
    }
}