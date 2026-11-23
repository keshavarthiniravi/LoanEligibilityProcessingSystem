package com.loansystem.model;

import com.loansystem.util.StringUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoanApplication implements Reportable, Comparable<LoanApplication> {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final String appId;
    private final Applicant applicant;
    private final Loan loan;
    private final LocalDateTime submittedAt;
    private volatile ApplicationStatus status;   
    private volatile String remarks;

    public LoanApplication(String appId, Applicant applicant, Loan loan) {
        this(appId, applicant, loan, ApplicationStatus.SUBMITTED, "Waiting in processing queue", LocalDateTime.now());
    }

    public LoanApplication(String appId, Applicant applicant, Loan loan, ApplicationStatus status,
                           String remarks, LocalDateTime submittedAt) {
        this.appId = appId;
        this.applicant = applicant;
        this.loan = loan;
        this.status = status;
        this.remarks = remarks;
        this.submittedAt = submittedAt;
    }

    public String getAppId() { return appId; }
    public Applicant getApplicant() { return applicant; }
    public Loan getLoan() { return loan; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public ApplicationStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }

    public void setStatus(ApplicationStatus status, String remarks) {
        this.status = status;
        this.remarks = StringUtil.sanitize(remarks);
    }

    @Override
    public int compareTo(LoanApplication other) {
        int byScore = Integer.compare(other.applicant.getCreditScore(), applicant.getCreditScore());
        return byScore != 0 ? byScore : submittedAt.compareTo(other.submittedAt);
    }

    @Override
    public String toReportLine() {
        return String.format("%-8s %-20s %-10s %14s %5d %-13s",
                appId, applicant.getName(), loan.getLoanType(),
                StringUtil.money(loan.getPrincipal()), loan.getTenureMonths(), status.getLabel());
    }
}
