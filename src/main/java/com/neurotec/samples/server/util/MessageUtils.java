/*    */ package com.neurotec.samples.server.util;
/*    */ 
/*    */ import java.awt.Container;
/*    */ import javax.swing.JOptionPane;
/*    */ import javax.swing.SwingUtilities;
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
/*    */ public final class MessageUtils
/*    */ {
/*    */   private static final String ERROR_TITLE = "Error";
/*    */   private static final String INFORMATION_TITLE = "Information";
/*    */   private static final String QUESTION_TITLE = "Question";
/*    */   
/*    */   public static String getCurrentApplicationName() {
/* 23 */     return "ServerSampleJava";
/*    */   }
/*    */   
/*    */   public static void showError(Container owner, String message) {
/* 27 */     SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(owner, message, String.format("%s: %s", new Object[] { getCurrentApplicationName(), "Error" }), 0));
/*    */   }
/*    */   
/*    */   public static void showError(Container owner, Exception exception) {
/* 31 */     if (exception == null) throw new NullPointerException("exception"); 
/* 32 */     exception.printStackTrace();
/* 33 */     showError(owner, exception.toString());
/*    */   }
/*    */   
/*    */   public static void showError(Container owner, String format, Object[] args) {
/* 37 */     String str = String.format(format, args);
/* 38 */     showError(owner, str);
/*    */   }
/*    */   
/*    */   public static void showInformation(Container owner, String message) {
/* 42 */     SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(owner, message, String.format("%s: %s", new Object[] { getCurrentApplicationName(), "Information" }), 1));
/*    */   }
/*    */   
/*    */   public static void showInformation(Container owner, String format, Object[] args) {
/* 46 */     String str = String.format(format, args);
/* 47 */     showInformation(owner, str);
/*    */   }
/*    */   
/*    */   public static boolean showQuestion(Container owner, String message) {
/* 51 */     return (0 == JOptionPane.showConfirmDialog(owner, message, String.format("%s: %s", new Object[] { getCurrentApplicationName(), "Question" }), 0));
/*    */   }
/*    */ 
/*    */   
/*    */   public static boolean showQuestion(Container owner, String format, Object[] args) {
/* 56 */     String str = String.format(format, args);
/* 57 */     return showQuestion(owner, str);
/*    */   }
/*    */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\serve\\util\MessageUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */