package com.kit.biometric;

import com.neurotec.licensing.NLicense;
import com.neurotec.samples.server.MainFrame;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.server.util.PropertyLoader;
import com.neurotec.samples.util.LibraryManager;
import com.neurotec.samples.util.Utils;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;


public final class AFISServer {
    static Collection<String> licenses = null;
    public static void main(String[] args) {
        Utils.setupLookAndFeel();
        System.out.println(System.getProperty("user.home"));

        PropertyLoader.fileName = args[0];

        LibraryManager.initLibraryPath();
        licenses = new ArrayList<>();

        licenses.add("Biometrics.FingerClient");

        try {
            if (!NLicense.obtain("/local", 5000, "FingerExtractor")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerExtractor"});
            }


            if (!NLicense.obtain("/local", 5000, "FingerClient")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerClient"});
            }


            if (!NLicense.obtain("/local", 5000, "FingerMatcher")) {
                System.err.format("Could not obtain license: %s%n", new Object[]{"FingerMatcher"});
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(-1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();
                    Dimension d = new Dimension(935, 450);
                    frame.setSize(d);
                    frame.setMinimumSize(d);
                    frame.setPreferredSize(d);

                    frame.setResizable(true);
                    frame.setDefaultCloseOperation(2);
                    frame.setTitle("Server Sample");
                    frame.setLocationRelativeTo(null);
                    frame.showMainFrame();
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageUtils.showError(null, e);
                }
            }
        });
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\kit\biometric\AFISServer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */