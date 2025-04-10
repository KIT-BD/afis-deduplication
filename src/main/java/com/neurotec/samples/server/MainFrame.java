/*     */ package com.neurotec.samples.server;
/*     */ 
/*     */ import com.neurotec.biometrics.client.NBiometricClient;
/*     */ import com.neurotec.biometrics.client.NClusterBiometricConnection;
/*     */ import com.neurotec.samples.server.connection.DatabaseConnection;
/*     */ import com.neurotec.samples.server.connection.DirectoryEnumerator;
/*     */ import com.neurotec.samples.server.connection.TemplateLoader;
/*     */ import com.neurotec.samples.server.controls.BasePanel;
/*     */ import com.neurotec.samples.server.controls.DeduplicationPanel;
/*     */ import com.neurotec.samples.server.controls.EnrollPanel;
/*     */ import com.neurotec.samples.server.controls.TestSpeedPanel;
/*     */ import com.neurotec.samples.server.settings.ConnectionSettingsDialog;
/*     */ import com.neurotec.samples.server.settings.MatchingSettingsPanel;
/*     */ import com.neurotec.samples.server.settings.Settings;
/*     */ import com.neurotec.samples.server.util.MessageUtils;
/*     */ import com.neurotec.samples.util.Utils;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.ExecutionException;
/*     */ import javax.swing.BorderFactory;
/*     */ import javax.swing.Box;
/*     */ import javax.swing.BoxLayout;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.border.Border;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class MainFrame
/*     */   extends JFrame
/*     */   implements ActionListener
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private static final String SAMPLE_TITLE = "Server Sample";
/*  52 */   private static final Color SELECTED_BUTTON_COLOR = Color.DARK_GRAY;
/*  53 */   private static final Color NOT_SELECTED_BUTTON_COLOR = new Color(162, 181, 205);
/*     */   private TemplateLoader templateLoader;
/*     */   private NBiometricClient biometricClient;
/*     */   private NClusterBiometricConnection biometricConnection;
/*     */   
/*     */   public enum Task
/*     */   {
/*  60 */     DEDUPLICATION(0), ENROLL(1), SPEED_TEST(2), SETTINGS(3);
/*     */     
/*     */     private int value;
/*     */     
/*     */     Task(int value) {
/*  65 */       this.value = value;
/*     */     }
/*     */     
/*     */     public int value() {
/*  69 */       return this.value;
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
/*  81 */   private Settings settings = Settings.getInstance();
/*  82 */   private Dimension buttonSize = new Dimension(185, 40);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  88 */   private final JButton[] mainFrameButtons = new JButton[5];
/*  89 */   private final List<BasePanel> panels = new ArrayList<>();
/*     */   
/*     */   private JButton btnConnection;
/*     */   
/*     */   private JButton btnDeduplication;
/*     */   
/*     */   private JButton btnEnroll;
/*     */   
/*     */   private JButton btnTestSpeed;
/*     */   
/*     */   private JButton btnMatchingSettings;
/*     */   private JPanel panelCardLayoutContainer;
/*     */   private JPanel panelLeft;
/*     */   private CardLayout cardLayoutTaskPanels;
/*     */   private JLabel lblServerAddress;
/*     */   private JLabel lblClientPortValue;
/*     */   private JLabel lblAdminPortValue;
/*     */   private JLabel lblSource;
/*     */   private JLabel lblSourceValue;
/*     */   private JLabel lblDSN;
/*     */   private JLabel lblDSNValue;
/*     */   private JLabel lblTable;
/*     */   private JLabel lblTableValue;
/*     */   private BasePanel activePanel;
/*     */   
/*     */   public MainFrame() {
/* 115 */     setIconImage(Utils.createIconImage("images/Logo16x16.png"));
/* 116 */     initializeComponents();
/*     */     
/* 118 */     addWindowListener(new WindowAdapter()
/*     */         {
/*     */           public void windowClosing(WindowEvent e) {
/* 121 */             MainFrame.this.mainFrameClosing();
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void initializeComponents() {
/* 131 */     Container contentPane = getContentPane();
/* 132 */     contentPane.setLayout(new BorderLayout());
/*     */     
/* 134 */     this.panelLeft = new JPanel();
/* 135 */     this.panelLeft.setBackground(Color.LIGHT_GRAY);
/* 136 */     this.panelLeft.setLayout(new BoxLayout(this.panelLeft, 1));
/*     */     
/* 138 */     Dimension leftPanelSize = new Dimension(195, 415);
/* 139 */     this.panelLeft.setPreferredSize(leftPanelSize);
/* 140 */     this.panelLeft.setMinimumSize(leftPanelSize);
/* 141 */     this.panelLeft.setMaximumSize(leftPanelSize);
/*     */     
/* 143 */     this.btnConnection = createMainButton("<html><p align=\"center\">Change connection<br>settings</p></html>", 0);
/* 144 */     this.btnConnection.setIcon(Utils.createIcon("images/settings.png"));
/* 145 */     this.btnConnection.setToolTipText("Change connection settings to Server and/or database containing templates");
/* 146 */     this.btnDeduplication = createMainButton("Deduplication", 1);
/* 147 */     this.btnDeduplication.setToolTipText("Perform template deduplication on Megamatcher Accelerator");
/* 148 */     this.btnEnroll = createMainButton("Enroll templates", 2);
/* 149 */     this.btnEnroll.setToolTipText("Enroll templates to MegaMatcher Accelerator");
/* 150 */     this.btnTestSpeed = createMainButton("<html><p align=\"center\"> Calculate/Test Accelerator<br>matching speed</p></html>", 3);
/* 151 */     this.btnTestSpeed.setToolTipText("Test MegaMatcher Accelerator matching speed");
/* 152 */     this.btnMatchingSettings = createMainButton("Change matching settings", 4);
/*     */     
/* 154 */     this.cardLayoutTaskPanels = new CardLayout();
/* 155 */     this.panelCardLayoutContainer = new JPanel(this.cardLayoutTaskPanels);
/* 156 */     this.panelCardLayoutContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
/* 157 */     initializeTaskPanels();
/*     */     
/* 159 */     contentPane.add(this.panelCardLayoutContainer, "Center");
/* 160 */     contentPane.add(this.panelLeft, "Before");
/* 161 */     contentPane.add(initializeInformationPanel(), "Last");
/* 162 */     pack();
/*     */   }
/*     */   
/*     */   private JButton createMainButton(String text, int i) {
/* 166 */     JButton button = new JButton(text);
/* 167 */     button.setOpaque(true);
/* 168 */     button.setPreferredSize(this.buttonSize);
/* 169 */     button.setMinimumSize(this.buttonSize);
/* 170 */     button.setMaximumSize(this.buttonSize);
/* 171 */     button.addActionListener(this);
/* 172 */     this.panelLeft.add(Box.createVerticalStrut(5));
/* 173 */     this.panelLeft.add(button);
/* 174 */     button.setAlignmentX(0.5F);
/* 175 */     this.mainFrameButtons[i] = button;
/* 176 */     return button;
/*     */   }
/*     */   
/*     */   private JPanel initializeInformationPanel() {
/* 180 */     JPanel informationPanel = new JPanel(new BorderLayout(2, 2));
/* 181 */     informationPanel.setBorder(BorderFactory.createTitledBorder("Connection information"));
/* 182 */     informationPanel.setBackground(Color.LIGHT_GRAY);
/*     */     
/* 184 */     JPanel serverInfoPanel = new JPanel();
/* 185 */     serverInfoPanel.setOpaque(false);
/* 186 */     BoxLayout serverPanelLayout = new BoxLayout(serverInfoPanel, 0);
/* 187 */     serverInfoPanel.setLayout(serverPanelLayout);
/*     */     
/* 189 */     this.lblServerAddress = new JLabel("N/A");
/* 190 */     this.lblClientPortValue = new JLabel("N/A");
/* 191 */     this.lblAdminPortValue = new JLabel("N/A");
/*     */     
/* 193 */     serverInfoPanel.add(Box.createHorizontalStrut(2));
/* 194 */     serverInfoPanel.add(new JLabel("Server:"));
/* 195 */     serverInfoPanel.add(Box.createHorizontalStrut(2));
/* 196 */     serverInfoPanel.add(this.lblServerAddress);
/* 197 */     serverInfoPanel.add(Box.createHorizontalStrut(10));
/* 198 */     serverInfoPanel.add(new JLabel("Client port:"));
/* 199 */     serverInfoPanel.add(Box.createHorizontalStrut(2));
/* 200 */     serverInfoPanel.add(this.lblClientPortValue);
/* 201 */     serverInfoPanel.add(Box.createHorizontalStrut(10));
/* 202 */     serverInfoPanel.add(new JLabel("Admin port:"));
/* 203 */     serverInfoPanel.add(Box.createHorizontalStrut(2));
/* 204 */     serverInfoPanel.add(this.lblAdminPortValue);
/* 205 */     serverInfoPanel.add(Box.createHorizontalGlue());
/*     */     
/* 207 */     JPanel templateInfoPanel = new JPanel();
/* 208 */     templateInfoPanel.setOpaque(false);
/* 209 */     BoxLayout templateInfoLayout = new BoxLayout(templateInfoPanel, 0);
/* 210 */     templateInfoPanel.setLayout(templateInfoLayout);
/*     */     
/* 212 */     this.lblSource = new JLabel("Templates loaded from:");
/* 213 */     this.lblSourceValue = new JLabel("N/A");
/* 214 */     this.lblDSN = new JLabel("DSN:");
/* 215 */     this.lblDSNValue = new JLabel("N/A");
/* 216 */     this.lblTable = new JLabel("Table:");
/* 217 */     this.lblTableValue = new JLabel("N/A");
/*     */     
/* 219 */     templateInfoPanel.add(Box.createHorizontalStrut(2));
/* 220 */     templateInfoPanel.add(this.lblSource);
/* 221 */     templateInfoPanel.add(Box.createHorizontalStrut(2));
/* 222 */     templateInfoPanel.add(this.lblSourceValue);
/* 223 */     templateInfoPanel.add(Box.createHorizontalStrut(10));
/* 224 */     templateInfoPanel.add(this.lblDSN);
/* 225 */     templateInfoPanel.add(Box.createHorizontalStrut(2));
/* 226 */     templateInfoPanel.add(this.lblDSNValue);
/* 227 */     templateInfoPanel.add(Box.createHorizontalStrut(10));
/* 228 */     templateInfoPanel.add(this.lblTable);
/* 229 */     templateInfoPanel.add(Box.createHorizontalStrut(2));
/* 230 */     templateInfoPanel.add(this.lblTableValue);
/* 231 */     templateInfoPanel.add(Box.createHorizontalGlue());
/*     */     
/* 233 */     informationPanel.add(serverInfoPanel, "First");
/* 234 */     informationPanel.add(templateInfoPanel, "Last");
/* 235 */     return informationPanel;
/*     */   }
/*     */   
/*     */   private void initializeTaskPanels() {
/* 239 */     DeduplicationPanel deduplicationPanel = new DeduplicationPanel(this);
/* 240 */     this.panels.add(deduplicationPanel);
/* 241 */     this.panelCardLayoutContainer.add((Component)deduplicationPanel, Task.DEDUPLICATION.name());
/*     */     
/* 243 */     EnrollPanel enrollPanel = new EnrollPanel(this);
/* 244 */     this.panels.add(enrollPanel);
/* 245 */     this.panelCardLayoutContainer.add((Component)enrollPanel, Task.ENROLL.name());
/*     */     
/* 247 */     TestSpeedPanel testSpeedPanel = new TestSpeedPanel(this);
/* 248 */     this.panels.add(testSpeedPanel);
/* 249 */     this.panelCardLayoutContainer.add((Component)testSpeedPanel, Task.SPEED_TEST.name());
/*     */     
/* 251 */     MatchingSettingsPanel matchingSettingsPanel = new MatchingSettingsPanel(this);
/* 252 */     this.panels.add(matchingSettingsPanel);
/* 253 */     this.panelCardLayoutContainer.add((Component)matchingSettingsPanel, Task.SETTINGS.name());
/*     */     
/* 255 */     for (BasePanel bPanel : this.panels) {
/* 256 */       bPanel.setBiometricClient(this.biometricClient);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private void showConnectionSettings(boolean isLoadingTime) {
/* 262 */     if (isLoadingTime) {
/* 263 */       this.settings.setDSNConnection(true);
/*     */     }
/* 265 */     ConnectionSettingsDialog dialog = new ConnectionSettingsDialog(this);
/* 266 */     dialog.setLocationRelativeTo(this);
/* 267 */     dialog.setModal(true);
/* 268 */     dialog.setVisible(true);
/* 269 */     connectionSettingsChanged(isLoadingTime);
/*     */   }
/*     */   
/*     */   private void changeConnectionSettings() {
/* 273 */     if (this.activePanel.isBusy()) {
/* 274 */       if (MessageUtils.showQuestion(this, "Action in progress. Stop current action?")) {
/* 275 */         this.activePanel.cancel();
/*     */       } else {
/*     */         return;
/*     */       } 
/*     */     }
/* 280 */     showConnectionSettings(false);
/* 281 */     this.activePanel.setBiometricClient(this.biometricClient);
/* 282 */     this.activePanel.setTemplateLoader(this.templateLoader);
/*     */   }
/*     */   
/*     */   private void mainFrameClosing() {
/* 286 */     if (this.activePanel != null && this.activePanel.isBusy()) {
/* 287 */       this.activePanel.cancel();
/* 288 */       setTitle(String.format("%s: Closing, please wait ...", new Object[] { "Server Sample" }));
/*     */       try {
/* 290 */         this.activePanel.waitForCurrentProcessToFinish();
/* 291 */       } catch (InterruptedException e) {
/* 292 */         e.printStackTrace();
/* 293 */         MessageUtils.showError(this, e);
/* 294 */       } catch (ExecutionException e) {
/* 295 */         e.printStackTrace();
/* 296 */         MessageUtils.showError(this, e);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void showPanel(Task task, boolean force) {
/* 302 */     int index = task.value();
/* 303 */     if (this.activePanel == this.panels.get(index)) {
/*     */       return;
/*     */     }
/* 306 */     if (!force && this.activePanel != null && this.activePanel.isBusy()) {
/* 307 */       if (MessageUtils.showQuestion(this, "Action in progress. Stop current action?")) {
/* 308 */         this.activePanel.cancel();
/*     */         try {
/* 310 */           this.activePanel.waitForCurrentProcessToFinish();
/* 311 */         } catch (InterruptedException e) {
/* 312 */           e.printStackTrace();
/* 313 */           MessageUtils.showError(this, e);
/* 314 */         } catch (ExecutionException e) {
/* 315 */           e.printStackTrace();
/* 316 */           MessageUtils.showError(this, e);
/*     */         } 
/*     */       } else {
/*     */         return;
/*     */       } 
/*     */     }
/*     */     
/* 323 */     this.activePanel = this.panels.get(index);
/*     */     
/* 325 */     for (int i = 1; i < 5; i++) {
/* 326 */       Border border = BorderFactory.createLineBorder((index == i - 1) ? SELECTED_BUTTON_COLOR : NOT_SELECTED_BUTTON_COLOR);
/* 327 */       this.mainFrameButtons[i].setBorder(border);
/*     */     } 
/*     */     
/* 330 */     this.cardLayoutTaskPanels.show(this.panelCardLayoutContainer, task.name());
/*     */     
/* 332 */     this.activePanel.setBiometricClient(this.biometricClient);
/* 333 */     this.activePanel.setTemplateLoader(this.templateLoader);
/*     */     
/* 335 */     String title = this.activePanel.getTitle();
/* 336 */     if (title == null || title.equals("")) {
/* 337 */       setTitle("Server Sample");
/*     */     } else {
/* 339 */       setTitle(String.format("%s: %s", new Object[] { "Server Sample", title }));
/*     */     } 
/*     */   }
/*     */   
/*     */   private void connectionSettingsChanged(boolean isLoadingTime) {
/* 344 */     this.biometricClient = new NBiometricClient();
/* 345 */     this.biometricConnection = new NClusterBiometricConnection(this.settings.getServer(), this.settings.getClientPort(), this.settings.getAdminPort());
/* 346 */     this.biometricClient.getRemoteConnections().add(this.biometricConnection);
/* 347 */     boolean isUseDB = this.settings.isTemplateSourceDb();
/* 348 */     if (!isUseDB) {
/* 349 */       this.lblSourceValue.setText(this.settings.getTemplateDirectory());
/*     */     } else {
/* 351 */       this.lblDSNValue.setText(this.settings.getDSN());
/* 352 */       this.lblTableValue.setText(this.settings.getTable());
/*     */     } 
/*     */     
/* 355 */     updateConnectionInformation();
/*     */     
/* 357 */     if (isLoadingTime) {
/* 358 */       setVisible(true);
/* 359 */       showPanel(Task.DEDUPLICATION, false);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void updateConnectionInformation() {
/* 364 */     boolean isUseDB = this.settings.isTemplateSourceDb();
/* 365 */     if (!isUseDB) {
/* 366 */       this.lblSourceValue.setText(this.settings.getTemplateDirectory());
/* 367 */       this.templateLoader = (TemplateLoader)new DirectoryEnumerator(this.settings.getTemplateDirectory());
/*     */     } else {
/* 369 */       this.lblDSNValue.setText(this.settings.getDSN());
/* 370 */       this.lblTableValue.setText(this.settings.getTable());
/* 371 */       this.lblSourceValue.setText("N/A");
/* 372 */       this.templateLoader = (TemplateLoader)new DatabaseConnection();
/*     */     } 
/* 374 */     this.lblDSN.setVisible(isUseDB);
/* 375 */     this.lblDSNValue.setVisible(isUseDB);
/* 376 */     this.lblTable.setVisible(isUseDB);
/* 377 */     this.lblTableValue.setVisible(isUseDB);
/*     */     
/* 379 */     this.lblServerAddress.setText(this.settings.getServer());
/* 380 */     this.lblClientPortValue.setText(String.valueOf(this.settings.getClientPort()));
/* 381 */     this.lblAdminPortValue.setText(String.valueOf(this.settings.getAdminPort()));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void showMainFrame() {
/* 389 */     setVisible(false);
/* 390 */     showConnectionSettings(true);
/*     */   }
/*     */   
/*     */   public boolean isPanelBusy() {
/* 394 */     if (this.activePanel != null && this.activePanel.isBusy()) {
/* 395 */       return true;
/*     */     }
/* 397 */     return false;
/*     */   }
/*     */   
/*     */   public void actionPerformed(ActionEvent e) {
/* 401 */     Object source = e.getSource();
/* 402 */     if (source == this.btnConnection && !isPanelBusy()) {
/* 403 */       changeConnectionSettings();
/* 404 */     } else if (source == this.btnDeduplication) {
/* 405 */       showPanel(Task.DEDUPLICATION, false);
/* 406 */     } else if (source == this.btnEnroll) {
/* 407 */       showPanel(Task.ENROLL, false);
/* 408 */     } else if (source == this.btnTestSpeed) {
/* 409 */       showPanel(Task.SPEED_TEST, false);
/* 410 */     } else if (source == this.btnMatchingSettings) {
/* 411 */       showPanel(Task.SETTINGS, false);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\MainFrame.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */