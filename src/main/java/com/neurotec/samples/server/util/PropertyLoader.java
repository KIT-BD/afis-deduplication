package com.neurotec.samples.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.neurotec.samples.server.settings.Settings;

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
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yy_(HH_mm_ss)");
        String dirResult = properties.getProperty("dir.result");
        return dirResult.split(".csv")[0]+"_"+sdf.format(now)+".csv";
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

    public static String getProdDSN() {
        return properties.getProperty("prod.database.dsn");
    }

    public static String getProdUser() {
        return properties.getProperty("prod.database.username");
    }

    public static String getProdPassword() {
        return properties.getProperty("prod.database.password");
    }

    public static String getProdTable() {
        return properties.getProperty("prod.database.table");
    }

    public static String getProdTemplateColumn() {
        return properties.getProperty("prod.database.template.column");
    }

    public static String getProdIdColumn() {
        return properties.getProperty("prod.database.id.column");
    }

    public static long getTableMaxId() {
        return Long.parseLong(properties.getProperty("table.max.id", "0"));
    }

    public static void setTableMaxId(long maxId) {
        properties.setProperty("table.max.id", String.valueOf(maxId));
        try {
            Path path = Paths.get(fileName);
            List<String> lines = Files.readAllLines(path);
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("table.max.id")) {
                    lines.set(i, "table.max.id=" + maxId);
                    found = true;
                    break;
                }
            }
            if (!found) {
                lines.add("table.max.id=" + maxId);
            }
            Files.write(path, lines);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to properties file", e);
        }
    }
}
