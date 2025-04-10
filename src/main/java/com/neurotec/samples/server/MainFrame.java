package com.neurotec.samples.server;

import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.client.NClusterBiometricConnection;
import com.neurotec.samples.server.connection.DatabaseConnection;
import com.neurotec.samples.server.connection.DirectoryEnumerator;
import com.neurotec.samples.server.connection.TemplateLoader;
import com.neurotec.samples.server.controls.BasePanel;
import com.neurotec.samples.server.controls.DeduplicationPanel;
import com.neurotec.samples.server.controls.EnrollPanel;
import com.neurotec.samples.server.controls.TestSpeedPanel;
import com.neurotec.samples.server.settings.ConnectionSettingsDialog;
import com.neurotec.samples.server.settings.MatchingSettingsPanel;
import com.neurotec.samples.server.settings.Settings;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.util.Utils;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;








public final class MainFrame
  extends JFrame
  implements ActionListener
{
  private static final long serialVersionUID = 1L;
  private static final String SAMPLE_TITLE = "Server Sample";
  private static final Color SELECTED_BUTTON_COLOR = Color.DARK_GRAY;
  private static final Color NOT_SELECTED_BUTTON_COLOR = new Color(162, 181, 205);
  private TemplateLoader templateLoader;
  private NBiometricClient biometricClient;
  private NClusterBiometricConnection biometricConnection;

  public enum Task
  {
    DEDUPLICATION(0), ENROLL(1), SPEED_TEST(2), SETTINGS(3);

    private int value;

    Task(int value) {
      this.value = value;
    }

    public int value() {
      return this.value;
    }
  }









  private Settings settings = Settings.getInstance();
  private Dimension buttonSize = new Dimension(185, 40);





  private final JButton[] mainFrameButtons = new JButton[5];
  private final List<BasePanel> panels = new ArrayList<>();

  private JButton btnConnection;

  private JButton btnDeduplication;

  private JButton btnEnroll;

  private JButton btnTestSpeed;

  private JButton btnMatchingSettings;
  private JPanel panelCardLayoutContainer;
  private JPanel panelLeft;
  private CardLayout cardLayoutTaskPanels;
  private JLabel lblServerAddress;
  private JLabel lblClientPortValue;
  private JLabel lblAdminPortValue;
  private JLabel lblSource;
  private JLabel lblSourceValue;
  private JLabel lblDSN;
  private JLabel lblDSNValue;
  private JLabel lblTable;
  private JLabel lblTableValue;
  private BasePanel activePanel;

  public MainFrame() {
    setIconImage(Utils.createIconImage("images/Logo16x16.png"));
    initializeComponents();

    addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            MainFrame.this.mainFrameClosing();
          }
        });
  }





  private void initializeComponents() {
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    this.panelLeft = new JPanel();
    this.panelLeft.setBackground(Color.LIGHT_GRAY);
    this.panelLeft.setLayout(new BoxLayout(this.panelLeft, 1));

    Dimension leftPanelSize = new Dimension(195, 415);
    this.panelLeft.setPreferredSize(leftPanelSize);
    this.panelLeft.setMinimumSize(leftPanelSize);
    this.panelLeft.setMaximumSize(leftPanelSize);

    this.btnConnection = createMainButton("<html><p align=\"center\">Change connection<br>settings</p></html>", 0);
    this.btnConnection.setIcon(Utils.createIcon("images/settings.png"));
    this.btnConnection.setToolTipText("Change connection settings to Server and/or database containing templates");
    this.btnDeduplication = createMainButton("Deduplication", 1);
    this.btnDeduplication.setToolTipText("Perform template deduplication on Megamatcher Accelerator");
    this.btnEnroll = createMainButton("Enroll templates", 2);
    this.btnEnroll.setToolTipText("Enroll templates to MegaMatcher Accelerator");
    this.btnTestSpeed = createMainButton("<html><p align=\"center\"> Calculate/Test Accelerator<br>matching speed</p></html>", 3);
    this.btnTestSpeed.setToolTipText("Test MegaMatcher Accelerator matching speed");
    this.btnMatchingSettings = createMainButton("Change matching settings", 4);

    this.cardLayoutTaskPanels = new CardLayout();
    this.panelCardLayoutContainer = new JPanel(this.cardLayoutTaskPanels);
    this.panelCardLayoutContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    initializeTaskPanels();

    contentPane.add(this.panelCardLayoutContainer, "Center");
    contentPane.add(this.panelLeft, "Before");
    contentPane.add(initializeInformationPanel(), "Last");
    pack();
  }

  private JButton createMainButton(String text, int i) {
    JButton button = new JButton(text);
    button.setOpaque(true);
    button.setPreferredSize(this.buttonSize);
    button.setMinimumSize(this.buttonSize);
    button.setMaximumSize(this.buttonSize);
    button.addActionListener(this);
    this.panelLeft.add(Box.createVerticalStrut(5));
    this.panelLeft.add(button);
    button.setAlignmentX(0.5F);
    this.mainFrameButtons[i] = button;
    return button;
  }

  private JPanel initializeInformationPanel() {
    JPanel informationPanel = new JPanel(new BorderLayout(2, 2));
    informationPanel.setBorder(BorderFactory.createTitledBorder("Connection information"));
    informationPanel.setBackground(Color.LIGHT_GRAY);

    JPanel serverInfoPanel = new JPanel();
    serverInfoPanel.setOpaque(false);
    BoxLayout serverPanelLayout = new BoxLayout(serverInfoPanel, 0);
    serverInfoPanel.setLayout(serverPanelLayout);

    this.lblServerAddress = new JLabel("N/A");
    this.lblClientPortValue = new JLabel("N/A");
    this.lblAdminPortValue = new JLabel("N/A");

    serverInfoPanel.add(Box.createHorizontalStrut(2));
    serverInfoPanel.add(new JLabel("Server:"));
    serverInfoPanel.add(Box.createHorizontalStrut(2));
    serverInfoPanel.add(this.lblServerAddress);
    serverInfoPanel.add(Box.createHorizontalStrut(10));
    serverInfoPanel.add(new JLabel("Client port:"));
    serverInfoPanel.add(Box.createHorizontalStrut(2));
    serverInfoPanel.add(this.lblClientPortValue);
    serverInfoPanel.add(Box.createHorizontalStrut(10));
    serverInfoPanel.add(new JLabel("Admin port:"));
    serverInfoPanel.add(Box.createHorizontalStrut(2));
    serverInfoPanel.add(this.lblAdminPortValue);
    serverInfoPanel.add(Box.createHorizontalGlue());

    JPanel templateInfoPanel = new JPanel();
    templateInfoPanel.setOpaque(false);
    BoxLayout templateInfoLayout = new BoxLayout(templateInfoPanel, 0);
    templateInfoPanel.setLayout(templateInfoLayout);

    this.lblSource = new JLabel("Templates loaded from:");
    this.lblSourceValue = new JLabel("N/A");
    this.lblDSN = new JLabel("DSN:");
    this.lblDSNValue = new JLabel("N/A");
    this.lblTable = new JLabel("Table:");
    this.lblTableValue = new JLabel("N/A");

    templateInfoPanel.add(Box.createHorizontalStrut(2));
    templateInfoPanel.add(this.lblSource);
    templateInfoPanel.add(Box.createHorizontalStrut(2));
    templateInfoPanel.add(this.lblSourceValue);
    templateInfoPanel.add(Box.createHorizontalStrut(10));
    templateInfoPanel.add(this.lblDSN);
    templateInfoPanel.add(Box.createHorizontalStrut(2));
    templateInfoPanel.add(this.lblDSNValue);
    templateInfoPanel.add(Box.createHorizontalStrut(10));
    templateInfoPanel.add(this.lblTable);
    templateInfoPanel.add(Box.createHorizontalStrut(2));
    templateInfoPanel.add(this.lblTableValue);
    templateInfoPanel.add(Box.createHorizontalGlue());

    informationPanel.add(serverInfoPanel, "First");
    informationPanel.add(templateInfoPanel, "Last");
    return informationPanel;
  }

  private void initializeTaskPanels() {
    DeduplicationPanel deduplicationPanel = new DeduplicationPanel(this);
    this.panels.add(deduplicationPanel);
    this.panelCardLayoutContainer.add((Component)deduplicationPanel, Task.DEDUPLICATION.name());

    EnrollPanel enrollPanel = new EnrollPanel(this);
    this.panels.add(enrollPanel);
    this.panelCardLayoutContainer.add((Component)enrollPanel, Task.ENROLL.name());

    TestSpeedPanel testSpeedPanel = new TestSpeedPanel(this);
    this.panels.add(testSpeedPanel);
    this.panelCardLayoutContainer.add((Component)testSpeedPanel, Task.SPEED_TEST.name());

    MatchingSettingsPanel matchingSettingsPanel = new MatchingSettingsPanel(this);
    this.panels.add(matchingSettingsPanel);
    this.panelCardLayoutContainer.add((Component)matchingSettingsPanel, Task.SETTINGS.name());

    for (BasePanel bPanel : this.panels) {
      bPanel.setBiometricClient(this.biometricClient);
    }
  }


  private void showConnectionSettings(boolean isLoadingTime) {

    this.settings.setDSNConnection(true);

    ConnectionSettingsDialog dialog = new ConnectionSettingsDialog(this);
    dialog.setLocationRelativeTo(this);
    dialog.setModal(true);
    dialog.setVisible(true);
    connectionSettingsChanged(isLoadingTime);
  }

  private void changeConnectionSettings() {
    if (this.activePanel.isBusy()) {
      if (MessageUtils.showQuestion(this, "Action in progress. Stop current action?")) {
        this.activePanel.cancel();
      } else {
        return;
      }
    }
    showConnectionSettings(false);
    this.activePanel.setBiometricClient(this.biometricClient);
    this.activePanel.setTemplateLoader(this.templateLoader);
  }

  private void mainFrameClosing() {
    if (this.activePanel != null && this.activePanel.isBusy()) {
      this.activePanel.cancel();
      setTitle(String.format("%s: Closing, please wait ...", new Object[] { "Server Sample" }));
      try {
        this.activePanel.waitForCurrentProcessToFinish();
      } catch (InterruptedException e) {
        e.printStackTrace();
        MessageUtils.showError(this, e);
      } catch (ExecutionException e) {
        e.printStackTrace();
        MessageUtils.showError(this, e);
      }
    }
  }

  private void showPanel(Task task, boolean force) {
    int index = task.value();
    if (this.activePanel == this.panels.get(index)) {
      return;
    }
    if (!force && this.activePanel != null && this.activePanel.isBusy()) {
      if (MessageUtils.showQuestion(this, "Action in progress. Stop current action?")) {
        this.activePanel.cancel();
        try {
          this.activePanel.waitForCurrentProcessToFinish();
        } catch (InterruptedException e) {
          e.printStackTrace();
          MessageUtils.showError(this, e);
        } catch (ExecutionException e) {
          e.printStackTrace();
          MessageUtils.showError(this, e);
        }
      } else {
        return;
      }
    }

    this.activePanel = this.panels.get(index);

    for (int i = 1; i < 5; i++) {
      Border border = BorderFactory.createLineBorder((index == i - 1) ? SELECTED_BUTTON_COLOR : NOT_SELECTED_BUTTON_COLOR);
      this.mainFrameButtons[i].setBorder(border);
    }

    this.cardLayoutTaskPanels.show(this.panelCardLayoutContainer, task.name());

    this.activePanel.setBiometricClient(this.biometricClient);
    this.activePanel.setTemplateLoader(this.templateLoader);

    String title = this.activePanel.getTitle();
    if (title == null || title.equals("")) {
      setTitle("Server Sample");
    } else {
      setTitle(String.format("%s: %s", new Object[] { "Server Sample", title }));
    }
  }

  private void connectionSettingsChanged(boolean isLoadingTime) {
    this.biometricClient = new NBiometricClient();
    this.biometricConnection = new NClusterBiometricConnection(
      this.settings.getServer(),
      this.settings.getClientPort(),
      this.settings.getAdminPort()
    );
    this.biometricClient.getRemoteConnections().add(this.biometricConnection);
    boolean isUseDB = this.settings.isTemplateSourceDb();
    if (!isUseDB) {
      this.lblSourceValue.setText(this.settings.getTemplateDirectory());
    } else {
      this.lblDSNValue.setText(this.settings.getDSN());
      this.lblTableValue.setText(this.settings.getTable());
    }

    updateConnectionInformation();

    if (isLoadingTime) {
      setVisible(true);
      showPanel(Task.DEDUPLICATION, false);
    }
  }

  private void updateConnectionInformation() {
    boolean isUseDB = this.settings.isTemplateSourceDb();
    if (!isUseDB) {
      this.lblSourceValue.setText(this.settings.getTemplateDirectory());
      this.templateLoader = (TemplateLoader)new DirectoryEnumerator(this.settings.getTemplateDirectory());
    } else {
      this.lblDSNValue.setText(this.settings.getDSN());
      this.lblTableValue.setText(this.settings.getTable());
      this.lblSourceValue.setText("N/A");
      this.templateLoader = (TemplateLoader)new DatabaseConnection();
    }
    this.lblDSN.setVisible(isUseDB);
    this.lblDSNValue.setVisible(isUseDB);
    this.lblTable.setVisible(isUseDB);
    this.lblTableValue.setVisible(isUseDB);

    this.lblServerAddress.setText(this.settings.getServer());
    this.lblClientPortValue.setText(String.valueOf(this.settings.getClientPort()));
    this.lblAdminPortValue.setText(String.valueOf(this.settings.getAdminPort()));
  }





  public void showMainFrame() {
    setVisible(false);
    showConnectionSettings(true);
  }

  public boolean isPanelBusy() {
    if (this.activePanel != null && this.activePanel.isBusy()) {
      return true;
    }
    return false;
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == this.btnConnection && !isPanelBusy()) {
      changeConnectionSettings();
    } else if (source == this.btnDeduplication) {
      showPanel(Task.DEDUPLICATION, false);
    } else if (source == this.btnEnroll) {
      showPanel(Task.ENROLL, false);
    } else if (source == this.btnTestSpeed) {
      showPanel(Task.SPEED_TEST, false);
    } else if (source == this.btnMatchingSettings) {
      showPanel(Task.SETTINGS, false);
    }
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\MainFrame.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */