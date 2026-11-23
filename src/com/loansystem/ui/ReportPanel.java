package com.loansystem.ui;

import com.loansystem.model.ApplicationStatus;
import com.loansystem.model.LoanApplication;
import com.loansystem.service.LoanService;
import com.loansystem.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Text report generator with export to the reports/ folder. */
public class ReportPanel extends JPanel implements Refreshable {
    private final LoanService service;
    private final JTextArea area = new JTextArea();

    public ReportPanel(LoanService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.add(Theme.title("Loan Reports"));
        head.add(Theme.subtitle("Generate a summary of all applications and export it as a text file."));
        add(head, BorderLayout.NORTH);

        area.setEditable(false);
        area.setFont(Theme.MONO);
        area.setBackground(new Color(0xFBFCFF));
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        area.setText("Press 'Generate Report' to create the report.");
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E6F1)));
        add(scroll, BorderLayout.CENTER);

        Theme.RoundedButton gen = new Theme.RoundedButton("Generate Report", Theme.TEAL);
        Theme.RoundedButton export = new Theme.RoundedButton("Export to File", Theme.PURPLE);
        gen.addActionListener(e -> area.setText(buildReport()));
        export.addActionListener(e -> exportReport());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        buttons.setOpaque(false);
        buttons.add(gen);
        buttons.add(export);
        add(buttons, BorderLayout.SOUTH);
    }

    private String buildReport() {
        List<LoanApplication> all = service.getAll();
        all.sort(Comparator.comparing(LoanApplication::getStatus).thenComparing(LoanApplication::getAppId));

        Map<ApplicationStatus, Integer> counts = service.countByStatus();
        Map<String, double[]> byType = new LinkedHashMap<>();     // type -> {count, amount}
        for (LoanApplication app : all) {
            double[] slot = byType.computeIfAbsent(app.getLoan().getLoanType(), k -> new double[2]);
            slot[0]++;
            slot[1] += app.getLoan().getPrincipal();
        }
        int decided = counts.get(ApplicationStatus.APPROVED) + counts.get(ApplicationStatus.REJECTED);
        double rate = decided == 0 ? 0 : counts.get(ApplicationStatus.APPROVED) * 100.0 / decided;

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtil.line('=', 90)).append('\n');
        sb.append("LOAN ELIGIBILITY AND PROCESSING SYSTEM - SUMMARY REPORT\n");
        sb.append("Generated on: ").append(LocalDateTime.now().format(LoanApplication.FMT)).append('\n');
        sb.append(StringUtil.line('=', 90)).append("\n\n");

        sb.append("OVERALL SUMMARY\n").append(StringUtil.line('-', 40)).append('\n');
        sb.append(String.format("Total applications : %d%n", all.size()));
        for (ApplicationStatus s : ApplicationStatus.values()) {
            sb.append(String.format("%-19s: %d%n", s.getLabel(), counts.get(s)));
        }
        sb.append(String.format("Approval rate      : %.1f %% (of decided applications)%n", rate));
        sb.append(String.format("Total approved     : %s%n%n", StringUtil.money(service.totalApprovedAmount())));

        sb.append("BY LOAN TYPE\n").append(StringUtil.line('-', 40)).append('\n');
        for (Map.Entry<String, double[]> e : byType.entrySet()) {
            sb.append(String.format("%-10s applications: %-3d total requested: %s%n", e.getKey(),
                    (int) e.getValue()[0], StringUtil.money(e.getValue()[1])));
        }

        sb.append("\nAPPLICATION DETAILS\n").append(StringUtil.line('-', 90)).append('\n');
        sb.append(String.format("%-8s %-20s %-10s %14s %5s %-13s%n", "ID", "Applicant", "Type", "Amount", "Mths", "Status"));
        sb.append(StringUtil.line('-', 90)).append('\n');
        List<String> rejectedNotes = new ArrayList<>();
        for (LoanApplication app : all) {
            sb.append(app.toReportLine()).append('\n');
            if (app.getStatus() == ApplicationStatus.REJECTED) {
                rejectedNotes.add(app.getAppId() + ": " + app.getRemarks());
            }
        }
        if (!rejectedNotes.isEmpty()) {
            sb.append("\nREJECTION REASONS\n").append(StringUtil.line('-', 90)).append('\n');
            for (String note : rejectedNotes) {
                sb.append(note).append('\n');
            }
        }
        sb.append('\n').append(StringUtil.line('=', 90)).append("\nEnd of report\n");
        return sb.toString();
    }

    private void exportReport() {
        if (area.getText().startsWith("Press")) {
            area.setText(buildReport());
        }
        try {
            Path file = service.getFileManager().exportReport(area.getText());
            JOptionPane.showMessageDialog(this, "Report saved to:\n" + file.toAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save the report: " + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        // report is generated on demand
    }
}
