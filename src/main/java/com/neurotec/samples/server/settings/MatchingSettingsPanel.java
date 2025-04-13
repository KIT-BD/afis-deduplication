package com.neurotec.samples.server.settings;

import com.neurotec.biometrics.NMatchingSpeed;
import com.neurotec.samples.server.controls.BasePanel;
import com.neurotec.samples.server.util.BiometricUtils;
import com.neurotec.samples.server.util.GridBagUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public final class MatchingSettingsPanel
        extends BasePanel {
    private static final long serialVersionUID = 1L;
    private GridBagUtils gridBagUtils;
    private Settings currentSettings = Settings.getInstance();


    private JComboBox cmbThreshold;


    private JComboBox cmbFingersSpeed;


    private JSpinner spinnerFingersRotation;


    private JComboBox cmbFacesSpeed;

    private JComboBox cmbIrisesSpeed;

    private JSpinner spinnerIrisesRotation;

    private JComboBox cmbPalmsSpeed;

    private JSpinner spinnerPalmsRotation;

    private JButton btnReset;

    private JButton btnApply;


    public MatchingSettingsPanel(Frame owner) {
        super(owner);

        initializeComponents();


        String[] thresholds = {BiometricUtils.matchingThresholdToString(12), BiometricUtils.matchingThresholdToString(24), BiometricUtils.matchingThresholdToString(36), BiometricUtils.matchingThresholdToString(48), BiometricUtils.matchingThresholdToString(60), BiometricUtils.matchingThresholdToString(72)};

        for (String t : thresholds) {
            this.cmbThreshold.addItem(t);
        }

        NMatchingSpeed[] speeds = {NMatchingSpeed.HIGH, NMatchingSpeed.MEDIUM, NMatchingSpeed.LOW};
        for (NMatchingSpeed s : speeds) {
            this.cmbFingersSpeed.addItem(s);
            this.cmbFacesSpeed.addItem(s);
            this.cmbIrisesSpeed.addItem(s);
            this.cmbPalmsSpeed.addItem(s);
        }

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                MatchingSettingsPanel.this.loadSettings();
            }
        });
    }


    private void initializeComponents() {
        this.gridBagUtils = new GridBagUtils(1, new Insets(4, 4, 4, 4));

        setLayout(new BorderLayout(5, 5));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", initializeGeneralPanel());
        tabbedPane.addTab("Fingers", initializeFingersPanel());
        tabbedPane.addTab("Faces", initializeFacesPanel());
        tabbedPane.addTab("Irises", initializeIrisesPanel());
        tabbedPane.addTab("Palms", initializePalmsPanel());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));

        this.btnReset = new JButton("Reset");
        this.btnReset.addActionListener((ActionListener) this);

        this.btnApply = new JButton("Apply");
        this.btnApply.addActionListener((ActionListener) this);

        buttonPanel.add(this.btnReset);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(this.btnApply);
        buttonPanel.add(Box.createGlue());

        add(tabbedPane, "Center");
        add(buttonPanel, "Last");
    }


    private JPanel initializeGeneralPanel() {
        JPanel generalPanel = new JPanel();
        generalPanel.setBackground(Color.WHITE);
        generalPanel.setOpaque(true);

        GridBagLayout generalPanelLayout = new GridBagLayout();
        generalPanelLayout.columnWidths = new int[]{100, 150, 70, 150, 50};
        generalPanelLayout.rowHeights = new int[]{25, 25, 100};
        generalPanel.setLayout(generalPanelLayout);

        this.cmbThreshold = new JComboBox();
        this.cmbThreshold.setEditable(true);
        ((JTextField) this.cmbThreshold.getEditor().getEditorComponent()).setInputVerifier(new ThresholdComboBoxVerifier(this.cmbThreshold));

        this.gridBagUtils.addToGridBagLayout(0, 0, generalPanel, new JLabel("Matching threshold:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, generalPanel, this.cmbThreshold);
        this.gridBagUtils.addToGridBagLayout(0, 2, 3, 1, 0, 1, generalPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(4, 0, 3, 1, 1, 0, generalPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();

        return generalPanel;
    }


    private JPanel initializeFingersPanel() {
        JPanel fingersPanel = new JPanel();
        fingersPanel.setBackground(Color.WHITE);
        fingersPanel.setOpaque(true);

        GridBagLayout fingersPanelLayout = new GridBagLayout();
        fingersPanelLayout.columnWidths = new int[]{175, 150, 100};
        fingersPanelLayout.rowHeights = new int[]{25, 25, 25, 25, 25, 100};
        fingersPanel.setLayout(fingersPanelLayout);

        this.cmbFingersSpeed = new JComboBox();
        this.spinnerFingersRotation = new JSpinner(new SpinnerNumberModel(0, 0, 180, 1));

        this.gridBagUtils.addToGridBagLayout(0, 0, fingersPanel, new JLabel("Speed:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, fingersPanel, this.cmbFingersSpeed);
        this.gridBagUtils.addToGridBagLayout(0, 1, fingersPanel, new JLabel("Maximal rotation:"));
        this.gridBagUtils.addToGridBagLayout(1, 1, fingersPanel, this.spinnerFingersRotation);
        this.gridBagUtils.addToGridBagLayout(0, 5, 1, 1, 0, 1, fingersPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, 1, 0, fingersPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();

        return fingersPanel;
    }


    private JPanel initializeFacesPanel() {
        JPanel facesPanel = new JPanel();
        facesPanel.setBackground(Color.WHITE);
        facesPanel.setOpaque(true);

        GridBagLayout facesPanelLayout = new GridBagLayout();
        facesPanelLayout.columnWidths = new int[]{60, 175, 100};
        facesPanelLayout.rowHeights = new int[]{25, 25};
        facesPanel.setLayout(facesPanelLayout);

        this.cmbFacesSpeed = new JComboBox();

        this.gridBagUtils.setInsets(new Insets(6, 6, 6, 6));
        this.gridBagUtils.addToGridBagLayout(0, 0, facesPanel, new JLabel("Speed:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, facesPanel, this.cmbFacesSpeed);
        this.gridBagUtils.addToGridBagLayout(0, 2, 1, 1, 0, 1, facesPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, 1, 0, facesPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();

        return facesPanel;
    }


    private JPanel initializeIrisesPanel() {
        JPanel irisesPanel = new JPanel();
        irisesPanel.setBackground(Color.WHITE);
        irisesPanel.setOpaque(true);

        GridBagLayout irisesPanelLayout = new GridBagLayout();
        irisesPanelLayout.columnWidths = new int[]{175, 150, 100};
        irisesPanelLayout.rowHeights = new int[]{25, 25, 25, 25, 25};
        irisesPanel.setLayout(irisesPanelLayout);

        this.cmbIrisesSpeed = new JComboBox();

        this.spinnerIrisesRotation = new JSpinner(new SpinnerNumberModel(0, 0, 180, 1));

        this.gridBagUtils.setInsets(new Insets(4, 4, 4, 4));
        this.gridBagUtils.addToGridBagLayout(0, 0, irisesPanel, new JLabel("Speed:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, irisesPanel, this.cmbIrisesSpeed);
        this.gridBagUtils.addToGridBagLayout(0, 1, irisesPanel, new JLabel("Maximal rotation:"));
        this.gridBagUtils.addToGridBagLayout(1, 1, irisesPanel, this.spinnerIrisesRotation);
        this.gridBagUtils.addToGridBagLayout(0, 5, 1, 1, 0, 1, irisesPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, 1, 0, irisesPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();

        return irisesPanel;
    }


    private JPanel initializePalmsPanel() {
        JPanel palmsPanel = new JPanel();
        palmsPanel.setBackground(Color.WHITE);
        palmsPanel.setOpaque(true);

        GridBagLayout palmsPanelLayout = new GridBagLayout();
        palmsPanelLayout.columnWidths = new int[]{175, 150, 100};
        palmsPanelLayout.rowHeights = new int[]{25, 25, 25, 25};
        palmsPanel.setLayout(palmsPanelLayout);

        this.cmbPalmsSpeed = new JComboBox();
        this.spinnerPalmsRotation = new JSpinner(new SpinnerNumberModel(0, 0, 180, 1));

        this.gridBagUtils.addToGridBagLayout(0, 0, palmsPanel, new JLabel("Speed:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, palmsPanel, this.cmbPalmsSpeed);
        this.gridBagUtils.addToGridBagLayout(0, 1, palmsPanel, new JLabel("Maximal rotation:"));
        this.gridBagUtils.addToGridBagLayout(1, 1, palmsPanel, this.spinnerPalmsRotation);
        this.gridBagUtils.addToGridBagLayout(0, 4, 1, 1, 0, 1, palmsPanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(2, 0, 1, 1, 1, 0, palmsPanel, new JLabel());

        return palmsPanel;
    }

    private void applyChanges() {
        this.currentSettings.setMatchingThreshold(BiometricUtils.matchingThresholdFromString(String.valueOf(this.cmbThreshold.getSelectedItem())));

        this.currentSettings.setFingersMatchingSpeed((NMatchingSpeed) this.cmbFingersSpeed.getSelectedItem());
        this.currentSettings.setFingersMaximalRotation(BiometricUtils.maximalRotationFromDegrees(((Integer) this.spinnerFingersRotation.getValue()).intValue()));

        this.currentSettings.setFacesMatchingSpeed((NMatchingSpeed) this.cmbFacesSpeed.getSelectedItem());
        this.currentSettings.setIrisesMatchingSpeed((NMatchingSpeed) this.cmbIrisesSpeed.getSelectedItem());
        this.currentSettings.setIrisesMaximalRotation(BiometricUtils.maximalRotationFromDegrees(((Integer) this.spinnerIrisesRotation.getValue()).intValue()));

        this.currentSettings.setPalmsMatchingSpeed((NMatchingSpeed) this.cmbPalmsSpeed.getSelectedItem());
        this.currentSettings.setPalmsMaximalRotation(BiometricUtils.maximalRotationFromDegrees(((Integer) this.spinnerPalmsRotation.getValue()).intValue()));

        this.currentSettings.save();
    }

    private void loadSettings() {
        selectThreshold(this.cmbThreshold, this.currentSettings.getMatchingThreshold());
        this.cmbFingersSpeed.setSelectedItem(this.currentSettings.getFingersMatchingSpeed());

        this.spinnerFingersRotation.setValue(Integer.valueOf(BiometricUtils.maximalRotationToDegrees(this.currentSettings.getFingersMaximalRotation())));
        this.spinnerIrisesRotation.setValue(Integer.valueOf(BiometricUtils.maximalRotationToDegrees(this.currentSettings.getIrisesMaximalRotation())));
        this.spinnerPalmsRotation.setValue(Integer.valueOf(BiometricUtils.maximalRotationToDegrees(this.currentSettings.getPalmsMaximalRotation())));

        this.cmbFacesSpeed.setSelectedItem(this.currentSettings.getFacesMatchingSpeed());
        this.cmbIrisesSpeed.setSelectedItem(this.currentSettings.getIrisesMatchingSpeed());

        this.cmbPalmsSpeed.setSelectedItem(this.currentSettings.getPalmsMatchingSpeed());
    }


    private void selectThreshold(JComboBox<String> target, int value) {
        String str = BiometricUtils.matchingThresholdToString(value);
        int index = getIndexOfComboBoxItem(target, str);
        if (index != -1) {
            target.setSelectedIndex(index);
        } else {
            target.addItem(str);
            target.setSelectedItem(str);
        }
    }


    private int getIndexOfComboBoxItem(JComboBox cmb, Object item) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            Object o = cmb.getItemAt(i);
            if (o.equals(item)) {
                return i;
            }
        }
        return -1;
    }


    public String getTitle() {
        return "Matching parameters";
    }


    public boolean isBusy() {
        return false;
    }


    public void cancel() {
    }


    public void waitForCurrentProcessToFinish() throws InterruptedException, ExecutionException {
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.btnApply) {
            applyChanges();
        } else if (source == this.btnReset) {
            this.currentSettings.loadDefaultMatchingSettings();
            loadSettings();
        }
    }


    private static class ThresholdComboBoxVerifier
            extends InputVerifier {
        private JComboBox comboBox;


        ThresholdComboBoxVerifier(JComboBox comboBox) {
            this.comboBox = comboBox;
        }


        public boolean verify(JComponent input) {
            try {
                if (input instanceof JTextField) {
                    int value = BiometricUtils.matchingThresholdFromString(((JTextField) input).getText());
                    String item = BiometricUtils.matchingThresholdToString(value);
                    this.comboBox.addItem(item);
                    this.comboBox.setSelectedItem(item);
                    return true;
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(input, "Matching threshold is invalid"));
            }
            return false;
        }
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\settings\MatchingSettingsPanel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */