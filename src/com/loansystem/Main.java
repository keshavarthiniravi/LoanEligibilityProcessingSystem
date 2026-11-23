package com.loansystem;

import com.loansystem.ui.MainFrame;
import com.loansystem.util.AppLogger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            AppLogger.get().warning("Could not set look and feel: " + e.getMessage());
        }
        AppLogger.get().info("Starting Loan Eligibility and Processing System");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
