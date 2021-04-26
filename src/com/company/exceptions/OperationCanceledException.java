package com.company.exceptions;

/**
 * Is thrown when operation cancelled or is invalid, like EOF
 */
public class OperationCanceledException extends Exception{
    public OperationCanceledException() {
        super("Operation was canceled");
    }
}
