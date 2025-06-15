package com.neurotec.samples.server.controls;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.samples.server.EnrollmentCompleteListener;
import com.neurotec.samples.server.TaskListener;
import com.neurotec.samples.server.TaskSender;
import com.neurotec.samples.server.enums.Task;
import com.neurotec.samples.server.util.DataMigrationManager;
import com.neurotec.samples.server.util.GridBagUtils;
import com.neurotec.samples.util.Utils;


public final class EnrollPanel
        extends BasePanel {
    private static final long serialVersionUID = 1L;
    private TaskSender enrollmentTaskSender;
    private EnrollmentCompleteListener enrollmentCompleteListener;
    private long startTime;
    private GridBagUtils gridBagUtils;
    private TaskListener taskListener;
    private JButton btnStart;
    private JButton btnMigrate;
    private JButton btnCancel;
    private Icon iconOk;
    private Icon iconError;
    private JLabel lblProgress;
    private JLabel lblStatusIcon;
    private JPanel panelProperties;
    private JProgressBar progressBar;
    private JSpinner spinnerBunchSize;
    private JTextField txtTemplatesCount;
    private JTextField txtTimeElapsed;
    private JTextArea txtStatus;

    public EnrollPanel(Frame owner) {
        super(owner);
        initializeComponents();
        addAncestorListener(new AncestorListener() {
            public void ancestorRemoved(AncestorEvent event) {
            }

            public void ancestorMoved(AncestorEvent event) {
            }

            public void ancestorAdded(AncestorEvent event) {
                EnrollPanel.this.enrollPanelLoaded();
            }
        });
    }

    public void setEnrollmentCompleteListener(EnrollmentCompleteListener listener) {
        this.enrollmentCompleteListener = listener;
    }

    private void initializeComponents() {
        this.gridBagUtils = new GridBagUtils(1, new Insets(3, 3, 3, 3));
        GridBagLayout enrollLayout = new GridBagLayout();
        enrollLayout.columnWidths = new int[]{75, 70, 320, 140, 50};
        enrollLayout.rowHeights = new int[]{25, 25, 20, 25, 180};
        setLayout(enrollLayout);

        this.btnStart = new JButton("Start");
        this.btnStart.addActionListener(this);

        this.btnMigrate = new JButton("Migrate & Enroll");
        this.btnMigrate.addActionListener(this);

        this.btnCancel = new JButton("Cancel");
        this.btnCancel.setEnabled(false);
        this.btnCancel.addActionListener(this);

        initializePropertiesPanel();

        this.lblProgress = new JLabel("progress", 4);
        this.progressBar = new JProgressBar(0, 100);

        this.gridBagUtils.addToGridBagLayout(0, 0, this, this.btnStart);
        this.gridBagUtils.addToGridBagLayout(1, 0, this, this.btnMigrate);
        this.gridBagUtils.addToGridBagLayout(0, 1, this, this.btnCancel);
        this.gridBagUtils.addToGridBagLayout(1, 1, 2, 2, this, this.panelProperties);
        this.gridBagUtils.addToGridBagLayout(0, 2, 3, 1, this, this.lblProgress);
        this.gridBagUtils.addToGridBagLayout(0, 3, 5, 1, this, this.progressBar);
        this.gridBagUtils.addToGridBagLayout(0, 4, 5, 1, 0, 1, this, initializeResultsPanel());
    }

    public void triggerAutoStartIfReady() {
        if (getBiometricClient() != null && getTemplateLoader() != null) {
            this.btnStart.doClick(); // ✅ safe call
        } else {
            System.err.println("❌ Cannot auto-start. Dependencies not set.");
        }
    }


    private void initializePropertiesPanel() {
        this.panelProperties = new JPanel();
        this.panelProperties.setBorder(BorderFactory.createTitledBorder("Properties"));
        GridBagLayout propertiesPanelLayout = new GridBagLayout();
        propertiesPanelLayout.columnWidths = new int[]{100, 125, 125};
        this.panelProperties.setLayout(propertiesPanelLayout);

        this.spinnerBunchSize = new JSpinner(new SpinnerNumberModel(350, 1, 10000, 1));

        this.gridBagUtils.addToGridBagLayout(0, 2, this.panelProperties, new JLabel("Bunch size:"));
        this.gridBagUtils.addToGridBagLayout(1, 2, this.panelProperties, this.spinnerBunchSize);
        this.gridBagUtils.clearGridBagConstraints();
    }

    private JPanel initializeResultsPanel() {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        GridBagLayout resultsPanelLayout = new GridBagLayout();
        resultsPanelLayout.columnWidths = new int[]{55, 45, 110, 80, 135, 240};
        resultsPanelLayout.rowHeights = new int[]{25, 50, 75};
        resultsPanel.setLayout(resultsPanelLayout);

        this.txtTemplatesCount = new JTextField();
        this.txtTemplatesCount.setEditable(false);

        this.txtTimeElapsed = new JTextField("N/A");
        this.txtTimeElapsed.setEditable(false);

        this.lblStatusIcon = new JLabel();
        this.txtStatus = new JTextArea();
        this.iconOk = Utils.createIcon("images/ok.png");
        this.iconError = Utils.createIcon("images/error.png");
        JScrollPane txtStatusScrollPane = new JScrollPane(this.txtStatus, 20, 30);

        this.gridBagUtils.addToGridBagLayout(0, 0, 2, 1, resultsPanel, new JLabel("Templates to enroll:"));
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, resultsPanel, this.txtTemplatesCount);
        this.gridBagUtils.addToGridBagLayout(3, 0, resultsPanel, new JLabel("Time elapsed:"));
        this.gridBagUtils.addToGridBagLayout(4, 0, resultsPanel, this.txtTimeElapsed);
        this.gridBagUtils.addToGridBagLayout(5, 0, 1, 1, 1, 0, resultsPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(0, 1, 1, 1, 0, 0, resultsPanel, this.lblStatusIcon);
        this.gridBagUtils.addToGridBagLayout(0, 2, 1, 1, 0, 1, resultsPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(1, 1, 5, 2, 0, 0, resultsPanel, txtStatusScrollPane);
        this.gridBagUtils.clearGridBagConstraints();
        return resultsPanel;
    }

    public void startEnrolling() {
        try {
            if (isBusy()) return;

            if (getBiometricClient() == null || getTemplateLoader() == null) {
                System.err.println("❌ Cannot start enrollment: BiometricClient or TemplateLoader is null.");
                return;
            }

            System.out.println("Preparing enrollment...");
            System.out.printf("Enrolling from: %s%n", getTemplateLoader());

            // ✅ This line caused the NPE
            getBiometricClient().getCount();

            int templateCount = getTemplateCount();
            System.out.printf("Templates to enroll: %d%n", templateCount);

            this.enrollmentTaskSender.setBunchSize((Integer) this.spinnerBunchSize.getValue());
            this.enrollmentTaskSender.setBiometricClient(getBiometricClient());
            this.enrollmentTaskSender.setTemplateLoader(getTemplateLoader());

            this.startTime = System.currentTimeMillis();
            this.enrollmentTaskSender.start(Task.ENROLL);
            enableControls(false);
        } catch (Exception e) {
//            System.err.println("Enrollment failed due to: " + e.getMessage());
//            e.printStackTrace();
        }
    }


    private void enrollPanelLoaded() {
        try {
            if (getBiometricClient() == null || getTemplateLoader() == null) {
                System.err.println("❌ BiometricClient or TemplateLoader is null — skipping enrollment initialization.");
                return;
            }

            this.enrollmentTaskSender = new TaskSender(getBiometricClient(), getTemplateLoader(), NBiometricOperation.ENROLL);

            this.taskListener = new TaskListener() {
                public void taskFinished() {
                    EnrollPanel.this.taskSenderFinished();
                }

                public void taskErrorOccurred(Exception e) {
                    EnrollPanel.this.taskSenderExceptionOccured(e);
                }

                public void taskProgressChanged(int completed) {
                    EnrollPanel.this.taskSenderProgressChanged(completed);
                }

                public void matchingTaskCompleted(NBiometricTask task) {
                    // Not used in enroll
                }
            };

            this.enrollmentTaskSender.addTaskListener(this.taskListener);

            this.lblProgress.setText("");
            this.lblProgress.setText("");
            this.progressBar.setValue(0);

            // Automatically start enrollment if ready
            startEnrolling();

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize EnrollPanel: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void taskSenderProgressChanged(int numberOfTasksCompleted) {
        int maxProgress = this.progressBar.getMaximum();
        int progressValue = Math.min(numberOfTasksCompleted, maxProgress);

        // Simulate progress update in console
        System.out.printf("Progress: %d / %d%n", progressValue, maxProgress);

        // Time calculations
        long elapsed = System.currentTimeMillis() - this.startTime;
        System.out.printf("Elapsed Time: %.2f s%n", elapsed / 1000.0);

        this.progressBar.setValue(progressValue);
        this.lblProgress.setText(String.format("%d / %d", progressValue, maxProgress));
    }

    private void taskSenderExceptionOccured(Exception e) {
        appendStatus(String.format("%s\r\n", new Object[]{e}), Color.RED.darker());
    }

    private void taskSenderFinished() {
        // You may still want to enable controls if relevant, or remove this
         enableControls(true); // remove if only used in Swing

        long elapsed = this.enrollmentTaskSender.getElapsedTime();

        long hr = TimeUnit.MILLISECONDS.toHours(elapsed);
        long min = TimeUnit.MILLISECONDS.toMinutes(elapsed - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(elapsed - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));

        System.out.printf("Total Time Elapsed: %02d:%02d:%02d%n", hr, min, sec);

        if (this.enrollmentTaskSender.isSuccessful()) {
            System.out.println("✔ Enrollment successful");
            this.enrollmentCompleteListener.onEnrollmentComplete();
        } else if (this.enrollmentTaskSender.isCanceled()) {
            System.out.println("⚠ Enrollment canceled");
            // Reset progress bar equivalent if needed
            System.out.println("Progress reset to 0%");
        } else {
            System.out.println("✖ Enrollment finished with errors");
        }
    }


    private void enableControls(boolean isIdle) {
        this.btnStart.setEnabled(isIdle);
        this.btnCancel.setEnabled(!isIdle);

        for (Component c : this.panelProperties.getComponents()) {
            c.setEnabled(isIdle);
        }
    }

//    private void setStatus(String msg, Color color, Icon icon) {
//        this.txtStatus.setForeground(color);
//        this.txtStatus.setText(msg);
//        this.lblStatusIcon.setIcon(icon);
//    }
//
//    private void appendStatus(String msg) {
//        appendStatus(msg, Color.BLACK);
//    }
//
//    private void appendStatus(String msg, Color color) {
//        this.txtStatus.setText(this.txtStatus.getText() + msg);
//        this.txtStatus.setForeground(color);
//    }

    private void setStatus(String msg, Color color, Icon icon) {
        System.out.println("[STATUS] " + msg);
    }

    private void appendStatus(String msg) {
        System.out.println("[LOG] " + msg);
    }

    private void appendStatus(String msg, Color color) {
        System.out.println("[LOG] " + msg);
    }

    public String getTitle() {
        return "Enroll Templates";
    }


    public boolean isBusy() {
        if (this.enrollmentTaskSender != null) {
            return this.enrollmentTaskSender.isBusy();
        }
        return false;
    }


    public void cancel() {
        appendStatus("\r\nCanceling, please wait ...\r\n");
        this.enrollmentTaskSender.cancel();
        this.btnCancel.setEnabled(false);
    }


    public void waitForCurrentProcessToFinish() throws InterruptedException, ExecutionException {
        this.enrollmentTaskSender.waitForCurrentProcessToFinish(getOwner());
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.btnStart) {
            startEnrolling();
        } else if (source == this.btnCancel) {
            cancel();
        } else if (source == this.btnMigrate) {
            runMigrationAndEnroll();
        }
    }

    private void runMigrationAndEnroll() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("Starting data migration...");
                DataMigrationManager migrationManager = new DataMigrationManager();
                migrationManager.runMigration();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    System.out.println("Data migration completed successfully.");
                    startEnrolling();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Data migration failed: " + e.getMessage());
                }
            }
        }.execute();
    }
}