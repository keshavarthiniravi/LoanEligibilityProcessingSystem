package com.loansystem.ui;

import com.loansystem.model.ApplicationStatus;
import com.loansystem.model.LoanApplication;
import com.loansystem.service.LoanService;
import com.loansystem.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Home screen: statistic tiles, bar chart and recent applications. */
public class DashboardPanel extends JPanel implements Refreshable {
    private final LoanService service;
    private final JLabel[] tileValues = new JLabel[4];
    private final BarChart chart = new BarChart();
    private final JPanel recentList = new JPanel();

    public DashboardPanel(LoanService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel heading = new JPanel(new GridLayout(2, 1));
        heading.setOpaque(false);
        heading.add(Theme.title("Welcome to your Dashboard"));
        heading.add(Theme.subtitle("Loan applications are checked automatically by background worker threads."));
        top.add(heading, BorderLayout.WEST);

        Theme.RoundedButton sample = new Theme.RoundedButton("Load Sample Data", Theme.PINK);
        sample.addActionListener(e -> {
            int n = SampleLoader.run(service);
            JOptionPane.showMessageDialog(this, n + " sample applications submitted.\n"
                    + "Watch their status change from Submitted to Under Review to Approved / Rejected.",
                    "Sample Data", JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setOpaque(false);
        btnWrap.add(sample);
        top.add(btnWrap, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);

        JPanel tiles = new JPanel(new GridLayout(1, 4, 16, 0));
        tiles.setOpaque(false);
        String[] names = {"TOTAL APPLICATIONS", "APPROVED", "REJECTED", "IN PROCESS"};
        Color[][] colours = {{Theme.BLUE, Theme.INDIGO}, {Theme.GREEN, Theme.TEAL},
                {Theme.RED, Theme.PINK}, {Theme.ORANGE, new Color(0xF2C14E)}};
        for (int i = 0; i < 4; i++) {
            Theme.GradientPanel tile = new Theme.GradientPanel(colours[i][0], colours[i][1], 22);
            tile.setLayout(new GridLayout(2, 1));
            tile.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            JLabel name = new JLabel(names[i]);
            name.setForeground(new Color(255, 255, 255, 220));
            name.setFont(Theme.BOLD);
            tileValues[i] = new JLabel("0");
            tileValues[i].setForeground(Color.WHITE);
            tileValues[i].setFont(new Font("SansSerif", Font.BOLD, 34));
            tile.add(name);
            tile.add(tileValues[i]);
            tiles.add(tile);
        }
        center.add(tiles, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        JPanel chartCard = Theme.card();
        chartCard.setLayout(new BorderLayout());
        JLabel chartTitle = new JLabel("Applications by Status");
        chartTitle.setFont(Theme.HEADING);
        chartTitle.setForeground(Theme.NAVY);
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);
        lower.add(chartCard);

        JPanel recentCard = Theme.card();
        recentCard.setLayout(new BorderLayout(0, 8));
        JLabel recentTitle = new JLabel("Recent Applications");
        recentTitle.setFont(Theme.HEADING);
        recentTitle.setForeground(Theme.NAVY);
        recentCard.add(recentTitle, BorderLayout.NORTH);
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        recentList.setBackground(Color.WHITE);
        recentCard.add(recentList, BorderLayout.CENTER);
        lower.add(recentCard);

        center.add(lower, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        Map<ApplicationStatus, Integer> counts = service.countByStatus();
        int total = 0;
        for (int value : counts.values()) {          // Week 1: for-each loop
            total += value;
        }
        tileValues[0].setText(String.valueOf(total));
        tileValues[1].setText(String.valueOf(counts.get(ApplicationStatus.APPROVED)));
        tileValues[2].setText(String.valueOf(counts.get(ApplicationStatus.REJECTED)));
        tileValues[3].setText(String.valueOf(counts.get(ApplicationStatus.SUBMITTED)
                + counts.get(ApplicationStatus.UNDER_REVIEW)));
        chart.setData(counts);

        recentList.removeAll();
        List<LoanApplication> all = service.getAll();
        int shown = 0;
        int index = all.size() - 1;
        while (index >= 0 && shown < 6) {            // Week 1: while loop (newest first)
            LoanApplication app = all.get(index);
            JLabel row = new JLabel("<html><b>" + app.getAppId() + "</b> &nbsp;" + app.getApplicant().getName()
                    + " &nbsp;<font color='gray'>" + app.getLoan().getLoanType() + " - "
                    + StringUtil.money(app.getLoan().getPrincipal()) + "</font> &nbsp;<font color='"
                    + hex(Theme.statusColor(app.getStatus())) + "'><b>" + app.getStatus().getLabel()
                    + "</b></font></html>");
            row.setFont(Theme.BODY);
            row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
            recentList.add(row);
            shown++;
            index--;
        }
        if (shown == 0) {
            recentList.add(Theme.subtitle("No applications yet - use 'New Application' or load sample data."));
        }
        recentList.revalidate();
        recentList.repaint();
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Custom-painted bar chart. */
    private static class BarChart extends JPanel {
        private Map<ApplicationStatus, Integer> data;

        BarChart() {
            setBackground(Color.WHITE);
        }

        void setData(Map<ApplicationStatus, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int max = 1;
            for (int v : data.values()) {
                max = Math.max(max, v);
            }
            ApplicationStatus[] statuses = ApplicationStatus.values();
            int slot = getWidth() / statuses.length;
            int baseY = getHeight() - 30;
            int chartH = getHeight() - 70;
            for (int i = 0; i < statuses.length; i++) {      // Week 1: indexed for loop
                int value = data.get(statuses[i]);
                int h = (int) ((double) value / max * chartH);
                int x = i * slot + slot / 6;
                int w = slot * 2 / 3;
                g2.setColor(Theme.statusColor(statuses[i]));
                g2.fillRoundRect(x, baseY - h, w, Math.max(h, 4), 12, 12);
                g2.setColor(Theme.NAVY);
                g2.setFont(Theme.HEADING);
                g2.drawString(String.valueOf(value), x + w / 2 - 5, baseY - h - 6);
                g2.setFont(Theme.BODY);
                g2.setColor(Theme.MUTED);
                g2.drawString(statuses[i].getLabel(), x + 4, baseY + 18);
            }
            g2.dispose();
        }
    }
}
