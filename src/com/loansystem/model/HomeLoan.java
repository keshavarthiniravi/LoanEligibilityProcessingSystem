package com.loansystem.model;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.util.StringUtil;
import java.util.List;

public class HomeLoan extends Loan {
    private double propertyValue;

    public HomeLoan(double principal, int tenureMonths, double propertyValue) throws InvalidInputException {
        super("Home", principal, tenureMonths);
        setPropertyValue(propertyValue);
    }

    public double getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(double propertyValue) throws InvalidInputException {
        if (propertyValue <= 0) {
            throw new InvalidInputException("Property value must be greater than 0.");
        }
        this.propertyValue = propertyValue;
    }

    @Override public double getBaseInterestRate() { return 9.0; }
    @Override public double getMinIncome() { return 30_000; }
    @Override public int getMinCreditScore() { return 700; }
    @Override public int getMaxTenureMonths() { return 360; }
    @Override public int getMinAge() { return 21; }
    @Override public int getMaxAge() { return 60; }
    @Override public double getIncomeMultiplier() { return 80; }
    @Override public String getExtraLabel() { return "Property Value (Rs.)"; }
    @Override public String getExtraValue() { return String.valueOf((long) propertyValue); }

    @Override
    public double getInterestRate(Applicant applicant) {
        double rate = super.getInterestRate(applicant);
        return getPrincipal() > 5_000_000 ? rate + 0.25 : rate; 
    }

    @Override
    protected void collectReasons(Applicant a, List<String> reasons) {
        super.collectReasons(a, reasons);
        if (getPrincipal() < 500_000) {
            reasons.add("Minimum home loan amount is Rs. 5,00,000.");
        }
        if (getPrincipal() > propertyValue * 0.80) {
            reasons.add("Loan cannot exceed 80% of the property value ("
                    + StringUtil.money(propertyValue * 0.80) + ").");
        }
    }
}
