package com.loansystem.model;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.util.StringUtil;
import java.util.List;

/** Week 3 - Inheritance: EducationLoan extends Loan. */
public class EducationLoan extends Loan {
    private String courseName;

    public EducationLoan(double principal, int tenureMonths, String courseName) throws InvalidInputException {
        super("Education", principal, tenureMonths);
        setCourseName(courseName);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) throws InvalidInputException {
        if (courseName == null || courseName.trim().length() < 2) {
            throw new InvalidInputException("Please enter the course name.");
        }
        this.courseName = StringUtil.sanitize(courseName);
    }

    @Override public double getBaseInterestRate() { return 8.5; }
    @Override public double getMinIncome() { return 15_000; }
    @Override public int getMinCreditScore() { return 600; }
    @Override public int getMaxTenureMonths() { return 180; }
    @Override public int getMinAge() { return 18; }
    @Override public int getMaxAge() { return 35; }
    @Override public double getIncomeMultiplier() { return 40; }
    @Override public String getExtraLabel() { return "Course Name"; }
    @Override public String getExtraValue() { return courseName; }

    @Override
    public double maxEligibleAmount(Applicant applicant) {
        return Math.min(super.maxEligibleAmount(applicant), 7_500_000);   // cap Rs. 75 lakh
    }

    @Override
    protected void collectReasons(Applicant a, List<String> reasons) {
        super.collectReasons(a, reasons);
        if (getPrincipal() < 50_000) {
            reasons.add("Minimum education loan amount is Rs. 50,000.");
        }
    }
}
