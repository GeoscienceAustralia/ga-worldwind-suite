/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2002, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package gistoolkit.datasources.db2spatialextender;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.sql.*;

/**
 * Class for creating a SQL window on a DB2 Database
 */
public class DB2Query extends JFrame{
    private JTextField myTextFieldDatabaseURLBase = new JTextField("jdbc:db2");
    private String getURLBase(){return myTextFieldDatabaseURLBase.getText();}
    private JTextField myTextFieldDatabaseServername= new JTextField("Servername");
    private String getServername(){
        String tempServer = myTextFieldDatabaseServername.getText();
        setTitle(tempServer + " - DB2Query");
        return tempServer;
    }
    private JTextField myTextFieldDatabaseName = new JTextField("Databasename");
    private String getDatabaseName(){return myTextFieldDatabaseName.getText();}
    private JTextField myTextFieldDatabaseSchema = new JTextField("DatabaseSchema");
    private String getSchema(){return myTextFieldDatabaseSchema.getText();}
    private JTextField myTextFieldDatabaseUsername = new JTextField("Username");
    private String getUsername(){return myTextFieldDatabaseUsername.getText();}
    private JTextField myTextFieldDatabaseDriver = new JTextField("COM.ibm.db2.jdbc.net.DB2Driver");
    private String getDriver(){return myTextFieldDatabaseDriver.getText();}
    private JPasswordField myPasswordFieldDatabasePassword = new JPasswordField("Password");
    private String getPassword(){return new String(myPasswordFieldDatabasePassword.getPassword());}
    private JTextField myTextFieldDatabasePort =  new JTextField("1150");
    private int getDatabasePort(){try{return Integer.parseInt(myTextFieldDatabasePort.getText());}catch(NumberFormatException e){}return 1150;}
    /** Creates new DB2Query */
    public DB2Query() {
        initPanel();
        setTitle("Database Panel");
    }
    
    
    /** Set up the GUI components for the main panel. */
    private void initPanel(){
        
        // create a split pane with the connect information at the top.
        JSplitPane tempSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel tempConnectPanel = initConnectPanel();
        tempConnectPanel.setMinimumSize(new Dimension(0,0));
        tempSplitPane.setTopComponent(tempConnectPanel);
        
        // create the tab panel to hold the sub panels.
        JTabbedPane tempTabPane = new JTabbedPane();
        tempSplitPane.setBottomComponent(tempTabPane);
        
        // create the tab for querying the database.
        myDescribePanel = new MyDescribePanel();
        tempTabPane.addTab("Describe", myDescribePanel);
        myQueryPanel = new MyQueryPanel();
        tempTabPane.addTab("Query", myQueryPanel);
        myScriptPanel = new MyScriptPanel();
        tempTabPane.addTab("Script", myScriptPanel);
        setContentPane(tempSplitPane);
    }
    
    /**
     * Set up the GUI components for requesting the information from the user.
     */
    private JPanel initConnectPanel() {
        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        
        // URL Base
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        JLabel tempLabel = new JLabel("URL Base");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseURLBase, c);
        myTextFieldDatabaseURLBase.setText(System.getProperty("DBURLBase", myTextFieldDatabaseURLBase.getText()));
        
        // Driver
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Driver");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseDriver, c);
        myTextFieldDatabaseDriver.setText(System.getProperty("DBDriver", myTextFieldDatabaseDriver.getText()));
        
        // Servername
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Servername");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseServername, c);
        myTextFieldDatabaseServername.setText(System.getProperty("DBServername", myTextFieldDatabaseServername.getText()));
        
        // Port
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Port");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabasePort, c);
        myTextFieldDatabasePort.setText(System.getProperty("DBPort", myTextFieldDatabasePort.getText()));
        
        // DatabaseName
        c.gridx = 2;
        c.gridy=0;
        c.weightx = 0;
        tempLabel = new JLabel("Database Name");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseName, c);
        myTextFieldDatabaseName.setText(System.getProperty("DBDatabaseName", myTextFieldDatabaseName.getText()));
        
        // Schema
        c.gridx = 2;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Schema");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseSchema, c);
        myTextFieldDatabaseSchema.setText(System.getProperty("DBSchema", myTextFieldDatabaseSchema.getText()));
        
        // Username
        c.gridx = 2;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Username");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myTextFieldDatabaseUsername, c);
        myTextFieldDatabaseUsername.setText(System.getProperty("DBUsername", myTextFieldDatabaseUsername.getText()));
        
        // Password
        c.gridx = 2;
        c.gridy++;
        c.weightx = 0;
        tempLabel = new JLabel("Password");
        tempPanel.add(tempLabel, c);
        c.gridx++;
        c.weightx = 1;
        tempPanel.add(myPasswordFieldDatabasePassword, c);
        myPasswordFieldDatabasePassword.setText(System.getProperty("DBPassword"));
        
        // add some space at the bottom
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        tempPanel.add(new JPanel(), c);
        return tempPanel;
    }
    /** Return the frame associated with this application so sub dialogs can be displayed properly. */
    private JFrame getFrame(){return this;}
    
    private class MyDescribePanel extends JPanel implements ActionListener, Runnable, ListSelectionListener {
        // The list for displaying the tables in the database.
        JList myList;
        // The model for the list.
        DefaultListModel myListModel = new DefaultListModel();
        
        // The button for listing the Tables.
        private JButton myButtonDescribeExecute = new JButton("Describe");
        
        // the label for show ing the status of the describe
        private JLabel myLabelStatus = new JLabel("Status");
        // The status label for this panel.
        private JLabel getLabelStatus(){return myLabelStatus;}
        
        // The text area for showing the sql for the tables.
        private JTextArea myTextAreaDescribe = new JTextArea();
        
        // boolean to tell the user interface to listen for mouse clicks.
        private boolean myListen = true;
        
        public MyDescribePanel(){
            initDescribePanel();
        }
        
        private void initDescribePanel(){
            setLayout(new BorderLayout());

            // The top panel
            // Describe button.
            JPanel tempPanel = new JPanel(new BorderLayout(4,4));
            tempPanel.add(myButtonDescribeExecute, BorderLayout.WEST);
            tempPanel.add(myLabelStatus, BorderLayout.CENTER);
            add(tempPanel, BorderLayout.NORTH);
            
            // the center panel
            JSplitPane tempSplitPane = new JSplitPane();
            add(tempSplitPane, BorderLayout.CENTER);
            
            // set the left hand panel.
            myList = new JList(myListModel);
            JScrollPane tempScrollPane = new JScrollPane(myList);
            tempSplitPane.setLeftComponent(tempScrollPane);
                        
            // The text Area for showing the output sql.
            tempScrollPane = new JScrollPane(myTextAreaDescribe);
            tempSplitPane.setRightComponent(tempScrollPane);
            myButtonDescribeExecute.addActionListener(this);
            myList.addListSelectionListener(this);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
            if (myThread == null){
                myButtonDescribeExecute.setEnabled(false);
                myThread = new Thread(this);
                myThread.start();
            }
        }
        
        // Thread to hold this class when running
        Thread myThread = null;
        
        // actually retrieve the data from DB2.
        public void run() {
            String tempSchema = getSchema();
            tempSchema = tempSchema.toUpperCase();
            try{
                try {
                    getLabelStatus().setText("Loading Interface");
                    Class.forName(getDriver()).newInstance();
                }
                catch (Exception e) {
                    String tempString = "Error generating database driver\n";
                    tempString = tempString + e;
                    myTextAreaDescribe.setText(tempString);
                    getLabelStatus().setText("Error Loading Interface");
                    throw new Exception(tempString);
                }
                
                Connection con = null;
                
                // URL is jdbc:db2:dbname
                String url = getURLBase()+"//"+getServername()+":"+getDatabasePort()+"/"+getDatabaseName();
                System.out.println("URL="+url);
                try {
                    getLabelStatus().setText("Connecting");
                    con = DriverManager.getConnection(url, getUsername(), getPassword());
                }
                catch (Exception e){
                    String tempString = "Error connecting to database\n";
                    tempString = tempString + e;
                    myTextAreaDescribe.setText(tempString);
                    getLabelStatus().setText("Error connecting to database");
                    myButtonDescribeExecute.setEnabled(true);
                    throw new Exception(tempString);
                }
                
                // retrieve data from the database
                getLabelStatus().setText("Retrieving Data");
                String tempQuery = "";
                
                try{
                    myListModel.removeAllElements();
                    Statement stmt = con.createStatement();
                    tempQuery = "SELECT\n"
                    +"	SYSCAT.TABLES.TABSCHEMA AS TABSCHEMA,\n"
                    +"	SYSCAT.TABLES.TABNAME AS TABNAME,\n"
                    +"	SYSCAT.TABLES.REMARKS AS REMARKS\n"
                    +"FROM\n"
                    +"	SYSCAT.TABLES\n"
                    +"WHERE\n"
                    +"	SYSCAT.TABLES.TABSCHEMA = '"+tempSchema+"'";
                    ResultSet rset = stmt.executeQuery(tempQuery);
                    
                    // Loop through the results
                    String tempOutString = "";
                    Vector tempTableVect = new Vector();
                    while (rset.next()){
                        String[] tempRow = new String[3];
                        tempRow[0] = rset.getString(1);
                        tempRow[1] = rset.getString(2);
                        tempRow[2] = rset.getString(3);
                        tempTableVect.addElement(tempRow);
                    }
                    
                    //Loop through the tables retrieving their rows
                    String tempResultString = "";
                    String tempCommentString = "";
                    String tempColumnName;
                    String tempColumnType;
                    String tempColumnLength;
                    String tempColumnComment;
                    String tempColumnNullable;
                    String tempColumnKeySequence;
                    String tempColumnPrimaryKey;
                    for (int i=0; i<tempTableVect.size(); i++){
                        Vector tempColumnNameVect = new Vector();
                        tempColumnPrimaryKey = null;
                        String[] tempRow = (String[]) tempTableVect.elementAt(i);
                        getLabelStatus().setText("Reading Table "+tempRow[1]);
                        tempQuery = "SELECT\n"
                        +"   SYSCAT.COLUMNS.TABNAME AS TABLENAME,\n"
                        +"   SYSCAT.COLUMNS.COLNAME AS COLNAME,\n"
                        +"   SYSCAT.COLUMNS.TYPENAME AS TYPENAME,\n"
                        +"   SYSCAT.COLUMNS.LENGTH AS LENGTH,\n"
                        +"   SYSCAT.COLUMNS.REMARKS AS REMARKS,\n"
                        +"   SYSCAT.COLUMNS.NULLS AS NULLS,\n"
                        +"   SYSCAT.COLUMNS.KEYSEQ AS KEYSEQ\n"
                        +"FROM\n"
                        +"   SYSCAT.COLUMNS\n"
                        +"WHERE\n"
        	  	 		+"	SYSCAT.COLUMNS.TABSCHEMA = '"+tempRow[0]+"' AND\n"
                        +"  SYSCAT.COLUMNS.TABNAME = '"+tempRow[1]+"'\n";
                        
                        rset = stmt.executeQuery(tempQuery);
                        tempResultString = "CREATE TABLE "+tempSchema+"."+tempRow[1]+"(\n";
                        if (tempRow[2] != null)
                            tempCommentString = "COMMENT ON Table "+tempSchema+"."+tempRow[1] +" IS '"+tempRow[2]+"';\n";
                        else tempCommentString = "";
                        
                        int count = 0;
                        while (rset.next()){
                            if (count != 0) tempResultString = tempResultString + ",\n";
                            tempColumnName = rset.getString(2);
                            tempColumnNameVect.addElement(tempColumnName);
                            tempColumnType = rset.getString(3);
                            tempColumnLength = rset.getString(4);
                            tempColumnComment = rset.getString(5);
                            tempColumnNullable = rset.getString(6);
                            tempColumnKeySequence = rset.getString(7);
                            /** Check for types */
                            if (tempColumnType.startsWith("ST_")) tempColumnType = "db2gse."+tempColumnType;
                            tempResultString = tempResultString +"\t"+ tempColumnName + " ";
                            tempResultString = tempResultString + tempColumnType;
                            if (tempColumnType.equalsIgnoreCase("CHARACTER")){
                                tempResultString = tempResultString + "("+tempColumnLength+")";
                            }
                            if (tempColumnType.equalsIgnoreCase("VARCHAR")){
                                tempResultString = tempResultString + "("+tempColumnLength+")";
                            }
                            if (tempColumnNullable.equals("N")){
                                tempResultString = tempResultString + " NOT NULL";
                            }
                            if (tempColumnComment != null){
                                tempCommentString = tempCommentString + "COMMENT ON COLUMN "+tempSchema+"."+tempRow[1]+"."+tempColumnName+" IS '"+tempColumnComment+"';\n";
                            }
                            if ((tempColumnKeySequence != null) && (tempColumnPrimaryKey == null)){
                                tempColumnPrimaryKey = tempColumnName;
                            }
                            count++;
                        }
                        if (tempColumnPrimaryKey != null) {
                            tempResultString = tempResultString + ",\n\tPRIMARY KEY ("+tempColumnPrimaryKey+")";
                        }
                        tempResultString = tempResultString + "\n);\n";
                        tempResultString = tempResultString + tempCommentString;
                        MyTableDescription tempDescription = new MyTableDescription(tempRow[1], tempResultString, tempColumnNameVect);
                        myListen = false;
                        myListModel.addElement(tempDescription);
                        myListen = true;
                    }
                }
                catch (Exception e){
                    String tempString = "Error executing database query\n";
                    tempString = tempString + tempQuery+"\n";
                    tempString = tempString + e;
                    myTextAreaDescribe.setText(tempString);
                    getLabelStatus().setText("Error executing database query");
                    throw new Exception(tempString);
                }
                
                try{
                    con.close();
                }
                catch (Exception e){
                    String tempString = "Error executing connection close\n";
                    tempString = tempString + e;
                    myTextAreaDescribe.setText(tempString);
                    getLabelStatus().setText("Error executing connection close");
                }
            }
            catch (Exception e){
            }
            myButtonDescribeExecute.setEnabled(true);
            getLabelStatus().setText("Done");
            myThread = null;
        }
        
        public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
            if (myListen){
                MyTableDescription tempMessage = (MyTableDescription) myList.getSelectedValue();
                if (tempMessage == null) return;
                myTextAreaDescribe.setText(tempMessage.myDescription);
            }
            return;
        }
    }
    private MyDescribePanel myDescribePanel = null;
    
    
    /** Class to hold information about a table. */
    private class MyTableDescription {
        public String myTablename;
        public String myDescription;
        public Vector myVectorColumnNames;
        public MyTableDescription(String inName, String inDescription, Vector inColumnnames){
            myTablename = inName;
            myDescription = inDescription;
            myVectorColumnNames = inColumnnames;
        }
        
        public String toString(){
            return myTablename;
        }
    }
    
    private class MyQueryPanel extends JPanel implements ActionListener, Runnable {
        // The button for executing the query.
        private JButton myButtonExecute = new JButton("Execute");
        
        // combo box for holding the old queries.
        JComboBox myComboOldQueries = new JComboBox();
        
        // the label for show ing the status of the describe
        private JLabel myLabelStatus = new JLabel("Status");
        // The status label for this panel.
        private JLabel getLabelStatus(){return myLabelStatus;}
        
        // The text area used for typing the query.
        private JTextArea myTextAreaQuery = new JTextArea();
        
        // The JTable for displaying the results of the query.
        private JTable myTable = new JTable();
        
        // button for saving the data in the table to a file.
        private JButton myButtonSaveResults = new JButton("Save Results");
        
        // Dialog for saving files.
        private JFileChooser myFileChooser = new JFileChooser();
        
        public MyQueryPanel(){
            initQueryPanel();
            String tempDefaultDirectory = System.getProperty("DBSaveDir");
            if (tempDefaultDirectory != null) myFileChooser.setCurrentDirectory(new File(tempDefaultDirectory));
        }
        
        private void initQueryPanel(){
            setLayout(new BorderLayout());
            
            // the split pane.
            JSplitPane tempSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            add(tempSplitPane, BorderLayout.CENTER);
            
            // set the Top panel.
            JPanel tempTopPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            
            // Query button.
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.BOTH;
            tempTopPanel.add(myButtonExecute, c);
            c.gridx++;
            c.weightx = 1;
            tempTopPanel.add(myLabelStatus, c);
            
            // The text area for the query
            c.gridx = 0;
            c.gridy++;
            c.weighty = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            JScrollPane tempScrollPane = new JScrollPane(myTextAreaQuery);
            tempTopPanel.add(tempScrollPane, c);
            tempSplitPane.setTopComponent(tempTopPanel);
            
            // set the bottom panel.
            tempSplitPane.setBottomComponent(new JScrollPane(myTable));
            myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            
            // the save button.
            JPanel tempButtonPanel = new JPanel(new BorderLayout());
            tempButtonPanel.add(myButtonSaveResults, BorderLayout.EAST);
            add(tempButtonPanel, BorderLayout.SOUTH);
            
            // the action events.
            myButtonExecute.addActionListener(this);
            myButtonSaveResults.addActionListener(this);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent inAE) {
            if (inAE.getSource() == myButtonExecute){
                if (myThread == null){
                    myButtonExecute.setEnabled(false);
                    myThread = new Thread(this);
                    myThread.start();
                }
            }
            if (inAE.getSource() == myButtonSaveResults){
                myFileChooser.showSaveDialog(getFrame());
                File tempFile = myFileChooser.getSelectedFile();
                if (tempFile != null){
                    try{
                        FileWriter tempFileWriter = new FileWriter(tempFile);
                        if (myTable != null){
                            for (int i=0; i<myTable.getRowCount(); i++){
                                if (i>0) tempFileWriter.write("\n");
                                for (int j=0; j<myTable.getColumnCount(); j++){
                                    if (j>0) tempFileWriter.write(",");
                                    tempFileWriter.write("\""+myTable.getValueAt(i, j)+"\"");
                                }
                            }
                        }
                        tempFileWriter.close();
                    }
                    catch (Exception e){
                        System.out.println(e);
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        // thread to use to run the run command.
        private Thread myThread = null;
        
        public void run() {
            String tempSchema = getSchema();
            tempSchema = tempSchema.toUpperCase();
            try{
                try {
                    getLabelStatus().setText("Loading Interface");
                    Class.forName(getDriver()).newInstance();
                }
                catch (Exception e) {
                    String tempString = "Error generating database driver\n";
                    tempString = tempString + e;
                    getLabelStatus().setText("Error Loading Interface");
                    throw new Exception(tempString);
                }
                
                Connection con = null;
                
                // URL is jdbc:db2:dbname
                String url = getURLBase()+"//"+getServername()+":"+getDatabasePort()+"/"+getDatabaseName();
                System.out.println("URL="+url);
                try {
                    getLabelStatus().setText("Connecting");
                    con = DriverManager.getConnection(url, getUsername(), getPassword());
                }
                catch (Exception e){
                    String tempString = "Error connecting to database\n";
                    tempString = tempString + e;
                    getLabelStatus().setText("Error connecting to database");
                    throw new Exception(tempString);
                }
                
                // retrieve data from the database
                String tempQuery = "";
                
                try{
                    Statement stmt = con.createStatement();
                    tempQuery = myTextAreaQuery.getText();
                    tempQuery = tempQuery.trim();
                    System.out.println(tempQuery);
                    if (tempQuery.toUpperCase().startsWith("SELECT")){
                        getLabelStatus().setText("Retrieving Data");
                        ResultSet rset = stmt.executeQuery(tempQuery);
                    
                        // Add the columns
                        getLabelStatus().setText("Populating Display");
                        ResultSetMetaData rmet = rset.getMetaData();
                        int tempColCount = rmet.getColumnCount();
                        DefaultTableModel tempModel = new DefaultTableModel(0, tempColCount);
                        myTable.setModel(tempModel);

                        for (int i=0; i<tempColCount; i++){
                            TableColumn tempColumn = myTable.getColumnModel().getColumn(i);
                            tempColumn.setHeaderValue(rmet.getColumnName(i+1));
                        }

                        // add the rows
                        String[] tempString = new String[tempColCount];
                        long tempCurrentRow = 0;
                        while (rset.next()){
                            tempCurrentRow = tempCurrentRow + 1;
                            for (int i=0; i<tempColCount; i++) tempString[i] = rset.getString(i+1);

                            tempModel.addRow(tempString);
                        }
                    }
                    else{
                        getLabelStatus().setText("Executing Statement");
                        stmt.executeUpdate(tempQuery);                        
                    }
                }
                catch (Exception e){
                    String tm = e.getMessage();
                    boolean tempMaskNoResultSet = false;
                    if (tm != null){
                        if (tm.indexOf("CLI0101E") != -1){
                            tempMaskNoResultSet = true;
                        }
                    }
                    if (!tempMaskNoResultSet){
                        String tempString = "Error executing database query\n";
                        tempString = tempString + tempQuery+"\n";
                        tempString = tempString + e;
                        System.out.println(tempString);
                        getLabelStatus().setText("Error "+e.getMessage());
                        throw new Exception(tempString);
                    }
                }
                
                try{
                    getLabelStatus().setText("Closing Interface");
                    con.close();
                }
                catch (Exception e){
                    String tempString = "Error executing connection close\n";
                    tempString = tempString + e;
                    //                getTextPaneResults().setText(tempString);
                    getLabelStatus().setText("Error "+e.getMessage());
                    throw new Exception(tempString);
                }
                getLabelStatus().setText("Done");
            }
            catch(Exception e){
            }
            myButtonExecute.setEnabled(true);
            myThread = null;
            return;
        }
    }
    private MyQueryPanel myQueryPanel = null;
    
    private class MyScriptPanel extends JPanel implements ActionListener, Runnable {
        // The button for executing the query.
        private JButton myButtonExecute = new JButton("Execute");
        
        // combo box for holding the old queries.
        JComboBox myComboOldQueries = new JComboBox();
        
        // the label for show ing the status of the describe
        private JLabel myLabelStatus = new JLabel("Status");
        // The status label for this panel.
        private JLabel getLabelStatus(){return myLabelStatus;}
        
        // The text area used for typing the query.
        private JTextArea myTextAreaQuery = new JTextArea();
        
        // The JTable for displaying the results of the query.
        private JTable myTable = new JTable();
        
        // button for saving the data in the table to a file.
        private JButton myButtonSaveResults = new JButton("Save Results");
        
        // Dialog for saving files.
        private JFileChooser myFileChooser = new JFileChooser();
        
        public MyScriptPanel(){
            initScriptPanel();
            String tempDefaultDirectory = System.getProperty("DBSaveDir");
            if (tempDefaultDirectory != null) myFileChooser.setCurrentDirectory(new File(tempDefaultDirectory));
        }
        
        private void initScriptPanel(){
            setLayout(new BorderLayout());
            
            // the split pane.
            JSplitPane tempSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            add(tempSplitPane, BorderLayout.CENTER);
            
            // set the Top panel.
            JPanel tempTopPanel = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 2, 2);
            
            // Query button.
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.BOTH;
            tempTopPanel.add(myButtonExecute, c);
            c.gridx++;
            c.weightx = 1;
            tempTopPanel.add(myLabelStatus, c);
            
            // The text area for the query
            c.gridx = 0;
            c.gridy++;
            c.weighty = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            JScrollPane tempScrollPane = new JScrollPane(myTextAreaQuery);
            tempTopPanel.add(tempScrollPane, c);
            tempSplitPane.setTopComponent(tempTopPanel);
            
            // set the bottom panel.
            tempSplitPane.setBottomComponent(new JScrollPane(myTable));
            myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            
            // the save button.
            JPanel tempButtonPanel = new JPanel(new BorderLayout());
            tempButtonPanel.add(myButtonSaveResults, BorderLayout.EAST);
            add(tempButtonPanel, BorderLayout.SOUTH);
            
            // the action events.
            myButtonExecute.addActionListener(this);
            myButtonSaveResults.addActionListener(this);
        }
        
        public void actionPerformed(java.awt.event.ActionEvent inAE) {
            if (inAE.getSource() == myButtonExecute){
                if (myThread == null){
                    myButtonExecute.setEnabled(false);
                    myThread = new Thread(this);
                    myThread.start();
                }
            }
            if (inAE.getSource() == myButtonSaveResults){
                myFileChooser.showSaveDialog(getFrame());
                File tempFile = myFileChooser.getSelectedFile();
                if (tempFile != null){
                    try{
                        FileWriter tempFileWriter = new FileWriter(tempFile);
                        if (myTable != null){
                            for (int i=0; i<myTable.getRowCount(); i++){
                                if (i>0) tempFileWriter.write("\n");
                                for (int j=0; j<myTable.getColumnCount(); j++){
                                    if (j>0) tempFileWriter.write(",");
                                    tempFileWriter.write("\""+myTable.getValueAt(i, j)+"\"");
                                }
                            }
                        }
                        tempFileWriter.close();
                    }
                    catch (Exception e){
                        System.out.println(e);
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        
        // thread to use to run the run command.
        private Thread myThread = null;
        
        public void run() {
            String tempSchema = getSchema();
            tempSchema = tempSchema.toUpperCase();
            try{
                try {
                    getLabelStatus().setText("Loading Interface");
                    Class.forName(getDriver()).newInstance();
                }
                catch (Exception e) {
                    String tempString = "Error generating database driver\n";
                    tempString = tempString + e;
                    getLabelStatus().setText("Error Loading Interface");
                    throw new Exception(tempString);
                }
                
                Connection con = null;
                
                // URL is jdbc:db2:dbname
                String url = getURLBase()+"//"+getServername()+":"+getDatabasePort()+"/"+getDatabaseName();
                System.out.println("URL="+url);
                try {
                    getLabelStatus().setText("Connecting");
                    con = DriverManager.getConnection(url, getUsername(), getPassword());
                }
                catch (Exception e){
                    String tempString = "Error connecting to database\n";
                    tempString = tempString + e;
                    getLabelStatus().setText("Error connecting to database");
                    throw new Exception(tempString);
                }
                
                // retrieve data from the database
                String tempQuery = "";
                
                try{
                    Statement stmt = con.createStatement();
                    tempQuery = myTextAreaQuery.getText();
                    tempQuery = tempQuery.trim();
                    System.out.println(tempQuery);
                    if (tempQuery.toUpperCase().startsWith("SELECT")){
                        getLabelStatus().setText("Retrieving Data");
                        ResultSet rset = stmt.executeQuery(tempQuery);
                    
                        // Add the columns
                        getLabelStatus().setText("Populating Display");
                        ResultSetMetaData rmet = rset.getMetaData();
                        int tempColCount = rmet.getColumnCount();
                        DefaultTableModel tempModel = new DefaultTableModel(0, tempColCount);
                        myTable.setModel(tempModel);

                        for (int i=0; i<tempColCount; i++){
                            TableColumn tempColumn = myTable.getColumnModel().getColumn(i);
                            tempColumn.setHeaderValue(rmet.getColumnName(i+1));
                        }

                        // add the rows
                        String[] tempString = new String[tempColCount];
                        long tempCurrentRow = 0;
                        while (rset.next()){
                            tempCurrentRow = tempCurrentRow + 1;
                            for (int i=0; i<tempColCount; i++) tempString[i] = rset.getString(i+1);

                            tempModel.addRow(tempString);
                        }
                    }
                    else{
                        getLabelStatus().setText("Parsing Statements");
                        ArrayList tempList = new ArrayList();
                        StringTokenizer st = new StringTokenizer(tempQuery,";");
                        while (st.hasMoreElements()){                      
                            String tempString = st.nextToken(";");
                            getLabelStatus().setText("ExecutingStatement "+tempString);
                            stmt.executeUpdate(tempString);                        
                        }
                    }
                }
                catch (Exception e){
                    String tm = e.getMessage();
                    boolean tempMaskNoResultSet = false;
                    if (tm != null){
                        if (tm.indexOf("CLI0101E") != -1){
                            tempMaskNoResultSet = true;
                        }
                    }
                    if (!tempMaskNoResultSet){
                        String tempString = "Error executing database query\n";
                        tempString = tempString + tempQuery+"\n";
                        tempString = tempString + e;
                        System.out.println(tempString);
                        getLabelStatus().setText("Error "+e.getMessage());
                        throw new Exception(tempString);
                    }
                }
                
                try{
                    getLabelStatus().setText("Closing Interface");
                    con.close();
                }
                catch (Exception e){
                    String tempString = "Error executing connection close\n";
                    tempString = tempString + e;
                    //                getTextPaneResults().setText(tempString);
                    getLabelStatus().setText("Error "+e.getMessage());
                    throw new Exception(tempString);
                }
                getLabelStatus().setText("Done");
            }
            catch(Exception e){
            }
            myButtonExecute.setEnabled(true);
            myThread = null;
            return;
        }
    }
    private MyScriptPanel myScriptPanel = null;
    
    /** The main routine for running the panel. */
    public static void main(String[] inString){
        DB2Query tempQuery = new DB2Query();
        
        // create a window listener to close this window when the X is selected
        WindowListener tempListener = new WindowAdapter(){
            public void windowClosing(WindowEvent inWE){
                System.exit(0);
            }
            public void windowClosed(WindowEvent inWE){
                System.exit(0);
            }
        };
        tempQuery.addWindowListener(tempListener);
        
        // set the size and location on the screen.
        Dimension tempScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        tempQuery.setSize(600, 600);
        Dimension tempSize = tempQuery.getSize();
        tempQuery.setLocation((tempScreenSize.width - tempSize.width) / 2, (tempScreenSize.height - tempSize.height) / 2);
        
        // show the main window.
        tempQuery.setVisible(true);
    }
}
