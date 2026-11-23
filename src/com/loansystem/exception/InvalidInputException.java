package com.loansystem.exception;

/** Week 6 - Custom (checked) exception for invalid user input. */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
