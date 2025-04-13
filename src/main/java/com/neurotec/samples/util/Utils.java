package com.neurotec.samples.util;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.neurotec.plugins.NDataFileManager;
import com.sun.jna.Platform;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;


public final class Utils {
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final boolean DEFAULT_TRIAL_MODE = false;

    private static ImageIcon createImageIcon(String path) {
        URL imgURL = Utils.class.getClassLoader().getResource(path);
        if (imgURL == null) {
//            System.err.println("Couldn't find file: " + path);
            return null;
        }
        return new ImageIcon(imgURL);
    }


    public static boolean getTrialModeFlag() throws IOException {
        Path p = Paths.get(".." + FILE_SEPARATOR + "Licenses" + FILE_SEPARATOR, new String[]{"TrialFlag.txt"});
        if (p.toFile().exists()) {
            List<String> str = Files.readAllLines(p);
            if (str.size() > 0 && ((String) str.get(0)).trim().toLowerCase().contentEquals("true")) {
                return true;
            }
        } else {
            System.out.println();
            System.out.println("Failed to locate file: " + p.toString());
            System.out.println();
        }
        return false;
    }

    public static void writeText(String pathname, String text) throws IOException {
        if (text == null) throw new NullPointerException("text");
        File file = new File(pathname);
        if (file.isAbsolute() && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        } else if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("No such file: " + file.getAbsolutePath());
        }
        Writer writer = new FileWriter(file);
        Closeable resource = writer;
        try {
            BufferedWriter bw = new BufferedWriter(writer);
            resource = bw;
            bw.write(text);
        } finally {
            resource.close();
        }
    }

    public static String readText(String file) throws IOException {
        Reader reader = new FileReader(file);
        Closeable resource = reader;
        try {
            BufferedReader br = new BufferedReader(reader);
            resource = br;
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            if (line == null) {
                return "";
            }
            while (true) {
                sb.append(line);
                line = br.readLine();
                if (line == null) {
                    return sb.toString();
                }
                sb.append(System.getProperty("line.separator"));
            }
        } finally {

            resource.close();
        }
    }


    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }


    public static String getHomeDirectory() {
        return System.getProperty("user.home");
    }

    public static String combinePath(String part1, String part2) {
        return String.format("%s%s%s", new Object[]{part1, FILE_SEPARATOR, part2});
    }

    public static Icon createIcon(String path) {
        return createImageIcon(path);
    }

    public static Image createIconImage(String path) {
        ImageIcon icon = createImageIcon(path);
        if (icon == null) {
            return null;
        }
        return icon.getImage();
    }


    public static boolean isNullOrEmpty(String value) {
        return (value == null || "".equals(value));
    }

    public static final class TemplateFileFilter
            extends FileFilter {
        private final String description;

        public TemplateFileFilter() {
            this.description = "*.dat; *.data; *.xml";
        }


        public TemplateFileFilter(String description) {
            if (description == null) {
                this.description = "*.dat; *.data; *.xml";
            } else {
                this.description = description + " (*.dat; *.data; *.xml)";
            }
        }


        public boolean accept(File f) {
            return (f.isDirectory() || f.getName().endsWith(".dat") || f.getName().endsWith(".data") || f.getName().endsWith(".xml"));
        }


        public String getDescription() {
            return this.description;
        }
    }


    public static final class XMLFileFilter
            extends FileFilter {
        private final String description;

        public XMLFileFilter() {
            this.description = "*.xml";
        }


        public XMLFileFilter(String description) {
            if (description == null) {
                this.description = "*.xml";
            } else {
                this.description = description + " (*.xml)";
            }
        }


        public boolean accept(File f) {
            return (f.isDirectory() || f.getName().endsWith(".xml"));
        }


        public String getDescription() {
            return this.description;
        }
    }

    public static final class ImageFileFilter
            extends FileFilter {
        private final List<String> extensions;
        private final String description;

        public ImageFileFilter(String extentionsString) {
            this(extentionsString, null);
        }

        public ImageFileFilter(String extentionsString, String description) {
            StringBuilder sb;
            this.extensions = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(extentionsString, ";");

            if (description == null) {
                sb = new StringBuilder(64);
            } else {
                sb = (new StringBuilder(description)).append(" (");
            }
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                sb.append(token);
                sb.append(", ");
                this.extensions.add(token.replaceAll("\\*", "").replaceAll("\\.", ""));
            }
            sb.delete(sb.length() - 2, sb.length());
            if (description != null) {
                sb.append(')');
            }
            this.description = sb.toString();
        }


        public boolean accept(File f) {
            for (String extension : this.extensions) {
                if (f.isDirectory() || f.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }


        public String getDescription() {
            return this.description;
        }

        public List<String> getExtensions() {
            return new ArrayList<>(this.extensions);
        }
    }


    public static int qualityToPercent(int value) {
        return (2 * value * 100 + 255) / 510;
    }

    public static int qualityFromPercent(int value) {
        return (2 * value * 255 + 100) / 200;
    }

    public static String matchingThresholdToString(int value) {
        double p = -value / 12.0D;
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(Math.max(0, (int) Math.ceil(-p) - 2));
        nf.setMinimumIntegerDigits(1);
        return nf.format(Math.pow(10.0D, p));
    }

    public static int matchingThresholdFromString(String value) throws ParseException {
        char percent = (new DecimalFormatSymbols()).getPercent();
        value = value.replace(percent, ' ');
        Number number = NumberFormat.getNumberInstance().parse(value);
        double parse = number.doubleValue();
        double p = Math.log10(Math.max(Double.MIN_VALUE, Math.min(1.0D, parse / 100.0D)));
        return Math.max(0, (int) Math.round(-12.0D * p));
    }

    public static void initDataFiles(Object obj) throws Exception {
        if (obj == null) throw new NullPointerException("obj");
        String outputFolder = combinePath(System.getProperty("java.io.tmpdir"), "data");

        URL srcLocation = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
        ZipInputStream zip = new ZipInputStream(srcLocation.openStream());
        boolean isZip = false;
        try {
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) {
                    break;
                }
                isZip = true;
                String name = e.getName();
                if (name.endsWith(".ndf")) {
                    InputStream is = obj.getClass().getClassLoader().getResourceAsStream(name);
                    FileUtils.copyInputStreamToFile(is, new File(outputFolder, FilenameUtils.getName(name)));
                }
            }
        } finally {
            zip.close();
        }

        if (!isZip) {
            URL resourceUrl = Utils.class.getClassLoader().getResource("data");
            if (resourceUrl != null) {
                List<String> files = IOUtils.readLines(Utils.class.getClassLoader().getResourceAsStream("data"), Charsets.UTF_8);
                if (files != null) {
                    for (String file : files) {
                        InputStream is = Utils.class.getClassLoader().getResourceAsStream(combinePath("data", file));
                        FileUtils.copyInputStreamToFile(is, new File(outputFolder, file));
                    }
                } else {
                    throw new IllegalStateException("Data directory is empty");
                }
            } else {
                throw new IllegalStateException("Data directory is not present inside the jar file");
            }
        }

        NDataFileManager.getInstance().addFromDirectory(outputFolder, true);
    }

    public static void setupLookAndFeel() {
        if (Platform.isMac()) {
            FlatMacLightLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        if (Platform.isLinux()) {

            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
    }
}
