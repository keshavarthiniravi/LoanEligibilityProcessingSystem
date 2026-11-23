package com.loansystem.model;

import com.loansystem.exception.LoanNotEligibleException;

/** Week 5 - Interface: anything that can decide eligibility for an applicant. */
public interface Eligible {
    void checkEligibility(Applicant applicant) throws LoanNotEligibleException;

    double maxEligibleAmount(Applicant applicant);
}
