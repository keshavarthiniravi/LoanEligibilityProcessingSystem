package com.loansystem.service;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.model.EducationLoan;
import com.loansystem.model.HomeLoan;
import com.loansystem.model.Loan;
import com.loansystem.model.PersonalLoan;

/** Week 4 - Polymorphism: returns the right Loan subclass behind the Loan type. */
public final class LoanFactory {
    public static final String[] TYPES = {"Education", "Home", "Personal"};

    private LoanFactory() { }

    public static Loan create(String type, double principal, int months, String extra)
            throws InvalidInputException {
        switch (type) {
            case "Education":
                return new EducationLoan(principal, months, extra);
            case "Home":
                try {
                    return new HomeLoan(principal, months, Double.parseDouble(extra.trim()));
                } catch (NumberFormatException e) {      // built-in exception -> custom one
                    throw new InvalidInputException("Property value must be a number.");
                }
            case "Personal":
                return new PersonalLoan(principal, months, extra);
            default:
                throw new InvalidInputException("Unknown loan type: " + type);
        }
    }
}
