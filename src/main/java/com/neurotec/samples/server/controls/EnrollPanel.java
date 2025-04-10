package com.neurotec.samples.server.controls;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.samples.server.TaskListener;
import com.neurotec.samples.server.TaskSender;
import com.neurotec.samples.server.util.GridBagUtils;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.util.Utils;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;






















public final class EnrollPanel
  extends BasePanel
{
  private static final long serialVersionUID = 1L;
  private TaskSender enrollmentTaskSender;
  private long startTime;
  private GridBagUtils gridBagUtils;
  private TaskListener taskListener;
  private JButton btnStart;
  private JButton btnCancel;
  private Icon iconOk;
  private Icon iconError;
  private JLabel lblRemaining;
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
    addAncestorListener(new AncestorListener()
        {
          public void ancestorRemoved(AncestorEvent event) {}


          public void ancestorMoved(AncestorEvent event) {}


          public void ancestorAdded(AncestorEvent event) {
            EnrollPanel.this.enrollPanelLoaded();
          }
        });
  }





  private void initializeComponents() {
    this.gridBagUtils = new GridBagUtils(1, new Insets(3, 3, 3, 3));
    GridBagLayout enrollLayout = new GridBagLayout();
    enrollLayout.columnWidths = new int[] { 75, 70, 320, 140, 50 };
    enrollLayout.rowHeights = new int[] { 25, 25, 20, 25, 180 };
    setLayout(enrollLayout);

    this.btnStart = new JButton("Start");
    this.btnStart.addActionListener(this);

    this.btnCancel = new JButton("Cancel");
    this.btnCancel.setEnabled(false);
    this.btnCancel.addActionListener(this);

    initializePropertiesPanel();

    this.lblRemaining = new JLabel("Estimated time remaining:");
    this.lblProgress = new JLabel("progress", 4);
    this.progressBar = new JProgressBar(0, 100);

    this.gridBagUtils.addToGridBagLayout(0, 0, this, this.btnStart);
    this.gridBagUtils.addToGridBagLayout(0, 1, this, this.btnCancel);
    this.gridBagUtils.addToGridBagLayout(1, 0, 2, 2, this, this.panelProperties);
    this.gridBagUtils.addToGridBagLayout(0, 2, 3, 1, this, this.lblRemaining);
    this.gridBagUtils.addToGridBagLayout(3, 2, 1, 1, 1, 0, this, new JLabel());
    this.gridBagUtils.addToGridBagLayout(4, 2, 1, 1, 0, 0, this, this.lblProgress);
    this.gridBagUtils.addToGridBagLayout(0, 3, 5, 1, this, this.progressBar);
    this.gridBagUtils.addToGridBagLayout(0, 4, 5, 1, 0, 1, this, initializeResultsPanel());
  }


  private void initializePropertiesPanel() {
    this.panelProperties = new JPanel();
    this.panelProperties.setBorder(BorderFactory.createTitledBorder("Properties"));
    GridBagLayout propertiesPanelLayout = new GridBagLayout();
    propertiesPanelLayout.columnWidths = new int[] { 100, 125, 125 };
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
    resultsPanelLayout.columnWidths = new int[] { 55, 45, 110, 80, 135, 240 };
    resultsPanelLayout.rowHeights = new int[] { 25, 50, 75 };
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

  private void startEnrolling() {
    try {
      if (isBusy()) {
        return;
      }
      setStatus("Preparing...", Color.BLACK, (Icon)null);
      appendStatus(String.format("Enrolling from: %s\r\n", new Object[] { getTemplateLoader() }));
      this.progressBar.setValue(0);


      getBiometricClient().getCount();

      int templateCount = getTemplateCount();
      this.progressBar.setMaximum(templateCount);
      this.txtTemplatesCount.setText(String.valueOf(templateCount));

      this.lblStatusIcon.setIcon(null);
      this.txtTimeElapsed.setText("N/A");

      this.enrollmentTaskSender.setBunchSize(((Integer)this.spinnerBunchSize.getValue()).intValue());
      this.enrollmentTaskSender.setBiometricClient(getBiometricClient());
      this.enrollmentTaskSender.setTemplateLoader(getTemplateLoader());

      this.startTime = System.currentTimeMillis();
      this.enrollmentTaskSender.start();
      enableControls(false);
    } catch (Exception e) {
      MessageUtils.showError(this, e);
      setStatus("Enrollment failed due to: " + e.toString(), Color.RED.darker(), this.iconError);
    }
  }

  private void enrollPanelLoaded() {
    this.enrollmentTaskSender = new TaskSender(getBiometricClient(), getTemplateLoader(), NBiometricOperation.ENROLL);
    this.taskListener = new TaskListener()
      {
        public void taskFinished() {
          EnrollPanel.this.taskSenderFinished();
        }

        public void taskErrorOccured(Exception e) {
          EnrollPanel.this.taskSenderExceptionOccured(e);
        }

        public void taskProgressChanged(int completed) {
          EnrollPanel.this.taskSenderProgressChanged(completed);
        }



        public void matchingTaskCompleted(NBiometricTask task) {}
      };
    this.enrollmentTaskSender.addTaskListener(this.taskListener);

    this.lblProgress.setText("");
    this.lblRemaining.setText("");
    this.progressBar.setValue(0);
  }

  private void taskSenderProgressChanged(int numberOfTasksCompleted) {
    int currentProgress = this.progressBar.getValue();
    int progressValue = (numberOfTasksCompleted < this.progressBar.getMaximum()) ? numberOfTasksCompleted : this.progressBar.getMaximum();
    for (int i = currentProgress; i <= progressValue; i++) {
      this.progressBar.setValue(i);
    }

    long elapsed = System.currentTimeMillis() - this.startTime;
    this.lblProgress.setText(String.format("%s / %s", new Object[] { Integer.valueOf(numberOfTasksCompleted), this.txtTemplatesCount.getText() }));
    this.txtTimeElapsed.setText(String.format("%.2f s", new Object[] { Double.valueOf((elapsed / 1000L)) }));

    long remaining = elapsed / numberOfTasksCompleted * (this.progressBar.getMaximum() - numberOfTasksCompleted);
    if (remaining / 1000L < 0L) {
      remaining = 0L;
    }

    long days = TimeUnit.MILLISECONDS.toDays(remaining);
    long hr = TimeUnit.MILLISECONDS.toHours(remaining - TimeUnit.DAYS.toMillis(days));
    long min = TimeUnit.MILLISECONDS.toMinutes(remaining - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hr));
    long sec = TimeUnit.MILLISECONDS.toSeconds(remaining - TimeUnit.DAYS.toMillis(days) - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
    this.lblRemaining.setText(String.format("Estimated time remaining: %02d.%02d:%02d:%02d", new Object[] { Long.valueOf(days), Long.valueOf(hr), Long.valueOf(min), Long.valueOf(sec) }));
  }

  private void taskSenderExceptionOccured(Exception e) {
    appendStatus(String.format("%s\r\n", new Object[] { e }), Color.RED.darker());
  }

  private void taskSenderFinished() {
    enableControls(true);
    long elapsed = this.enrollmentTaskSender.getElapsedTime();

    long hr = TimeUnit.MILLISECONDS.toHours(elapsed);
    long min = TimeUnit.MILLISECONDS.toMinutes(elapsed - TimeUnit.HOURS.toMillis(hr));
    long sec = TimeUnit.MILLISECONDS.toSeconds(elapsed - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
    this.txtTimeElapsed.setText(String.format("%02d:%02d:%02d", new Object[] { Long.valueOf(hr), Long.valueOf(min), Long.valueOf(sec) }));
    this.lblRemaining.setText("");
    this.lblProgress.setText("");
    if (this.enrollmentTaskSender.isSuccessful()) {
      appendStatus("\r\nEnrollment successful", this.txtStatus.getForeground());
      this.lblStatusIcon.setIcon(this.iconOk);
    } else if (this.enrollmentTaskSender.isCanceled()) {
      appendStatus("\r\nEnrollment canceled", Color.RED.darker());
      this.lblStatusIcon.setIcon(this.iconError);
      this.btnStart.setEnabled(true);
      this.progressBar.setValue(0);
    } else {
      appendStatus("\r\nEnrollment finished with errors", this.txtStatus.getForeground());
      this.lblStatusIcon.setIcon(this.iconError);
    }
  }

  private void enableControls(boolean isIdle) {
    this.btnStart.setEnabled(isIdle);
    this.btnCancel.setEnabled(!isIdle);

    for (Component c : this.panelProperties.getComponents()) {
      c.setEnabled(isIdle);
    }
  }

  private void setStatus(String msg, Color color, Icon icon) {
    this.txtStatus.setForeground(color);
    this.txtStatus.setText(msg);
    this.lblStatusIcon.setIcon(icon);
  }

  private void appendStatus(String msg) {
    appendStatus(msg, Color.BLACK);
  }

  private void appendStatus(String msg, Color color) {
    this.txtStatus.setText(this.txtStatus.getText() + msg);
    this.txtStatus.setForeground(color);
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
    }
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\controls\EnrollPanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */