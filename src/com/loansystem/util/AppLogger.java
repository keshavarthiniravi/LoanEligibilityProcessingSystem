package com.loansystem.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Week 11 - Central logging (console + logs/loan_system.log). */
public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("LoanSystem");

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.ALL);
        Formatter formatter = new Formatter() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord r) {
                String text = String.format("%s [%-7s] [%s] %s%n", LocalDateTime.now().format(fmt),
                        r.getLevel(), Thread.currentThread().getName(), formatMessage(r));
                if (r.getThrown() != null) {
                    text += "    cause: " + r.getThrown() + System.lineSeparator();
                }
                return text;
            }
        };
        ConsoleHandler console = new ConsoleHandler();
        console.setFormatter(formatter);
        console.setLevel(Level.INFO);
        LOGGER.addHandler(console);
        try {
            Files.createDirectories(Paths.get("logs"));
            FileHandler file = new FileHandler("logs/loan_system.log", true);
            file.setFormatter(formatter);
            file.setLevel(Level.ALL);
            LOGGER.addHandler(file);
        } catch (IOException e) {
            LOGGER.warning("Could not create log file: " + e.getMessage());
        }
    }

    private AppLogger() { }

    public static Logger get() {
        return LOGGER;
    }
}
