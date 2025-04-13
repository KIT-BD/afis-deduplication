package com.neurotec.samples.server.settings;

import com.neurotec.samples.server.connection.DatabaseConnection;
import com.neurotec.samples.server.util.GridBagUtils;
import com.neurotec.samples.server.util.MessageUtils;
import com.neurotec.samples.server.util.PropertyLoader;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.SQLException;
import javax.swing.*;


public final class ConnectionSettingsDialog extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    private GridBagUtils gridBagUtils;
    private final Settings settings = PropertyLoader.getSettings();


    private JTextField txtServer;

    private JSpinner spinnerClientPort;

    private JSpinner spinnerAdminPort;

    private JRadioButton radioFromDirectory;

    private JRadioButton radioFromDatabase;

    private JTextField txtDirectoryPath;

    private JButton btnBrowse;

    private JPanel databasePanel;

    private JTextField txtDSN;

    private JTextField txtDBUser;

    private JPasswordField txtDBPassword;

    private JButton btnConnect;

    private JButton btnReset;

    private JComboBox cmbTable;

    private JComboBox cmbTemplateColumn;

    private JComboBox cmbIdColumn;

    private JLabel lblDBConnectMsg;
    private JButton btnResetAll;
    private JButton btnOK;
    private JButton btnCancel;
    private JFileChooser folderBrowserDialog;

    public ConnectionSettingsDialog(Frame owner) {
        super(owner, "Connection settings", true);
        setPreferredSize(new Dimension(320, 690));
        setMinimumSize(new Dimension(280, 290));

        initializeComponents();
        this.folderBrowserDialog = new JFileChooser();
        this.folderBrowserDialog.setFileSelectionMode(1);

        loadSettings();
    }

    private void initializeComponents() {
        this.gridBagUtils = new GridBagUtils(1);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, 1));
        contentPane.add(initializeServerConnectionPanel());
        contentPane.add(initializeTemplatesPanel());
        contentPane.add(initializeButtonPanel());
        pack();
    }

    private JPanel initializeServerConnectionPanel() {
        JPanel serverConnectionPanel = new JPanel();
        serverConnectionPanel.setBorder(BorderFactory.createTitledBorder("Server connection"));

        GridBagLayout serverConnectionLayout = new GridBagLayout();
        serverConnectionLayout.columnWidths = new int[]{85, 70, 10, 100};
        serverConnectionPanel.setLayout(serverConnectionLayout);

        this.txtServer = new JTextField();
        this.spinnerClientPort = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
        JSpinner.NumberEditor editorClientPort = new JSpinner.NumberEditor(this.spinnerClientPort, "#");
        this.spinnerClientPort.setEditor(editorClientPort);
        this.spinnerAdminPort = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 1));
        JSpinner.NumberEditor editorAdminPort = new JSpinner.NumberEditor(this.spinnerAdminPort, "#");
        this.spinnerAdminPort.setEditor(editorAdminPort);

        this.gridBagUtils.setInsets(new Insets(3, 3, 3, 3));

        this.gridBagUtils.addToGridBagLayout(0, 0, serverConnectionPanel, new JLabel("Server:"));
        this.gridBagUtils.addToGridBagLayout(0, 1, serverConnectionPanel, new JLabel("Client port:"));
        this.gridBagUtils.addToGridBagLayout(0, 2, serverConnectionPanel, new JLabel("Admin port:"));
        this.gridBagUtils.addToGridBagLayout(1, 0, 3, 1, serverConnectionPanel, this.txtServer);
        this.gridBagUtils.addToGridBagLayout(1, 1, 1, 1, serverConnectionPanel, this.spinnerClientPort);
        this.gridBagUtils.addToGridBagLayout(1, 2, serverConnectionPanel, this.spinnerAdminPort);
        this.gridBagUtils.addToGridBagLayout(2, 1, 1, 1, 1, 0, serverConnectionPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();
        return serverConnectionPanel;
    }

    private JPanel initializeTemplatesPanel() {
        JPanel templatesPanel = new JPanel();
        templatesPanel.setBorder(BorderFactory.createTitledBorder("Templates"));
        GridBagLayout templatesPanelLayout = new GridBagLayout();
        templatesPanelLayout.columnWidths = new int[]{20, 270};
        templatesPanelLayout.rowHeights = new int[]{20, 25, 20, 275};
        templatesPanel.setLayout(templatesPanelLayout);

        this.radioFromDirectory = new JRadioButton("Load templates from directory");
        this.radioFromDirectory.addActionListener(this);
        this.radioFromDirectory.setSelected(false);

        this.radioFromDatabase = new JRadioButton("Load templates from database");
        this.radioFromDatabase.addActionListener(this);
        this.radioFromDatabase.setSelected(true);

        ButtonGroup sourceGroup = new ButtonGroup();
        sourceGroup.add(this.radioFromDirectory);
        sourceGroup.add(this.radioFromDatabase);

        this.txtDirectoryPath = new JTextField("c:\\");

        this.btnBrowse = new JButton("...");
        this.btnBrowse.addActionListener(this);

        JPanel fromDirectoryPanel = new JPanel();
        fromDirectoryPanel.setLayout(new BoxLayout(fromDirectoryPanel, 0));
        fromDirectoryPanel.add(this.txtDirectoryPath);
        fromDirectoryPanel.add(Box.createHorizontalStrut(5));
        fromDirectoryPanel.add(this.btnBrowse);

        initializeDatabasePanel();

        this.gridBagUtils.setInsets(new Insets(2, 2, 2, 2));
        this.gridBagUtils.addToGridBagLayout(0, 0, 2, 1, templatesPanel, this.radioFromDirectory);
        this.gridBagUtils.addToGridBagLayout(1, 1, 1, 1, 1, 0, templatesPanel, fromDirectoryPanel);
        this.gridBagUtils.addToGridBagLayout(0, 2, 2, 1, 0, 0, templatesPanel, this.radioFromDatabase);
        this.gridBagUtils.addToGridBagLayout(0, 3, templatesPanel, this.databasePanel);
        this.gridBagUtils.addToGridBagLayout(0, 4, 1, 1, 0, 1, templatesPanel, new JLabel());
        this.gridBagUtils.clearGridBagConstraints();

        this.radioFromDirectory.setAlignmentX(0.0F);
        this.radioFromDatabase.setAlignmentX(0.0F);
        return templatesPanel;
    }


    private void initializeDatabasePanel() {
        this.databasePanel = new JPanel();
        this.databasePanel.setBorder(BorderFactory.createTitledBorder(""));

        GridBagLayout databaseLayout = new GridBagLayout();
        databaseLayout.columnWidths = new int[]{55, 40, 90, 90, 1};
        this.databasePanel.setLayout(databaseLayout);

        KeyListener textFieldKeyListener = new DatabaseTextFieldKeyListener();
        this.txtDSN = new JTextField();
        this.txtDSN.addKeyListener(textFieldKeyListener);

        this.txtDBUser = new JTextField();
        this.txtDBUser.addKeyListener(textFieldKeyListener);

        this.txtDBPassword = new JPasswordField();
        this.txtDBPassword.addKeyListener(textFieldKeyListener);

        this.btnConnect = new JButton("Connect*");
        this.btnConnect.addActionListener(this);

        this.btnReset = new JButton("Reset");
        this.btnReset.addActionListener(this);

        this.cmbTable = new JComboBox<>();
        this.cmbTable.addActionListener(this);

        this.cmbTemplateColumn = new JComboBox<>();
        this.cmbIdColumn = new JComboBox<>();

        this.lblDBConnectMsg = new JLabel("*- Connect database to change table settings");

        this.gridBagUtils.setInsets(new Insets(5, 5, 5, 5));
        this.gridBagUtils.addToGridBagLayout(0, 1, 4, 1, this.databasePanel, new JLabel("DSN:"));
        this.gridBagUtils.addToGridBagLayout(0, 3, this.databasePanel, new JLabel("UID:"));
        this.gridBagUtils.addToGridBagLayout(0, 4, this.databasePanel, new JLabel("PWD:"));
        this.gridBagUtils.addToGridBagLayout(1, 1, this.databasePanel, this.txtDSN);
        this.gridBagUtils.addToGridBagLayout(1, 3, this.databasePanel, this.txtDBUser);
        this.gridBagUtils.addToGridBagLayout(1, 4, this.databasePanel, this.txtDBPassword);
        this.gridBagUtils.addToGridBagLayout(2, 5, 1, 1, this.databasePanel, this.btnConnect);
        this.gridBagUtils.addToGridBagLayout(3, 5, this.databasePanel, this.btnReset);
        this.gridBagUtils.addToGridBagLayout(4, 5, 1, 1, 1, 0, this.databasePanel, new JLabel());
        this.gridBagUtils.addToGridBagLayout(0, 6, 2, 1, 0, 0, this.databasePanel, new JLabel("Table:"));
        this.gridBagUtils.addToGridBagLayout(0, 7, this.databasePanel, new JLabel("Template column:"));
        this.gridBagUtils.addToGridBagLayout(0, 8, this.databasePanel, new JLabel("ID column:"));
        this.gridBagUtils.addToGridBagLayout(2, 6, this.databasePanel, this.cmbTable);
        this.gridBagUtils.addToGridBagLayout(2, 7, 3, 1, this.databasePanel, this.cmbTemplateColumn);
        this.gridBagUtils.addToGridBagLayout(2, 8, this.databasePanel, this.cmbIdColumn);
        this.gridBagUtils.addToGridBagLayout(0, 9, 4, 1, this.databasePanel, this.lblDBConnectMsg);
    }

    private JPanel initializeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));

        this.btnResetAll = new JButton("Reset all");
        this.btnResetAll.addActionListener(this);

        this.btnOK = new JButton("OK");
        this.btnOK.setPreferredSize(new Dimension(75, 25));
        this.btnOK.addActionListener(this);

        this.btnCancel = new JButton("Cancel");
        this.btnCancel.setPreferredSize(new Dimension(75, 25));
        this.btnCancel.addActionListener(this);

        buttonPanel.add(Box.createHorizontalStrut(3));
        buttonPanel.add(this.btnResetAll);
        buttonPanel.add(Box.createGlue());
        buttonPanel.add(this.btnOK);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(this.btnCancel);
        buttonPanel.add(Box.createHorizontalStrut(3));
        return buttonPanel;
    }

    private void loadSettings() {
        this.txtServer.setText(this.settings.getServer());
        this.spinnerClientPort.setValue(Integer.valueOf(this.settings.getClientPort()));
        this.spinnerAdminPort.setValue(Integer.valueOf(this.settings.getAdminPort()));

        this.txtDSN.setText(this.settings.getDSN());
        this.txtDBUser.setText(this.settings.getDBUser());
        this.txtDBPassword.setText(this.settings.getDBPassword());

        setUseDb(this.settings.isTemplateSourceDb());
        this.txtDirectoryPath.setText(this.settings.getTemplateDirectory());

        if (this.settings.isDSNConnected()) {
            listTables();
        }
        setTableSettingSelectionEnabled(this.settings.isDSNConnected());
    }

    private void resetAllToDefault() {
        this.settings.loadDefaultConnectionSettings();
        this.settings.loadDefaultDatabaseConnectionSettings();
        loadSettings();
    }

    private void resetDatabaseConnectionSettings() {
        this.settings.loadDefaultDatabaseConnectionSettings();
        this.txtDSN.setText(this.settings.getDSN());
        this.txtDBUser.setText(this.settings.getDBUser());
        this.txtDBPassword.setText(this.settings.getDBPassword());

        this.cmbTable.removeActionListener(this);
        this.cmbTable.removeAllItems();
        this.cmbIdColumn.removeAllItems();
        this.cmbTemplateColumn.removeAllItems();
    }

    private void selectTemplateSource() {
        boolean isUseDB = this.radioFromDatabase.isSelected();
        this.settings.setTemplateSourceDb(isUseDB);
        setDatabasePanelEnabled(isUseDB);
        this.btnBrowse.setEnabled(!isUseDB);
        this.txtDirectoryPath.setEnabled(!isUseDB);
    }

    private void setDatabasePanelEnabled(boolean enabled) {
        for (Component c : this.databasePanel.getComponents()) {
            c.setEnabled(enabled);
        }
        if (enabled) {
            this.txtDSN.setEnabled(enabled);
            this.txtDBUser.setEnabled(enabled);
            this.txtDBPassword.setEnabled(enabled);
            this.cmbTable.setEnabled((this.settings.isDSNConnected() && enabled));
            this.cmbTemplateColumn.setEnabled((this.settings.isDSNConnected() && enabled));
            this.cmbIdColumn.setEnabled((this.settings.isDSNConnected() && enabled));
        }
    }

    private void setTableSettingSelectionEnabled(boolean enabled) {
        this.cmbTable.setEnabled((this.settings.isDSNConnected() && enabled));
        this.cmbTemplateColumn.setEnabled((this.settings.isDSNConnected() && enabled));
        this.cmbIdColumn.setEnabled((this.settings.isDSNConnected() && enabled));
        this.lblDBConnectMsg.setVisible(!enabled);
        this.btnConnect.setText(enabled ? "Connect" : "Connect*");
    }

    private void applyChanges() {
        this.settings.setTemplateSourceDb(this.radioFromDatabase.isSelected());
        this.settings.setTemplateDirectory(this.txtDirectoryPath.getText());
        this.settings.setDSN(this.txtDSN.getText().trim());
        String value = String.valueOf(this.cmbTable.getSelectedItem());
        if (value != null && !value.equals("")) {
            this.settings.setTable(value);
        }
        this.settings.setDBUser(this.txtDBUser.getText().trim());
        this.settings.setDBPassword(String.valueOf(this.txtDBPassword.getPassword()));
        value = String.valueOf(this.cmbIdColumn.getSelectedItem());
        if (value != null && !value.equals("")) {
            this.settings.setIdColumn(value);
        }
        value = String.valueOf(this.cmbTemplateColumn.getSelectedItem());
        if (value != null && !value.equals("")) {
            this.settings.setTemplateColumn(value);
        }
        this.settings.setClientPort(((Integer) this.spinnerClientPort.getValue()).intValue());
        this.settings.setAdminPort(((Integer) this.spinnerAdminPort.getValue()).intValue());
        this.settings.setServer(this.txtServer.getText().trim());
        this.settings.save();
        dispose();
    }


    private void listCollumns(DatabaseConnection db) {
        this.cmbIdColumn.removeAllItems();
        this.cmbTemplateColumn.removeAllItems();

        if (this.cmbTable.getSelectedItem() != null) {
            try {
                String[] collumns = db.getColumns(String.valueOf(this.cmbTable.getSelectedItem()));
                for (String c : collumns) {
                    this.cmbIdColumn.addItem(c);
                    this.cmbTemplateColumn.addItem(c);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                MessageUtils.showError(this, e);
            }
        }
    }

    private void connectToDatabase() {
        this.settings.setDSN(this.txtDSN.getText().trim());
        this.settings.setDBUser(this.txtDBUser.getText().trim());
        this.settings.setDBPassword(String.valueOf(this.txtDBPassword.getPassword()));
        this.settings.setDSNConnection(true);
        listTables();
    }


    private void listTables() {
        try {
            String table = this.settings.getTable();
            DatabaseConnection db = new DatabaseConnection();
            db.checkConnection();
            this.cmbTable.removeAllItems();
            this.cmbTable.removeActionListener(this);

            String[] tables = db.getTables();
            for (String t : tables) {
                this.cmbTable.addItem(t);
            }
            this.cmbTable.addActionListener(this);
            if (comboBoxContainsItem(this.cmbTable, table)) {
                this.cmbTable.setSelectedItem(table);
            } else if (tables.length > 0) {
                this.cmbTable.setSelectedIndex(0);
            }

            setTableSettingSelectionEnabled(true);
        } catch (SQLException ex) {
            this.settings.setDSNConnection(true);
            MessageUtils.showError(this, "Database connetion failed due to: %s", new Object[]{ex});
        }
    }


    private boolean comboBoxContainsItem(JComboBox<?> cmbBox, Object item) {
        for (int i = 0; i < cmbBox.getItemCount(); i++) {
            if (cmbBox.getItemAt(i).equals(item)) {
                return true;
            }
        }
        return false;
    }

    private void loadTableInformation() {
        this.settings.setTable(String.valueOf(this.cmbTable.getSelectedItem()));
        DatabaseConnection db = new DatabaseConnection();
        try {
            db.checkConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        listCollumns(db);
        String column = this.settings.getIdColumn();
        if (comboBoxContainsItem(this.cmbIdColumn, column)) {
            this.cmbIdColumn.setSelectedItem(column);
        } else if (this.cmbIdColumn.getItemCount() > 0) {
            this.cmbIdColumn.setSelectedIndex(0);
        }
        column = this.settings.getTemplateColumn();
        if (comboBoxContainsItem(this.cmbTemplateColumn, column)) {
            this.cmbTemplateColumn.setSelectedItem(column);
        } else if (this.cmbTemplateColumn.getItemCount() > 0) {
            this.cmbTemplateColumn.setSelectedIndex(0);
        }
    }

    private void browseForDirectory() {
        String path = this.txtDirectoryPath.getText().trim();
        if (path != null && !path.equals("")) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                this.folderBrowserDialog.setCurrentDirectory(dir);
            }
        }
        if (this.folderBrowserDialog.showOpenDialog(this) == 0) {
            this.txtDirectoryPath.setText(this.folderBrowserDialog.getSelectedFile().getPath());
        }
    }

    private void setUseDb(boolean useDb) {
        if (useDb) {
            this.radioFromDatabase.setSelected(true);
        } else {
            this.radioFromDirectory.setSelected(true);
        }
        selectTemplateSource();
    }

    private boolean checkDBStatus() {
        boolean isUseDB = this.radioFromDatabase.isSelected();

        if (isUseDB) {
            if (!this.settings.isDSNConnected()) {
                MessageUtils.showInformation(this, "Connection with database must be established before proceeding");
                return false;
            }
        } else {
            String templateDir = this.txtDirectoryPath.getText().trim();
            if (templateDir == null || templateDir.equals("")) {
                MessageUtils.showInformation(this, "Specified directory doesn't exist");
                return false;
            }
            File directory = new File(templateDir);
            if (!directory.exists() || !directory.isDirectory()) {
                MessageUtils.showInformation(this, "Specified directory doesn't exist");
                return false;
            }
        }
        return true;
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.radioFromDatabase || source == this.radioFromDirectory) {
            selectTemplateSource();
        } else if (source == this.btnBrowse) {
            browseForDirectory();
        } else if (source == this.btnConnect) {
            connectToDatabase();
        } else if (source == this.btnReset) {
            resetDatabaseConnectionSettings();
        } else if (source == this.cmbTable) {
            loadTableInformation();
        } else if (source == this.btnResetAll) {
            resetAllToDefault();
        } else if (source == this.btnOK && checkDBStatus()) {
            applyChanges();
        } else if (source == this.btnCancel) {
            this.settings.load();
            applyChanges();
            dispose();
        }
    }


    private class DatabaseTextFieldKeyListener
            extends KeyAdapter {
        private DatabaseTextFieldKeyListener() {
        }

        public void keyTyped(KeyEvent e) {
            if (ConnectionSettingsDialog.this.settings.isDSNConnected())
                ConnectionSettingsDialog.this.setTableSettingSelectionEnabled(false);
        }
    }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\settings\ConnectionSettingsDialog.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */