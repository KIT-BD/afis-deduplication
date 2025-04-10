package com.neurotec.samples.server.controls;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.samples.server.LongTask;
import com.neurotec.samples.server.connection.TemplateLoader;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;

public abstract class BasePanel
  extends JPanel
  implements ActionListener
{
  private static final long serialVersionUID = 1L;
  private Frame owner;
  private NBiometricClient biometricClient;
  private TemplateLoader templateLoader;

  public BasePanel(Frame owner) {
    this.owner = owner;
  }

  public final Frame getOwner() {
    return this.owner;
  }

  public final int getTemplateCount() throws InterruptedException, ExecutionException {
    return ((Integer)LongTaskDialog.runLongTask(this.owner, "Calculating template count", new TemplateCounter())).intValue();
  }

  public final NBiometricClient getBiometricClient() {
    return this.biometricClient;
  }

  public final void setBiometricClient(NBiometricClient biometricClient) {
    this.biometricClient = biometricClient;
  }

  public final TemplateLoader getTemplateLoader() {
    return this.templateLoader;
  }

  public abstract boolean isBusy();

  public abstract void cancel();

  public final void setTemplateLoader(TemplateLoader templateLoader) {
    this.templateLoader = templateLoader;
  }

  public abstract String getTitle();

  public abstract void waitForCurrentProcessToFinish() throws InterruptedException, ExecutionException;

  private class TemplateCounter
    implements LongTask {
    public Object doInBackground() {
      int result = 0;
      try {
        if (BasePanel.this.templateLoader != null) {
          System.out.println("Retrieving template count using: " + BasePanel.this.templateLoader.getClass().getName());
          result = BasePanel.this.templateLoader.getTemplateCount();
          System.out.println("Template count: " + result);
        } else {
          System.out.println("Template loader is null");
          result = -1;
        }
      } catch (Exception e) {
        System.err.println("Error retrieving template count: " + e.getMessage());
        e.printStackTrace();
        // Return a meaningful error value
        result = -1;
      }
      return Integer.valueOf(result);
    }

    private TemplateCounter() {}
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\controls\BasePanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */