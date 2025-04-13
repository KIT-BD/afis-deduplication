package com.neurotec.samples.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public final class Settings {
    private static Settings defaultInstance;
    private static final String HOME_PATH = System.getProperty("user.home");
    private static final String APP_DATA_PATH = HOME_PATH + System.getProperty("file.separator") + "AppData";
    private static final String APP_DATA_LOCAL_PATH = APP_DATA_PATH + System.getProperty("file.separator") + "Local";
    private static final String NEURO_DIRECTORY_PATH = APP_DATA_LOCAL_PATH + System.getProperty("file.separator") + "Neurotechnology";


    private final String propertiesPath;


    public static Settings getDefault(String sampleName) {
        synchronized (Settings.class) {
            if (defaultInstance == null) {
                defaultInstance = new Settings(sampleName);
            }
            return defaultInstance;
        }
    }


    private final Properties properties = new Properties();


    private Settings(String sampleName) {
        String sampleDirectoryPath = NEURO_DIRECTORY_PATH + System.getProperty("file.separator") + sampleName;
        this.propertiesPath = sampleDirectoryPath + System.getProperty("file.separator") + "user.properties";

        File sampleDirectory = new File(sampleDirectoryPath);
        if (!sampleDirectory.exists() || !sampleDirectory.isDirectory()) {
            sampleDirectory.mkdirs();
        }
        File propertiesFile = new File(this.propertiesPath);
        if (propertiesFile.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(propertiesFile);
                this.properties.load(is);
            } catch (IOException e) {
                e.printStackTrace();
                loadDefaultSettings();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void loadDefaultSettings() {
        this.properties.put("lastDirectory", "");
    }


    public String getLastDirectory() {
        return this.properties.getProperty("lastDirectory");
    }

    public void setLastDirectory(String value) {
        this.properties.setProperty("lastDirectory", value);
    }

    public void save() {
        File propertiesFile = new File(this.propertiesPath);
        OutputStream os = null;
        try {
            os = new FileOutputStream(propertiesFile);
            this.properties.store(os, (String) null);
        } catch (IOException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.toString()));
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
