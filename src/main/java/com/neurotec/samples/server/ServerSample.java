package com.neurotec.samples.server;

import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.util.LibraryManager;
import com.neurotec.samples.util.Utils;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.SwingUtilities;



public final class ServerSample
{
  public static void main(String[] args) {
    Utils.setupLookAndFeel();
    LibraryManager.initLibraryPath();
    
    SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              MainFrame frame = new MainFrame();
              Dimension d = new Dimension(935, 450);
              
              frame.setSize(d);
              frame.setMinimumSize(d);
              frame.setPreferredSize(d);
              
              frame.setResizable(true);
              frame.setDefaultCloseOperation(2);
              frame.setTitle("Server Sample");
              frame.setLocationRelativeTo((Component)null);
              frame.showMainFrame();
            } catch (Exception e) {
              e.printStackTrace();
              MessageUtils.showError(null, e);
            } 
          }
        });
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\ServerSample.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */