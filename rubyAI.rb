require 'socket'

$currentPlayer
$playerCards = Array.new
$dealerCards = Array.new
$playerSoft = false
$gameState = Array.new
$deck


# returns the sum of the players cards
def getPlayTotal()
	total = 0
	i = 0
	while i < $playerCards.length
		x = $playerCards.at(i)
		if x == 1
			$playerSoft = true
		end	
		total = total + x
		i += 1
	end
	return total
end

# return the sum of the dealers cards
def getDealTotal()
	total = 0
	i = 0
	while i < $dealerCards.length
		x = $dealerCards.at(i)
		if x == 1
			$playerSoft = true
		end	
		total = total + x
		i += 1
	end
	return total
end

# returns true if the string is a number
def is_num(str)
	return true if str =~ /^\d+$/
    true if Int(str) rescue false
end

def clearArrays()
	$gameState.clear()
	$playerCards.clear()
	$dealerCards.clear()
	$playerSoft = false
end

def printState()
	$gameState.each { |x| puts x}
end

#
def nextMove() 
    playerTotal = getPlayTotal()
    dealerTotal = getDealTotal()
    puts "Player total is  #{playerTotal}"
    if (playerTotal == 21) 
       return "Stay"
    end
    if ($playerSoft)
       if ((playerTotal >= 8) && (playerTotal <= 11)) 
         return "Stay"
       else 
         return "Stay"    
       end
    else 
      if (playerTotal < 12) 
        return "Hit"
      else 
        if (playerTotal < 15)
           return "Hit"
        else
           return "Stay"
        end
      end
   end
end
    

=begin
parseGameState function parses the current game state
=end
def parseGameState()
	splitState = $gameState
	$currentPlayer = splitState.at(0)
	dealer = "Dealer"
	$deck = splitState.at(1)
	i = 2
	while i < splitState.length do
	    if ($currentPlayer.include?(splitState.at(i)))
	        i += 1
	    	while i < splitState.length do
	    	   splitLine = (splitState.at(i)).split(" ")
	    	   if (!is_num(splitLine.at(0)))
	    		   break
	    	   else 
	    	   	   $playerCards.push((splitLine.at(0)).to_i())
	    	   end
	    	   i +=1    
	    	end 
	    end
	    if dealer.include?(splitState.at(i)) 
	    	i += 1
	    	while i < splitState.length do
	    	   splitLine = (splitState.at(i)).split(" ")
	    	   if (!is_num(splitLine.at(0)))
	    		   break
	    	   else 
	    	   	   $dealerCards.push((splitLine.at(0)).to_i())
	    	   	   break;
	    	   end
	    	   i += 1   
	    	end 
	    end
		i +=1 
	end
end


def main()
	hostname = "localhost"
	port = 9897
	begin 
	    client = TCPSocket.open(hostname,port)
	rescue Exception => msg
	    puts msg
	    exit 1
	end
	puts "connected to server"
	line = client.gets
	if line.include? "Enter your name"
	    client.puts "Ruby"
	end
	while true
	    line = client.gets
	    if line == nil
	        break
	    end
		if line.include? "How many players"
		   puts "How many players?"
		   sysIn = gets.chomp
		   number = sysIn.to_i
 		   client.puts (number)
		else 
		   while true
		       if line.include? "End"
		          break;
		       end
		       if (!(line.eql? "") && !(line.eql? "\n"))
		          $gameState.push(line)  
		       end
		       line = client.gets   
		   end
		   printState()
		   parseGameState()
		   turn = nextMove()
		   puts turn
		   puts ""
		   clearArrays()
		   client.puts(turn) 
		end
		#client.puts("Ready")
	end
	client.close
end

if __FILE__ == $0
   main()
end
