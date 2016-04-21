<b>Instructions to run:</b><br>
<b>Requirements:</b><br>
Maven 3.x <br>
Mongo 3.x Server Running <br>
The unit tests will also fail if there is no mongo server running locally. <br>
<b>Commands:</b><br>
cd /project-path/gameofstones/ <br>
mvn jetty:run <br><br>
Go to your browser (Only tested on Chrome) and go to http://localhost:8080

![alt tag](https://raw.githubusercontent.com/shravanask/gameofstones/master/src/main/webapp/static/GameOfStones.png)

<b>Board Setup</b> <br>
Each of the two players has his six pits in front of him. To the right of the six pits, each player has a larger pit. In each of the six round pits are put six stones when the game starts.

<b>Rules</b> <br> <br>

<b>Game Play</b> <br>
The player who begins with the first move picks up all the stones in anyone of his own six pits, and sows the stones on to the right, one in each of the following pits, including his own big pit. No stones are put in the opponents' big pit. If the player's last stone lands in his own big pit, he gets another turn. This can be repeated several times before it's the other player's turn.

<b>Capturing Stones</b> <br>
During the game the pits are emptied on both sides. Always when the last stone lands in an own empty pit, the player captures his own stone and all stones in the opposite pit (the other players' pit) and puts them in his own. 

<b>The Game Ends</b> <br>
The game is over as soon as one of the sides run out of stones. The player who still has stones in his pits keeps them and puts them in his/hers big pit. Winner of the game is the player who has the most stones in his big pit.
