package com.neurotec.samples.server.util;

import com.neurotec.samples.server.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

    private static final Properties properties = new Properties();

    public PropertyLoader() {
        File file = new File("C:/Servers/AFISServer_Asif/application.properties");
        try (FileInputStream fis = new FileInputStream(file)){
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDSN() {
        return properties.getProperty("database.dsn");
    }

    public static String getUser() {
        return properties.getProperty("database.username");
    }

    public static String getPassword() {
        return properties.getProperty("database.password");
    }

    public static String getTable() {
        return properties.getProperty("database.table");
    }

    public static String getTemplateColumn() {
        return properties.getProperty("database.template.column");
    }

    public static String getIdColumn() {
        return properties.getProperty("database.id.column");
    }

    public static String getServerHost() {
        return properties.getProperty("server.host");
    }

    public static Integer getClientPort() {
        return Integer.parseInt(properties.getProperty("server.client.port"));
    }

    public static Integer getAdminPort() {
        return Integer.parseInt(properties.getProperty("server.admin.port"));
    }

    public String getResultDirectory() {
        return properties.getProperty("dir.result");
    }

    public static Settings getSettings() {
        Settings settings = Settings.getInstance();
        
        // Set connection settings from properties
        settings.setServer(getServerHost());
        settings.setClientPort(getClientPort());
        settings.setAdminPort(getAdminPort());
        
        // Set database connection settings
        settings.setDSN(getDSN());
        settings.setDBUser(getUser());
        settings.setDBPassword(getPassword());
        settings.setTable(getTable());
        settings.setTemplateColumn(getTemplateColumn());
        settings.setIdColumn(getIdColumn());
        
        return settings;
    }
}
