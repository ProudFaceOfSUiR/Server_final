package com.company.exceptions;

/**
 * Is thrown when unknown command is given
 */

public class UnknownCommandException extends Exception{
    public UnknownCommandException(){
        super("Unknown command. Operation cancelled");
    }

    public UnknownCommandException(String message) {
        super(message);
    }
}
