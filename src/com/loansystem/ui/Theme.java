package com.loansystem.ui;

import com.loansystem.model.ApplicationStatus;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;

public final class Theme {
    public static final Color NAVY = new Color(0x1B2A49);
    public static final Color INDIGO = new Color(0x3B3F8F);
    public static final Color TEAL = new Color(0x20B2A5);
    public static final Color BLUE = new Color(0x2E6BC6);
    public static final Color ORANGE = new Color(0xE8963A);
    public static final Color PURPLE = new Color(0x8A63C8);
    public static final Color GREEN = new Color(0x2EAD6B);
    public static final Color RED = new Color(0xD9534F);
    public static final Color PINK = new Color(0xE0558F);
    public static final Color BG = new Color(0xF2F5FB);
    public static final Color TEXT = new Color(0x26324B);
    public static final Color MUTED = new Color(0x7A869A);

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 13);

    private Theme() { }

    public static Color statusColor(ApplicationStatus status) {
        switch (status) {
            case APPROVED: return GREEN;
            case REJECTED: return RED;
            case UNDER_REVIEW: return ORANGE;
            default: return BLUE;
        }
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TITLE);
        l.setForeground(NAVY);
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BODY);
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(TEXT);
        return l;
    }

    public static JTextField input() {
        JTextField f = new JTextField();
        f.setFont(BODY);
        f.setPreferredSize(new Dimension(220, 34));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xC9D3E6)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        return f;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0E6F1)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(cardBorder());
        return p;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(0xE6EBF5));
        table.setSelectionBackground(new Color(0xD6E4FA));
        table.setSelectionForeground(NAVY);
        JTableHeader h = table.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(NAVY);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(100, 34));
    }

    public static class RoundedButton extends JButton {
        private final Color base;
        private boolean hover;

        public RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(BOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(170, 38));
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? base.darker() : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class GradientPanel extends JPanel {
        private final Color from;
        private final Color to;
        private final int arc;

        public GradientPanel(Color from, Color to, int arc) {
            this.from = from;
            this.to = to;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, from, getWidth(), getHeight(), to));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setStroke(new BasicStroke(1f));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
