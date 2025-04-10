/*     */ package com.neurotec.samples.util;
/*     */ 
/*     */ import com.formdev.flatlaf.FlatLightLaf;
/*     */ import com.formdev.flatlaf.themes.FlatMacLightLaf;
/*     */ import com.neurotec.plugins.NDataFileManager;
/*     */ import com.sun.jna.Platform;
/*     */ import java.awt.Image;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.net.URL;
/*     */ import java.nio.file.Files;
/*     */ import java.nio.file.Path;
/*     */ import java.nio.file.Paths;
/*     */ import java.text.DecimalFormatSymbols;
/*     */ import java.text.NumberFormat;
/*     */ import java.text.ParseException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipInputStream;
/*     */ import javax.swing.Icon;
/*     */ import javax.swing.ImageIcon;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.filechooser.FileFilter;
/*     */ import org.apache.commons.io.Charsets;
/*     */ import org.apache.commons.io.FileUtils;
/*     */ import org.apache.commons.io.FilenameUtils;
/*     */ import org.apache.commons.io.IOUtils;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class Utils
/*     */ {
/*  47 */   public static final String FILE_SEPARATOR = System.getProperty("file.separator");
/*  48 */   public static final String PATH_SEPARATOR = System.getProperty("path.separator");
/*  49 */   public static final String LINE_SEPARATOR = System.getProperty("line.separator");
/*     */   
/*     */   private static final boolean DEFAULT_TRIAL_MODE = false;
/*     */   
/*     */   private static ImageIcon createImageIcon(String path) {
/*  54 */     URL imgURL = Utils.class.getClassLoader().getResource(path);
/*  55 */     if (imgURL == null) {
/*  56 */       System.err.println("Couldn't find file: " + path);
/*  57 */       return null;
/*     */     } 
/*  59 */     return new ImageIcon(imgURL);
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean getTrialModeFlag() throws IOException {
/*  64 */     Path p = Paths.get(".." + FILE_SEPARATOR + "Licenses" + FILE_SEPARATOR, new String[] { "TrialFlag.txt" });
/*  65 */     if (p.toFile().exists()) {
/*  66 */       List<String> str = Files.readAllLines(p);
/*  67 */       if (str.size() > 0 && ((String)str.get(0)).trim().toLowerCase().contentEquals("true")) {
/*  68 */         return true;
/*     */       }
/*     */     } else {
/*  71 */       System.out.println();
/*  72 */       System.out.println("Failed to locate file: " + p.toString());
/*  73 */       System.out.println();
/*     */     } 
/*  75 */     return false;
/*     */   }
/*     */   
/*     */   public static void writeText(String pathname, String text) throws IOException {
/*  79 */     if (text == null) throw new NullPointerException("text"); 
/*  80 */     File file = new File(pathname);
/*  81 */     if (file.isAbsolute() && file.getParentFile() != null) {
/*  82 */       file.getParentFile().mkdirs();
/*  83 */     } else if (!file.exists() || !file.isFile()) {
/*  84 */       throw new IllegalArgumentException("No such file: " + file.getAbsolutePath());
/*     */     } 
/*  86 */     Writer writer = new FileWriter(file);
/*  87 */     Closeable resource = writer;
/*     */     try {
/*  89 */       BufferedWriter bw = new BufferedWriter(writer);
/*  90 */       resource = bw;
/*  91 */       bw.write(text);
/*     */     } finally {
/*  93 */       resource.close();
/*     */     } 
/*     */   }
/*     */   
/*     */   public static String readText(String file) throws IOException {
/*  98 */     Reader reader = new FileReader(file);
/*  99 */     Closeable resource = reader;
/*     */     try {
/* 101 */       BufferedReader br = new BufferedReader(reader);
/* 102 */       resource = br;
/* 103 */       StringBuilder sb = new StringBuilder();
/* 104 */       String line = br.readLine();
/* 105 */       if (line == null) {
/* 106 */         return "";
/*     */       }
/*     */       while (true) {
/* 109 */         sb.append(line);
/* 110 */         line = br.readLine();
/* 111 */         if (line == null) {
/* 112 */           return sb.toString();
/*     */         }
/* 114 */         sb.append(System.getProperty("line.separator"));
/*     */       } 
/*     */     } finally {
/*     */       
/* 118 */       resource.close();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getWorkingDirectory() {
/* 126 */     return System.getProperty("user.dir");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static String getHomeDirectory() {
/* 133 */     return System.getProperty("user.home");
/*     */   }
/*     */   
/*     */   public static String combinePath(String part1, String part2) {
/* 137 */     return String.format("%s%s%s", new Object[] { part1, FILE_SEPARATOR, part2 });
/*     */   }
/*     */   
/*     */   public static Icon createIcon(String path) {
/* 141 */     return createImageIcon(path);
/*     */   }
/*     */   
/*     */   public static Image createIconImage(String path) {
/* 145 */     ImageIcon icon = createImageIcon(path);
/* 146 */     if (icon == null) {
/* 147 */       return null;
/*     */     }
/* 149 */     return icon.getImage();
/*     */   }
/*     */ 
/*     */   
/*     */   public static boolean isNullOrEmpty(String value) {
/* 154 */     return (value == null || "".equals(value));
/*     */   }
/*     */   
/*     */   public static final class TemplateFileFilter
/*     */     extends FileFilter
/*     */   {
/*     */     private final String description;
/*     */     
/*     */     public TemplateFileFilter() {
/* 163 */       this.description = "*.dat; *.data; *.xml";
/*     */     }
/*     */ 
/*     */     
/*     */     public TemplateFileFilter(String description) {
/* 168 */       if (description == null) {
/* 169 */         this.description = "*.dat; *.data; *.xml";
/*     */       } else {
/* 171 */         this.description = description + " (*.dat; *.data; *.xml)";
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean accept(File f) {
/* 177 */       return (f.isDirectory() || f.getName().endsWith(".dat") || f.getName().endsWith(".data") || f.getName().endsWith(".xml"));
/*     */     }
/*     */ 
/*     */     
/*     */     public String getDescription() {
/* 182 */       return this.description;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static final class XMLFileFilter
/*     */     extends FileFilter
/*     */   {
/*     */     private final String description;
/*     */     
/*     */     public XMLFileFilter() {
/* 193 */       this.description = "*.xml";
/*     */     }
/*     */ 
/*     */     
/*     */     public XMLFileFilter(String description) {
/* 198 */       if (description == null) {
/* 199 */         this.description = "*.xml";
/*     */       } else {
/* 201 */         this.description = description + " (*.xml)";
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean accept(File f) {
/* 207 */       return (f.isDirectory() || f.getName().endsWith(".xml"));
/*     */     }
/*     */ 
/*     */     
/*     */     public String getDescription() {
/* 212 */       return this.description;
/*     */     }
/*     */   }
/*     */   
/*     */   public static final class ImageFileFilter
/*     */     extends FileFilter
/*     */   {
/*     */     private final List<String> extensions;
/*     */     private final String description;
/*     */     
/*     */     public ImageFileFilter(String extentionsString) {
/* 223 */       this(extentionsString, null);
/*     */     }
/*     */     
/*     */     public ImageFileFilter(String extentionsString, String description) {
/*     */       StringBuilder sb;
/* 228 */       this.extensions = new ArrayList<>();
/* 229 */       StringTokenizer tokenizer = new StringTokenizer(extentionsString, ";");
/*     */       
/* 231 */       if (description == null) {
/* 232 */         sb = new StringBuilder(64);
/*     */       } else {
/* 234 */         sb = (new StringBuilder(description)).append(" (");
/*     */       } 
/* 236 */       while (tokenizer.hasMoreTokens()) {
/* 237 */         String token = tokenizer.nextToken();
/* 238 */         sb.append(token);
/* 239 */         sb.append(", ");
/* 240 */         this.extensions.add(token.replaceAll("\\*", "").replaceAll("\\.", ""));
/*     */       } 
/* 242 */       sb.delete(sb.length() - 2, sb.length());
/* 243 */       if (description != null) {
/* 244 */         sb.append(')');
/*     */       }
/* 246 */       this.description = sb.toString();
/*     */     }
/*     */ 
/*     */     
/*     */     public boolean accept(File f) {
/* 251 */       for (String extension : this.extensions) {
/* 252 */         if (f.isDirectory() || f.getName().toLowerCase().endsWith(extension.toLowerCase())) {
/* 253 */           return true;
/*     */         }
/*     */       } 
/* 256 */       return false;
/*     */     }
/*     */ 
/*     */     
/*     */     public String getDescription() {
/* 261 */       return this.description;
/*     */     }
/*     */     
/*     */     public List<String> getExtensions() {
/* 265 */       return new ArrayList<>(this.extensions);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static int qualityToPercent(int value) {
/* 271 */     return (2 * value * 100 + 255) / 510;
/*     */   }
/*     */   
/*     */   public static int qualityFromPercent(int value) {
/* 275 */     return (2 * value * 255 + 100) / 200;
/*     */   }
/*     */   
/*     */   public static String matchingThresholdToString(int value) {
/* 279 */     double p = -value / 12.0D;
/* 280 */     NumberFormat nf = NumberFormat.getPercentInstance();
/* 281 */     nf.setMaximumFractionDigits(Math.max(0, (int)Math.ceil(-p) - 2));
/* 282 */     nf.setMinimumIntegerDigits(1);
/* 283 */     return nf.format(Math.pow(10.0D, p));
/*     */   }
/*     */   
/*     */   public static int matchingThresholdFromString(String value) throws ParseException {
/* 287 */     char percent = (new DecimalFormatSymbols()).getPercent();
/* 288 */     value = value.replace(percent, ' ');
/* 289 */     Number number = NumberFormat.getNumberInstance().parse(value);
/* 290 */     double parse = number.doubleValue();
/* 291 */     double p = Math.log10(Math.max(Double.MIN_VALUE, Math.min(1.0D, parse / 100.0D)));
/* 292 */     return Math.max(0, (int)Math.round(-12.0D * p));
/*     */   }
/*     */   
/*     */   public static void initDataFiles(Object obj) throws Exception {
/* 296 */     if (obj == null) throw new NullPointerException("obj"); 
/* 297 */     String outputFolder = combinePath(System.getProperty("java.io.tmpdir"), "data");
/*     */     
/* 299 */     URL srcLocation = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
/* 300 */     ZipInputStream zip = new ZipInputStream(srcLocation.openStream());
/* 301 */     boolean isZip = false;
/*     */     try {
/*     */       while (true) {
/* 304 */         ZipEntry e = zip.getNextEntry();
/* 305 */         if (e == null) {
/*     */           break;
/*     */         }
/* 308 */         isZip = true;
/* 309 */         String name = e.getName();
/* 310 */         if (name.endsWith(".ndf")) {
/* 311 */           InputStream is = obj.getClass().getClassLoader().getResourceAsStream(name);
/* 312 */           FileUtils.copyInputStreamToFile(is, new File(outputFolder, FilenameUtils.getName(name)));
/*     */         } 
/*     */       } 
/*     */     } finally {
/* 316 */       zip.close();
/*     */     } 
/*     */     
/* 319 */     if (!isZip) {
/* 320 */       URL resourceUrl = Utils.class.getClassLoader().getResource("data");
/* 321 */       if (resourceUrl != null) {
/* 322 */         List<String> files = IOUtils.readLines(Utils.class.getClassLoader().getResourceAsStream("data"), Charsets.UTF_8);
/* 323 */         if (files != null) {
/* 324 */           for (String file : files) {
/* 325 */             InputStream is = Utils.class.getClassLoader().getResourceAsStream(combinePath("data", file));
/* 326 */             FileUtils.copyInputStreamToFile(is, new File(outputFolder, file));
/*     */           } 
/*     */         } else {
/* 329 */           throw new IllegalStateException("Data directory is empty");
/*     */         } 
/*     */       } else {
/* 332 */         throw new IllegalStateException("Data directory is not present inside the jar file");
/*     */       } 
/*     */     } 
/*     */     
/* 336 */     NDataFileManager.getInstance().addFromDirectory(outputFolder, true);
/*     */   }
/*     */   
/*     */   public static void setupLookAndFeel() {
/* 340 */     if (Platform.isMac()) {
/* 341 */       FlatMacLightLaf.setup();
/*     */     } else {
/* 343 */       FlatLightLaf.setup();
/*     */     } 
/* 345 */     if (Platform.isLinux()) {
/*     */       
/* 347 */       JFrame.setDefaultLookAndFeelDecorated(true);
/* 348 */       JDialog.setDefaultLookAndFeelDecorated(true);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\sample\\util\Utils.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */