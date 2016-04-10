package com.shravan.gameofstones.model;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.shravan.gameofstones.core.Mongodb;

/**
 * Simple bean to wrap player information
 * 
 * @author shravanshetty
 */
public class Play {

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
    private String player1;
    private String player2;
    private PlayState playState;
    private String winnerId;

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

    public String getPlayer1() {

        return player1;
    }

    public void setPlayer1(String player1) {

        this.player1 = player1;
    }

    public String getPlayer2() {

        return player2;
    }

    public void setPlayer2(String player2) {

        this.player2 = player2;
    }

    //mongo access methods
    /**
     * Create or update this instance to the mongoDb
     * 
     * @return Returns the created/updated entity that is persisted
     */
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

            //update/create the play
            Play play = new Play();
            play.setPlayer1(player1.getId());
            play.setPlayer2(player2.getId());
            play.setPlayState(PlayState.IN_PROGRESS);
            play = play.createOrUpdate();
            return play;
        }
        return null;
    }
}
