package com.neurotec.samples.server.controls;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NEMatchingDetails;
import com.neurotec.biometrics.NFMatchingDetails;
import com.neurotec.biometrics.NLMatchingDetails;
import com.neurotec.biometrics.NMatchingDetails;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSMatchingDetails;
import com.neurotec.biometrics.NSubject;
import com.neurotec.io.NBuffer;
import com.neurotec.samples.server.TaskListener;
import com.neurotec.samples.server.TaskSender;
import com.neurotec.samples.server.util.GridBagUtils;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.server.util.PropertyLoader;
import com.neurotec.samples.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public final class DeduplicationPanel
        extends BasePanel {

    private static final Logger log = LogManager.getLogger(DeduplicationPanel.class);
    PropertyLoader propertyLoader = new PropertyLoader();
    private static final long serialVersionUID = 1L;
    private TaskSender deduplicationTaskSender;
    private long startTime;
    private String resultsFilePath = propertyLoader.getResultDirectory() == null ? "result.csv" : propertyLoader.getResultDirectory();

    private GridBagUtils gridBagUtils;

    private JButton btnStart;

    private JButton btnCancel;

    private JButton btnBrowseResultFile;

    private Icon iconOk;

    private Icon iconError;

    private JLabel lblProgress;

    private JLabel lblRemaining;

    private JLabel lblStatusIcon;
    private JPanel panelProperties;
    private JProgressBar progressBar;
    private JTextField txtResultFilePath;
    private JTextArea txtStatus;
    private JFileChooser openFileDialog;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private Frame ownerFrame;
    private int startIndex;
    private boolean processCompleted = false;

    public DeduplicationPanel(Frame owner) {
        super(owner);
        this.ownerFrame = owner;
        initializeComponents();
        this.openFileDialog = new JFileChooser(this.resultsFilePath);
        addAncestorListener(new AncestorListener() {
            public void ancestorRemoved(AncestorEvent event) {
            }

            public void ancestorMoved(AncestorEvent event) {
            }

            public void ancestorAdded(AncestorEvent event) {
                DeduplicationPanel.this.deduplicationPanelLoaded();
            }
        });
        
        if (SystemTray.isSupported()) {
            setupSystemTray();
        } else {
            log.warn("System tray is not supported on this platform");
        }
    }

    private void setupSystemTray() {
        try {
            systemTray = SystemTray.getSystemTray();
            PopupMenu popup = new PopupMenu();
            
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> {
                if (ownerFrame != null) {
                    ownerFrame.setVisible(true);
                    ownerFrame.setState(Frame.NORMAL);
                }
            });
            
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                systemTray.remove(trayIcon);
                System.exit(0);
            });
            
            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);
            
            int width = 16;
            int height = 16;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, Color.GRAY.getRGB());
                }
            }
            
            trayIcon = new TrayIcon(image, "AFIS Deduplication", popup);
            trayIcon.setImageAutoSize(true);
            
            systemTray.add(trayIcon);
            
            trayIcon.addActionListener(e -> {
                if (ownerFrame != null) {
                    ownerFrame.setVisible(true);
                    ownerFrame.setState(Frame.NORMAL);
                }
            });
            
            if (ownerFrame != null) {
                ownerFrame.setVisible(false);
            }
        } catch (AWTException e) {
            log.error("Error setting up system tray", e);
        }
    }

    private void initializeComponents() {
        this.gridBagUtils = new GridBagUtils(1, new Insets(3, 3, 3, 3));
        GridBagLayout duplicationLayout = new GridBagLayout();
        duplicationLayout.columnWidths = new int[]{75, 70, 400, 30, 50};
        duplicationLayout.rowHeights = new int[]{25, 25, 20, 25, 50, 150};
        setLayout(duplicationLayout);

        this.btnStart = new JButton("Start");
        this.btnStart.addActionListener(this);

        this.btnCancel = new JButton("Cancel");
        this.btnCancel.setEnabled(false);
        this.btnCancel.addActionListener(this);

        initializePropertiesPanel();

        this.lblRemaining = new JLabel("Estimated time remaining:");
        this.lblProgress = new JLabel("progress label", 4);
        this.progressBar = new JProgressBar(0, 100);
        this.lblStatusIcon = new JLabel();
        this.lblStatusIcon.setHorizontalAlignment(0);
        this.txtStatus = new JTextArea();

        this.iconOk = Utils.createIcon("images/ok.png");
        this.iconError = Utils.createIcon("images/error.png");
        JScrollPane txtStatusScrollPane = new JScrollPane(this.txtStatus, 20, 30);

        this.gridBagUtils.addToGridBagLayout(0, 0, this, this.btnStart);
        this.gridBagUtils.addToGridBagLayout(0, 1, this, this.btnCancel);
        this.gridBagUtils.addToGridBagLayout(1, 0, 2, 2, this, this.panelProperties);
        this.gridBagUtils.addToGridBagLayout(0, 2, 3, 1, this, this.lblRemaining);
        this.gridBagUtils.addToGridBagLayout(3, 2, 1, 1, 1, 0, this, new JLabel());
        this.gridBagUtils.addToGridBagLayout(4, 2, 1, 1, 0, 0, this, this.lblProgress);
        this.gridBagUtils.addToGridBagLayout(0, 3, 5, 1, this, this.progressBar);
        this.gridBagUtils.addToGridBagLayout(0, 4, 1, 1, this, this.lblStatusIcon);
        this.gridBagUtils.addToGridBagLayout(0, 5, 1, 1, 0, 1, this, new JLabel());
        this.gridBagUtils.addToGridBagLayout(1, 4, 4, 2, 0, 0, this, txtStatusScrollPane);
        this.gridBagUtils.clearGridBagConstraints();
    }

    private void initializePropertiesPanel() {
        this.panelProperties = new JPanel();
        this.panelProperties.setBorder(BorderFactory.createTitledBorder("Properties"));
        GridBagLayout propertiesPanelLayout = new GridBagLayout();
        propertiesPanelLayout.columnWidths = new int[]{125, 75, 265, 40};
        this.panelProperties.setLayout(propertiesPanelLayout);

        this.txtResultFilePath = new JTextField(propertyLoader.getResultDirectory() == null ? "result.csv" : propertyLoader.getResultDirectory());
        this.btnBrowseResultFile = new JButton("...");
        this.btnBrowseResultFile.addActionListener(this);

        this.gridBagUtils.addToGridBagLayout(0, 2, this.panelProperties, new JLabel("Duplication results file:"));
        this.gridBagUtils.addToGridBagLayout(1, 2, 2, 1, this.panelProperties, this.txtResultFilePath);
        this.gridBagUtils.addToGridBagLayout(3, 2, 1, 1, this.panelProperties, this.btnBrowseResultFile);
        this.gridBagUtils.clearGridBagConstraints();
    }

    private void setPropertiesPanelEnabled(boolean enabled) {
        for (Component c : this.panelProperties.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    private void enableControls(boolean isIdle) {
        this.btnStart.setEnabled(isIdle);
        this.btnCancel.setEnabled(!isIdle);
        setPropertiesPanelEnabled(isIdle);
    }

    private void setStatus(String msg, Color color, Icon icon) {
        this.txtStatus.setForeground(color);
        this.txtStatus.setText(msg);
        this.lblStatusIcon.setIcon(icon);
    }

    private void appendStatus(String msg, Color color) {
        this.txtStatus.setText(this.txtStatus.getText() + msg);
        this.txtStatus.setForeground(color);
    }

    private void writeLogHeader() {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("TemplateId,MatchedWith,Score,FingersScore,FingersScores,IrisesScore,IrisesScores");
        headerBuilder.append(",FacesScore,FacesScores,VoicesScore,VoicesScores,PalmsScore,PalmsScores");

        headerBuilder.append("\n");

        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(new File(this.resultsFilePath));
            writer = new BufferedWriter(fw);
            writer.write(headerBuilder.toString());
        } catch (IOException e) {
            appendStatus(String.format("%s\r\n", new Object[]{e}), Color.RED.darker());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.fillInStackTrace();
                    MessageUtils.showError(this, e);
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.fillInStackTrace();
                    MessageUtils.showError(this, e);
                }
            }
        }
    }

    private void matchingTasksCompleted(NBiometricTask task) {
        try {
            StringBuilder builder = new StringBuilder();
            for (NSubject subject : task.getSubjects()) {
                if (subject.getMatchingResults() != null && subject.getMatchingResults().size() > 0) {
                    for (NMatchingResult item : subject.getMatchingResults()) {
                        NBuffer buffer = item.getMatchingDetailsBuffer();
                        NMatchingDetails details = new NMatchingDetails(buffer);

                        try {
                            builder.append(String.format("%s,%s,%d", new Object[]{subject.getId(), item.getId(), Integer.valueOf(item.getScore())}));

                            builder.append(String.format(",%d,", new Object[]{Integer.valueOf(details.getFingersScore())}));
                            NMatchingDetails.FingerCollection fingers = details.getFingers();
                            for (NFMatchingDetails finger : fingers) {
                                try {
                                    builder.append(String.format("%d;", new Object[]{Integer.valueOf(finger.getScore())}));
                                } finally {
                                    finger.dispose();
                                }
                            }

                            builder.append(String.format(",%d,", new Object[]{Integer.valueOf(details.getIrisesScore())}));
                            NMatchingDetails.IrisCollection irises = details.getIrises();
                            for (NEMatchingDetails iris : irises) {
                                try {
                                    builder.append(String.format("%d;", new Object[]{Integer.valueOf(iris.getScore())}));
                                } finally {
                                    iris.dispose();
                                }
                            }

                            builder.append(String.format(",%d,", new Object[]{Integer.valueOf(details.getFacesScore())}));
                            NMatchingDetails.FaceCollection faces = details.getFaces();
                            for (NLMatchingDetails face : faces) {
                                try {
                                    builder.append(String.format("%d;", new Object[]{Integer.valueOf(face.getScore())}));
                                } finally {
                                    face.dispose();
                                }
                            }

                            builder.append(String.format(",%d,", new Object[]{Integer.valueOf(details.getVoicesScore())}));
                            NMatchingDetails.VoiceCollection voices = details.getVoices();
                            for (NSMatchingDetails voice : voices) {
                                try {
                                    builder.append(String.format("%d;", new Object[]{Integer.valueOf(voice.getScore())}));
                                } finally {
                                    voice.dispose();
                                }
                            }

                            builder.append(String.format(",%d,", new Object[]{Integer.valueOf(details.getPalmsScore())}));
                            NMatchingDetails.PalmCollection palms = details.getPalms();
                            for (NFMatchingDetails palm : palms) {
                                try {
                                    builder.append(String.format("%d;", new Object[]{Integer.valueOf(palm.getScore())}));
                                } finally {
                                    palm.dispose();
                                }
                            }
                            builder.append("\n");
                        } finally {
                            details.dispose();
                        }
                        details.close();
                    }
                    continue;
                }
                builder.append(String.format("%s,NoMatches", new Object[]{subject.getId()}));
                builder.append("\n");
            }


            FileWriter fw = null;
            BufferedWriter writer = null;
            try {
                fw = new FileWriter(new File(this.resultsFilePath), true);
                writer = new BufferedWriter(fw);
                writer.write(builder.toString());
                writer.flush();
            } finally {
                if (writer != null) {
                    writer.close();
                }
                if (fw != null) {
                    fw.close();
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            appendStatus(String.format("%s\r\n", new Object[]{e}), Color.RED.darker());
        }
    }

    private void taskSenderExceptionOccurred(Exception ex) {
        StringBuilder stacktrace = new StringBuilder();
        StackTraceElement[] elements = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : elements) {
            stacktrace.append(stackTraceElement.toString()).append("\r\n");
        }
        appendStatus(String.format("%s\r\n", new Object[]{ex.getMessage()}), Color.RED.darker());
        appendStatus(String.format("%s\r\n", new Object[]{stacktrace}), Color.RED.darker());
    }

    private void taskSenderFinished() {
        enableControls(true);
        this.lblRemaining.setText("");
        if (this.deduplicationTaskSender.isSuccessful() && !this.deduplicationTaskSender.isCanceled()) {
            appendStatus("Deduplication completed without errors", Color.BLACK);
            this.lblStatusIcon.setIcon(this.iconOk);
            System.out.println(
                     "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"+
                       "~~~~~~~~~~~~~ Deduplication Completed ~~~~~~~~~~~~\n"+
                       "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
        } else {
            appendStatus(this.deduplicationTaskSender.isCanceled() ? "Deduplication canceled." : "There were errors during deduplication", Color.RED.darker());
            this.btnStart.setEnabled(true);
            this.lblStatusIcon.setIcon(this.iconError);
            this.progressBar.setValue(0);
        }
        // Don't exit automatically, let user decide from tray menu
//         System.exit(0);
    }

    private void taskSenderProgressChanged(int numberOfTasksCompleted) {
        if (processCompleted) {
            return;
        }
        
        if (numberOfTasksCompleted == 1) {
            setStatus("Matching templates ...\r\n", Color.BLACK, (Icon) null);
        }
        String eta = "Estimated time remaining: 00:00:00:00";
        if (numberOfTasksCompleted % 10 == 0) {
            long remaining = (System.currentTimeMillis() - this.startTime) / numberOfTasksCompleted * (this.progressBar.getMaximum() - numberOfTasksCompleted);
            if (remaining / 1000L < 0L) {
                remaining = 0L;
            }
            long days = TimeUnit.MILLISECONDS.toDays(remaining);
            long hr = TimeUnit.MILLISECONDS.toHours(remaining - TimeUnit.DAYS.toMillis(days));
            long min = TimeUnit.MILLISECONDS.toMinutes(remaining - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hr));
            long sec = TimeUnit.MILLISECONDS.toSeconds(remaining - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
            this.lblRemaining.setText(
                    String.format(
                            "Estimated time remaining: %02d.%02d:%02d:%02d",
                            new Object[]{
                                    days,
                                    hr,
                                    min,
                                    sec
                            })
            );
            eta = String.format(
                    "Estimated time remaining: %02d:%02d:%02d:%02d",
                    days,
                    hr,
                    min,
                    sec
            );
        }
        
        // Calculate the actual progress value (starting from startIndex)
        int actualProgress = this.startIndex + numberOfTasksCompleted;
        
        // Ensure we don't exceed the maximum template count
        if (actualProgress > this.progressBar.getMaximum()) {
            actualProgress = this.progressBar.getMaximum();
            // If we've reached the maximum, cancel the task to prevent further processing
            if (this.deduplicationTaskSender != null && this.deduplicationTaskSender.isBusy()) {
                System.out.println("\nReached maximum template count of " + this.progressBar.getMaximum() + ". Completing deduplication process.");
                this.deduplicationTaskSender.cancel();
                
                // Set the process as completed to prevent further progress updates
                processCompleted = true;
                
                // Manually trigger task completion to ensure the process finishes properly
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        taskSenderFinished();
                    }
                });
            }
        }
        
        this.progressBar.setValue(actualProgress);
        
        this.lblProgress.setText(String.format("%s / %s", new Object[]{
                actualProgress,
                this.progressBar.getMaximum()
        }));
        
        // Show progress bar in console
        showProgress(actualProgress, this.progressBar.getMaximum(), eta);
        
        // Add detailed progress logging for each template
        System.out.println("\nProgress: " + numberOfTasksCompleted + " templates processed since index " + this.startIndex + 
                          " (Template " + actualProgress + " of " + this.progressBar.getMaximum() + 
                          ", " + String.format("%.2f", (actualProgress * 100.0 / this.progressBar.getMaximum())) + "%)");
    }

    private void showProgress(int completed, int total, String eta) {
        int barLength = 50;
        int progress = (int) (((double) completed / total) * barLength);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("=");
            } else if (i == progress) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");

        int percent = (int) (((double) completed / total) * 100);
        System.out.print("\r" + bar + String.format(" %3d%% (%d / %d)   %s", percent, completed, total, eta));
        System.out.flush();
    }

    private void startDeduplication() {
        try {
            if (isBusy()) {
                MessageUtils.showInformation(this, "Previous process is not completed yet.");
                return;
            }
            setStatus("Preparing ...", Color.BLACK, (Icon) null);
            this.lblProgress.setText("");
            this.lblRemaining.setText("");
            System.out.println(
                    "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"+
                      "~~~~~~~~~~~~~ Deduplication started ~~~~~~~~~~~~~~\n"+
                      "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

            getBiometricClient().getCount();

            this.resultsFilePath = this.txtResultFilePath.getText().trim();
            if (this.resultsFilePath == null || this.resultsFilePath.isEmpty()) {
                this.resultsFilePath = propertyLoader.getResultDirectory() == null ? "result.csv" : propertyLoader.getResultDirectory();
                this.txtResultFilePath.setText(this.resultsFilePath);
            }
            writeLogHeader();

            this.progressBar.setValue(0);
            this.processCompleted = false;
            try {
                int templateCount = getTemplateCount();
                if (templateCount <= 0) {
                    MessageUtils.showInformation(this, "No templates found or error retrieving template count. Please check connection settings and permissions.");
                    setStatus("No templates found or error retrieving template count.", Color.RED.darker(), this.iconError);
                    return;
                }
                
                // Set the progress bar maximum to the total template count
                this.progressBar.setMaximum(templateCount);
                this.lblProgress.setText(String.format("0 / %s", templateCount));
                
                // Store the starting index for later use
                this.startIndex = 300;
                if (this.startIndex >= templateCount) {
                    MessageUtils.showInformation(this, "Starting index " + this.startIndex + " is greater than or equal to the total template count of " + templateCount + ".");
                    setStatus("Starting index " + this.startIndex + " is greater than or equal to the total template count.", Color.RED.darker(), this.iconError);
                    return;
                }
                
                System.out.println("Starting from index " + this.startIndex + ", processing templates " + this.startIndex + " to " + (templateCount - 1) + " out of " + templateCount + " total templates");

            } catch (Exception e) {
                System.err.println("Error getting template count: " + e.getMessage());
                e.fillInStackTrace();
                MessageUtils.showError(this, "Error getting template count: " + e.getMessage());
                setStatus("Error getting template count: " + e.getMessage(), Color.RED.darker(), this.iconError);
                return;
            }

            getBiometricClient().setMatchingWithDetails(true);
            this.deduplicationTaskSender.setBunchSize(350);
            this.deduplicationTaskSender.setBiometricClient(getBiometricClient());
            this.deduplicationTaskSender.setTemplateLoader(getTemplateLoader());

            this.startTime = System.currentTimeMillis();
            this.deduplicationTaskSender.start();
            enableControls(false);
            
            // Minimize to tray when task starts
            if (ownerFrame != null) {
                ownerFrame.setVisible(false);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            MessageUtils.showError(this, e);
            setStatus("Deduplication failed due to: " + e.toString(), Color.RED.darker(), this.iconError);
            log.error("Deduplication failed due to: {}", e.getMessage());
        }
    }

    private void deduplicationPanelLoaded() {
        try {
            this.deduplicationTaskSender = new TaskSender(getBiometricClient(), getTemplateLoader(), NBiometricOperation.IDENTIFY);
            this.deduplicationTaskSender.addTaskListener(new TaskListener() {
                public void taskFinished() {
                    DeduplicationPanel.this.taskSenderFinished();
                }

                public void taskErrorOccurred(Exception e) {
                    DeduplicationPanel.this.taskSenderExceptionOccurred(e);
                }

                public void taskProgressChanged(int completed) {
                    DeduplicationPanel.this.taskSenderProgressChanged(completed);
                }

                public void matchingTaskCompleted(NBiometricTask task) {
                    DeduplicationPanel.this.matchingTasksCompleted(task);
                }
            });
            this.lblProgress.setText("");
            this.lblRemaining.setText("");

            startDeduplication();
        } catch (Exception e) {
            e.fillInStackTrace();
            MessageUtils.showError(this, e);
        }
    }

    private void browseForResultFile() {
        if (this.openFileDialog.showOpenDialog(this) == 0) {
            this.txtResultFilePath.setText(this.openFileDialog.getSelectedFile().getPath());
        }
    }


    public void cancel() {
        appendStatus("\r\nCanceling, please wait ...\r\n", Color.BLACK);
        this.deduplicationTaskSender.cancel();
        this.btnCancel.setEnabled(false);
    }


    public boolean isBusy() {
        if (this.deduplicationTaskSender != null) {
            return this.deduplicationTaskSender.isBusy();
        }
        return false;
    }


    public void waitForCurrentProcessToFinish() throws InterruptedException, ExecutionException {
        this.deduplicationTaskSender.waitForCurrentProcessToFinish(getOwner());
    }


    public String getTitle() {
        return "Deduplication";
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.btnStart) {
            startDeduplication();
        } else if (source == this.btnCancel) {
            cancel();
        } else if (source == this.btnBrowseResultFile) {
            browseForResultFile();
        }
    }
}
