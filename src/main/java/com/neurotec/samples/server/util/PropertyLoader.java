package com.neurotec.samples.server.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {

    private final Properties properties = new Properties();

    public PropertyLoader() {
        File file = new File("D:/NeuroTechnology/application.properties");
        try (FileInputStream fis = new FileInputStream(file)){
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDSN() {
        return properties.getProperty("database.dsn");
    }

    public String getUser() {
        return properties.getProperty("database.username");
    }

    public String getPassword() {
        return properties.getProperty("database.password");
    }

    public String getTable() {
        return properties.getProperty("database.table");
    }

    public String getTemplateColumn() {
        return properties.getProperty("database.template.column");
    }

    public String getIdColumn() {
        return properties.getProperty("database.id.column");
    }

    public String getServerHost() {
        return properties.getProperty("server.host");
    }

    public Integer getClientPort() {
        return Integer.parseInt(properties.getProperty("server.client.port"));
    }

    public Integer getAdminPort() {
        return Integer.parseInt(properties.getProperty("server.admin.port"));
    }
}
