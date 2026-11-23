package com.loansystem.model;

/** Week 5 - Interface: anything that produces repayment figures. */
public interface Payable {
    double getPrincipal();

    double calculateEMI(Applicant applicant);

    double totalPayable(Applicant applicant);

    /** Default method (Java 8+ interface feature). */
    default double totalInterest(Applicant applicant) {
        return totalPayable(applicant) - getPrincipal();
    }
}
