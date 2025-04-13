package com.neurotec.samples.swing;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.sun.jna.Platform;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;


public final class ImageThumbnailFileChooser
        extends JFileChooser {
    private static final long serialVersionUID = 1L;
    private static final int ICON_SIZE = 16;
    private static final Image LOADING_IMAGE = new BufferedImage(16, 16, 2);


    private Pattern imageFilePattern;


    private Map<File, ImageIcon> imageCache;


    private Image iconImage;


    public ImageThumbnailFileChooser() {
        init();
    }

    public ImageThumbnailFileChooser(String currentDirectoryPath) {
        super(currentDirectoryPath);
        init();
    }

    public ImageThumbnailFileChooser(File currentDirectory) {
        super(currentDirectory);
        init();
    }

    public ImageThumbnailFileChooser(FileSystemView fsv) {
        super(fsv);
        init();
    }

    public ImageThumbnailFileChooser(File currentDirectory, FileSystemView fsv) {
        super(currentDirectory, fsv);
        init();
    }

    public ImageThumbnailFileChooser(String currentDirectoryPath, FileSystemView fsv) {
        super(currentDirectoryPath, fsv);
        init();
    }


    private void init() {
        this.imageFilePattern = Pattern.compile(".+?\\.(png|jpe?g|gif|tiff?)$", 2);
        this.imageCache = new WeakHashMap<>();
        setFileView(new ThumbnailView());
        setPreferredSize(new Dimension(1100, 600));
    }


    protected JDialog createDialog(Component parent) {
        JDialog dialog = super.createDialog(parent);
        if (this.iconImage != null) {
            dialog.setIconImage(this.iconImage);
        }
        return dialog;
    }


    public void setIcon(Image image) {
        this.iconImage = image;
    }


    public void updateUI() {
        if (Platform.isMac()) {
            LookAndFeel old = UIManager.getLookAndFeel();
            try {
                FlatMacLightLaf.setup();
            } catch (Exception e) {
                old = null;
            }

            super.updateUI();

            if (old != null) {
                try {
                    UIManager.setLookAndFeel(old);
                } catch (UnsupportedLookAndFeelException e) {
                    throw new AssertionError("Can't happen");
                }
            }
        } else {
            super.updateUI();
        }
    }


    private class ThumbnailView
            extends FileView {
        private final ExecutorService executor = Executors.newCachedThreadPool();


        public Icon getIcon(File file) {
            if (!ImageThumbnailFileChooser.this.imageFilePattern.matcher(file.getName()).matches()) {
                return null;
            }
            synchronized (ImageThumbnailFileChooser.this.imageCache) {
                ImageIcon icon = (ImageIcon) ImageThumbnailFileChooser.this.imageCache.get(file);
                if (icon == null) {
                    icon = new ImageIcon(ImageThumbnailFileChooser.LOADING_IMAGE);
                    ImageThumbnailFileChooser.this.imageCache.put(file, icon);
                    this.executor.submit(new ImageThumbnailFileChooser.ThumbnailIconLoader(icon, file));
                }
                return icon;
            }
        }

        private ThumbnailView() {
        }
    }

    private class ThumbnailIconLoader implements Runnable {
        private final ImageIcon icon;
        private final File file;

        ThumbnailIconLoader(ImageIcon i, File f) {
            this.icon = i;
            this.file = f;
        }

        private BufferedImage getDefaultIconImage(File f) {
            Icon defaultIcon = FileSystemView.getFileSystemView().getSystemIcon(f);
            BufferedImage image = new BufferedImage(defaultIcon.getIconWidth(), defaultIcon.getIconHeight(), 2);
            defaultIcon.paintIcon(null, image.getGraphics(), 0, 0);
            return image;
        }

        public void run() {
            try {
                int x, y;
                Image scaledImg;
                BufferedImage img = ImageIO.read(this.file);
                if (img == null) {
                    img = getDefaultIconImage(this.file);
                }


                if (img.getHeight() >= img.getWidth()) {
                    scaledImg = img.getScaledInstance(-1, 16, 4);
                    x = (int) Math.round((16 - scaledImg.getWidth(null)) / 2.0D);
                    y = 0;
                } else {
                    scaledImg = img.getScaledInstance(16, -1, 4);
                    x = 0;
                    y = (int) Math.round((16 - scaledImg.getHeight(null)) / 2.0D);
                }

                BufferedImage imgPadding = new BufferedImage(16, 16, 2);
                Graphics2D g2d = imgPadding.createGraphics();
                g2d.drawImage(scaledImg, x, y, (ImageObserver) null);
                g2d.dispose();
                this.icon.setImage(imgPadding);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ImageThumbnailFileChooser.this.repaint();
                    }
                });
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
