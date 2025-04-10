/*    */ package com.neurotec.samples.util;
/*    */ 
/*    */ import com.sun.jna.Platform;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public final class LibraryManager
/*    */ {
/*    */   private static final String WIN32_X86 = "Win32_x86";
/*    */   private static final String WIN64_X64 = "Win64_x64";
/*    */   private static final String LINUX_X86 = "Linux_x86";
/*    */   private static final String LINUX_X86_64 = "Linux_x86_64";
/*    */   
/*    */   public static void initLibraryPath() {
/* 23 */     String libraryPath = getLibraryPath();
/* 24 */     String jnaLibraryPath = System.getProperty("jna.library.path");
/* 25 */     if (Utils.isNullOrEmpty(jnaLibraryPath)) {
/* 26 */       System.out.println(">>>> JNA LIbrary Path : " + libraryPath.toString());
/* 27 */       System.setProperty("jna.library.path", libraryPath.toString());
/*    */     } else {
/*    */       
/* 30 */       System.setProperty("jna.library.path", String.format("%s%s%s", new Object[] { jnaLibraryPath, Utils.PATH_SEPARATOR, libraryPath.toString() }));
/*    */     } 
/* 32 */     System.setProperty("java.library.path", String.format("%s%s%s", new Object[] { System.getProperty("java.library.path"), Utils.PATH_SEPARATOR, libraryPath.toString() }));
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public static String getLibraryPath() {
/* 46 */     StringBuilder path = new StringBuilder();
/* 47 */     int index = Utils.getWorkingDirectory().lastIndexOf(Utils.FILE_SEPARATOR);
/* 48 */     if (index == -1) {
/* 49 */       return null;
/*    */     }
/* 51 */     String part = Utils.getWorkingDirectory().substring(0, index);
/*    */     
/* 53 */     if (Platform.isWindows()) {
/* 54 */       if (part.endsWith("Bin")) {
/* 55 */         path.append(part);
/* 56 */         path.append(Utils.FILE_SEPARATOR);
/* 57 */         path.append(Platform.is64Bit() ? "Win64_x64" : "Win32_x86");
/*    */       } 
/* 59 */     } else if (Platform.isLinux()) {
/* 60 */       index = part.lastIndexOf(Utils.FILE_SEPARATOR);
/* 61 */       if (index == -1) {
/* 62 */         return null;
/*    */       }
/* 64 */       part = part.substring(0, index);
/* 65 */       path.append(part);
/* 66 */       path.append(Utils.FILE_SEPARATOR);
/* 67 */       path.append("Lib");
/* 68 */       path.append(Utils.FILE_SEPARATOR);
/* 69 */       path.append(Platform.is64Bit() ? "Linux_x86_64" : "Linux_x86");
/* 70 */     } else if (Platform.isMac()) {
/* 71 */       index = part.lastIndexOf(Utils.FILE_SEPARATOR);
/* 72 */       if (index == -1) {
/* 73 */         return null;
/*    */       }
/* 75 */       part = part.substring(0, index);
/* 76 */       path.append(part);
/* 77 */       path.append(Utils.FILE_SEPARATOR);
/* 78 */       path.append("Frameworks");
/* 79 */       path.append(Utils.FILE_SEPARATOR);
/* 80 */       path.append("MacOSX");
/*    */     } 
/* 82 */     return path.toString();
/*    */   }
/*    */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\sample\\util\LibraryManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */