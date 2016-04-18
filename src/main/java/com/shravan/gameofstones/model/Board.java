package com.shravan.gameofstones.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shravan.gameofstones.core.Mongodb;
import com.shravan.gameofstones.exception.BadMoveException;

/**
 * Simple class to encapsulate the board data. (Player pits, and the number of
 * stones in each pit)
 * 
 * @author shravanshetty
 */
public class Board {

    private static Logger log = Logger.getLogger(Board.class.getSimpleName());

    @MongoId
    @MongoObjectId
    private String id;
    private List<Integer> player1Pits;
    private List<Integer> player2Pits;

    //simple constructors
    public Board() {
    }

    /**
     * Method to setup the board with 6 stones in each pit.
     * 
     * @param persistBoard
     *            Persist this board in mongo, if set to true.
     * @return Returns a {@link Board} instance that is setup
     */
    public static Board setupBoard(boolean persistBoard) {

        Board board = new Board();
        board.player1Pits = setUpPits(new ArrayList<Integer>(7));
        board.player2Pits = setUpPits(new ArrayList<Integer>(7));
        if (persistBoard) {
            board.createOrUpdate();
        }
        return board;
    }

    // getters and setters
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public List<Integer> getPlayer1Pits() {

        return player1Pits;
    }

    public void setPlayer1Pits(List<Integer> player1Pits) {

        //a player pit cannot be more than 7
        if (player1Pits != null && player1Pits.size() > 7) {
            player1Pits.subList(0, 7);
        }
        this.player1Pits = player1Pits;
    }

    public List<Integer> getPlayer2Pits() {

        return player2Pits;
    }

    public void setPlayer2Pits(List<Integer> player2Pits) {

        //a player pit cannot be more than 7
        if (player2Pits != null && player2Pits.size() > 7) {
            player2Pits.subList(0, 7);
        }
        this.player2Pits = player2Pits;
    }

    /**
     * Method to see if Player1 is the current leader of the board. Basically
     * just checks if player1 has more stones in the big pit compared to
     * player2.
     * 
     * @return True if player1 is leading, false if player2 is leading or null
     *         if no winner is established (not properly setup boards or on a
     *         draw)
     */
    @JsonIgnore
    public Boolean isPlayer1Leader() {

        Boolean result = false;
        if (player1Pits != null && player1Pits.size() == 7) {
            if (player2Pits != null && player2Pits.size() == 7) {
                if (player1Pits.get(6) > player2Pits.get(6)) {
                    result = true;
                }
                else if (player1Pits.get(6) == player2Pits.get(6)) {
                    result = null;
                }
                else {
                    result = false;
                }
            }
            else {
                log.severe(String.format("Improperly setup board. Player2 pit count: %s",
                    player2Pits != null ? player2Pits.size() : null));
            }
        }
        else {
            log.severe(String.format("Improperly setup board. Player1 pit count: %s",
                player1Pits != null ? player1Pits.size() : null));
        }
        return result;
    }

    /**
     * Method to see if either of {@link Board#player1Pits} or
     * {@link Board#player2Pits} has small empty pits
     * 
     * @return
     */
    @JsonIgnore
    public boolean isCompleted() {

        boolean isStonesLeftForPlayer1 = isStonesLeft(player1Pits);
        boolean isStonesLeftForPlayer2 = isStonesLeft(player2Pits);
        //if either player1 or player2 has no stones left, then game is completed
        boolean isCompleted = !isStonesLeftForPlayer1 || !isStonesLeftForPlayer2;
        //if the board is completed, move all the stones from the small pits to the big pit
        if (isCompleted) {
            moveStonesToBigPit(player1Pits);
            moveStonesToBigPit(player2Pits);
            //update board in mongo
            createOrUpdate();
        }
        return isCompleted;
    }

    //private methods
    /**
     * Simple method to setup 6 stones in the small pit and no stones in the big
     * pit for a particular player
     * 
     * @param playerPits
     *            A player pit
     * @return A setup player pit.
     */
    private static List<Integer> setUpPits(List<Integer> playerPits) {

        playerPits = playerPits != null ? playerPits : new ArrayList<Integer>(7);
        //put 6 stones to all intial 6 pits and 0 in the last big pit
        playerPits = Arrays.asList(6, 6, 6, 6, 6, 6, 0);
        return playerPits;
    }

    /**
     * Simple method to check if any stone is left in a players small pits
     */
    private boolean isStonesLeft(List<Integer> playerPits) {

        if (playerPits != null) {
            for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                if (playerPits.get(pitIndex) > 0) {
                    return true;
                }
            }
        }
        //assume stones are left if playerPit is null. just log it
        else {
            log.severe("Player pit is null to check if any stones left. Returning true");
            return true;
        }
        return false;
    }

    /**
     * This will move all stones from small pits (index 0...5) to the big pit
     * (index 6)
     * 
     * @param playerPits
     * @return The updated pits after the move
     */
    private static List<Integer> moveStonesToBigPit(List<Integer> playerPits) {

        if (playerPits != null && playerPits.size() == 7) {
            for (int pitIndex = 0; pitIndex < 6; pitIndex++) {
                //fetch stones in bit pit
                Integer stonesInBigPit = playerPits.get(6);
                playerPits.set(6, stonesInBigPit + playerPits.get(pitIndex));
                playerPits.set(pitIndex, 0);
            }
        }
        return playerPits;
    }

    //mongo access methods
    /**
     * Simple method to create or update this entity in the mongoDb
     */
    public Board createOrUpdate() {

        return Mongodb.getInstance().updateEntity(this);
    }

    /**
     * Get a specific Player entity by id
     * 
     * @param playerId
     *            PlayerId to be fetched
     * @return Player if match is successfull, else returns null.
     */
    public static Board getBoard(String boardId) {

        if (boardId != null) {
            return Mongodb.getInstance().getEntity("{_id: #}", Board.class, new ObjectId(boardId));
        }
        else {
            return null;
        }
    }

    /**
     * Execute a move performed by the Player. This moves the stones to the
     * right pit. This will also persit/update this entity
     * 
     * @param playerPit
     *            The corresponding player pits on which this move is made
     * @param opponentPlayerPit
     *            The opponent player pits from which stones can be taken after
     *            the move of current player, if last pit to which a stone is
     *            placed has only 1 stone. So before the move had no stones.
     * @param isPlayer1Move
     *            Flag to mark whose move it is. true indicates player1, false
     *            is player2
     * @param pitIndex
     *            The 0-based pit index on which the Player is making his move
     * @return Returns true if this player is given a chance again. Null if some
     *         error or precondition did not match
     * @throws BadMoveException
     *             This exception is thrown when a move is not allowed.
     */
    public Boolean makeMove(boolean isPlayer1sMove, Integer pitIndex) throws BadMoveException {

        Boolean result = false;
        List<Integer> playerPit = null;
        List<Integer> opponentPlayerPit = null;
        if (isPlayer1sMove) {
            playerPit = player1Pits;
            opponentPlayerPit = player2Pits;
        }
        else {
            playerPit = player2Pits;
            opponentPlayerPit = player1Pits;
        }
        if (playerPit != null && pitIndex >= 0 && pitIndex <= 5 && playerPit.size() == 7 &&
            opponentPlayerPit.size() == 7) {
            Integer stonesInPit = playerPit.get(pitIndex);
            //check if the number of stones are greater than 0
            if (stonesInPit == 0) {
                throw new BadMoveException(String.format("Player%s move at index: %s is not allowed. No stones here.",
                    isPlayer1sMove ? 1 : 2, pitIndex));
            }
            else {
                playerPit.set(pitIndex, 0);
                while (stonesInPit != 0) {
                    //move one stone to the right
                    pitIndex++;
                    //if pitIndex has come to the right most big pit, reset it to first small pit
                    if (pitIndex == 7) {
                        pitIndex = 0;
                    }
                    playerPit.set(pitIndex, playerPit.get(pitIndex) + 1);
                    stonesInPit--;
                }
                //if pitIndex is the last big pit, give this user to playAgain
                if (pitIndex == 6) {
                    result = true;
                }
                //move all the stones from the opponent pit to this players, if the current pit has one stone after the move
                else if (playerPit.get(pitIndex) == 1) {
                    //the opponent is ofcourse indexes in opposite order. E.g. 0 is 5, 1 is 4,..., and 5 is 0
                    Integer opponentIndex = 5 - pitIndex;
                    playerPit.set(pitIndex, opponentPlayerPit.get(opponentIndex) + 1);
                    //remove all stones in the opponent pit
                    opponentPlayerPit.set(opponentIndex, 0);
                    result = false;
                }
                //update the entity
                Board board = createOrUpdate();
                this.player1Pits = board.player1Pits;
                this.player2Pits = board.player2Pits;
            }
        }
        else {
            log.severe(String.format("Cannot perform move. Invalid number of pits: %s, %s pitIndex: %s. Ignoring move",
                playerPit != null ? playerPit.size() : null,
                opponentPlayerPit != null ? opponentPlayerPit.size() : null, pitIndex));
        }
        return result;
    }
}
