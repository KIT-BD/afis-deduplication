package com.neurotec.samples.server;

import com.neurotec.biometrics.NBiometricTask;

import java.util.EventListener;

public interface TaskListener extends EventListener {
    void taskFinished();

    void taskErrorOccurred(Exception paramException);

    void taskProgressChanged(int paramInt);

    void matchingTaskCompleted(NBiometricTask paramNBiometricTask);
}
