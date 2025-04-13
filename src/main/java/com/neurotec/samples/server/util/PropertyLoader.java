package com.neurotec.samples.server.util;

import com.neurotec.samples.server.settings.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

    public static String fileName;
    private static final Properties properties = new Properties();

    public PropertyLoader() {
//        File file = new File("C:/Servers/AFISServer_Asif/application.properties");
//        File file = new File("D:/NeuroTechnology/application.properties");
//        System.out.println(fileName);
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getDSN() {
        return properties.getProperty("database.dsn") == null ? "jdbc:sqlserver://167.86.68.15:1433;databaseName=SNSOP_AFIS_TEST;encrypt=false" : properties.getProperty("database.dsn");
    }

    public static String getUser() {
        return properties.getProperty("database.username") == null ? "snsop" : properties.getProperty("database.username");
    }

    public static String getPassword() {
        return properties.getProperty("database.password") == null ? "Win@2019!@#$" : properties.getProperty("database.password");
    }

    public static String getTable() {
        return properties.getProperty("database.table") == null ? "Subjects" : properties.getProperty("database.table");
    }

    public static String getTemplateColumn() {
        return properties.getProperty("database.template.column") == null ? "Template" : properties.getProperty("database.template.column");
    }

    public static String getIdColumn() {
        return properties.getProperty("database.id.column") == null ? "SubjectId" : properties.getProperty("database.id.column");
    }

    public static String getServerHost() {
        return properties.getProperty("server.host") == null ? "localhost" : properties.getProperty("server.host");
    }

    public static Integer getClientPort() {
        return Integer.parseInt(properties.getProperty("server.client.port") == null ? "25452" : properties.getProperty("server.client.port"));
    }

    public static Integer getAdminPort() {
        return Integer.parseInt(properties.getProperty("server.admin.port") == null ? "24932" : properties.getProperty("server.admin.port"));
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
