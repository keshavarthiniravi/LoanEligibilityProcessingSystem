package com.loansystem.ui;

import com.loansystem.exception.InvalidInputException;
import com.loansystem.exception.LoanNotEligibleException;
import com.loansystem.model.Applicant;
import com.loansystem.model.Loan;
import com.loansystem.model.LoanApplication;
import com.loansystem.service.LoanFactory;
import com.loansystem.service.LoanService;
import com.loansystem.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class ApplyPanel extends JPanel implements Refreshable {
    private final LoanService service;
    private final Runnable onSubmitted;

    private final JTextField nameF = Theme.input();
    private final JTextField ageF = Theme.input();
    private final JTextField incomeF = Theme.input();
    private final JTextField phoneF = Theme.input();
    private final JTextField emailF = Theme.input();
    private final JTextField extraF = Theme.input();
    private final JTextField amountF = Theme.input();
    private final JSlider creditSlider = new JSlider(300, 900, 720);
    private final JSlider tenureSlider = new JSlider(1, 30, 5);
    private final JLabel creditLabel = Theme.label("720 (Good)");
    private final JLabel tenureLabel = Theme.label("5 years");
    private final JComboBox<String> typeBox = new JComboBox<>(LoanFactory.TYPES);
    private final JLabel extraLabel = Theme.label("Course Name");

    private final JLabel pvMax = previewValue();
    private final JLabel pvRate = previewValue();
    private final JLabel pvEmi = previewValue();
    private final JLabel pvTotal = previewValue();
    private final JLabel pvInterest = previewValue();
    private final JLabel pvVerdict = new JLabel(" ");

    public ApplyPanel(LoanService service, Runnable onSubmitted) {
        this.service = service;
        this.onSubmitted = onSubmitted;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.add(Theme.title("New Loan Application"));
        head.add(Theme.subtitle("Fill in the details - the preview on the right updates as you type."));
        add(head, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(buildForm(), BorderLayout.CENTER);
        body.add(buildPreview(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        wireEvents();
        updatePreview();
    }

    private static JLabel previewValue() {
        JLabel l = new JLabel("-");
        l.setForeground(Color.WHITE);
        l.setFont(Theme.HEADING);
        return l;
    }

    private JPanel buildForm() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 14));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        creditSlider.setBackground(Color.WHITE);
        tenureSlider.setBackground(Color.WHITE);
        JPanel creditBox = sliderBox(creditSlider, creditLabel);
        JPanel tenureBox = sliderBox(tenureSlider, tenureLabel);

        addRow(form, 0, "Full Name", nameF, "Age", ageF);
        addRow(form, 1, "Monthly Income (Rs.)", incomeF, "Credit Score", creditBox);
        addRow(form, 2, "Phone (10 digits)", phoneF, "E-mail", emailF);
        addRow(form, 3, "Loan Type", typeBox, extraLabel, extraF);
        addRow(form, 4, "Loan Amount (Rs.)", amountF, "Tenure", tenureBox);
        typeBox.setFont(Theme.BODY);
        typeBox.setPreferredSize(new Dimension(220, 34));
        card.add(form, BorderLayout.NORTH);

        Theme.RoundedButton check = new Theme.RoundedButton("Check Eligibility", Theme.BLUE);
        Theme.RoundedButton submit = new Theme.RoundedButton("Submit Application", Theme.TEAL);
        Theme.RoundedButton clear = new Theme.RoundedButton("Clear Form", Theme.ORANGE);
        check.addActionListener(e -> doCheck());
        submit.addActionListener(e -> doSubmit());
        clear.addActionListener(e -> clearForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(check);
        buttons.add(submit);
        buttons.add(clear);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private static JPanel sliderBox(JSlider slider, JLabel label) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.WHITE);
        p.add(slider, BorderLayout.CENTER);
        label.setPreferredSize(new Dimension(90, 20));
        p.add(label, BorderLayout.EAST);
        p.setPreferredSize(new Dimension(220, 34));
        return p;
    }

    private void addRow(JPanel form, int row, String l1, java.awt.Component c1,
                        Object l2, java.awt.Component c2) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(9, 6, 9, 10);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        form.add(Theme.label(l1), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(c1, g);
        g.gridx = 2;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;
        g.insets = new Insets(9, 18, 9, 10);
        form.add(l2 instanceof JLabel ? (JLabel) l2 : Theme.label(l2.toString()), g);
        g.gridx = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(9, 6, 9, 6);
        form.add(c2, g);
    }

    private JPanel buildPreview() {
        Theme.GradientPanel p = new Theme.GradientPanel(Theme.INDIGO, Theme.PURPLE, 24);
        p.setLayout(new GridLayout(0, 1, 0, 4));
        p.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        p.setPreferredSize(new Dimension(290, 0));
        JLabel title = new JLabel("Live Loan Preview");
        title.setFont(Theme.HEADING);
        title.setForeground(Color.WHITE);
        p.add(title);
        addPreviewRow(p, "Max eligible amount", pvMax);
        addPreviewRow(p, "Interest rate (p.a.)", pvRate);
        addPreviewRow(p, "Monthly EMI", pvEmi);
        addPreviewRow(p, "Total payable", pvTotal);
        addPreviewRow(p, "Total interest", pvInterest);
        pvVerdict.setFont(Theme.BOLD);
        pvVerdict.setForeground(Color.WHITE);
        p.add(pvVerdict);
        return p;
    }

    private static void addPreviewRow(JPanel p, String caption, JLabel value) {
        JLabel c = new JLabel(caption);
        c.setForeground(new Color(255, 255, 255, 190));
        c.setFont(Theme.BODY);
        p.add(c);
        p.add(value);
    }

    private void wireEvents() {
        digitsOnly(ageF, 2);
        digitsOnly(incomeF, 9);
        digitsOnly(phoneF, 10);
        digitsOnly(amountF, 9);

        KeyAdapter refreshOnKey = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        };
        for (JTextField f : new JTextField[]{ageF, incomeF, amountF, extraF}) {
            f.addKeyListener(refreshOnKey);
        }
        amountF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    doSubmit();
                }
            }
        });

        creditSlider.addChangeListener(e -> {
            int score = creditSlider.getValue();
            creditLabel.setText(score + " (" + bandOf(score) + ")");
            updatePreview();
        });
        tenureSlider.addChangeListener(e -> {
            tenureLabel.setText(tenureSlider.getValue() + (tenureSlider.getValue() == 1 ? " year" : " years"));
            updatePreview();
        });
        typeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                extraLabel.setText(labelFor((String) typeBox.getSelectedItem()));
                extraF.setText("");
                updatePreview();
            }
        });
    }

    private static String bandOf(int score) {
        if (score >= 800) {
            return "Excellent";
        }
        if (score >= 750) {
            return "Very Good";
        }
        if (score >= 700) {
            return "Good";
        }
        return score >= 650 ? "Fair" : "Poor";
    }

    private static String labelFor(String type) {
        switch (type) {
            case "Home": return "Property Value (Rs.)";
            case "Personal": return "Purpose";
            default: return "Course Name";
        }
    }

    private static void digitsOnly(JTextField field, int maxLength) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c)) {
                    if (field.getText().length() >= maxLength) {
                        e.consume();
                    }
                } else if (c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE && c != KeyEvent.VK_ENTER) {
                    e.consume();
                }
            }
        });
    }

    private Applicant buildApplicant() throws InvalidInputException {
        try {
            return new Applicant(nameF.getText(), Integer.parseInt(ageF.getText().trim()),
                    Double.parseDouble(incomeF.getText().trim()), creditSlider.getValue(),
                    phoneF.getText(), emailF.getText());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Age and monthly income must be valid numbers.");
        }
    }

    private Loan buildLoan() throws InvalidInputException {
        try {
            return LoanFactory.create((String) typeBox.getSelectedItem(),
                    Double.parseDouble(amountF.getText().trim()), tenureSlider.getValue() * 12,
                    extraF.getText());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Loan amount must be a valid number.");
        }
    }

    private void updatePreview() {
        try {
            Applicant a = new Applicant("Preview", Integer.parseInt(ageF.getText().trim()),
                    Double.parseDouble(incomeF.getText().trim()), creditSlider.getValue(),
                    "9999999999", "preview@mail.com");
            String type = (String) typeBox.getSelectedItem();
            String extra = extraF.getText().isBlank() ? ("Home".equals(type) ? "100000000" : "Not specified")
                    : extraF.getText();
            Loan loan = LoanFactory.create(type, Double.parseDouble(amountF.getText().trim()),
                    tenureSlider.getValue() * 12, extra);
            pvMax.setText(StringUtil.money(loan.maxEligibleAmount(a)));
            pvRate.setText(String.format("%.2f %%", loan.getInterestRate(a)));
            pvEmi.setText(StringUtil.money(loan.calculateEMI(a)));
            pvTotal.setText(StringUtil.money(loan.totalPayable(a)));
            pvInterest.setText(StringUtil.money(loan.totalInterest(a)));
            try {
                loan.checkEligibility(a);
                pvVerdict.setText("Looks eligible");
            } catch (LoanNotEligibleException ex) {
                pvVerdict.setText(ex.getReasons().size() + " issue(s) found - press Check");
            }
        } catch (InvalidInputException | NumberFormatException e) {
            pvMax.setText("-");
            pvRate.setText("-");
            pvEmi.setText("-");
            pvTotal.setText("-");
            pvInterest.setText("-");
            pvVerdict.setText("Enter age, income and amount");
        }
    }

    private void doCheck() {
        try {
            Applicant a = buildApplicant();
            Loan loan = buildLoan();
            loan.checkEligibility(a);
            JOptionPane.showMessageDialog(this, String.format(
                    "Congratulations %s!%nYou are ELIGIBLE for this %s loan.%n%n"
                            + "Interest rate : %.2f %% p.a.%nMonthly EMI   : %s%nTotal payable : %s",
                    a.getName(), loan.getLoanType(), loan.getInterestRate(a),
                    StringUtil.money(loan.calculateEMI(a)), StringUtil.money(loan.totalPayable(a))),
                    "Eligible", JOptionPane.INFORMATION_MESSAGE);
        } catch (LoanNotEligibleException e) {
            StringBuilder sb = new StringBuilder("Not eligible for the following reasons:\n\n");
            int n = 1;
            for (String reason : e.getReasons()) {
                sb.append(n++).append(". ").append(reason).append('\n');
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Not Eligible", JOptionPane.WARNING_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSubmit() {
        try {
            Applicant a = buildApplicant();
            Loan loan = buildLoan();
            LoanApplication app = service.submit(a, loan);
            JOptionPane.showMessageDialog(this, "Application " + app.getAppId() + " submitted successfully!\n"
                    + "It is now being processed in the background.", "Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            onSubmitted.run();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        for (JTextField f : new JTextField[]{nameF, ageF, incomeF, phoneF, emailF, extraF, amountF}) {
            f.setText("");
        }
        creditSlider.setValue(720);
        tenureSlider.setValue(5);
        typeBox.setSelectedIndex(0);
        updatePreview();
    }

    @Override
    public void refresh() {
    }
}
