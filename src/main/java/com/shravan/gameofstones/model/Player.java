package com.shravan.gameofstones.model;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import com.shravan.gameofstones.core.Mongodb;

public class Player {

    @MongoId
    private ObjectId id;
    private String name;
    private Integer score;
    private Integer moves;
    private Boolean isWinner;
    
    //simple constructors
    public Player() {

    }

    public Player(String name) {
        this.name = name;
    }
    
    // getters and setters
    public ObjectId getId() {
    
        return id;
    }
    public void setId(ObjectId id) {
    
        this.id = id;
    }
    
    public String getName() {
    
        return name;
    }
    
    public void setName(String name) {
    
        this.name = name;
    }
    
    public Integer getScore() {
    
        return score;
    }
    
    public void setScore(Integer score) {
    
        this.score = score;
    }
    
    public Integer getMoves() {
    
        return moves;
    }
    
    public void setMoves(Integer moves) {
    
        this.moves = moves;
    }
    
    public Boolean getIsWinner() {

        return isWinner;
    }
    
    public void setIsWinner(Boolean isWinner) {

        this.isWinner = isWinner;
    }

    //mongo access methods
    /**
     * Simple method to create or update this entity in the mongoDb
     */
    public Player createOrUpdate() {

        return Mongodb.getInstance().updateEntity(this);
    }
    
    /**
     * Get a specific Player entity by id
     * @param playerId PlayerId to be fetched
     * @return Player if match is successfull, else returns null.
     */
    public static Player getPlayer(ObjectId playerId) {

        return Mongodb.getInstance().getEntity("{_id: #}", Player.class, playerId);
    }
}
