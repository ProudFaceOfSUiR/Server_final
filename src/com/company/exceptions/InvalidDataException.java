package com.company.exceptions;

/**
 * Is thrown when invalid data is given
 */
public class InvalidDataException extends Exception{
    public InvalidDataException(String invalidData) {
        super("Invalid " + invalidData);
    }

    public InvalidDataException(String invalidData, String message) {
        super("Invalid " + invalidData + ". " + message);
    }
}
