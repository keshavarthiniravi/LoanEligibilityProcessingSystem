package com.loansystem.service;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.model.Applicant;
import com.loansystem.model.Loan;
import com.loansystem.util.AppLogger;

public final class SampleData {
    private SampleData() { }

    public static int load(LoanService service) {
        Object[][] rows = {
            {"Arun Kumar", 24, 35000, 745, "9876501234", "arun.kumar@mail.com", "Education", 600000, 84, "B.Tech Information Technology"},
            {"Priya Lakshmi", 34, 85000, 810, "9876505678", "priya.l@mail.com", "Home", 4500000, 240, "6000000"},
            {"Karthik Raja", 29, 28000, 620, "9876509012", "karthik.r@mail.com", "Personal", 400000, 36, "Wedding expenses"},
            {"Meena Sundar", 41, 60000, 770, "9876503456", "meena.s@mail.com", "Personal", 500000, 48, "Medical treatment"},
            {"Vignesh Babu", 38, 45000, 690, "9876507890", "vignesh.b@mail.com", "Home", 3500000, 180, "4000000"},
            {"Divya Bharathi", 22, 18000, 655, "9876502468", "divya.b@mail.com", "Education", 300000, 60, "MBA Finance"},
        };
        int added = 0;
        for (Object[] r : rows) {
            try {
                Applicant a = new Applicant((String) r[0], (Integer) r[1], ((Integer) r[2]).doubleValue(),
                        (Integer) r[3], (String) r[4], (String) r[5]);
                Loan loan = LoanFactory.create((String) r[6], ((Integer) r[7]).doubleValue(),
                        (Integer) r[8], (String) r[9]);
                service.submit(a, loan);
                added++;
            } catch (InvalidInputException e) {
                AppLogger.get().warning("Sample record skipped: " + e.getMessage());
            }
        }
        return added;
    }
}
