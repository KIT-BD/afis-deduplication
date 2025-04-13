package com.neurotec.samples.util;

import com.neurotec.licensing.NLicense;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class LicenseManager {
    private static final String ADDRESS = "/local";
    private static final String PORT = "5000";
    public static final String PROGRESS_CHANGED_PROPERTY = "progress";
    public static final String LAST_STATUS_MESSAGE_PROPERTY = "last-status-message";
    private static LicenseManager instance;
    private final PropertyChangeSupport propertyChangeSupport;
    private final Set<String> obtainedLicenses;
    private final Map<String, Boolean> licenseCache;
    private int progress;
    private String lastStatusMessage;
    private boolean debug = true;

    private LicenseManager() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.obtainedLicenses = new HashSet<>();
        this.licenseCache = new HashMap<>();
        this.lastStatusMessage = "";
    }


    public static LicenseManager getInstance() {
        synchronized (LicenseManager.class) {
            if (instance == null) {
                instance = new LicenseManager();
            }
            return instance;
        }
    }


    private void setProgress(int progress) {
        int oldProgress = getProgress();
        this.progress = progress;
        this.propertyChangeSupport.firePropertyChange("progress", oldProgress, progress);
    }


    public boolean isActivated(String component) {
        return isActivated(component, false);
    }

    public boolean isActivated(String component, boolean cache) {
        boolean result;
        if (component == null) {
            throw new NullPointerException("component");
        }
        if (cache &&
                this.licenseCache.containsKey(component)) {
            return ((Boolean) this.licenseCache.get(component)).booleanValue();
        }


        try {
            result = NLicense.isComponentActivated(component);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (cache) {
            this.licenseCache.put(component, Boolean.valueOf(result));
        }
        return result;
    }

    public synchronized boolean obtainComponents(Collection<String> licenses) throws IOException {
        return obtainComponents(licenses, "/local", "5000");
    }

    public synchronized boolean obtainComponents(Collection<String> licenses, String address, String port) throws IOException {
        String oldStatus = this.lastStatusMessage;
        this.lastStatusMessage = String.format("Obtaining licenses from server %s:%s\n", new Object[]{address, port});
        this.propertyChangeSupport.firePropertyChange("last-status-message", oldStatus, this.lastStatusMessage);
        if (this.debug) {
            System.out.print(this.lastStatusMessage);
        }
        int i = 0;
        setProgress(i);
        boolean result = false;
        try {
            for (String license : licenses) {
                boolean obtained = false;
                try {
                    obtained = NLicense.obtainComponents(address, port, license);
                    if (obtained) {
                        this.obtainedLicenses.add(license);
                    }
                    result |= obtained;
                } finally {
                    oldStatus = this.lastStatusMessage;
                    this.lastStatusMessage = license + ": " + (obtained ? "obtained" : "not obtained") + "\n";
                    this.propertyChangeSupport.firePropertyChange("last-status-message", oldStatus, this.lastStatusMessage);
                    if (this.debug) {
                        System.out.print(this.lastStatusMessage);
                    }
                }
                setProgress(++i);
            }
        } finally {
            setProgress(100);
        }
        return result;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isProgress() {
        return (this.progress != 0 && this.progress != 100);
    }

    public int getProgress() {
        return this.progress;
    }

    public int getLicenseCount() {
        return this.obtainedLicenses.size();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
