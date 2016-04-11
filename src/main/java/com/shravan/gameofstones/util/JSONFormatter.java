package com.shravan.gameofstones.util;

import java.util.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Helper class on top of the {@link ObjectMapper}, to serialize, deserialize
 * and convert beans
 * 
 * @author shravanshetty
 */
public class JSONFormatter {

    private static final ObjectMapper om = new ObjectMapper();
    private static final Logger log = Logger.getLogger(JSONFormatter.class.getSimpleName());

    /**
     * Serializes the given object instance
     * 
     * @param objectToBeSerialized
     * @return
     */
    public static String serialize(Object objectToBeSerialized) {

        if (objectToBeSerialized == null) {
            return null;
        }
        ObjectWriter ow = om.writer();
        try {
            return ow.writeValueAsString(objectToBeSerialized);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the instance of the ObjectMapper
     * 
     * @return
     */
    public static ObjectMapper getMapper() {

        return om;
    }

    /**
     * Deserialize a json string to a given Type
     * 
     * @param jsonString
     * @param throwException
     *            When set to false: Silently tries to deserialize. Logs the
     *            exception, doesnt throw it, returns null.
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String jsonString, boolean throwException, TypeReference<T> type) throws Exception {

        try {
            return om.readValue(jsonString, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * Deserialize a json string to a given Type
     * 
     * @param jsonString
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T deserialize(String jsonString, Boolean throwException, Class<T> type) throws Exception {

        try {
            return om.readValue(jsonString, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * Converts the given jsonObject to the required. Throws an exception if
     * throwException is set to true
     * 
     * @param jsonObject
     * @param throwException
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T convert(Object jsonObject, boolean throwException, TypeReference<T> type) throws Exception {

        try {
            return om.convertValue(jsonObject, type);
        }
        catch (Exception e) {
            if (throwException) {
                throw e;
            }
            else {
                log.severe(e.getLocalizedMessage());
                return null;
            }
        }
    }

    /**
     * Converts the given jsonObject to the required. Throws an exception if
     * coversion fails
     * 
     * @param jsonObject
     * @param type
     * @return
     * @throws Exception
     */
    public static <T> T convert(Object jsonObject, Class<T> type) throws Exception {

        try {
            return om.convertValue(jsonObject, type);
        }
        catch (Exception e) {
            log.severe(e.getLocalizedMessage());
            throw e;
        }
    }
}
