package com.loansystem.util;

public final class StringUtil {
    private StringUtil() { }

    public static String toTitleCase(String text) {
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static String money(double value) {
        return String.format("Rs. %,.2f", value);
    }

    public static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 10) {
            return phone;
        }
        return phone.substring(0, 2) + "******" + phone.substring(8);
    }

    public static String line(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    public static String pad(String text, int width) {
        return String.format("%-" + width + "s", text);
    }
}
