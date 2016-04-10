package com.shravan.gameofstones.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import com.shravan.gameofstones.core.Mongodb;

/**
 * Simple class to encapsulate the board data. (Player pits, and the number of
 * stones in each pit)
 * 
 * @author shravanshetty
 */
public class Board {

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

        return Mongodb.getInstance().getEntity("{_id: #}", Board.class, new ObjectId(boardId));
    }
}
