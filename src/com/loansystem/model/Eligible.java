package com.loansystem.model;

import com.loansystem.exception.LoanNotEligibleException;

public interface Eligible {
    void checkEligibility(Applicant applicant) throws LoanNotEligibleException;

    double maxEligibleAmount(Applicant applicant);
}
