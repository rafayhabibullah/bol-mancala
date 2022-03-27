package com.bol.mancala.exception.model;

public class EntityNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8364963064612410715L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
