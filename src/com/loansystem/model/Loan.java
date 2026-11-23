package com.loansystem.model;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.exception.LoanNotEligibleException;
import com.loansystem.util.StringUtil;
import java.util.ArrayList;
import java.util.List;

public abstract class Loan implements Eligible, Payable {
    public static final double MAX_EMI_RATIO = 0.5;   

    private final String loanType;
    private double principal;
    private int tenureMonths;

    protected Loan(String loanType, double principal, int tenureMonths) throws InvalidInputException {
        this.loanType = loanType;
        setPrincipal(principal);
        setTenureMonths(tenureMonths);
    }

    public String getLoanType() {
        return loanType;
    }

    @Override
    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) throws InvalidInputException {
        if (principal < 10_000 || principal > 100_000_000) {
            throw new InvalidInputException("Loan amount must be between Rs. 10,000 and Rs. 10,00,00,000.");
        }
        this.principal = principal;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) throws InvalidInputException {
        if (tenureMonths < 6 || tenureMonths > 360) {
            throw new InvalidInputException("Tenure must be between 6 months and 30 years.");
        }
        this.tenureMonths = tenureMonths;
    }

    public abstract double getBaseInterestRate();

    public abstract double getMinIncome();

    public abstract int getMinCreditScore();

    public abstract int getMaxTenureMonths();

    public abstract int getMinAge();

    public abstract int getMaxAge();

    public abstract double getIncomeMultiplier();

    public abstract String getExtraLabel();

    public abstract String getExtraValue();

    public double getInterestRate(Applicant applicant) {
        double rate = getBaseInterestRate();
        int score = applicant.getCreditScore();
        if (score >= 800) {
            rate -= 0.75;
        } else if (score >= 750) {
            rate -= 0.50;
        } else if (score < 650) {
            rate += 1.00;
        }
        return rate;
    }

    @Override
    public double maxEligibleAmount(Applicant applicant) {
        return applicant.getMonthlyIncome() * getIncomeMultiplier();
    }

    @Override
    public double calculateEMI(Applicant applicant) {
        return calculateEMI(principal, getInterestRate(applicant), tenureMonths);
    }

    public static double calculateEMI(double principal, double annualRatePercent, int months) {
        double r = annualRatePercent / 12 / 100;
        if (r == 0) {
            return principal / months;
        }
        double factor = Math.pow(1 + r, months);
        return principal * r * factor / (factor - 1);
    }

    @Override
    public double totalPayable(Applicant applicant) {
        return calculateEMI(applicant) * tenureMonths;
    }

    @Override
    public void checkEligibility(Applicant applicant) throws LoanNotEligibleException {
        List<String> reasons = new ArrayList<>();
        collectReasons(applicant, reasons);
        if (!reasons.isEmpty()) {
            throw new LoanNotEligibleException(reasons);
        }
    }

    protected void collectReasons(Applicant a, List<String> reasons) {
        if (a.getAge() < getMinAge() || a.getAge() > getMaxAge()) {
            reasons.add("Age must be between " + getMinAge() + " and " + getMaxAge()
                    + " for a " + loanType + " loan (applicant is " + a.getAge() + ").");
        }
        if (a.getMonthlyIncome() < getMinIncome()) {
            reasons.add("Monthly income is below the minimum of " + StringUtil.money(getMinIncome()) + ".");
        }
        if (a.getCreditScore() < getMinCreditScore()) {
            reasons.add("Credit score " + a.getCreditScore() + " is below the required "
                    + getMinCreditScore() + ".");
        }
        if (tenureMonths > getMaxTenureMonths()) {
            reasons.add("Tenure exceeds the maximum of " + (getMaxTenureMonths() / 12) + " years.");
        }
        double max = maxEligibleAmount(a);
        if (principal > max) {
            reasons.add("Requested amount exceeds your eligible limit of " + StringUtil.money(max) + ".");
        }
        double emi = calculateEMI(a);
        if (emi > a.getMonthlyIncome() * MAX_EMI_RATIO) {
            reasons.add("EMI of " + StringUtil.money(emi) + " exceeds 50% of monthly income.");
        }
    }

    @Override
    public String toString() {
        return loanType + " Loan of " + StringUtil.money(principal) + " for " + tenureMonths + " months";
    }
}
