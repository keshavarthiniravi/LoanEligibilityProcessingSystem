package com.loansystem.exception;

public class ApplicationNotFoundException extends Exception {
    public ApplicationNotFoundException(String appId) {
        super("No loan application found with ID: " + appId);
    }
}
