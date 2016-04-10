package com.shravan.gameofstones.core;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shravan.gameofstones.util.JSONFormatter;

/**
 * A generic Response schema for all REST-level responses.
 * 
 * @author shravanshetty
 */
public class RestResponse {

    String version = "0.0.1";
    Object result;
    int code;
    String message;

    //getters and setters
    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public Object getResult() {

        return result;
    }

    public void setResult(Object result) {

        this.result = result;
    }

    public int getCode() {

        return code;
    }

    public void setCode(int code) {

        this.code = code;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public RestResponse() {

    }

    public RestResponse(Object result, int code, String message) {

        this.result = result;
        this.code = code;
        this.message = message;
    }

    /**
     * Return an ok response with message "OK" and code 200
     * 
     * @param version
     * @param result
     * @return
     */
    public static RestResponse ok(Object result) {

        return ok(result, "OK");
    }

    /**
     * Returns an ok response with code 200
     * 
     * @param version
     * @param result
     * @param message
     * @return
     */
    public static RestResponse ok(Object result, String message) {

        return new RestResponse(result, 200, message);
    }

    /**
     * Returns a forbidden response with null result and 403 as the code
     * 
     * @param version
     * @param message
     * @return
     */
    public static RestResponse forbidden(String message) {

        return new RestResponse(null, 403, message);
    }

    /**
     * Returns a error 502 response with null result and 403 as the code
     * 
     * @param version
     * @param message
     * @return
     */
    public static RestResponse error(String version, String message) {

        return new RestResponse(null, 502, message);
    }

    /**
     * Returns a response with null result and the given statusCode and message
     * 
     * @param version
     * @param message
     * @return
     */
    public static RestResponse error(int statusCode, String message) {

        return new RestResponse(null, statusCode, message);
    }

    /**
     * Returns a not found 404 response with null result and 403 as the code
     * 
     * @param version
     * @param message
     * @return
     */
    public static RestResponse notFound(String message) {

        return new RestResponse(null, 404, message);
    }

    /**
     * Casts the result into the given classType. Returns null if an exception
     * is thrown
     * 
     * @param classType
     * @return
     */
    public <T> T getResult(Class<T> classType) {

        if (getResult() != null) {
            try {
                return JSONFormatter.convert(getResult(), classType);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Gets the serialized version of this instance
     * 
     * @return Serialized entity
     */
    @JsonIgnore
    public String getJson() {

        return JSONFormatter.serialize(this);
    }

    /**
     * Builds a {@link Response} from the given instance of RestResponse
     * 
     * @return The response that contains information about this RestResponse
     */
    public Response buildResponse() {

        return Response.ok(getJson(), MediaType.APPLICATION_JSON).status(getCode()).build();
    }
}
