package com.loansystem.model;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.util.StringUtil;
import java.util.List;

/** Week 3 - Inheritance: PersonalLoan extends Loan. */
public class PersonalLoan extends Loan {
    private String purpose;

    public PersonalLoan(double principal, int tenureMonths, String purpose) throws InvalidInputException {
        super("Personal", principal, tenureMonths);
        setPurpose(purpose);
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) throws InvalidInputException {
        if (purpose == null || purpose.trim().length() < 2) {
            throw new InvalidInputException("Please enter the purpose of the loan.");
        }
        this.purpose = StringUtil.sanitize(purpose);
    }

    @Override public double getBaseInterestRate() { return 12.5; }
    @Override public double getMinIncome() { return 20_000; }
    @Override public int getMinCreditScore() { return 650; }
    @Override public int getMaxTenureMonths() { return 60; }
    @Override public int getMinAge() { return 21; }
    @Override public int getMaxAge() { return 58; }
    @Override public double getIncomeMultiplier() { return 20; }
    @Override public String getExtraLabel() { return "Purpose"; }
    @Override public String getExtraValue() { return purpose; }

    @Override
    public double maxEligibleAmount(Applicant applicant) {
        return Math.min(super.maxEligibleAmount(applicant), 2_500_000);    // cap Rs. 25 lakh
    }

    @Override
    protected void collectReasons(Applicant a, List<String> reasons) {
        super.collectReasons(a, reasons);
        if (getPrincipal() < 25_000) {
            reasons.add("Minimum personal loan amount is Rs. 25,000.");
        }
    }
}
