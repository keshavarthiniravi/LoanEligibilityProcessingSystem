package com.loansystem.exception;

/** Week 6 - Custom exception thrown when an application ID does not exist. */
public class ApplicationNotFoundException extends Exception {
    public ApplicationNotFoundException(String appId) {
        super("No loan application found with ID: " + appId);
    }
}
