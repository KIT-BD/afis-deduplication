package com.neurotec.samples.server;

import com.neurotec.biometrics.NBiometricOperation;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NBiometricTask;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.samples.server.connection.TemplateLoader;
import com.neurotec.samples.server.controls.LongTaskDialog;
import com.neurotec.samples.server.settings.Settings;
import com.neurotec.samples.server.util.PropertyLoader;
import com.neurotec.util.concurrent.CompletionHandler;

import java.awt.Frame;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;


public final class TaskSender {
    private Object lock = new Object();
    private int tasksSentCount = 0;
    private int tasksCompletedCount = 0;

    private BackgroundWorker worker = null;

    private NBiometricOperation operation;
    private boolean isBackgroundWorkerCompleted;
    private long startTime = -1L;
    private long stopTime = -1L;

    private int bunchSize = 350;
    private int maxActiveTaskCount = 100;

    private boolean canceled;

    private boolean successful;
    private boolean sendOneBunchOnly;
    private TaskListener taskListener;
    private TemplateLoader templateLoader = null;
    private NBiometricClient biometricClient = null;


    public TaskSender(NBiometricClient biometricClient, TemplateLoader templateLoader, NBiometricOperation operation) {
        if (biometricClient == null) throw new NullPointerException("biometricClient");
        if (templateLoader == null) throw new NullPointerException("templateLoader");
        this.templateLoader = templateLoader;
        this.biometricClient = biometricClient;
        this.operation = operation;
    }

    private void fireExceptionOccuredEvent(Exception e) {
        this.successful = false;
        this.taskListener.taskErrorOccurred(e);
    }

    public void setBunchSize(int bunchSize) {
        this.bunchSize = bunchSize;
    }

    public int getBunchSize() {
        return this.bunchSize;
    }

    public boolean isSuccessful() {
        return this.successful;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public void addTaskListener(TaskListener l) {
        this.taskListener = l;
    }

    public void removeTaskListener(TaskListener l) {
        this.taskListener = null;
    }

    public boolean isBusy() {
        if (this.worker == null) {
            return false;
        }
        return (this.worker.getState() == SwingWorker.StateValue.STARTED || this.tasksSentCount - this.tasksCompletedCount > 0);
    }

    public void setSendOneBunchOnly(boolean sendOneBunchOnly) {
        this.sendOneBunchOnly = sendOneBunchOnly;
    }

    public boolean isSendOneBunchOnly() {
        return this.sendOneBunchOnly;
    }

    public int getPerformedTaskCount() {
        return this.tasksCompletedCount;
    }

    public long getElapsedTime() {
        if (this.startTime < 0L) {
            return 0L;
        }
        if (this.stopTime < 0L) {
            return System.currentTimeMillis() - this.startTime;
        }
        return this.stopTime - this.startTime;
    }

    public final TemplateLoader getTemplateLoader() {
        return this.templateLoader;
    }

    public final void setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }

    public final NBiometricClient getBiometricClient() {
        return this.biometricClient;
    }

    public final void setBiometricClient(NBiometricClient biometricClient) {
        this.biometricClient = biometricClient;
    }

    public void start() throws IllegalAccessException {
        if (isBusy()) {
            throw new IllegalAccessException("Already started");
        }
        Settings settings = PropertyLoader.getSettings();
        this.tasksSentCount = 0;
        this.tasksCompletedCount = 0;
        this.isBackgroundWorkerCompleted = false;
        this.successful = true;
        this.canceled = false;
        this.maxActiveTaskCount = 1000;
        this.biometricClient.setMatchingThreshold(settings.getMatchingThreshold());
        this.worker = new BackgroundWorker();
        this.worker.execute();
    }

    public void cancel() {
        this.canceled = true;
        this.biometricClient.cancel();
        this.successful = false;
    }

    public void waitForCurrentProcessToFinish(Frame owner) {
        try {
            LongTaskDialog.runLongTask(owner, "Waiting to finish..", new WaitTask());
        } catch (InterruptedException | ExecutionException e) {
            e.fillInStackTrace();
        }
    }


    private class WaitTask
            implements LongTask {
        private WaitTask() {
        }

        public Object doInBackground() {
            try {
                TaskSender.this.worker.get();
                while (TaskSender.this.tasksSentCount > TaskSender.this.tasksCompletedCount) {
                    Thread.sleep(200L);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.fillInStackTrace();
            }
            return 0;
        }
    }


    private class BackgroundWorker
            extends SwingWorker<Boolean, Object> {
        protected Boolean doInBackground() {
            TaskSender.this.startTime = System.currentTimeMillis();
            TaskSender.this.stopTime = -1L;
            try {
                TaskSender.this.templateLoader.beginLoad();
            } catch (Exception e) {
                publish(new Object[]{e});
                return null;
            }

            try {
                while (!TaskSender.this.canceled) {
                    NSubject[] subjects = TaskSender.this.templateLoader.loadNext(TaskSender.this.bunchSize);
                    if (subjects == null || subjects.length == 0) {
                        break;
                    }

                    // Log the batch progress
                    System.out.println("Processing batch of " + subjects.length + " templates. Total processed: " + TaskSender.this.tasksSentCount);

                    if (TaskSender.this.operation == NBiometricOperation.IDENTIFY) {
                        for (NSubject subject : subjects) {
                            while (TaskSender.this.tasksSentCount - TaskSender.this.tasksCompletedCount > TaskSender.this.maxActiveTaskCount && !TaskSender.this.canceled) {
                                Thread.sleep(200L);
                            }
                            if (TaskSender.this.canceled) {
                                break;
                            }
                            NBiometricTask task = TaskSender.this.biometricClient.createTask(EnumSet.of(TaskSender.this.operation), subject);
                            TaskSender.this.biometricClient.performTask(task, null, new TaskSender.TaskCompletionHandler());
                            subject.dispose();
                            task.dispose();
                            synchronized (TaskSender.this.lock) {
                                TaskSender.this.tasksSentCount++;
                            }
                        }
                    } else {
                        while (TaskSender.this.tasksSentCount - TaskSender.this.tasksCompletedCount > TaskSender.this.maxActiveTaskCount && !TaskSender.this.canceled) {
                            Thread.sleep(200L);
                        }
                        if (TaskSender.this.canceled) {
                            break;
                        }
                        NBiometricTask task = TaskSender.this.biometricClient.createTask(EnumSet.of(TaskSender.this.operation), null);
                        for (NSubject subject : subjects) {
                            task.getSubjects().add(subject);
                            subject.dispose();
                        }
                        TaskSender.this.biometricClient.performTask(task, null, new TaskSender.TaskCompletionHandler());
                        task.dispose();
                        synchronized (TaskSender.this.lock) {
                            TaskSender.this.tasksSentCount = TaskSender.this.tasksSentCount + subjects.length;
                        }
                    }
                    if (TaskSender.this.sendOneBunchOnly) {
                        break;
                    }
                }
                while (TaskSender.this.tasksCompletedCount < TaskSender.this.tasksSentCount) {
                    Thread.sleep(200L);
                }
                TaskSender.this.stopTime = System.currentTimeMillis();
                TaskSender.this.taskListener.taskFinished();
            } catch (Exception e) {
                e.printStackTrace();
                publish(new Object[]{e});
            }

            try {
                TaskSender.this.templateLoader.endLoad();
            } catch (Exception e) {
                e.printStackTrace();
                publish(new Object[]{e});
            }
            return Boolean.valueOf(true);
        }

        protected void done() {
            super.done();
            TaskSender.this.isBackgroundWorkerCompleted = true;
        }

        protected void process(List<Object> objects) {
            TaskSender.this.successful = false;
            if (objects.size() == 1 && objects.get(0) instanceof Exception) {
                TaskSender.this.fireExceptionOccuredEvent((Exception) objects.get(0));
            }
        }
    }

    private final class TaskCompletionHandler
            implements CompletionHandler<NBiometricTask, Object> {
        private TaskCompletionHandler() {
        }

        public void completed(NBiometricTask task, Object attachment) {
            synchronized (TaskSender.this.taskListener) {
                if (task.getError() != null) {
                    TaskSender.this.successful = false;
                    TaskSender.this.taskListener.taskErrorOccurred(new Exception(task.getError()));
                } else if (task.getOperations().contains(NBiometricOperation.IDENTIFY)) {
                    if (task.getStatus() == NBiometricStatus.OK || task.getStatus() == NBiometricStatus.MATCH_NOT_FOUND) {
                        try {
                            TaskSender.this.taskListener.matchingTaskCompleted(task);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (task.getOperations().contains(NBiometricOperation.ENROLL)) {
                    switch (task.getStatus()) {
                        case DUPLICATE_ID:
                            TaskSender.this.successful = false;
                            TaskSender.this.taskListener.taskErrorOccurred(new Exception("Duplicate ID Found"));
                            break;
                    }


                }
                TaskSender.this.tasksCompletedCount = TaskSender.this.tasksCompletedCount + task.getSubjects().size();
                TaskSender.this.taskListener.taskProgressChanged(TaskSender.this.tasksCompletedCount);
                task.dispose();
            }
        }

        public void failed(Throwable exc, Object attachment) {
            synchronized (TaskSender.this.taskListener) {
                TaskSender.this.successful = false;
                TaskSender.this.tasksCompletedCount++;
                TaskSender.this.taskListener.taskProgressChanged(TaskSender.this.tasksCompletedCount);
                TaskSender.this.taskListener.taskErrorOccurred((Exception) exc);
                if (TaskSender.this.tasksCompletedCount == TaskSender.this.tasksSentCount && TaskSender.this.isBackgroundWorkerCompleted) {
                    TaskSender.this.stopTime = System.currentTimeMillis();
                    TaskSender.this.taskListener.taskFinished();
                }
            }
        }
    }
}