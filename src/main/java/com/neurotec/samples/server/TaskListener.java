package com.neurotec.samples.server;

import com.neurotec.biometrics.NBiometricTask;
import java.util.EventListener;

public interface TaskListener extends EventListener {
  void taskFinished();
  
  void taskErrorOccured(Exception paramException);
  
  void taskProgressChanged(int paramInt);
  
  void matchingTaskCompleted(NBiometricTask paramNBiometricTask);
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\TaskListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */