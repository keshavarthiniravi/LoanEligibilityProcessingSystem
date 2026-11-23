package com.loansystem.ui;

import com.loansystem.model.Applicant;
import com.loansystem.model.ApplicationStatus;
import com.loansystem.model.Loan;
import com.loansystem.model.LoanApplication;
import com.loansystem.service.LoanService;
import com.loansystem.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Table of all applications with search, filter and detail view. */
public class ApplicationsPanel extends JPanel implements Refreshable {
    private static final String[] COLUMNS = {"App ID", "Applicant", "Type", "Amount", "Tenure", "Rate", "EMI", "Status", "Submitted"};

    private final LoanService service;
    private final Consumer<LoanApplication> onSchedule;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchF = Theme.input();
    private final JComboBox<Object> statusBox = new JComboBox<>();
    private final JTextArea details = new JTextArea(7, 40);
    private final List<LoanApplication> visible = new ArrayList<>();

    public ApplicationsPanel(LoanService service, Consumer<LoanApplication> onSchedule) {
        this.service = service;
        this.onSchedule = onSchedule;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Theme.title("Loan Applications"));
        titles.add(Theme.subtitle("Search, filter and track every application. Status updates live."));
        head.add(titles, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        filters.setOpaque(false);
        searchF.setPreferredSize(new java.awt.Dimension(200, 34));
        statusBox.addItem("All Status");
        for (ApplicationStatus s : ApplicationStatus.values()) {
            statusBox.addItem(s);
        }
        filters.add(Theme.label("Search:"));
        filters.add(searchF);
        filters.add(statusBox);
        head.add(filters, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        int[] widths = {70, 140, 90, 140, 70, 70, 120, 100, 140};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E6F1)));
        add(scroll, BorderLayout.CENTER);

        details.setEditable(false);
        details.setFont(Theme.MONO);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBackground(new Color(0xFBFCFF));
        details.setText("Select an application to see its details.");
        JScrollPane detailScroll = new JScrollPane(details);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Application Details"));

        Theme.RoundedButton schedule = new Theme.RoundedButton("View EMI Schedule", Theme.PURPLE);
        Theme.RoundedButton refresh = new Theme.RoundedButton("Refresh", Theme.BLUE);
        schedule.addActionListener(e -> openSchedule());
        refresh.addActionListener(e -> refresh());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        buttons.setOpaque(false);
        buttons.add(schedule);
        buttons.add(refresh);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(detailScroll, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // ---- events (Week 12) ----
        searchF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        });
        statusBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                refresh();
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetails();
            }
        });
        refresh();
    }

    @Override
    public void refresh() {
        String selectedId = selectedApplication() == null ? null : selectedApplication().getAppId();
        String query = searchF.getText().trim().toLowerCase();
        Object statusFilter = statusBox.getSelectedItem();

        model.setRowCount(0);
        visible.clear();
        for (LoanApplication app : service.getAll()) {
            boolean statusOk = !(statusFilter instanceof ApplicationStatus) || app.getStatus() == statusFilter;
            String haystack = (app.getAppId() + " " + app.getApplicant().getName() + " "
                    + app.getLoan().getLoanType()).toLowerCase();
            if (statusOk && haystack.contains(query)) {
                Loan loan = app.getLoan();
                Applicant person = app.getApplicant();
                model.addRow(new Object[]{app.getAppId(), person.getName(), loan.getLoanType(),
                        StringUtil.money(loan.getPrincipal()), loan.getTenureMonths() + " m",
                        String.format("%.2f %%", loan.getInterestRate(person)),
                        StringUtil.money(loan.calculateEMI(person)), app.getStatus().getLabel(),
                        app.getSubmittedAt().format(LoanApplication.FMT)});
                visible.add(app);
                if (app.getAppId().equals(selectedId)) {
                    int row = visible.size() - 1;
                    table.setRowSelectionInterval(row, row);
                }
            }
        }
        showDetails();
    }

    private LoanApplication selectedApplication() {
        int row = table.getSelectedRow();
        return row >= 0 && row < visible.size() ? visible.get(row) : null;
    }

    private void showDetails() {
        LoanApplication app = selectedApplication();
        if (app == null) {
            details.setText("Select an application to see its details.");
            return;
        }
        Applicant p = app.getApplicant();
        Loan l = app.getLoan();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Application : %s   (%s)%n", app.getAppId(), app.getStatus().getLabel()));
        sb.append(String.format("Applicant   : %s, %d yrs | Income %s | Credit %d (%s)%n", p.getName(), p.getAge(),
                StringUtil.money(p.getMonthlyIncome()), p.getCreditScore(), p.getCreditBand()));
        sb.append(String.format("Contact     : %s | %s%n", StringUtil.maskPhone(p.getPhone()), p.getEmail()));
        sb.append(String.format("Loan        : %s | %s: %s%n", l, l.getExtraLabel(), l.getExtraValue()));
        sb.append(String.format("Repayment   : EMI %s | Total payable %s | Interest %s%n",
                StringUtil.money(l.calculateEMI(p)), StringUtil.money(l.totalPayable(p)),
                StringUtil.money(l.totalInterest(p))));
        sb.append("Remarks     : ").append(app.getRemarks());
        details.setText(sb.toString());
        details.setCaretPosition(0);
    }

    private void openSchedule() {
        LoanApplication app = selectedApplication();
        if (app == null) {
            JOptionPane.showMessageDialog(this, "Please select an application first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        onSchedule.accept(app);
    }

    /** Colours the status column. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                       boolean focus, int row, int col) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            c.setHorizontalAlignment(SwingConstants.CENTER);
            c.setFont(Theme.BOLD);
            for (ApplicationStatus s : ApplicationStatus.values()) {
                if (s.getLabel().equals(String.valueOf(value))) {
                    c.setForeground(Theme.statusColor(s));
                }
            }
            return c;
        }
    }
}
