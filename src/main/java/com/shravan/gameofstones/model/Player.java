package com.shravan.gameofstones.model;

import org.bson.types.ObjectId;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import com.shravan.gameofstones.core.Mongodb;

public class Player {

    @MongoId
    @MongoObjectId
    private String id;
    private String name;

    //simple constructors
    public Player() {

    }

    public Player(String name) {
        this.name = name;
    }

    // getters and setters
    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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
     * 
     * @param playerId
     *            PlayerId to be fetched
     * @return Player if match is successfull, else returns null.
     */
    public static Player getPlayer(String playerId) {

        if (playerId != null) {
            return Mongodb.getInstance().getEntity("{_id: #}", Player.class, new ObjectId(playerId));
        }
        else {
            return null;
        }
    }
}
