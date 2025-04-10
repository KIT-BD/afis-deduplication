/*    */ package com.neurotec.samples.server.util;
/*    */ 
/*    */ 
/*    */ public final class BiometricUtils
/*    */ {
/*    */   public static String matchingThresholdToString(int value) {
/*  7 */     double p = -value / 12.0D + 2.0D;
/*  8 */     int decimals = (int)Math.max(0.0D, Math.ceil(-p));
/*  9 */     return String.format("%." + decimals + "f %%", new Object[] { Double.valueOf(Math.pow(10.0D, p)) });
/*    */   }
/*    */   
/*    */   public static int matchingThresholdFromString(String value) {
/* 13 */     double p = Math.log10(Math.max(0.0D, Math.min(1.0D, Double.parseDouble(value.replace("%", "")) / 100.0D)));
/* 14 */     return Math.max(0, (int)Math.round(-12.0D * p));
/*    */   }
/*    */   
/*    */   public static int maximalRotationToDegrees(int value) {
/* 18 */     return (2 * value * 360 + 256) / 512;
/*    */   }
/*    */   
/*    */   public static int maximalRotationFromDegrees(int value) {
/* 22 */     return (2 * value * 256 + 360) / 720;
/*    */   }
/*    */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\serve\\util\BiometricUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */