package com.loansystem.ui;

import com.loansystem.model.Loan;
import com.loansystem.model.LoanApplication;
import com.loansystem.service.LoanService;
import com.loansystem.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class EmiPanel extends JPanel implements Refreshable {
    private final JTextField principalF = Theme.input();
    private final JTextField rateF = Theme.input();
    private final JTextField monthsF = Theme.input();
    private final JLabel emiL = big();
    private final JLabel interestL = big();
    private final JLabel totalL = big();
    private final JLabel sourceL = Theme.subtitle("Manual calculation");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Month", "EMI", "Interest", "Principal", "Balance"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public EmiPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.add(Theme.title("EMI Calculator & Repayment Schedule"));
        head.add(sourceL);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        JPanel inputs = Theme.card();
        inputs.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        inputs.add(Theme.label("Amount (Rs.)"));
        inputs.add(principalF);
        inputs.add(Theme.label("Rate % p.a."));
        inputs.add(rateF);
        inputs.add(Theme.label("Months"));
        inputs.add(monthsF);
        Theme.RoundedButton calc = new Theme.RoundedButton("Calculate", Theme.TEAL);
        calc.addActionListener(e -> calculate());
        inputs.add(calc);
        top.add(inputs, BorderLayout.NORTH);

        JPanel tiles = new JPanel(new GridLayout(1, 3, 14, 0));
        tiles.setOpaque(false);
        tiles.add(tile("Monthly EMI", emiL, Theme.BLUE, Theme.INDIGO));
        tiles.add(tile("Total Interest", interestL, Theme.ORANGE, Theme.PINK));
        tiles.add(tile("Total Payable", totalL, Theme.GREEN, Theme.TEAL));
        top.add(tiles, BorderLayout.CENTER);
        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(head, BorderLayout.NORTH);
        north.add(top, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        JTable table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E6F1)));
        add(scroll, BorderLayout.CENTER);

        KeyAdapter enter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    calculate();
                }
            }
        };
        principalF.addKeyListener(enter);
        rateF.addKeyListener(enter);
        monthsF.addKeyListener(enter);
    }

    private static JLabel big() {
        JLabel l = new JLabel("-");
        l.setForeground(Color.WHITE);
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        return l;
    }

    private static JPanel tile(String caption, JLabel value, Color a, Color b) {
        Theme.GradientPanel p = new Theme.GradientPanel(a, b, 20);
        p.setLayout(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel c = new JLabel(caption);
        c.setForeground(new Color(255, 255, 255, 210));
        c.setFont(Theme.BOLD);
        p.add(c);
        p.add(value);
        return p;
    }

    public void showFor(LoanApplication app) {
        Loan loan = app.getLoan();
        principalF.setText(String.valueOf((long) loan.getPrincipal()));
        rateF.setText(String.format("%.2f", loan.getInterestRate(app.getApplicant())));
        monthsF.setText(String.valueOf(loan.getTenureMonths()));
        sourceL.setText("Schedule for " + app.getAppId() + " - " + app.getApplicant().getName()
                + " (" + loan.getLoanType() + " loan)");
        calculate();
    }

    private void calculate() {
        try {
            double principal = Double.parseDouble(principalF.getText().trim());
            double rate = Double.parseDouble(rateF.getText().trim());
            int months = Integer.parseInt(monthsF.getText().trim());
            if (principal <= 0 || rate < 0 || months <= 0) {
                throw new IllegalArgumentException("Values must be positive.");
            }
            double emi = Loan.calculateEMI(principal, rate, months);
            emiL.setText(StringUtil.money(emi));
            totalL.setText(StringUtil.money(emi * months));
            interestL.setText(StringUtil.money(emi * months - principal));

            model.setRowCount(0);
            List<double[]> rows = LoanService.buildSchedule(principal, rate, months);
            for (double[] r : rows) {
                model.addRow(new Object[]{(int) r[0], String.format("%,.2f", r[1]), String.format("%,.2f", r[2]),
                        String.format("%,.2f", r[3]), String.format("%,.2f", r[4])});
            }
        } catch (IllegalArgumentException e) {       
            JOptionPane.showMessageDialog(this, "Please enter valid positive numbers.\n(" + e.getMessage() + ")",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
    }
}
