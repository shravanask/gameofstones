package com.shravan.gameofstones.exception;

/**
 * This can be used as a checked exception to show that a move performed is not
 * allowed.
 * 
 * @author shravanshetty
 *
 */
public class BadMoveException extends Exception {

    /**
     * Simple constructor
     */
    public BadMoveException() {
    }

    /**
     * Constuctor for passing a message to show information of why this is a bad
     * move
     * 
     * @param messsage
     */
    public BadMoveException(String messsage) {
        super(messsage);
    }
}
