import Network
import System.IO
import Text.Printf
import Data.List

----------------IO Handling (Main function)--------------
---------------------------------------------------------
server = "localhost"
port   = 9897

main :: IO ()
main = do
    hSetBuffering stdout NoBuffering
    h <- connectTo server (PortNumber (fromIntegral port))
    hSetBuffering h NoBuffering
    s <- hGetLine h
    putStrLn s
    if (head (words s))=="Enter"
    then do putStrLn "Haskell"
            hPutStrLn h "Haskell"
            s <- hGetLine h
            putStrLn s
            amount <-getLine
            hPutStrLn h amount
            hPutStrLn h "Ready"
            readit h ""
    else return ()

readit :: Handle -> String -> IO ()
readit h s = do if isAtEnd
                    then do hPutStrLn h decision
                            putStrLn decision
                            readit h ""
                    else do i <- hGetLine h
                            putStrLn i
                            if (s=="\n"||s=="\r\n")
                                then readit h (i++"\n")
                                else readit h (s++i++"\n")
    where isAtEnd = length content > 0 && last content == "End"
          content = words s
          decision = myTurn s

------------Data Structure--------------------
--Here is the data structure for making a Card
----------------------------------------------
data Card = Card {
    value :: Int,
    symbol :: String,
    suit :: String
} deriving (Show, Eq)

-----------------Input for Testing---------------
-------------------------------------------------
input :: String
input = "Haskell\n1\nPython\n7 7 Diamonds\n9 9 Hearts\n10 King Clubs\nHaskell\n"++
		"1 Ace Spades\n1 Ace Diamonds\nJava\n2 2 Clubs\n5 5 Spades\n"++
		"3 3 Hearts\nDealer1\n9 9 Spades\n10 King Diamonds\nEnd"

---------------------Deck------------------------
-- The deck is also comprised of a list of cards.
-------------------------------------------------
theDeck :: [Card]
theDeck = [(Card 1 "Ace" "Diamonds"), (Card 2 "2" "Diamonds"), (Card 3 "3" "Diamonds"), 
           (Card 4 "4" "Diamonds"), (Card 5 "5" "Diamonds"), (Card 6 "6" "Diamonds"), 
           (Card 7 "7" "Diamonds"), (Card 8 "8" "Diamonds"), (Card 9 "9" "Diamonds"), 
           (Card 10 "10" "Diamonds"), (Card 10 "Jack" "Diamonds"), 
           (Card 10 "Queen" "Diamonds"), (Card 10 "King" "Diamonds"),
           (Card 1 "Ace" "Clubs"), (Card 2 "2" "Clubs"),(Card 3 "3" "Clubs"), 
           (Card 4 "4" "Clubs"), (Card 5 "5" "Clubs"), (Card 6 "6" "Clubs"), 
           (Card 7 "7" "Clubs"), (Card 8 "8" "Clubs"), (Card 9 "9" "Clubs"), 
           (Card 10 "10" "Clubs"), (Card 10 "Jack" "Clubs"), 
           (Card 10 "Queen" "Clubs"), (Card 10 "King" "Clubs"),
           (Card 1 "Ace" "Hearts"), (Card 2 "2" "Hearts"), (Card 3 "3" "Hearts"), 
           (Card 4 "4" "Hearts"), (Card 5 "5" "Hearts"), (Card 6 "6" "Hearts"), 
           (Card 7 "7" "Hearts"), (Card 8 "8" "Hearts"), (Card 9 "9" "Hearts"), 
           (Card 10 "10" "Hearts"), (Card 10 "Jack" "Hearts"), 
           (Card 10 "Queen" "Hearts"), (Card 10 "King" "Hearts"),
           (Card 1 "Ace" "Spades"), (Card 2 "2" "Spades"), (Card 3 "3" "Spades"), 
           (Card 4 "4" "Spades"), (Card 5 "5" "Spades"), (Card 6 "6" "Spades"), 
           (Card 7 "7" "Spades"), (Card 8 "8" "Spades"), (Card 9 "9" "Spades"), 
           (Card 10 "10" "Spades"), (Card 10 "Jack" "Spades"), 
           (Card 10 "Queen" "Spades"), (Card 10 "King" "Spades")] 

--------------------PARSER------------------------
--Most of the titles should let you know what they
--do exactly. All of these functions organize info
--given from the socket for the AI to interpret 
--and make a decision.
--------------------------------------------------

--Gets the number of decks

numDecks :: String -> Int
numDecks txt = read first :: Int
	where first = head (tail (words txt))


--Takes a full deck and subtracts whats on the table

makeDeck :: [Card] -> [Card] -> [Card]
makeDeck [] fullDeck = fullDeck
makeDeck (x:xs) fullDeck = makeDeck xs (removeCard x fullDeck)


--Helper function for removing a single card from a list

removeCard :: Card -> [Card] -> [Card]
removeCard _ []                 = []
removeCard x (y:ys) | x == y    = ys
                    | otherwise = y : removeCard x ys


--Organizes all visible cards

makeTable :: [[String]] -> [Card]
makeTable parsed = map makeCard justCards
    where justCards = filter (\n -> length n/=1) parsed


--Organizes a group or cards for anyone
--(Not specific to the current player)

makeMyHand :: [[String]] -> [Card]
makeMyHand parsed = map makeCard justCards
    where justCards = tail parsed


--Helper function that takes a piece of parsed info
--and turns it into a single card. Used above.

makeCard :: [String] -> Card
makeCard parsed = myCard
    where myCard = Card (value) (parsed!!1) (parsed!!2)
          value = read (parsed!!0) :: Int


--Creates a name for the current player

setMyName :: String -> String
setMyName txt = head (head ontable)
    where ontable = map words (lines txt)


--Takes out the first two lines of input
--(Everything in here is just the names of players and their cards)

playersCards :: String -> [[String]]
playersCards txt = init (tail (tail ontable))
    where ontable = map words (lines txt)


--Pulls out the current player and his cards

me :: [[String]] -> String -> [[String]]
me parsed myName = take (head indAft) (drop (head indBef) parsed)
    where names = filter (\n -> length n==1) parsed
          aftMe = afterMe names myName
          indBef = elemIndices [myName] parsed
          indAft = elemIndices [aftMe] (drop (head indBef) parsed)


--Pulls out everyone else except the current player and his cards

them :: [[String]] -> String -> [[String]]
them parsed myName = (fst fstSpl) ++ (snd sndSpl)
    where names = filter (\n -> length n==1) parsed
          aftMe = afterMe names myName
          indBef = elemIndices [myName] parsed
          fstSpl = splitAt (head indBef) parsed
          indAft = elemIndices [aftMe] (snd fstSpl)
          sndSpl = splitAt (head indAft) (snd fstSpl)


--Helper function that figures out who comes after the current player

afterMe :: [[String]] -> String -> String
afterMe parsed name = (concat parsed)!!next
   where at = elemIndices name (concat parsed)
         ind = head at
         next = ind+1

getDealer :: [[String]] -> [[String]]
getDealer parsed = snd disjoint
    where at = elemIndices ["Dealer1"] parsed
          disjoint = splitAt (head at) parsed
----------------------AI------------------------
--This top function is the main AI function. It
--takes the entire input of gamestate and gives
--back either "Hit" or "Stay" using a combination
--of all the parsing and AI functions.
------------------------------------------------
myTurn :: String -> String
myTurn txt = 
    if (mySoftVal==myVal)
    then (hard myVal count dealers)
    else (soft (correctSoft mySoftVal) count dealers)
    where mine = makeMyHand (me (allCards) (setMyName txt))
          myVal = cardsValue(mine)
          mySoftVal = softHand mine
          allCards = playersCards txt
          onTable = makeTable (allCards)
          dealers = head (makeMyHand (getDealer allCards))
          count = countCards onTable

--This algorithm counts the cards and gives a value for each set of cards
--(+1) for 2,3,4,5,6
--(+0) for 7,8,9
--(-1) for 10,J,Q,K,A
--The final value MAY be used to determine decision in difficult scenarios
countCards :: [Card] -> Int
countCards [] = 0
countCards (Card {value = v,symbol = a,suit = s}:xs)
    | v == 1 = countCards xs - 1
    | v > 1 && v < 7 = countCards xs + 1
    | v == 10 = countCards xs - 1
    | otherwise = 0 + countCards xs

--This calculates the value of you cards
cardsValue :: [Card] -> Int
cardsValue [] = 0
cardsValue (Card {value = v,symbol = a,suit = s}:xs) = v + cardsValue xs

--This calculates the value of you cards and if you have an ace
--then it counts it for 11
softHand :: [Card] -> Int
softHand [] = 0
softHand (Card {value = v,symbol = a,suit = s}:xs) = if a == "Ace"
                                                     then 11 + softHand xs
                                                     else v + softHand xs

--This does proper calculation for softhands to make sure that if
--you have more than one Ace, only one of the Aces is counted for 11
correctSoft :: Int -> Int
correctSoft x = if x>21
                then correctSoft (x-10)
                else x
                     
--Decision in twhich the player does have an Ace
soft :: Int -> Int -> Card -> String
soft val count (Card {value = v,symbol = a,suit = s})
    | val<18 = "Hit"
    | val==18 && count<(-1) = "Hit"
    | val==18 && count>1 = "Stay"
    | val==18 && count>=(-1) && count<=1 && ((v>2 && v<7)||(v>8)) = "Hit"
    | val>18 = "Stay"
    | otherwise = "Stay"
    
--Decision in which the player doesn't have an Ace
hard :: Int -> Int -> Card -> String
hard val count (Card {value = v,symbol = a,suit = s})
    | val<12 = "Hit"
    | val==12 && ((count==0 && v<7 && v>3) || count>0) = "Stay"
    | val==12 && ((count==0 && v>6 && v<4) || count<0) = "Hit"
    | val==13 && ((count==0 && v<7) || count>0) = "Stay"
    | val==13 && ((count==0 && v>6) || count<0) = "Hit"
    | val==13 && ((count==0 && v<7) || count>0) = "Stay"
    | val==14 && ((count==0 && v>6) || count<0) = "Hit"
    | val==14 && ((count==0 && v<7) || count>0) = "Stay"
    | val==15 && ((count==0 && v>6) || count<(-1)) = "Hit"
    | val==15 && ((count==0 && v<7) || count>(-2)) = "Stay"
    | val==16 && ((count==0 && v>6) || count<(-1)) = "Hit"
    | val==16 && ((count==0 && v<7) || count>(-2)) = "Stay"
    | val>16 && count>(-3)= "Stay"
    | val==17 && count<(-2) = "Hit"
    | otherwise = "Stay"
