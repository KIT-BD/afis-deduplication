/*    */ package com.neurotec.samples.server.util;
/*    */ 
/*    */ import java.awt.GridBagConstraints;
/*    */ import java.awt.Insets;
/*    */ import javax.swing.JComponent;
/*    */ import javax.swing.JPanel;
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
/*    */ public final class GridBagUtils
/*    */ {
/*    */   private GridBagConstraints gridBagConstraints;
/*    */   
/*    */   public GridBagUtils(int fill) {
/* 22 */     this.gridBagConstraints = new GridBagConstraints();
/* 23 */     this.gridBagConstraints.fill = fill;
/*    */   }
/*    */   
/*    */   public GridBagUtils(int fill, Insets insets) {
/* 27 */     this.gridBagConstraints = new GridBagConstraints();
/* 28 */     this.gridBagConstraints.fill = fill;
/* 29 */     this.gridBagConstraints.insets = insets;
/*    */   }
/*    */   
/*    */   public void setInsets(Insets insets) {
/* 33 */     this.gridBagConstraints.insets = insets;
/*    */   }
/*    */   
/*    */   public void addToGridBagLayout(int x, int y, JPanel parent, JComponent component) {
/* 37 */     this.gridBagConstraints.gridx = x;
/* 38 */     this.gridBagConstraints.gridy = y;
/* 39 */     parent.add(component, this.gridBagConstraints);
/*    */   }
/*    */   
/*    */   public void addToGridBagLayout(int x, int y, int width, int height, JPanel parent, JComponent component) {
/* 43 */     this.gridBagConstraints.gridx = x;
/* 44 */     this.gridBagConstraints.gridy = y;
/* 45 */     this.gridBagConstraints.gridwidth = width;
/* 46 */     this.gridBagConstraints.gridheight = height;
/* 47 */     parent.add(component, this.gridBagConstraints);
/*    */   }
/*    */   
/*    */   public void addToGridBagLayout(int x, int y, int width, int height, int weightX, int weightY, JPanel parent, JComponent component) {
/* 51 */     this.gridBagConstraints.gridx = x;
/* 52 */     this.gridBagConstraints.gridy = y;
/* 53 */     this.gridBagConstraints.gridwidth = width;
/* 54 */     this.gridBagConstraints.gridheight = height;
/* 55 */     this.gridBagConstraints.weightx = weightX;
/* 56 */     this.gridBagConstraints.weighty = weightY;
/* 57 */     parent.add(component, this.gridBagConstraints);
/*    */   }
/*    */   
/*    */   public void clearGridBagConstraints() {
/* 61 */     this.gridBagConstraints.gridwidth = 1;
/* 62 */     this.gridBagConstraints.gridheight = 1;
/* 63 */     this.gridBagConstraints.weightx = 0.0D;
/* 64 */     this.gridBagConstraints.weighty = 0.0D;
/*    */   }
/*    */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\serve\\util\GridBagUtils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */