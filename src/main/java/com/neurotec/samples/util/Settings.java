/*     */ package com.neurotec.samples.util;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.SwingUtilities;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class Settings
/*     */ {
/*     */   private static Settings defaultInstance;
/*  21 */   private static final String HOME_PATH = System.getProperty("user.home");
/*  22 */   private static final String APP_DATA_PATH = HOME_PATH + System.getProperty("file.separator") + "AppData";
/*  23 */   private static final String APP_DATA_LOCAL_PATH = APP_DATA_PATH + System.getProperty("file.separator") + "Local";
/*  24 */   private static final String NEURO_DIRECTORY_PATH = APP_DATA_LOCAL_PATH + System.getProperty("file.separator") + "Neurotechnology";
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final String propertiesPath;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static Settings getDefault(String sampleName) {
/*  37 */     synchronized (Settings.class) {
/*  38 */       if (defaultInstance == null) {
/*  39 */         defaultInstance = new Settings(sampleName);
/*     */       }
/*  41 */       return defaultInstance;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  50 */   private final Properties properties = new Properties();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Settings(String sampleName) {
/*  57 */     String sampleDirectoryPath = NEURO_DIRECTORY_PATH + System.getProperty("file.separator") + sampleName;
/*  58 */     this.propertiesPath = sampleDirectoryPath + System.getProperty("file.separator") + "user.properties";
/*     */     
/*  60 */     File sampleDirectory = new File(sampleDirectoryPath);
/*  61 */     if (!sampleDirectory.exists() || !sampleDirectory.isDirectory()) {
/*  62 */       sampleDirectory.mkdirs();
/*     */     }
/*  64 */     File propertiesFile = new File(this.propertiesPath);
/*  65 */     if (propertiesFile.exists()) {
/*  66 */       InputStream is = null;
/*     */       try {
/*  68 */         is = new FileInputStream(propertiesFile);
/*  69 */         this.properties.load(is);
/*  70 */       } catch (IOException e) {
/*  71 */         e.printStackTrace();
/*  72 */         loadDefaultSettings();
/*     */       } finally {
/*  74 */         if (is != null) {
/*     */           try {
/*  76 */             is.close();
/*  77 */           } catch (IOException e) {
/*  78 */             e.printStackTrace();
/*     */           } 
/*     */         }
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void loadDefaultSettings() {
/*  90 */     this.properties.put("lastDirectory", "");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getLastDirectory() {
/*  98 */     return this.properties.getProperty("lastDirectory");
/*     */   }
/*     */   
/*     */   public void setLastDirectory(String value) {
/* 102 */     this.properties.setProperty("lastDirectory", value);
/*     */   }
/*     */   
/*     */   public void save() {
/* 106 */     File propertiesFile = new File(this.propertiesPath);
/* 107 */     OutputStream os = null;
/*     */     try {
/* 109 */       os = new FileOutputStream(propertiesFile);
/* 110 */       this.properties.store(os, (String)null);
/* 111 */     } catch (IOException e) {
/* 112 */       e.printStackTrace();
/* 113 */       SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.toString()));
/*     */     } finally {
/*     */       try {
/* 116 */         if (os != null) {
/* 117 */           os.close();
/*     */         }
/* 119 */       } catch (IOException e) {
/* 120 */         e.printStackTrace();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\sample\\util\Settings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */