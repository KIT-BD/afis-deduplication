package com.neurotec.samples.server.controls;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.samples.server.TaskListener;
import com.neurotec.samples.server.TaskSender;
import com.neurotec.samples.server.enums.Task;
import com.neurotec.samples.server.util.GridBagUtils;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.util.Utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public final class TestSpeedPanel
        extends BasePanel {
    private static final long serialVersionUID = 1L;
    private TaskSender taskSender;
    private GridBagUtils gridBagUtils;
    private JButton btnStart;
    private JButton btnCancel;
    private JLabel lblCount;
    private JLabel lblRemaining;
    private JLabel lblStatusIcon;
    private JLabel lblTemplatesOnAcc;
    private Icon iconOk;
    private Icon iconError;
    private JPanel panelProperties;
    private JProgressBar progressBar;
    private JSpinner spinnerMaxCount;
    private JTextField txtMatchedTemplatesCount;
    private JTextField txtTime;
    private JTextField txtDBSize;
    private JTextField txtSpeed;
    private JTextArea txtStatus;

    public TestSpeedPanel(Frame owner) {
        super(owner);
        initializeComponents();
        addAncestorListener(new AncestorListener() {
            public void ancestorRemoved(AncestorEvent event) {
            }


            public void ancestorMoved(AncestorEvent event) {
            }


            public void ancestorAdded(AncestorEvent event) {
                TestSpeedPanel.this.testSpeedPanelLoaded();
            }
        });
    }


    private void initializeComponents() {
        this.gridBagUtils = new GridBagUtils(1, new Insets(3, 3, 3, 3));

        GridBagLayout speedLayout = new GridBagLayout();
        speedLayout.columnWidths = new int[]{75, 290, 160, 70};
        speedLayout.rowHeights = new int[]{25, 25, 25, 20, 25, 170};
        setLayout(speedLayout);

        this.btnStart = new JButton("Start");
        this.btnStart.addActionListener(this);

        this.btnCancel = new JButton("Cancel");
        this.btnCancel.setEnabled(false);
        this.btnCancel.addActionListener(this);

        initializePropertiesPanel();

        this.lblRemaining = new JLabel("Estimated time remaining:", 2);
        this.lblCount = new JLabel("progress", 4);
        this.progressBar = new JProgressBar(0, 100);

        this.gridBagUtils.addToGridBagLayout(0, 0, this, this.btnStart);
        this.gridBagUtils.addToGridBagLayout(0, 1, this, this.btnCancel);
        this.gridBagUtils.addToGridBagLayout(1, 0, 1, 3, this, this.panelProperties);
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, 1, 0, this, new JLabel());
        this.gridBagUtils.addToGridBagLayout(0, 3, 1, 1, 0, 0, this, this.lblRemaining);
        this.gridBagUtils.addToGridBagLayout(3, 3, 1, 1, 0, 0, this, this.lblCount);
        this.gridBagUtils.addToGridBagLayout(0, 4, 4, 1, this, this.progressBar);
        this.gridBagUtils.addToGridBagLayout(0, 5, 4, 1, 0, 1, this, initializeResultsPanel());
    }

    private void initializePropertiesPanel() {
        this.panelProperties = new JPanel();
        this.panelProperties.setBorder(BorderFactory.createTitledBorder("Properties"));
        GridBagLayout propertiesPanelLayout = new GridBagLayout();
        propertiesPanelLayout.columnWidths = new int[]{150, 100, 20};
        this.panelProperties.setLayout(propertiesPanelLayout);

        this.spinnerMaxCount = new JSpinner(new SpinnerNumberModel(1000, 10, 2147483647, 1));

        this.gridBagUtils.addToGridBagLayout(0, 2, this.panelProperties, new JLabel("Maximum templates to match:"));
        this.gridBagUtils.addToGridBagLayout(1, 2, this.panelProperties, this.spinnerMaxCount);
        this.gridBagUtils.addToGridBagLayout(2, 2, this.panelProperties, new JLabel("*"));
        this.gridBagUtils.addToGridBagLayout(0, 3, 3, 1, this.panelProperties, new JLabel("*- All templates should be able to fit into memory at once"));

        this.gridBagUtils.clearGridBagConstraints();
    }

    private JPanel initializeResultsPanel() {
        JPanel resultsPanel = new JPanel();
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        GridBagLayout resultsPanelLayout = new GridBagLayout();
        resultsPanelLayout.columnWidths = new int[]{55, 50, 110, 130, 110, 110};
        resultsPanelLayout.rowHeights = new int[]{25, 25, 25, 50, 35};
        resultsPanel.setLayout(resultsPanelLayout);

        this.txtMatchedTemplatesCount = new JTextField("N/A");
        this.txtMatchedTemplatesCount.setEditable(false);

        this.txtTime = new JTextField("N/A");
        this.txtTime.setEditable(false);

        this.txtDBSize = new JTextField("N/A");
        this.txtDBSize.setEditable(false);

        this.txtSpeed = new JTextField("N/A");
        this.txtSpeed.setEditable(false);

        this.lblStatusIcon = new JLabel();
        this.txtStatus = new JTextArea();
        this.iconOk = Utils.createIcon("images/ok.png");
        this.iconError = Utils.createIcon("images/error.png");
        JScrollPane txtStatusScrollPane = new JScrollPane(this.txtStatus, 20, 30);

        this.lblTemplatesOnAcc = new JLabel("Templates count:");
        this.gridBagUtils.addToGridBagLayout(0, 0, 2, 1, resultsPanel, new JLabel("Templates matched:"));
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, resultsPanel, this.txtMatchedTemplatesCount);
        this.gridBagUtils.addToGridBagLayout(3, 0, resultsPanel, this.lblTemplatesOnAcc);
        this.gridBagUtils.addToGridBagLayout(4, 0, resultsPanel, this.txtDBSize);
        this.gridBagUtils.addToGridBagLayout(0, 1, 2, 1, resultsPanel, new JLabel("Time elapsed:"));
        this.gridBagUtils.addToGridBagLayout(2, 1, 1, 1, resultsPanel, this.txtTime);
        this.gridBagUtils.addToGridBagLayout(3, 1, resultsPanel, new JLabel("Speed:"));
        this.gridBagUtils.addToGridBagLayout(4, 1, resultsPanel, this.txtSpeed);
        this.gridBagUtils.addToGridBagLayout(5, 1, 1, 1, 1, 0, resultsPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(0, 3, 1, 1, 0, 0, resultsPanel, this.lblStatusIcon);
        this.gridBagUtils.addToGridBagLayout(0, 4, 1, 1, 0, 1, resultsPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(1, 3, 5, 2, 0, 0, resultsPanel, txtStatusScrollPane);
        this.gridBagUtils.clearGridBagConstraints();
        return resultsPanel;
    }

    private void startSpeedTest() {
        try {
            if (isBusy())
                return;
            enableControls(false);
            this.txtSpeed.setText("N/A");
            this.txtTime.setText("N/A");
            this.txtMatchedTemplatesCount.setText("N/A");
            setStatus("Preparing ...", Color.BLACK, (Icon) null);
            this.lblCount.setText("");
            this.progressBar.setValue(0);


            getBiometricClient().getCount();

            int maxCount = ((Integer) this.spinnerMaxCount.getValue()).intValue();
            int loaderTemplateCount = getTemplateCount();
            this.progressBar.setMaximum((maxCount > loaderTemplateCount) ? loaderTemplateCount : maxCount);
            this.lblStatusIcon.setIcon(null);

            int templateCount = getBiometricClient().getCount();
            this.txtDBSize.setText(String.valueOf(templateCount));

            this.taskSender.setBunchSize(maxCount);
            this.taskSender.setSendOneBunchOnly(true);
            this.taskSender.setTemplateLoader(getTemplateLoader());
            this.taskSender.setBiometricClient(getBiometricClient());
            this.taskSender.start(Task.SPEED_TEST);
        } catch (Exception e) {
            MessageUtils.showError(this, e);
            setStatus("Testing speed failed due to: " + e.toString(), Color.RED.darker(), this.iconError);
            enableControls(true);
        }
    }

    private void testSpeedPanelLoaded() {
        try {
            this.taskSender = new TaskSender(getBiometricClient(), getTemplateLoader(), NBiometricOperation.IDENTIFY);
            this.taskSender.addTaskListener(new TaskListener() {
                public void taskFinished() {
                    TestSpeedPanel.this.taskSenderFinished();
                }


                public void taskErrorOccurred(Exception e) {
                    TestSpeedPanel.this.taskSenderExceptionOccured(e);
                }


                public void taskProgressChanged(int completed) {
                    TestSpeedPanel.this.taskSenderProgressChanged(completed);
                }


                public void matchingTaskCompleted(NBiometricTask task) {
                }
            });
            this.lblCount.setText("");
            this.lblRemaining.setText("");
        } catch (Exception e) {
            MessageUtils.showError(this, e);
        }
    }

    private void enableControls(boolean isIdle) {
        this.btnStart.setEnabled(isIdle);
        setPropertiesPanelEnabled(isIdle);
        this.btnCancel.setEnabled(!isIdle);
    }

    private void setPropertiesPanelEnabled(boolean enabled) {
        for (Component c : this.panelProperties.getComponents()) {
            c.setEnabled(enabled);
        }
    }

    private void taskSenderProgressChanged(int templatesMatched) {
        if (templatesMatched == 1) {
            setStatus("Matching templates ...", Color.BLACK, (Icon) null);
        }

        this.txtMatchedTemplatesCount.setText(String.valueOf(templatesMatched));

        int dbSize = Integer.valueOf(this.txtDBSize.getText()).intValue();
        long timeElapsed = this.taskSender.getElapsedTime();
        double timeElapsedSec = timeElapsed / 1000.0D;
        double speed = (dbSize * templatesMatched) / timeElapsedSec;

        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.applyPattern("###,###.##");
        this.txtSpeed.setText(df.format(speed));
        this.txtTime.setText(String.format("%.2f s", new Object[]{Double.valueOf(timeElapsedSec)}));

        int maxCount = ((Integer) this.spinnerMaxCount.getValue()).intValue();
        long remaining = Math.round((float) (timeElapsed / templatesMatched * (maxCount - templatesMatched)));
        long hr = TimeUnit.MILLISECONDS.toHours(remaining);
        long min = TimeUnit.MILLISECONDS.toMinutes(remaining - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(remaining - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        this.lblRemaining.setText(String.format("Estimated time remaining: %02d:%02d:%02d", new Object[]{Long.valueOf(hr), Long.valueOf(min), Long.valueOf(sec)}));

        this.progressBar.setValue(templatesMatched);
        this.lblCount.setText(String.format("%s / %s", new Object[]{Integer.valueOf(templatesMatched), Integer.valueOf(this.progressBar.getMaximum())}));
    }

    private void taskSenderFinished() {
        enableControls(true);
        if (this.taskSender.isCanceled()) {
            appendStatus("Speed test canceled\r\n", Color.RED);
            this.lblStatusIcon.setIcon(this.iconError);
            this.btnStart.setEnabled(true);
            this.progressBar.setValue(0);

            return;
        }
        if (this.taskSender.isSuccessful()) {
            this.txtMatchedTemplatesCount.setText(String.valueOf(this.taskSender.getPerformedTaskCount()));
            setStatus(
                    String.format("Speed: %s templates per second.\nTotal of %s templates were sent and matched against %s templates per %s seconds.", new Object[]{
                            this.txtSpeed.getText(), this.txtMatchedTemplatesCount.getText(), this.txtDBSize.getText(), this.txtTime.getText()
                    }), Color.BLACK, this.iconOk);
        } else {
            appendStatus("\r\nOperation completed with errors\r\n", Color.BLACK);
            this.lblStatusIcon.setIcon(this.iconError);
        }
        this.progressBar.setValue(this.progressBar.getMaximum());
    }

    private void taskSenderExceptionOccured(Exception e) {
        appendStatus(String.format("%s\r\n", new Object[]{e}), Color.RED.darker());
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


    public String getTitle() {
        return "Test matching speed";
    }


    public boolean isBusy() {
        if (this.taskSender != null) {
            return this.taskSender.isBusy();
        }
        return false;
    }


    public void cancel() {
        this.taskSender.cancel();
        setStatus("Canceling, please wait ...\r\n", Color.BLACK, (Icon) null);
        this.btnCancel.setEnabled(false);
    }


    public void waitForCurrentProcessToFinish() throws InterruptedException, ExecutionException {
        this.taskSender.waitForCurrentProcessToFinish(getOwner());
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.btnStart) {
            startSpeedTest();
        } else if (source == this.btnCancel) {
            cancel();
        }
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\controls\TestSpeedPanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */