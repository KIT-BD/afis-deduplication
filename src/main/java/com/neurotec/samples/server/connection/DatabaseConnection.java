package com.neurotec.samples.server.connection;

import com.neurotec.biometrics.NFRecord;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.biometrics.standards.BDIFStandard;
import com.neurotec.biometrics.standards.FMRecord;
import com.neurotec.io.NBuffer;
import com.neurotec.samples.server.settings.Settings;
import com.neurotec.samples.server.util.PropertyLoader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DatabaseConnection implements TemplateLoader {
  private String dsn;
  private String user;
  private String password;
  private String table;
  private String templateColumn;
  private String idColumn;
  private Connection connection;
  private Statement statResultSet;
  private ResultSet resultSet;
  private boolean isStarted;
  private boolean hasMultipleColumns;
  private List<String> templateColList;
  private boolean isBase64Encoded;
  private String fString = "ll_template,lr_template,lm_template,li_template,lt_template,rl_template,rr_template,rm_template,ri_template,rt_template";

  public DatabaseConnection() {
    PropertyLoader settings = new PropertyLoader();
    this.dsn = settings.getDSN();
    this.user = settings.getUser();
    this.password = settings.getPassword();
    this.table = settings.getTable();
    this.templateColumn = settings.getTemplateColumn();
    this.idColumn = settings.getIdColumn();

    this.hasMultipleColumns = false;
    this.templateColList = null;
    this.isBase64Encoded = false;

    if (this.hasMultipleColumns) {
      this.templateColList = Arrays.asList(this.fString.split(","));
    }
  }

  protected final Connection getConnection() {
    return this.connection;
  }

  protected final void setConnection(Connection connection) {
    this.connection = connection;
  }

  protected final ResultSet getResultSet() {
    return this.resultSet;
  }

  protected final String getDSN() {
    return this.dsn;
  }

  protected final String getUser() {
    return this.user;
  }

  protected final String getPassword() {
    return this.password;
  }

  protected final String getTable() {
    return this.table;
  }

  protected final void connect() throws SQLException {
    try {
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      setConnection(DriverManager.getConnection(getConnectionString(), this.user, this.password));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  protected final String getConnectionString() {
    return String.format("%s", new Object[] { this.dsn });
  }

  public final String[] getTables() throws SQLException {
    ResultSet result = null;
    List<String> results = new ArrayList<>();
    connect();
    try {
      DatabaseMetaData meta = getConnection().getMetaData();
      result = meta.getTables(null, null, null, new String[] { "TABLE" });
      while (result.next()) {
        String tableName = result.getString("TABLE_NAME");
        results.add(tableName);
      }
      return results.<String>toArray(new String[results.size()]);
    } finally {
      if (result != null) {
        result.close();
      }
      closeConnection();
    }
  }

  public final String[] getColumns(String table) throws SQLException {
    connect();
    ResultSet result = null;
    try {
      List<String> columnNames = new ArrayList<>();

      DatabaseMetaData meta = getConnection().getMetaData();
      result = meta.getColumns(null, null, table, null);

      while (result.next()) {
        String columnName = result.getString("COLUMN_NAME");
        columnNames.add(columnName);
      }

      return columnNames.<String>toArray(new String[columnNames.size()]);
    } finally {
      if (result != null) {
        result.close();
      }
      closeConnection();
    }
  }


  public final boolean equals(Object obj) {
    if (!(obj instanceof DatabaseConnection)) {
      return false;
    }
    DatabaseConnection target = (DatabaseConnection)obj;
    return (this.idColumn == target.idColumn && this.password == target.password && this.dsn == target.dsn && this.table == target.table && this.templateColumn == target.templateColumn && this.user == target.user);
  }



  public final int hashCode() {
    return super.hashCode();
  }


  public final String toString() {
    return String.format("dsn: %s; table: %s;", new Object[] { this.dsn, this.table });
  }


  public final synchronized void beginLoad() throws SQLException {
    if (this.isStarted) {
      throw new IllegalStateException();
    }
    this.isStarted = true;
    connect();
    this.statResultSet = this.connection.createStatement();
    if (!this.hasMultipleColumns) {
      this.resultSet = this.statResultSet.executeQuery(String.format("SELECT %s, %s FROM %s", new Object[] { this.idColumn, this.templateColumn, this.table }));
    } else {
      this.resultSet = this.statResultSet.executeQuery(String.format("SELECT %s, %s ,%s , %s ,%s ,%s , %s , %s , %s , %s , %s FROM %s", new Object[] { this.idColumn, "ll_template", "lr_template", "lm_template", "li_template", "lt_template", "rl_template", "rr_template", "rm_template", "ri_template", "rt_template", this.table }));
    }
  }



  public final synchronized void endLoad() throws SQLException {
    if (this.resultSet != null) {
      this.resultSet.close();
    }
    if (this.statResultSet != null) {
      this.statResultSet.close();
    }
    closeConnection();
    this.isStarted = false;
  }





  public final synchronized NSubject[] loadNext(int count) throws SQLException {
    if (!this.isStarted) {
      throw new IllegalStateException("Template loading not started");
    }

    List<NSubject> results = new ArrayList<>();


    while (results.size() < count && this.resultSet.next()) {

      NSubject tmpSubject = new NSubject();
      NTemplate tmpTemplate = new NTemplate();
      String base64Tmpl = null;
      byte[] tmplData = null;
      FMRecord fmRecord = null;

      String id = this.resultSet.getString(this.idColumn);

      if (this.hasMultipleColumns) {

        for (String nowCol : this.templateColList) {

          if (nowCol == null || nowCol.isEmpty()) {
            continue;
          }

          base64Tmpl = this.resultSet.getString(nowCol.trim());

          if (base64Tmpl == null) {
            continue;
          }


          try {
            tmplData = Base64.getDecoder().decode(base64Tmpl);

            fmRecord = new FMRecord(new NBuffer(tmplData), BDIFStandard.ISO);

            NFRecord nfRecord = new NFRecord(fmRecord.save());



            tmpTemplate.getFingers().getRecords().add(nfRecord);
          }
          catch (Exception exc) {
            System.err.println("Error occurred : " + exc.getMessage());
            exc.printStackTrace();
          }
        }


        tmpSubject.setTemplate(tmpTemplate);
        tmpSubject.setId(id);
      }
      else {

        tmplData = readTemplateData(this.resultSet, this.templateColumn, this.isBase64Encoded);

        tmpSubject.setTemplateBuffer(new NBuffer(tmplData));
        tmpSubject.setId(id);
      }

      results.add(tmpSubject);
    }

    return results.<NSubject>toArray(new NSubject[results.size()]);
  }

  public final synchronized int getTemplateCount() throws SQLException {
    if (this.isStarted) {
      throw new IllegalStateException("Can not get count while loading started");
    }
    connect();
    Statement stat = null;
    ResultSet res = null;
    try {
      stat = this.connection.createStatement();
      res = stat.executeQuery(String.format("SELECT COUNT(*) FROM %s", new Object[] { this.table }));

      if (res.next()) {
        return res.getInt(1);
      }
    } finally {
      if (res != null) {
        res.close();
      }
      if (stat != null) {
        stat.close();
      }
      closeConnection();
    }
    return -1;
  }

  public final void checkConnection() throws SQLException {
    if (this.isStarted) {
      return;
    }
    connect();
    closeConnection();
  }

  protected final void closeConnection() throws SQLException {
    if (this.connection != null) {
      this.connection.close();
    }
  }

  public final void dispose() throws SQLException {
    closeConnection();
  }

  private byte[] readTemplateData(ResultSet resultSet, String tmplCol, boolean isBase64) {
    byte[] tmplData = null;
    try {
      if (isBase64) {
        String base64Tmpl = resultSet.getString(tmplCol);
        tmplData = Base64.getDecoder().decode(base64Tmpl);
      } else {

        InputStream is = resultSet.getBinaryStream(tmplCol);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = 0;

        while ((len = is.read()) != -1) {
          bos.write(len);
        }
        tmplData = bos.toByteArray();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return tmplData;
  }
}


/* Location:              D:\NeuroTechnology\AFISServerNative.jar!\com\neurotec\samples\server\connection\DatabaseConnection.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */