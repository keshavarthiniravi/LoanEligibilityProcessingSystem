package com.loansystem.ui;

import com.loansystem.model.LoanApplication;
import com.loansystem.service.FileManager;
import com.loansystem.service.LoanService;
import com.loansystem.util.AppLogger;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Week 3 - Inheritance: the main window extends JFrame.
 * Week 7 - Threading: a daemon thread keeps the clock running.
 * Week 12 - Event handling: window, mouse and action listeners.
 */
public class MainFrame extends JFrame implements LoanService.ProcessingListener {
    private final LoanService service = new LoanService(new FileManager());
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel clockLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Ready");
    private final List<NavButton> navButtons = new ArrayList<>();
    private final List<Refreshable> screens = new ArrayList<>();
    private final EmiPanel emiPanel = new EmiPanel();
    private volatile boolean clockRunning = true;

    public MainFrame() {
        super("Loan Eligibility and Processing System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        DashboardPanel dashboard = new DashboardPanel(service);
        ApplyPanel apply = new ApplyPanel(service, () -> show("applications"));
        ApplicationsPanel applications = new ApplicationsPanel(service, app -> {
            emiPanel.showFor(app);
            show("emi");
        });
        ReportPanel reports = new ReportPanel(service);

        addScreen("dashboard", dashboard);
        addScreen("apply", apply);
        addScreen("applications", applications);
        addScreen("emi", emiPanel);
        addScreen("reports", reports);

        add(buildSidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout());
        right.add(buildHeader(), BorderLayout.NORTH);
        right.add(content, BorderLayout.CENTER);
        right.add(buildStatusBar(), BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);

        service.addListener(this);
        startClock();
        show("dashboard");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                clockRunning = false;
                service.shutdown();
                dispose();
                System.exit(0);
            }
        });
        AppLogger.get().info("Application window created");
    }

    private void addScreen(String name, javax.swing.JPanel panel) {
        content.add(panel, name);
        screens.add((Refreshable) panel);
    }

    private JPanel buildSidebar() {
        Theme.GradientPanel side = new Theme.GradientPanel(Theme.NAVY, Theme.INDIGO, 0);
        side.setLayout(new BorderLayout());
        side.setPreferredSize(new Dimension(230, 0));

        JPanel logo = new JPanel(new GridLayout(2, 1));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 10));
        JLabel brand = new JLabel("LOAN DESK");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel tag = new JLabel("ELIGIBILITY & PROCESSING");
        tag.setForeground(new Color(255, 255, 255, 150));
        tag.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logo.add(brand);
        logo.add(tag);
        side.add(logo, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 6));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        String[][] items = {{"Dashboard", "dashboard"}, {"New Application", "apply"},
                {"Applications", "applications"}, {"EMI Calculator", "emi"}, {"Reports", "reports"}};
        Color[] accents = {Theme.TEAL, Theme.ORANGE, Theme.PINK, Theme.PURPLE, Theme.BLUE};
        for (int i = 0; i < items.length; i++) {
            NavButton b = new NavButton(items[i][0], items[i][1], accents[i]);
            navButtons.add(b);
            menu.add(b);
        }
        JPanel menuWrap = new JPanel(new BorderLayout());
        menuWrap.setOpaque(false);
        menuWrap.add(menu, BorderLayout.NORTH);
        side.add(menuWrap, BorderLayout.CENTER);

        JLabel foot = new JLabel("22IT321 - OOP Mini Project");
        foot.setForeground(new Color(255, 255, 255, 140));
        foot.setBorder(BorderFactory.createEmptyBorder(10, 22, 18, 10));
        foot.setFont(new Font("SansSerif", Font.PLAIN, 11));
        side.add(foot, BorderLayout.SOUTH);
        return side;
    }

    private JPanel buildHeader() {
        Theme.GradientPanel header = new Theme.GradientPanel(Theme.TEAL, Theme.BLUE, 0);
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        JLabel t = new JLabel("Smart Loan Eligibility and Processing System");
        t.setForeground(Color.WHITE);
        t.setFont(new Font("SansSerif", Font.BOLD, 16));
        clockLabel.setForeground(Color.WHITE);
        clockLabel.setFont(Theme.BOLD);
        header.add(t, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 26, 6));
        bar.setBackground(new Color(0xE8EDF7));
        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.TEXT);
        bar.add(statusLabel);
        return bar;
    }

    /** Week 7 - separate thread that updates the clock every second. */
    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");
        Thread clock = new Thread(() -> {
            while (clockRunning) {
                String text = LocalDateTime.now().format(fmt);
                SwingUtilities.invokeLater(() -> clockLabel.setText(text));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "ClockThread");
        clock.setDaemon(true);
        clock.start();
    }

    private void show(String name) {
        cards.show(content, name);
        for (NavButton b : navButtons) {
            b.setActive(b.key.equals(name));
        }
        refreshAll();
    }

    private void refreshAll() {
        for (Refreshable r : screens) {
            r.refresh();
        }
    }

    /** Called from worker threads - hop onto the Swing thread before touching the UI. */
    @Override
    public void onStatusChanged(LoanApplication app) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(app.getAppId() + " (" + app.getApplicant().getName() + ") -> "
                    + app.getStatus().getLabel());
            refreshAll();
        });
    }

    /** Sidebar button with hover and active highlighting. */
    private class NavButton extends JButton {
        private final String key;
        private final Color accent;
        private boolean active;
        private boolean hover;

        NavButton(String text, String key, Color accent) {
            super(text);
            this.key = key;
            this.accent = accent;
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setHorizontalAlignment(LEFT);
            setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 10));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> MainFrame.this.show(key));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
