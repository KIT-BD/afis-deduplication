/*     */ package com.neurotec.samples.swing;
/*     */ 
/*     */ import com.formdev.flatlaf.themes.FlatMacLightLaf;
/*     */ import com.sun.jna.Platform;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Graphics2D;
/*     */ import java.awt.Image;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.image.ImageObserver;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Map;
/*     */ import java.util.WeakHashMap;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.regex.Pattern;
/*     */ import javax.imageio.ImageIO;
/*     */ import javax.swing.Icon;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.LookAndFeel;
/*     */ import javax.swing.SwingUtilities;
/*     */ import javax.swing.UIManager;
/*     */ import javax.swing.UnsupportedLookAndFeelException;
/*     */ import javax.swing.filechooser.FileSystemView;
/*     */ import javax.swing.filechooser.FileView;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class ImageThumbnailFileChooser
/*     */   extends JFileChooser
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private static final int ICON_SIZE = 16;
/*  40 */   private static final Image LOADING_IMAGE = new BufferedImage(16, 16, 2);
/*     */ 
/*     */ 
/*     */   
/*     */   private Pattern imageFilePattern;
/*     */ 
/*     */ 
/*     */   
/*     */   private Map<File, ImageIcon> imageCache;
/*     */ 
/*     */   
/*     */   private Image iconImage;
/*     */ 
/*     */ 
/*     */   
/*     */   public ImageThumbnailFileChooser() {
/*  56 */     init();
/*     */   }
/*     */   
/*     */   public ImageThumbnailFileChooser(String currentDirectoryPath) {
/*  60 */     super(currentDirectoryPath);
/*  61 */     init();
/*     */   }
/*     */   
/*     */   public ImageThumbnailFileChooser(File currentDirectory) {
/*  65 */     super(currentDirectory);
/*  66 */     init();
/*     */   }
/*     */   
/*     */   public ImageThumbnailFileChooser(FileSystemView fsv) {
/*  70 */     super(fsv);
/*  71 */     init();
/*     */   }
/*     */   
/*     */   public ImageThumbnailFileChooser(File currentDirectory, FileSystemView fsv) {
/*  75 */     super(currentDirectory, fsv);
/*  76 */     init();
/*     */   }
/*     */   
/*     */   public ImageThumbnailFileChooser(String currentDirectoryPath, FileSystemView fsv) {
/*  80 */     super(currentDirectoryPath, fsv);
/*  81 */     init();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void init() {
/*  89 */     this.imageFilePattern = Pattern.compile(".+?\\.(png|jpe?g|gif|tiff?)$", 2);
/*  90 */     this.imageCache = new WeakHashMap<>();
/*  91 */     setFileView(new ThumbnailView());
/*  92 */     setPreferredSize(new Dimension(1100, 600));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected JDialog createDialog(Component parent) {
/* 101 */     JDialog dialog = super.createDialog(parent);
/* 102 */     if (this.iconImage != null) {
/* 103 */       dialog.setIconImage(this.iconImage);
/*     */     }
/* 105 */     return dialog;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setIcon(Image image) {
/* 113 */     this.iconImage = image;
/*     */   }
/*     */ 
/*     */   
/*     */   public void updateUI() {
/* 118 */     if (Platform.isMac()) {
/* 119 */       LookAndFeel old = UIManager.getLookAndFeel();
/*     */       try {
/* 121 */         FlatMacLightLaf.setup();
/* 122 */       } catch (Exception e) {
/* 123 */         old = null;
/*     */       } 
/*     */       
/* 126 */       super.updateUI();
/*     */       
/* 128 */       if (old != null) {
/*     */         try {
/* 130 */           UIManager.setLookAndFeel(old);
/* 131 */         } catch (UnsupportedLookAndFeelException e) {
/* 132 */           throw new AssertionError("Can't happen");
/*     */         } 
/*     */       }
/*     */     } else {
/* 136 */       super.updateUI();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private class ThumbnailView
/*     */     extends FileView
/*     */   {
/* 146 */     private final ExecutorService executor = Executors.newCachedThreadPool();
/*     */ 
/*     */     
/*     */     public Icon getIcon(File file) {
/* 150 */       if (!ImageThumbnailFileChooser.this.imageFilePattern.matcher(file.getName()).matches()) {
/* 151 */         return null;
/*     */       }
/* 153 */       synchronized (ImageThumbnailFileChooser.this.imageCache) {
/* 154 */         ImageIcon icon = (ImageIcon)ImageThumbnailFileChooser.this.imageCache.get(file);
/* 155 */         if (icon == null) {
/* 156 */           icon = new ImageIcon(ImageThumbnailFileChooser.LOADING_IMAGE);
/* 157 */           ImageThumbnailFileChooser.this.imageCache.put(file, icon);
/* 158 */           this.executor.submit(new ImageThumbnailFileChooser.ThumbnailIconLoader(icon, file));
/*     */         } 
/* 160 */         return icon;
/*     */       } 
/*     */     }
/*     */     
/*     */     private ThumbnailView() {}
/*     */   }
/*     */   
/*     */   private class ThumbnailIconLoader implements Runnable {
/*     */     private final ImageIcon icon;
/*     */     private final File file;
/*     */     
/*     */     ThumbnailIconLoader(ImageIcon i, File f) {
/* 172 */       this.icon = i;
/* 173 */       this.file = f;
/*     */     }
/*     */     
/*     */     private BufferedImage getDefaultIconImage(File f) {
/* 177 */       Icon defaultIcon = FileSystemView.getFileSystemView().getSystemIcon(f);
/* 178 */       BufferedImage image = new BufferedImage(defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), 2);
/* 179 */       defaultIcon.paintIcon(null, image.getGraphics(), 0, 0);
/* 180 */       return image;
/*     */     }
/*     */     public void run() {
/*     */       try {
/*     */         int x, y;
/*     */         Image scaledImg;
/* 186 */         BufferedImage img = ImageIO.read(this.file);
/* 187 */         if (img == null) {
/* 188 */           img = getDefaultIconImage(this.file);
/*     */         }
/*     */ 
/*     */ 
/*     */         
/* 193 */         if (img.getHeight() >= img.getWidth()) {
/* 194 */           scaledImg = img.getScaledInstance(-1, 16, 4);
/* 195 */           x = (int)Math.round((16 - scaledImg.getWidth(null)) / 2.0D);
/* 196 */           y = 0;
/*     */         } else {
/* 198 */           scaledImg = img.getScaledInstance(16, -1, 4);
/* 199 */           x = 0;
/* 200 */           y = (int)Math.round((16 - scaledImg.getHeight(null)) / 2.0D);
/*     */         } 
/*     */         
/* 203 */         BufferedImage imgPadding = new BufferedImage(16, 16, 2);
/* 204 */         Graphics2D g2d = imgPadding.createGraphics();
/* 205 */         g2d.drawImage(scaledImg, x, y, (ImageObserver)null);
/* 206 */         g2d.dispose();
/* 207 */         this.icon.setImage(imgPadding);
/*     */         
/* 209 */         SwingUtilities.invokeLater(new Runnable()
/*     */             {
/*     */               public void run() {
/* 212 */                 ImageThumbnailFileChooser.this.repaint();
/*     */               }
/*     */             });
/* 215 */       } catch (RuntimeException e) {
/* 216 */         e.printStackTrace();
/* 217 */         throw e;
/* 218 */       } catch (IOException e) {
/* 219 */         e.printStackTrace();
/*     */       } 
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\swing\ImageThumbnailFileChooser.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */