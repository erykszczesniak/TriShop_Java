package com.trishop.app.core.network;

public class EmptyBodyException extends RuntimeException {

    public EmptyBodyException() {
        super("Response body was empty");
    }
}
