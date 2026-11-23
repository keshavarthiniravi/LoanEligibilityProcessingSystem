package com.loansystem.model;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.util.StringUtil;

public class Applicant {
    private String name;
    private int age;
    private double monthlyIncome;
    private int creditScore;
    private String phone;
    private String email;

    public Applicant(String name, int age, double monthlyIncome, int creditScore,
                     String phone, String email) throws InvalidInputException {
        setName(name);
        setAge(age);
        setMonthlyIncome(monthlyIncome);
        setCreditScore(creditScore);
        setPhone(phone);
        setEmail(email);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidInputException {
        if (name == null || !name.trim().matches("[A-Za-z][A-Za-z .]{1,39}")) {
            throw new InvalidInputException("Name must be 2-40 characters (letters, space and dot only).");
        }
        this.name = StringUtil.toTitleCase(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) throws InvalidInputException {
        if (age < 18 || age > 70) {
            throw new InvalidInputException("Age must be between 18 and 70.");
        }
        this.age = age;
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(double monthlyIncome) throws InvalidInputException {
        if (monthlyIncome <= 0 || monthlyIncome > 10_000_000) {
            throw new InvalidInputException("Monthly income must be greater than 0.");
        }
        this.monthlyIncome = monthlyIncome;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) throws InvalidInputException {
        if (creditScore < 300 || creditScore > 900) {
            throw new InvalidInputException("Credit score must be between 300 and 900.");
        }
        this.creditScore = creditScore;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws InvalidInputException {
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new InvalidInputException("Phone number must have exactly 10 digits.");
        }
        this.phone = phone.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws InvalidInputException {
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new InvalidInputException("Please enter a valid e-mail address.");
        }
        this.email = email.trim().toLowerCase();
    }

    /** Looping/decision logic: textual credit band. */
    public String getCreditBand() {
        if (creditScore >= 800) {
            return "Excellent";
        } else if (creditScore >= 750) {
            return "Very Good";
        } else if (creditScore >= 700) {
            return "Good";
        } else if (creditScore >= 650) {
            return "Fair";
        }
        return "Poor";
    }

    @Override
    public String toString() {
        return name + " (" + age + " yrs, income " + StringUtil.money(monthlyIncome)
                + ", credit " + creditScore + ")";
    }
}
