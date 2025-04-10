package com.neurotec.samples.server.controls;

import com.neurotec.samples.server.LongTask;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;






public final class LongTaskDialog
  extends JDialog
{
  private static final long serialVersionUID = 1L;

  public static Object runLongTask(Frame owner, String title, LongTask longTask) throws InterruptedException, ExecutionException {
    LongTaskDialog frmLongTask = new LongTaskDialog(owner, title, longTask);
    frmLongTask.setLocationRelativeTo(owner);
    frmLongTask.setVisible(true);
    return frmLongTask.backgroundWorker.get();
  }





  private final BackgroundWorker backgroundWorker = new BackgroundWorker();



  private LongTask longTask;


  private JLabel lblTitle;


  private JProgressBar progressBar;



  private LongTaskDialog(Frame owner, String text, LongTask longTask) {
    super(owner, "Working", true);
    setPreferredSize(new Dimension(375, 100));
    setResizable(false);
    initializeComponents();

    this.lblTitle.setText(text);
    this.longTask = longTask;
    addComponentListener(new ComponentAdapter()
        {
          public void componentShown(ComponentEvent e) {
            LongTaskDialog.this.backgroundWorker.execute();
          }
        });
  }





  private void initializeComponents() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, 1));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    this.lblTitle = new JLabel("Working...");
    this.lblTitle.setAlignmentX(0.0F);

    this.progressBar = new JProgressBar(0, 100);
    this.progressBar.setPreferredSize(new Dimension(345, 25));
    this.progressBar.setMinimumSize(new Dimension(345, 25));
    this.progressBar.setAlignmentX(0.0F);
    this.progressBar.setIndeterminate(true);

    mainPanel.add(Box.createVerticalStrut(8));
    mainPanel.add(this.lblTitle);
    mainPanel.add(this.progressBar);
    mainPanel.add(Box.createVerticalGlue());

    getContentPane().add(mainPanel);
    pack();
  }




  private final class BackgroundWorker
    extends SwingWorker<Object, Object>
  {
    private BackgroundWorker() {}




    protected Object doInBackground() {
      return LongTaskDialog.this.longTask.doInBackground();
    }


    protected void done() {
      LongTaskDialog.this.dispose();
    }
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\controls\LongTaskDialog.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */