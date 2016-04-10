package com.shravan.gameofstones.model;

import java.util.logging.Logger;
import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.shravan.gameofstones.core.Mongodb;
import com.shravan.gameofstones.util.JSONFormatter;

/**
 * Simple bean to wrap player information
 * 
 * @author shravanshetty
 */
public class Play {

    private static Logger log = Logger.getLogger(Play.class.getSimpleName());

    public enum PlayState {
            IN_PROGRESS, COMPLETED, ABORTED;

        /**
         * Simple method to get the {@link PlayState} based on a
         * case-insensitive manner
         * 
         * @param playState
         * @return
         */
        @JsonCreator
        public PlayState getValue(String playState) {

            for (PlayState state : PlayState.values()) {
                if (state.name().equalsIgnoreCase(playState)) {
                    return state;
                }
            }
            return null;
        }
    }

    @MongoId
    @MongoObjectId
    private String id;
    private String boardId;
    private String player1Id;
    private String player2Id;
    private PlayState playState;
    private String winnerId;
    /**
     * Use a flag to check whose move its next. If true, its player1, else
     * player2
     */
    private boolean isPlayer1sMove = true;

    //getters and setters
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public PlayState getPlayState() {

        return playState;
    }

    public void setPlayState(PlayState playState) {

        this.playState = playState;
    }

    public String getWinnerId() {

        return winnerId;
    }

    public void setWinnerId(String winnerId) {

        this.winnerId = winnerId;
    }

    public String getPlayer1Id() {

        return player1Id;
    }

    public void setPlayer1Id(String player1) {

        this.player1Id = player1;
    }

    public String getPlayer2Id() {

        return player2Id;
    }

    public void setPlayer2Id(String player2) {

        this.player2Id = player2;
    }

    public String getBoardId() {

        return boardId;
    }

    public void setBoardId(String boardId) {

        this.boardId = boardId;
    }
    
    /**
     * If true, its player1's chance to play next, else player2's
     * 
     * @return
     */
    @JsonProperty("isPlayer1sMove")
    public boolean isPlayer1sMove() {

        return isPlayer1sMove;
    }

    @JsonProperty("isPlayer1sMove")
    public void setPlayer1sMove(boolean isPlayer1sMove) {

        this.isPlayer1sMove = isPlayer1sMove;
    }

    //mongo access methods

    /**
     * Gets the full details in the form of an objectNode, with full entities in
     * places of entity ids
     * 
     * @return ObjectNode having full player details and full board details
     */
    @JsonIgnore
    public ObjectNode getFullPlayDetails() {

        ObjectNode playNode = JSONFormatter.getMapper().valueToTree(this);
        //fetch player details
        Player player1 = Player.getPlayer(this.player1Id);
        Player player2 = Player.getPlayer(this.player2Id);
        playNode.putPOJO("player1", player1);
        playNode.putPOJO("player2", player2);
        //fetch board details
        Board board = Board.getBoard(boardId);
        playNode.putPOJO("board", board);
        return playNode;
    }

    /**
     * Create or update this instance to the mongoDb
     * 
     * @return Returns the created/updated entity that is persisted
     */
    @JsonIgnore
    public Play createOrUpdate() {

        if (getId() != null && getPlay(getId()) != null) {
            return Mongodb.getInstance().updateEntity(this);
        }
        else {
            return Mongodb.getInstance().insertEntity(this);
        }
    }

    /**
     * Fetch a play by its Id
     * 
     * @param playId
     *            PlayId to be fetched
     * @return If the fetch is succesful returns the Play, else null.
     */
    public static Play getPlay(String playId) {

        return Mongodb.getInstance().getEntity("{_id: #}", Play.class, new ObjectId(playId));
    }

    /**
     * Persist a game in the database
     * 
     * @param player1
     *            Information about the first player
     * @param player2
     *            Information about the second player
     * @return Returns a {@link Play} that is setup between player1 and player2
     */
    public static Play startTwoPlayerGame(Player player1, Player player2) {

        if (player1 != null && player2 != null) {
            //save the players first
            player1.createOrUpdate();
            player2.createOrUpdate();

            //setup the board
            Board board = Board.setupBoard(true);

            //update/create the play
            Play play = new Play();
            play.setBoardId(board.getId());
            play.setPlayer1Id(player1.getId());
            play.setPlayer2Id(player2.getId());
            play.setPlayState(PlayState.IN_PROGRESS);
            play = play.createOrUpdate();
            return play;
        }
        return null;
    }

    /**
     * Execute a move performed by the Player. Updates this play too.
     * 
     * @param playerId
     *            PlayerId who is performing the move
     * @param pitIndex
     *            The 0-based pit index on which the Player is making his move
     */
    public void makeMove(String playerId, Integer pitIndex) {

        if (playerId != null) {
            //fetch the board in this play
            Board board = Board.getBoard(boardId);
            if (board != null) {
                if (playerId.equalsIgnoreCase(player1Id)) {
                    isPlayer1sMove = board.makeMove(true, pitIndex);
                }
                else if (player2Id.equalsIgnoreCase(player2Id)) {
                    isPlayer1sMove = board.makeMove(false, pitIndex);
                }
            }
            else {
                log.severe(
                    String.format("Cannot perform move. No linked Board found for id: %s. Ignoring move", boardId));
            }
        }
        else {
            log.severe("Cannot perform move. PlayerId is null. Ignoring move");
        }
        createOrUpdate();
    }
}
