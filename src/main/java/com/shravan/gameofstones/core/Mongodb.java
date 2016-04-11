package com.shravan.gameofstones.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * Mongodb class to interact with mongo database.
 * 
 * @author shravanshetty
 */

public class Mongodb {

    private Jongo jongo;
    private static Mongodb mongodb;
    Logger log = Logger.getLogger(Mongodb.class.getSimpleName());
    public static boolean IS_TEST = false;
    private static String DB_NAME;

    private Mongodb() {
        DB_NAME = IS_TEST ? "gameofstones-test" : "gameofstones";
    }

    /**
     * Retuns a singeton instance of this type
     * 
     * @return Mongodb singleton instance
     */
    @SuppressWarnings({"resource", "deprecation"})
    public static Mongodb getInstance() {

        mongodb = mongodb != null ? mongodb : new Mongodb();
        if (mongodb.jongo == null) {
            DB db = new MongoClient("localhost", 27017).getDB(DB_NAME);
            mongodb.jongo = new Jongo(db,
                new JacksonMapper.Builder().registerModule(new JodaModule()).enable(MapperFeature.AUTO_DETECT_GETTERS)
                                           .build());
        }
        return mongodb;
    }

    /**
     * Get the jongo instance for additional db querying
     * 
     * @return Jongo instance that is already instantiated
     */
    public Jongo getJongo() {

        return jongo;
    }

    /**
     * Generic method to read one entity from db.
     * 
     * @param query
     *            The query against which the entities has to be fetched
     * @param expected
     *            The expected bean/collection to query against.
     * @param params
     *            The query values
     * @return Returns one entity based the search criteria
     */
    public <T> T getEntity(String query, Class<T> expected, Object... params) {

        String collectionName = getCollectionName(expected);
        if (collectionName == null) {
            return null;
        }
        MongoCollection collection = jongo.getCollection(collectionName);
        if (params == null) {
            return collection.findOne(query).as(expected);

        }
        return collection.findOne(query, params).as(expected);
    }

    /**
     * Gets a list of all entities based on the given query
     * 
     * @param query
     *            The query against which the entities has to be fetched
     * @param expected
     *            The expected bean/collection to query against.
     * @param params
     *            The query values
     * @return Returns a list of entities.
     */
    public <T> List<T> getEntities(String query, Class<T> expected, Object... params) {

        String collectionName = getCollectionName(expected);
        if (collectionName == null) {
            return null;
        }
        MongoCollection collection = jongo.getCollection(collectionName);
        List<T> entityList = new ArrayList<T>();
        Iterable<T> iterableEntities;
        if (params == null) {
            iterableEntities = collection.find(query).as(expected);
        }
        else {
            iterableEntities = collection.find(query, params).as(expected);
        }
        for (T entity : iterableEntities) {
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * Insert an entity object to mongodb
     * 
     * @param entity
     * @return the entity with id assigned by mongodb
     */
    public <T> T insertEntity(T entity) {

        String collectionName = getCollectionName(entity.getClass());
        if (collectionName == null)
            return null;
        MongoCollection collection = jongo.getCollection(collectionName);
        collection.insert(entity);
        return entity;
    }

    /**
     * Updates the given entity in mongo.
     * 
     * @param entity
     *            The entity that has to be saved.
     * @return Returns the saved entity.
     */
    public <T> T updateEntity(T entity) {

        String collectionName = getCollectionName(entity.getClass());
        if (collectionName == null)
            return null;
        MongoCollection collection = jongo.getCollection(collectionName);
        collection.save(entity);
        return entity;
    }

    /**
     * Performs a size() on the query given. Returns the total number of
     * entities fetched for the given query
     * 
     * @param clazz
     *            Expected bean/collection that is queried
     * @param query
     *            The actual query
     * @param queryParams
     *            Query params for performing the query
     * @return The number of entities that match the query.
     */
    public <T> Long count(Class<T> clazz, String query, Object... queryParams) {

        String collectionName = getCollectionName(clazz);
        if (collectionName == null)
            return null;
        MongoCollection collection = jongo.getCollection(collectionName);
        return collection.count(query, queryParams);
    }

    /**
     * All collections are stored based on the Entity/bean simple name
     * 
     * @param expected
     *            Bean class for which the mongo collection name is expected.
     * @return The collection name as saved in mongo
     */
    private <T> String getCollectionName(Class<T> expected) {

        return expected.getSimpleName();
    }
}
