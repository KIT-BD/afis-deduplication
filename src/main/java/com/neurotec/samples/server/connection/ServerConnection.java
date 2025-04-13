package com.neurotec.samples.server.connection;

import com.neurotec.samples.server.util.PropertyLoader;

public class ServerConnection {
    private final String server;
    private final int clientPort;
    private final int adminPort;

    public static ServerConnection createInstance() {
        PropertyLoader propertyLoader = new PropertyLoader();
        return new ServerConnection(
                propertyLoader.getServerHost(),
                propertyLoader.getClientPort(),
                propertyLoader.getAdminPort()
        );
    }

    public ServerConnection(String url, int clientPort, int adminPort) {
        this.server = url;
        this.clientPort = clientPort;
        this.adminPort = adminPort;
    }

    public final String getServer() {
        return this.server;
    }

    public final int getClientPort() {
        return this.clientPort;
    }

    public final int getAdminPort() {
        return this.adminPort;
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\connection\ServerConnection.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */