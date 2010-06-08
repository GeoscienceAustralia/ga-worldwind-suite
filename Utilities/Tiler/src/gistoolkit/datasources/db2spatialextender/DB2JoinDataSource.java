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

import java.sql.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;

/**
 *
 * Class to join a DB2 Datatable to another data source.
 *
 * Why would you do this?  Because it may be more efficient to read shapes from a shape file layer
 * which can cache then on the server, and do the join in this layer.  This will prevent the rereading of the shapes
 * from the database each time that the values are needed. 
 *
 * Reading the shapes from a shape file in most cases, is much faster than reading them from DB2.  This may change with version 8 of the db2 spatial extender.
 *
 * To use this class, first create the data source used to read the shapes.  This data source may be of any type, and
 * can read the shapes from any of the data sources.  
 *
 * Then create the DB2JoinDataSource with the source data source as it's source. 
 * This data source will need to know which attributes it should join on

 * Set the column from the source data that is to be used with the setDatasourceJoinColumn method, and
 * set the column for this data source with the setTableJoinColumn method.  The data source will loop through the
 * records returned. 
 *
 * The data source first reads the data from the source datasource, then it compares the data from the
 * join data source to that of the source data source, and throws away any that do not have a match.  It 
 * also trims the source data source to the envelope.  This results in a standard inner join between the
 * source data, and the join data.
 */
public class DB2JoinDataSource extends SimpleJoinDBDataSource{
    
    /**Name of this datasource.*/
    private String myName = "None";
    /** Return the name of this datasource for display to the user. */
    public String getName(){return myName;}
    /** Set the name of this datasource for display purposes. */
    public void setName(String inName){myName = inName;}
    
    /**When connecting with the driver, a specific urlbase is required.*/
    private String myDatabaseURLBase = "jdbc:db2";
    /** Returns the url base to use when connecting through the JDBC driver. */
    public String getDatabaseURLBase() {return myDatabaseURLBase;}
    /** Sets the url base to use when connecting through the JDBC driver.*/
    public void setDatabaseURLBase(String inDatabaseURLBase){myDatabaseURLBase = inDatabaseURLBase;}
    
    /**
     * This driver must be present in the class path of this application as
     * it is required for connecting to DB2.
     */
    private String myDatabaseDriver = "COM.ibm.db2.jdbc.net.DB2Driver";
    /** Returns the jdbc driver class used for connection to the database. */
    public String getDatabaseDriver() {return myDatabaseDriver;}
    /** Sets the name of the jdbc driver class used for connection to the database. */
    public void setDatabaseDriver(String inDatabaseDriver){myDatabaseDriver = inDatabaseDriver;}
    
    /**The server name of the server to connect to.*/
    private String myDatabaseServername="Servername";
    /** Returns the computer name of the DB2 server. */
    public String getDatabaseServername() {return myDatabaseServername;}
    /** Sets the name of the DB2 Server computer. */
    public void setDatabaseServername(String inDatabaseServername){myDatabaseServername = inDatabaseServername;}
    
    /**The name of the database on the DB2 server to connect to.*/
    public String myDatabaseName = "Databasename";
    /** Returns the name of the DB2 Database. */
    public String getDatabaseName() {return myDatabaseName;}
    /** Sets the name of the DB2 Database. */
    public void setDatabaseName(String inDatabaseName){myDatabaseName = inDatabaseName;}
    
    /**DB2 supports Schemas, and this is the schema under which the data will reside.*/
    private String myDatabaseSchema = "Schemaname";
    /** Returns the name of the schema within the DB2 database where the data resides. */
    public String getDatabaseSchema() {return myDatabaseSchema;}
    /** Sets the name of the schema within the DB2 database where the data resides. */
    public void setDatabaseSchema(String inDatabaseSchema){myDatabaseSchema = inDatabaseSchema;}
    
    /**The username used to connect to the database.*/
    private String myDatabaseUsername = "Username";
    /** Returns the username to use when connecting to the DB2 server. */
    public String getDatabaseUsername() {return myDatabaseUsername;}
    /** Sets the username to use when connecting to the DB2 server. */
    public void setDatabaseUsername(String inDatabaseUsername){myDatabaseUsername = inDatabaseUsername;}
    
    /**Password for connecting to the database.*/
    private String myDatabasePassword = "Password";
    /** Returns the password to use when connecting to the DB2 server. */
    public String getDatabasePassword() {return myDatabasePassword;}
    /** Sets the password to use when connecting to the DB2 server. */
    public void setDatabasePassword(String inDatabasePassword){myDatabasePassword = inDatabasePassword;}
    
    /**Port needed for connecting to the database.*/
    private int myDatabasePort = 1150;
    /** Returns the tcpip port to use when connecting to the DB2 server */
    public int getDatabasePort() {return myDatabasePort;}
    /** Sets the tcpip port to use when connecting to the DB2 server*/
    public void setDatabasePort(int inDatabasePort){myDatabasePort = inDatabasePort;}
    
    /** SQL Query to use to retrieve data from the database. */
    private String myDatabaseQuery = null;
    /** SQL Query to use to retrieve data from the database. */
    public void setDatabaseQuery(String inQuery){myDatabaseQuery = inQuery;}
    /** SQL Query to use in retrieving data from the database. */
    public String getDatabaseQuery(){return myDatabaseQuery;}    
    
    /**Connection to the DB2 datasource used for maintaining persistent connections.*/
    protected Connection myCon = null;
    
    /**Statment used to maintain distributed transactions.*/
    protected Statement myStmt = null;
    
    /**
     * For use with configuration only where the source data source is to be set with the setNode() function.
     */
    public DB2JoinDataSource(){};
    
    /**
     * Create a new DB2JoinDataSource with this data source as the source node.
     */
    public DB2JoinDataSource(DataSource inSourceDataSource){
        super(inSourceDataSource);
    }

    /**
     * Initializes the connection to the database.
     */
    public void connect() throws Exception{
        if (myCon != null) return;
        if (myDatabaseDriver == null)
            throw new Exception("No Database Driver Class defined");
        if (myDatabasePassword == null)
            throw new Exception("No Database Password defined");
        if (myDatabaseSchema == null)
            throw new Exception("No Database Schema defined");
        if (myDatabaseName == null)
            throw new Exception("No Database Name defined");
        if (myDatabaseServername == null)
            throw new Exception("No Database Servername defined");
        if (myDatabaseURLBase == null)
            throw new Exception("No Database URL Base defined");
        if (myDatabaseUsername == null)
            throw new Exception("No Database Username defined");
        
        // load the driver
        try {
            Class.forName(getDatabaseDriver()).newInstance();
        }
        catch (Exception e) {
            System.out.println("Error Loading DBDriver Class " + e);
            throw new Exception("Error Loading Database Driver " + e);
        }
        
        // URL is jdbc:db2:dbname
        String url = "jdbc:db2://"+getDatabaseServername()+":"+myDatabasePort+"/"+getDatabaseName();
        try {
            myCon = DriverManager.getConnection(url, getDatabaseUsername(), getDatabasePassword());
            myStmt = myCon.createStatement();
            
            myStmt.close();
            myStmt = null;
        }
        catch (Exception e) {
            throw new Exception("Error Connecting " + e);
        }
    }

    /** Returns the converter for this Database. */
    public SQLConverter getSQLConverter(){return new DB2SQLConverter();}
            
    /**
     * Reads the objects from the database that fall within the given Envelope.
     * If a null is sent in for the Envelope, all the objects in the shape file are read.
     */
    public synchronized GISDataset readShapes(Envelope inEnvelope) throws Exception {
        
        // retrieve the dataset from the source
        readDataSource(inEnvelope);
        
        // retrieve the connection
        connect();
        
        // create the statement
        Statement tempStatement = myCon.createStatement();
        
        // add the where clause to include the filters.
        StringBuffer sb = new StringBuffer(myDatabaseQuery);
        String tempUpper = myDatabaseQuery.toUpperCase();
        
        // create the where clause
        String tempFilterSQL = getFilterSQL();
        System.out.println("FilterSQL = "+tempFilterSQL);
        String tempWhere = "";
        if ((tempFilterSQL != null) && (tempFilterSQL.length() > 0)){
            tempWhere =  tempFilterSQL;
            // check for joinwhere
            int tempIndex = tempUpper.indexOf("JOINWHERE");
            if (tempIndex == -1){
                // check for where.
                tempIndex = tempUpper.indexOf("WHERE");
                if ( tempIndex == -1){
                    // check for group by
                    tempIndex = tempUpper.indexOf("GROUP BY");
                    if (tempIndex == -1){
                        // check for order by
                        tempIndex = tempUpper.indexOf("ORDER BY");
                        if (tempIndex == -1){
                            // just append the where clause.
                            sb.append(" WHERE "+tempWhere);
                        }
                        else{
                            // insert before the ORDER BY
                            sb.insert(tempIndex, " WHERE "+tempWhere);
                        }
                    }
                    else{
                        // insert before the Group By clause
                        sb.insert(tempIndex, " WHERE "+tempWhere);
                    }
                }
                else{
                    // insert after the where clause
                    sb.insert(tempIndex+5, " "+tempWhere+" AND ");
                }
            }
            else{
                // replace SHAPEWHERE with the shape where clause
                sb.replace(tempIndex, tempIndex+10, tempWhere);
            }
        }

        String tempQuery = sb.toString();
        
        // send the SQLString to the database
        ResultSet tempResultSet = tempStatement.executeQuery(tempQuery);
        
        // read the attribute headers
        ResultSetMetaData tempMetaData = tempResultSet.getMetaData();
        int tempShapeCol = Integer.MAX_VALUE;
        
        // add the joined data to the array.
        int tempCount = tempMetaData.getColumnCount();
        
        String[] tempDatasourceAttributeNames = getJoinDataSourceAttributeNames();
        AttributeType[] tempDatasourceAttributeTypes = getJoinDataSourceAttributeTypes();
        int tempDatasourceCount =tempDatasourceAttributeNames.length;
        
        String[] tempNames = new String[tempCount + tempDatasourceCount];
        AttributeType[] tempTypes = new AttributeType[tempCount+tempDatasourceCount];
        for (int i=0; i<tempDatasourceCount; i++){
            tempNames[i] = tempDatasourceAttributeNames[i];
            tempTypes[i] = tempDatasourceAttributeTypes[i];
        }
        
        // add the db2 data to the array and find the shape column
        SQLConverter tempSQLConverter = getSQLConverter();
        int tempJoinIndex = -1;
        for (int i = 0; i < tempCount; i++) {
            String tempName = tempMetaData.getColumnName(i + 1);
            if (tempName.equalsIgnoreCase(getTableJoinColumn())){
                tempJoinIndex = i;
            }
            tempNames[tempDatasourceCount+i] = tempName;
            AttributeType tempType = tempSQLConverter.getAttributeType(tempMetaData, i);
            tempTypes[tempDatasourceCount+i] = tempType;
        }
        if (tempJoinIndex == -1){
            tempResultSet.close();
            tempStatement.close();
            throw new Exception("Did not find Table Join Column "+getTableJoinColumn());
        }
        
        
        // create the dataset
        GISDataset tempDataset = new GISDataset(tempNames, tempTypes);
        
        try{
            // add the records to the dataset.
            boolean tempContinue = true;
            Object[] tempObjects = new Object[tempNames.length];
            while (tempContinue) {
                try{
                    tempContinue = tempResultSet.next();
                }
                catch (Exception e){
                    System.out.println("The ResultSet.next() threw "+e);
                    tempContinue = false;
                }
                
                if (tempContinue){
                    // create the attributes
                    for (int i = 0; i < tempCount; i++) {
                        Object tempObject = tempResultSet.getObject(i+1);
                        if (i == tempJoinIndex) {
                            // locate this row.
                            Record tempRecord = getDatasourceRecord(tempObject);
                            // If the record is not found, then do not continue.
                            if (tempRecord == null) continue;
                            if (tempRecord.getShape() == null) continue;
                            Object[] tempAttributes = tempRecord.getAttributes();
                            for (int j=0; j<tempAttributes.length; j++){
                                tempObjects[j] = tempAttributes[j];
                            }
                            if (inEnvelope != null){
                                if (tempRecord.getShape().getEnvelope().overlaps(inEnvelope)){
                                    tempDataset.add(tempObjects, tempRecord.getShape());
                                }
                            }
                            else{
                                tempDataset.add(tempObjects, tempRecord.getShape());
                            }
                        }
                        tempObjects[tempDatasourceCount+i] = tempObject;
                    }
                    tempObjects = new Object[tempNames.length];
//                    fireRead(tempDataset.getRecord(tempDataset.getNumShapes()-1));
                }
            }// end while
        }
        catch (Exception e){
            System.out.println("Outer Loop "+e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println("Outer Loop "+t);
            t.printStackTrace();
        }
        
        // close the connection to the database.
        tempStatement.close();
        myCon.close();
        myCon = null;
        
        // return the completed dataset.
        System.out.println("Read "+tempDataset.size()+" Records");
        return tempDataset;
    }
    
    // static tags for use in saving the configuration information for this data source.
    private static final String SERVER_NAME = "Servername";
    private static final String PORT_NUMBER = "PortNumber";
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";
    private static final String DATABASE_NAME = "DatabaseName";
    private static final String SCHEMA = "Schema";
    private static final String DRIVER = "Driver";
    private static final String URLBASE = "URLBase";
    private static final String QUERY = "QUERY";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("DB2SJoinDataSource");
        
        // connection parameters
        tempRoot.addAttribute(SERVER_NAME, getDatabaseServername());
        tempRoot.addAttribute(PORT_NUMBER, ""+getDatabasePort());
        tempRoot.addAttribute(USERNAME, getDatabaseUsername());
        tempRoot.addAttribute(PASSWORD, getDatabasePassword());
        tempRoot.addAttribute(DATABASE_NAME, getDatabaseName());
        tempRoot.addAttribute(SCHEMA, getDatabaseSchema());
        tempRoot.addAttribute(DRIVER, getDatabaseDriver());
        tempRoot.addAttribute(URLBASE, getDatabaseURLBase());
        tempRoot.addAttribute(QUERY, getDatabaseQuery());
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("Can not set up DB2 Datasource configuration information is null");
        super.setNode(inNode);
        String tempName = null;
        String tempValue = null;
        try{
            // connection parameters
            tempName = SERVER_NAME;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseServername(tempValue);
            tempName = PORT_NUMBER;
            tempValue = inNode.getAttribute(tempName);
            setDatabasePort(Integer.parseInt(tempValue));
            tempName = USERNAME;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseUsername(tempValue);
            tempName = PASSWORD;
            tempValue = inNode.getAttribute(tempName);
            setDatabasePassword(tempValue);
            tempName = DATABASE_NAME;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseName(tempValue);
            tempName = SCHEMA;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseSchema(tempValue);
            tempName = DRIVER;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseDriver(tempValue);
            tempName = URLBASE;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseURLBase(tempValue);
            tempName = QUERY;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseQuery(tempValue);            
        }
        catch (Exception e){
            throw new Exception("Can not read value for "+tempName+" to configure "+getName()+ " DataSource.");
        }
    }    
    
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }
 }
