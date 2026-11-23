package com.loansystem.model;

public interface Payable {
    double getPrincipal();

    double calculateEMI(Applicant applicant);

    double totalPayable(Applicant applicant);

    default double totalInterest(Applicant applicant) {
        return totalPayable(applicant) - getPrincipal();
    }
}
