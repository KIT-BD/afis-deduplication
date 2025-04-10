/*     */ package com.neurotec.samples.util;
/*     */ 
/*     */ import com.neurotec.licensing.NLicense;
/*     */ import java.beans.PropertyChangeListener;
/*     */ import java.beans.PropertyChangeSupport;
/*     */ import java.io.IOException;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class LicenseManager
/*     */ {
/*     */   private static final String ADDRESS = "/local";
/*     */   private static final String PORT = "5000";
/*     */   public static final String PROGRESS_CHANGED_PROPERTY = "progress";
/*     */   public static final String LAST_STATUS_MESSAGE_PROPERTY = "last-status-message";
/*     */   private static LicenseManager instance;
/*     */   private final PropertyChangeSupport propertyChangeSupport;
/*     */   private final Set<String> obtainedLicenses;
/*     */   private final Map<String, Boolean> licenseCache;
/*     */   private int progress;
/*     */   private String lastStatusMessage;
/*     */   private boolean debug = true;
/*     */   
/*     */   private LicenseManager() {
/*  52 */     this.propertyChangeSupport = new PropertyChangeSupport(this);
/*  53 */     this.obtainedLicenses = new HashSet<>();
/*  54 */     this.licenseCache = new HashMap<>();
/*  55 */     this.lastStatusMessage = "";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static LicenseManager getInstance() {
/*  63 */     synchronized (LicenseManager.class) {
/*  64 */       if (instance == null) {
/*  65 */         instance = new LicenseManager();
/*     */       }
/*  67 */       return instance;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void setProgress(int progress) {
/*  76 */     int oldProgress = getProgress();
/*  77 */     this.progress = progress;
/*  78 */     this.propertyChangeSupport.firePropertyChange("progress", oldProgress, progress);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isActivated(String component) {
/*  86 */     return isActivated(component, false);
/*     */   }
/*     */   public boolean isActivated(String component, boolean cache) {
/*     */     boolean result;
/*  90 */     if (component == null) {
/*  91 */       throw new NullPointerException("component");
/*     */     }
/*  93 */     if (cache && 
/*  94 */       this.licenseCache.containsKey(component)) {
/*  95 */       return ((Boolean)this.licenseCache.get(component)).booleanValue();
/*     */     }
/*     */ 
/*     */     
/*     */     try {
/* 100 */       result = NLicense.isComponentActivated(component);
/* 101 */     } catch (IOException e) {
/* 102 */       e.printStackTrace();
/* 103 */       return false;
/*     */     } 
/* 105 */     if (cache) {
/* 106 */       this.licenseCache.put(component, Boolean.valueOf(result));
/*     */     }
/* 108 */     return result;
/*     */   }
/*     */   
/*     */   public synchronized boolean obtainComponents(Collection<String> licenses) throws IOException {
/* 112 */     return obtainComponents(licenses, "/local", "5000");
/*     */   }
/*     */   
/*     */   public synchronized boolean obtainComponents(Collection<String> licenses, String address, String port) throws IOException {
/* 116 */     String oldStatus = this.lastStatusMessage;
/* 117 */     this.lastStatusMessage = String.format("Obtaining licenses from server %s:%s\n", new Object[] { address, port });
/* 118 */     this.propertyChangeSupport.firePropertyChange("last-status-message", oldStatus, this.lastStatusMessage);
/* 119 */     if (this.debug) {
/* 120 */       System.out.print(this.lastStatusMessage);
/*     */     }
/* 122 */     int i = 0;
/* 123 */     setProgress(i);
/* 124 */     boolean result = false;
/*     */     try {
/* 126 */       for (String license : licenses) {
/* 127 */         boolean obtained = false;
/*     */         try {
/* 129 */           obtained = NLicense.obtainComponents(address, port, license);
/* 130 */           if (obtained) {
/* 131 */             this.obtainedLicenses.add(license);
/*     */           }
/* 133 */           result |= obtained;
/*     */         } finally {
/* 135 */           oldStatus = this.lastStatusMessage;
/* 136 */           this.lastStatusMessage = license + ": " + (obtained ? "obtained" : "not obtained") + "\n";
/* 137 */           this.propertyChangeSupport.firePropertyChange("last-status-message", oldStatus, this.lastStatusMessage);
/* 138 */           if (this.debug) {
/* 139 */             System.out.print(this.lastStatusMessage);
/*     */           }
/*     */         } 
/* 142 */         setProgress(++i);
/*     */       } 
/*     */     } finally {
/* 145 */       setProgress(100);
/*     */     } 
/* 147 */     return result;
/*     */   }
/*     */   
/*     */   public boolean isDebug() {
/* 151 */     return this.debug;
/*     */   }
/*     */   
/*     */   public void setDebug(boolean debug) {
/* 155 */     this.debug = debug;
/*     */   }
/*     */   
/*     */   public boolean isProgress() {
/* 159 */     return (this.progress != 0 && this.progress != 100);
/*     */   }
/*     */   
/*     */   public int getProgress() {
/* 163 */     return this.progress;
/*     */   }
/*     */   
/*     */   public int getLicenseCount() {
/* 167 */     return this.obtainedLicenses.size();
/*     */   }
/*     */   
/*     */   public void addPropertyChangeListener(PropertyChangeListener listener) {
/* 171 */     this.propertyChangeSupport.addPropertyChangeListener(listener);
/*     */   }
/*     */   
/*     */   public void removePropertyChangeListener(PropertyChangeListener listener) {
/* 175 */     this.propertyChangeSupport.removePropertyChangeListener(listener);
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\sample\\util\LicenseManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */