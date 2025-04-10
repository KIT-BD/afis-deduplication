/*     */ package com.neurotec.samples.server;
/*     */ 
/*     */ import com.neurotec.biometrics.NBiometricOperation;
/*     */ import com.neurotec.biometrics.NBiometricStatus;
/*     */ import com.neurotec.biometrics.NBiometricTask;
/*     */ import com.neurotec.biometrics.NSubject;
/*     */ import com.neurotec.biometrics.client.NBiometricClient;
/*     */ import com.neurotec.samples.server.connection.TemplateLoader;
/*     */ import com.neurotec.samples.server.controls.LongTaskDialog;
/*     */ import com.neurotec.samples.server.settings.Settings;
/*     */ import com.neurotec.util.concurrent.CompletionHandler;
/*     */ import java.awt.Frame;
/*     */ import java.util.EnumSet;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.SwingWorker;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class TaskSender
/*     */ {
/*  26 */   private Object lock = new Object();
/*  27 */   private int tasksSentCount = 0;
/*  28 */   private int tasksCompletedCount = 0;
/*     */   
/*  30 */   private BackgroundWorker worker = null;
/*     */   
/*     */   private NBiometricOperation operation;
/*     */   private boolean isBackgroundWorkerCompleted;
/*  34 */   private long startTime = -1L;
/*  35 */   private long stopTime = -1L;
/*     */   
/*  37 */   private int bunchSize = 350;
/*  38 */   private int maxActiveTaskCount = 100;
/*     */   
/*     */   private boolean canceled;
/*     */   
/*     */   private boolean successful;
/*     */   private boolean sendOneBunchOnly;
/*     */   private TaskListener taskListener;
/*  45 */   private TemplateLoader templateLoader = null;
/*  46 */   private NBiometricClient biometricClient = null;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public TaskSender(NBiometricClient biometricClient, TemplateLoader templateLoader, NBiometricOperation operation) {
/*  53 */     if (biometricClient == null) throw new NullPointerException("biometricClient"); 
/*  54 */     if (templateLoader == null) throw new NullPointerException("templateLoader"); 
/*  55 */     this.templateLoader = templateLoader;
/*  56 */     this.biometricClient = biometricClient;
/*  57 */     this.operation = operation;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void fireExceptionOccuredEvent(Exception e) {
/*  65 */     this.successful = false;
/*  66 */     this.taskListener.taskErrorOccured(e);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setBunchSize(int bunchSize) {
/*  74 */     this.bunchSize = bunchSize;
/*     */   }
/*     */   
/*     */   public int getBunchSize() {
/*  78 */     return this.bunchSize;
/*     */   }
/*     */   
/*     */   public boolean isSuccessful() {
/*  82 */     return this.successful;
/*     */   }
/*     */   
/*     */   public boolean isCanceled() {
/*  86 */     return this.canceled;
/*     */   }
/*     */   
/*     */   public void addTaskListener(TaskListener l) {
/*  90 */     this.taskListener = l;
/*     */   }
/*     */   
/*     */   public void removeTaskListener(TaskListener l) {
/*  94 */     this.taskListener = null;
/*     */   }
/*     */   
/*     */   public boolean isBusy() {
/*  98 */     if (this.worker == null) {
/*  99 */       return false;
/*     */     }
/* 101 */     return (this.worker.getState() == SwingWorker.StateValue.STARTED || this.tasksSentCount - this.tasksCompletedCount > 0);
/*     */   }
/*     */   
/*     */   public void setSendOneBunchOnly(boolean sendOneBunchOnly) {
/* 105 */     this.sendOneBunchOnly = sendOneBunchOnly;
/*     */   }
/*     */   
/*     */   public boolean isSendOneBunchOnly() {
/* 109 */     return this.sendOneBunchOnly;
/*     */   }
/*     */   
/*     */   public int getPerformedTaskCount() {
/* 113 */     return this.tasksCompletedCount;
/*     */   }
/*     */   
/*     */   public long getElapsedTime() {
/* 117 */     if (this.startTime < 0L) {
/* 118 */       return 0L;
/*     */     }
/* 120 */     if (this.stopTime < 0L) {
/* 121 */       return System.currentTimeMillis() - this.startTime;
/*     */     }
/* 123 */     return this.stopTime - this.startTime;
/*     */   }
/*     */   
/*     */   public final TemplateLoader getTemplateLoader() {
/* 127 */     return this.templateLoader;
/*     */   }
/*     */   
/*     */   public final void setTemplateLoader(TemplateLoader templateLoader) {
/* 131 */     this.templateLoader = templateLoader;
/*     */   }
/*     */   
/*     */   public final NBiometricClient getBiometricClient() {
/* 135 */     return this.biometricClient;
/*     */   }
/*     */   
/*     */   public final void setBiometricClient(NBiometricClient biometricClient) {
/* 139 */     this.biometricClient = biometricClient;
/*     */   }
/*     */   
/*     */   public void start() throws IllegalAccessException {
/* 143 */     if (isBusy()) {
/* 144 */       throw new IllegalAccessException("Already started");
/*     */     }
/* 146 */     Settings settings = Settings.getInstance();
/* 147 */     this.tasksSentCount = 0;
/* 148 */     this.tasksCompletedCount = 0;
/* 149 */     this.isBackgroundWorkerCompleted = false;
/* 150 */     this.successful = true;
/* 151 */     this.canceled = false;
/* 152 */     this.maxActiveTaskCount = 1000;
/* 153 */     this.biometricClient.setMatchingThreshold(settings.getMatchingThreshold());
/* 154 */     this.worker = new BackgroundWorker();
/* 155 */     this.worker.execute();
/*     */   }
/*     */   
/*     */   public void cancel() {
/* 159 */     this.canceled = true;
/* 160 */     this.biometricClient.cancel();
/* 161 */     this.successful = false;
/*     */   }
/*     */   
/*     */   public void waitForCurrentProcessToFinish(Frame owner) {
/*     */     try {
/* 166 */       LongTaskDialog.runLongTask(owner, "Waiting to finish..", new WaitTask());
/* 167 */     } catch (InterruptedException e) {
/* 168 */       e.printStackTrace();
/* 169 */     } catch (ExecutionException e) {
/* 170 */       e.printStackTrace();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private class WaitTask
/*     */     implements LongTask
/*     */   {
/*     */     private WaitTask() {}
/*     */     
/*     */     public Object doInBackground() {
/*     */       try {
/* 182 */         TaskSender.this.worker.get();
/* 183 */         while (TaskSender.this.tasksSentCount > TaskSender.this.tasksCompletedCount) {
/* 184 */           Thread.sleep(200L);
/*     */         }
/* 186 */       } catch (InterruptedException e) {
/* 187 */         e.printStackTrace();
/* 188 */       } catch (ExecutionException e) {
/* 189 */         e.printStackTrace();
/*     */       } 
/* 191 */       return Integer.valueOf(0);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private class BackgroundWorker
/*     */     extends SwingWorker<Boolean, Object>
/*     */   {
/*     */     protected Boolean doInBackground() {
/* 210 */       TaskSender.this.startTime = System.currentTimeMillis();
/* 211 */       TaskSender.this.stopTime = -1L;
/*     */       try {
/* 213 */         TaskSender.this.templateLoader.beginLoad();
/* 214 */       } catch (Exception e) {
/* 215 */         publish(new Object[] { e });
/* 216 */         return null;
/*     */       } 
/*     */       
/*     */       try {
/* 220 */         while (!TaskSender.this.canceled) {
/* 221 */           NSubject[] subjects = TaskSender.this.templateLoader.loadNext(TaskSender.this.bunchSize);
/* 222 */           if (subjects == null || subjects.length == 0) {
/*     */             break;
/*     */           }
/*     */           
/* 226 */           if (TaskSender.this.operation == NBiometricOperation.IDENTIFY) {
/* 227 */             for (NSubject subject : subjects) {
/* 228 */               while (TaskSender.this.tasksSentCount - TaskSender.this.tasksCompletedCount > TaskSender.this.maxActiveTaskCount && !TaskSender.this.canceled) {
/* 229 */                 Thread.sleep(200L);
/*     */               }
/* 231 */               if (TaskSender.this.canceled) {
/*     */                 break;
/*     */               }
/* 234 */               NBiometricTask task = TaskSender.this.biometricClient.createTask(EnumSet.of(TaskSender.this.operation), subject);
/* 235 */               TaskSender.this.biometricClient.performTask(task, null, new TaskSender.TaskCompletionHandler());
/* 236 */               subject.dispose();
/* 237 */               task.dispose();
/* 238 */               synchronized (TaskSender.this.lock) {
/* 239 */                 TaskSender.this.tasksSentCount++;
/*     */               } 
/*     */             } 
/*     */           } else {
/* 243 */             while (TaskSender.this.tasksSentCount - TaskSender.this.tasksCompletedCount > TaskSender.this.maxActiveTaskCount && !TaskSender.this.canceled) {
/* 244 */               Thread.sleep(200L);
/*     */             }
/* 246 */             if (TaskSender.this.canceled) {
/*     */               break;
/*     */             }
/* 249 */             NBiometricTask task = TaskSender.this.biometricClient.createTask(EnumSet.of(TaskSender.this.operation), null);
/* 250 */             for (NSubject subject : subjects) {
/* 251 */               task.getSubjects().add(subject);
/* 252 */               subject.dispose();
/*     */             } 
/* 254 */             TaskSender.this.biometricClient.performTask(task, null, new TaskSender.TaskCompletionHandler());
/* 255 */             task.dispose();
/* 256 */             synchronized (TaskSender.this.lock) {
/* 257 */               TaskSender.this.tasksSentCount = TaskSender.this.tasksSentCount + subjects.length;
/*     */             } 
/*     */           } 
/* 260 */           if (TaskSender.this.sendOneBunchOnly) {
/*     */             break;
/*     */           }
/*     */         } 
/* 264 */         while (TaskSender.this.tasksCompletedCount < TaskSender.this.tasksSentCount) {
/* 265 */           Thread.sleep(200L);
/*     */         }
/* 267 */         TaskSender.this.stopTime = System.currentTimeMillis();
/* 268 */         TaskSender.this.taskListener.taskFinished();
/* 269 */       } catch (Exception e) {
/* 270 */         e.printStackTrace();
/* 271 */         publish(new Object[] { e });
/*     */       } 
/*     */       
/*     */       try {
/* 275 */         TaskSender.this.templateLoader.endLoad();
/* 276 */       } catch (Exception e) {
/* 277 */         e.printStackTrace();
/* 278 */         publish(new Object[] { e });
/*     */       } 
/* 280 */       return Boolean.valueOf(true);
/*     */     }
/*     */ 
/*     */     
/*     */     protected void done() {
/* 285 */       super.done();
/* 286 */       TaskSender.this.isBackgroundWorkerCompleted = true;
/*     */     }
/*     */ 
/*     */     
/*     */     protected void process(List<Object> objects) {
/* 291 */       TaskSender.this.successful = false;
/* 292 */       if (objects.size() == 1 && objects.get(0) instanceof Exception) {
/* 293 */         TaskSender.this.fireExceptionOccuredEvent((Exception)objects.get(0));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private final class TaskCompletionHandler
/*     */     implements CompletionHandler<NBiometricTask, Object>
/*     */   {
/*     */     private TaskCompletionHandler() {}
/*     */ 
/*     */     
/*     */     public void completed(NBiometricTask task, Object attachment) {
/* 306 */       synchronized (TaskSender.this.taskListener) {
/* 307 */         if (task.getError() != null) {
/* 308 */           TaskSender.this.successful = false;
/* 309 */           TaskSender.this.taskListener.taskErrorOccured(new Exception(task.getError()));
/*     */         }
/* 311 */         else if (task.getOperations().contains(NBiometricOperation.IDENTIFY)) {
/* 312 */           if (task.getStatus() == NBiometricStatus.OK || task.getStatus() == NBiometricStatus.MATCH_NOT_FOUND) {
/*     */             try {
/* 314 */               TaskSender.this.taskListener.matchingTaskCompleted(task);
/* 315 */             } catch (Exception e) {
/* 316 */               e.printStackTrace();
/*     */             }
/*     */           
/*     */           }
/* 320 */         } else if (task.getOperations().contains(NBiometricOperation.ENROLL)) {
/* 321 */           switch (task.getStatus()) {
/*     */             case DUPLICATE_ID:
/* 323 */               TaskSender.this.successful = false;
/* 324 */               TaskSender.this.taskListener.taskErrorOccured(new Exception("Duplicate ID Found"));
/*     */               break;
/*     */           } 
/*     */ 
/*     */         
/*     */         } 
/* 330 */         TaskSender.this.tasksCompletedCount = TaskSender.this.tasksCompletedCount + task.getSubjects().size();
/* 331 */         TaskSender.this.taskListener.taskProgressChanged(TaskSender.this.tasksCompletedCount);
/* 332 */         task.dispose();
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/*     */     public void failed(Throwable exc, Object attachment) {
/* 338 */       synchronized (TaskSender.this.taskListener) {
/* 339 */         TaskSender.this.successful = false;
/* 340 */         TaskSender.this.tasksCompletedCount++;
/* 341 */         TaskSender.this.taskListener.taskProgressChanged(TaskSender.this.tasksCompletedCount);
/* 342 */         TaskSender.this.taskListener.taskErrorOccured((Exception)exc);
/* 343 */         if (TaskSender.this.tasksCompletedCount == TaskSender.this.tasksSentCount && TaskSender.this.isBackgroundWorkerCompleted) {
/* 344 */           TaskSender.this.stopTime = System.currentTimeMillis();
/* 345 */           TaskSender.this.taskListener.taskFinished();
/*     */         } 
/*     */       } 
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\TaskSender.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */