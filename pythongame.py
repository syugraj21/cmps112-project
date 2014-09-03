#!/usr/bin/python

import array
import socket
import sys

total_player = 0
total_dealer = 0
currentLine = 0
upper = 0
lower = 0
curretPlayer = ""
playerCards = []
dealerCards = []	
deck_number = 0
gameState =""
fir_half = ""

def printState():
	global gameState
	for x in gameState:
		print x	

def clearAll():
	#remove items from global arrays
	global playerCards
	global dealerCards
	global gameState
	global total_player
	global total_dealer
	playerCards[:] = []
	dealerCards[:] = []
	gameState = ""
	total_player = 0
	total_dealer = 0

def calc_player(x):
    global pcards
    global total_player
    for i in range(len(x)):
        total_player = total_player + x[i]
        if total_player > 21:
            for k in range(len(x)):
                if x[k] == 11:
                    x.remove(11)
                    x.insert(k, 1)
                    total_player = total_player - 10





		
def calc_dealer(x):
	global total_dealer
	for i in range(len(x)):
		total_dealer = total_dealer + x.pop()			
	
def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False	




def nextMove (int):
    #print "inside next move"
    total_upper = (upper/(deck_number*52)) * 100
    total_lower = (lower/(deck_number*52)) * 100
    print "total_upper is, ", total_upper
    print "total_lower is, ", total_lower
    if int == 21 or int == 20:
        return "Stay\n"
    else:
        if int > 17:
            if total_upper > 35:
                return "Hit\n"
            else:   
                return "Stay\n"
        elif int <= 10:
            return "Hit\n"
        elif int > 10 and int < 18:
            if int > 16 and total_upper > 28:
                return "Hit\n"
            elif int < 15 and total_upper < 25:
                return "Hit\n"
            else:
                return "Stay\n"

# 2 decks means that 104 cards are going to be in circulation


def parseGamState (str):
   global currentPlayer
   global playerCards
   global dealerCards
   global lower
   global upper
   global deck_number
   i = 0
   dealer = "Dealer1"
   card_number = 0
   splitStr = str.split("\n"); #edit to read in input
   print "*******************************************"
   print splitStr
   print "*******************************************"
   while True:
       value = splitStr[0]
       if ("Python" not in value):
          splitStr.pop(0)
       #elif (value == "\n"):
        #  splitStr.pop(0)
       #elif (value == ""):
        #  splitStr.pop(0)
       else:
          break
   currentPlayer = splitStr.pop(0)
   print "current player is ", currentPlayer
   temp = splitStr.pop(0) 
   print "deck size is ", temp
   deck_number = int(temp)
   for x in range(len(splitStr)):
   	
   	if currentPlayer in splitStr[x]:
   		x = x +1
   		while x < len(splitStr):
   			splitLine = splitStr[x].split(" ")
   			x = x + 1
   			if is_number(splitLine[0]) == False:
   				break
   			else:
   				card_number = int(splitLine[0])
   				if card_number == 1:
   					playerCards.append(11)
   				else:
   					playerCards.append(int(splitLine[0]))
   	elif dealer in splitStr[x]:
   		x = x +1
   		i = x
   		splitLine = splitStr[x].split(" ")
   		x = x + 1
   		#i = x
   		dealerCards.append(int(splitLine[0]))
   		card_number = int(splitLine[0])
		if card_number > 7:
			upper = upper + 1
		else:
			lower = lower + 1	
		break	
   	
   	else:
   		splitLine = splitStr[x].split(" ")
   		if is_number(splitLine[0]) == True:
   			card_number = int(splitLine[0])
   			if card_number > 7:
   				upper = upper + 1
   			else:
   				lower = lower + 1	

#msg = []

def main():
	global gameState
	global fir_half
	client = socket.socket()
	portNum  = 9897
 	
 	try :
 	    client.connect(("localhost",portNum))
 	except:
 	    print "unable to connect to server"
 	    sys.exit()

	print "connected to server"
	msg = client.recv(256)
	if ("Enter your name" in msg):
		print "sent name"
		client.send("Python\n")
	while (True):
		msg = client.recv(256)
		if msg is None:
			break
		if ("How many players" in msg):
			number = raw_input ('How many players ')
			num = int(number)
			client.send(str(num)+ "\n")
		else:
			temp = ""
			while (True):
				temp += msg
				if ("End" in temp):
					splitTemp = temp.split("End")
					sec_half = splitTemp[0]
					gameState = fir_half + sec_half
					fir_half = splitTemp[1]
					break
				msg = client.recv(256)
			print "+++++++++++++++++++++++++++++++++++++++++ before parse in main	"
			print gameState
			parseGamState(gameState)
			calc_player(playerCards)
			calc_dealer(dealerCards)
			next = nextMove(total_player)
			clearAll()
			print next
			client.send(next)
		#client.send("Ready\n")
	client.close()
	

if __name__ == "__main__":
    main()



