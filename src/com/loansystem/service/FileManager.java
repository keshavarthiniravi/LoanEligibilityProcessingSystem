package com.loansystem.service;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.model.Applicant;
import com.loansystem.model.ApplicationStatus;
import com.loansystem.model.Loan;
import com.loansystem.model.LoanApplication;
import com.loansystem.util.AppLogger;
import com.loansystem.util.StringUtil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileManager {
    private static final String HEADER =
            "appId|name|age|income|credit|phone|email|type|amount|months|extra|status|remarks|submitted";
    private final Path dataFile = Paths.get("data", "applications.csv");
    private final Path reportDir = Paths.get("reports");

    public void save(Collection<LoanApplication> applications) throws IOException {
        Files.createDirectories(dataFile.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {
            writer.write(HEADER);
            writer.newLine();
            for (LoanApplication app : applications) {
                writer.write(toLine(app));
                writer.newLine();
            }
        }
        AppLogger.get().info("Saved " + applications.size() + " applications to " + dataFile);
    }

    public List<LoanApplication> load() throws IOException {
        List<LoanApplication> list = new ArrayList<>();
        if (!Files.exists(dataFile)) {
            return list;
        }
        try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
            reader.readLine();                       
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    list.add(fromLine(line));
                } catch (InvalidInputException | RuntimeException e) {
                    AppLogger.get().log(java.util.logging.Level.WARNING,
                            "Skipping corrupted record: " + line, e);
                }
            }
        }
        AppLogger.get().info("Loaded " + list.size() + " applications from " + dataFile);
        return list;
    }

    private String toLine(LoanApplication a) {
        Applicant p = a.getApplicant();
        Loan l = a.getLoan();
        return String.join("|",
                a.getAppId(), p.getName(), String.valueOf(p.getAge()),
                String.valueOf(p.getMonthlyIncome()), String.valueOf(p.getCreditScore()),
                p.getPhone(), p.getEmail(), l.getLoanType(),
                String.valueOf(l.getPrincipal()), String.valueOf(l.getTenureMonths()),
                StringUtil.sanitize(l.getExtraValue()), a.getStatus().name(),
                StringUtil.sanitize(a.getRemarks()), a.getSubmittedAt().toString());
    }

    private LoanApplication fromLine(String line) throws InvalidInputException {
        String[] f = line.split("\\|", -1);
        if (f.length < 14) {
            throw new InvalidInputException("Record has missing fields");
        }
        Applicant applicant = new Applicant(f[1], Integer.parseInt(f[2]), Double.parseDouble(f[3]),
                Integer.parseInt(f[4]), f[5], f[6]);
        Loan loan = LoanFactory.create(f[7], Double.parseDouble(f[8]), Integer.parseInt(f[9]), f[10]);
        return new LoanApplication(f[0], applicant, loan, ApplicationStatus.valueOf(f[11]), f[12],
                LocalDateTime.parse(f[13]));
    }

    public Path exportReport(String content) throws IOException {
        Files.createDirectories(reportDir);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path file = reportDir.resolve("loan_report_" + stamp + ".txt");
        Files.writeString(file, content);
        AppLogger.get().info("Report exported: " + file.toAbsolutePath());
        return file;
    }
}
