package com.neurotec.samples.server.util;

import java.awt.Container;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public final class MessageUtils {
    private static final String ERROR_TITLE = "Error";
    private static final String INFORMATION_TITLE = "Information";
    private static final String QUESTION_TITLE = "Question";

    public static String getCurrentApplicationName() {
        return "ServerSampleJava";
    }

    public static void showError(Container owner, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(owner, message, String.format("%s: %s", new Object[]{getCurrentApplicationName(), "Error"}), 0));
    }

    public static void showError(Container owner, Exception exception) {
        if (exception == null) throw new NullPointerException("exception");
        exception.printStackTrace();
        showError(owner, exception.toString());
    }

    public static void showError(Container owner, String format, Object[] args) {
        String str = String.format(format, args);
        showError(owner, str);
    }

    public static void showInformation(Container owner, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(owner, message, String.format("%s: %s", new Object[]{getCurrentApplicationName(), "Information"}), 1));
    }

    public static void showInformation(Container owner, String format, Object[] args) {
        String str = String.format(format, args);
        showInformation(owner, str);
    }

    public static boolean showQuestion(Container owner, String message) {
        return (0 == JOptionPane.showConfirmDialog(owner, message, String.format("%s: %s", new Object[]{getCurrentApplicationName(), "Question"}), 0));
    }


    public static boolean showQuestion(Container owner, String format, Object[] args) {
        String str = String.format(format, args);
        return showQuestion(owner, str);
    }
}
