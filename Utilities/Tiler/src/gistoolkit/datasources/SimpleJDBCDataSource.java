/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2003, Ithaqua Enterprises Inc.
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

package gistoolkit.datasources;

import java.sql.*;
import gistoolkit.common.*;

/**
 * Contains many convenience methods for building a data source based on a database.
 */
public abstract class SimpleJDBCDataSource extends SimpleDBDataSource{
    
    /**When connecting with the driver, a specific urlbase is required.*/
    private String myDatabaseURLBase = null;
    /** Returns the url base to use when connecting through the JDBC driver. */
    public String getDatabaseURLBase() {return myDatabaseURLBase;}
    /** Sets the url base to use when connecting through the JDBC driver.*/
    public void setDatabaseURLBase(String inDatabaseURLBase){myDatabaseURLBase = inDatabaseURLBase;}
    
    /**
     * This driver must be present in the class path of this application as
     * it is required for connecting to the database.
     */
    private String myDatabaseDriver = null;
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
    private int myDatabasePort = 0;
    /** Returns the tcpip port to use when connecting to the DB2 server */
    public int getDatabasePort() {return myDatabasePort;}
    /** Sets the tcpip port to use when connecting to the DB2 server*/
    public void setDatabasePort(int inDatabasePort){myDatabasePort = inDatabasePort;}
    
    /**Column Name of the shape column, since the records can only handle a single shape column, this one is used.*/
    private String myDatabaseShapeColumn = "Shape";
    /** Returns the name of the shape column. */
    public String getDatabaseShapeColumn() {return myDatabaseShapeColumn;}
    /** Sets the name of the shape column. The contents of this column will be parsed as a shape. */
    public void setDatabaseShapeColumn(String inDatabaseShapeColumn){myDatabaseShapeColumn = inDatabaseShapeColumn;}
    
    /**Spatial Reference ID needed for accessing shape information.*/
    private int myDatabaseSpatialReferenceID = 1;
    /** Returns the spatial reference id to use for converting shapes to and from database format.*/
    public int getDatabaseSpatialReferenceID() {return myDatabaseSpatialReferenceID;}
    /** Sets the SpatialReferenceID to use when converting shapes to and from database format.*/
    public void setDatabaseSpatialReferenceID(int inDatabaseSpatialReferenceID){myDatabaseSpatialReferenceID = inDatabaseSpatialReferenceID;}
    /** Sets the SpatialReferenceID to use when converting shapes to and from database format.*/
    public void setDatabaseSpatialReferenceID(String inDatabaseSpatialReferenceID){myDatabaseSpatialReferenceID = Integer.parseInt(inDatabaseSpatialReferenceID);}

    /** Creates new SimpleDBDataSource */
    public SimpleJDBCDataSource() {
    }
    
    /** Returns the correctly formated URL for connecting to a database. */    
    public abstract String getJDBCURL();
    
    /** Connection to the JDBC datasource used for maintaining persistent connections.*/
    private Connection mySelectConnection = null;
    /** Determine if there is already a connection. */
    public boolean isConnected(){
        if (mySelectConnection == null) return false;
        return true;
    }

    /** The connection to use if the database is being updated. */
    private Connection myUpdateConnection = null;
    
    /** Get a connection that can be used for updates. */
    public Connection requestUpdateConnection() throws SQLException{
        if (myUpdateConnection == null){
            // get an instance of the JDBCConnection Pool
            JDBCConnectionPool tempPool = JDBCConnectionPool.getInstance();
            myUpdateConnection = tempPool.requestUpdateConnection(getJDBCURL(), getDatabaseUsername(), getDatabasePassword());
            myUpdateConnection.setAutoCommit(false);
        }
        return myUpdateConnection;            
    }
    
    /** Return the update connection. */
    public void releaseUpdateConnection() throws SQLException{
        if (myUpdateConnection == null) return;
        // get an instance of the JDBCConnection Pool
        JDBCConnectionPool tempPool = JDBCConnectionPool.getInstance();
        tempPool.releaseUpdateConnection(myUpdateConnection);
        myUpdateConnection = null;
    }
    
    /** Release the Connection. */
    public void releaseSelectConnection() throws SQLException{
        JDBCConnectionPool tempPool = JDBCConnectionPool.getInstance();
        tempPool.releaseSelectConnection(mySelectConnection);
        mySelectConnection = null;
    }
    
    /** Connects to the database. */
    public Connection connect() throws Exception{
        if (mySelectConnection == null){
            if (getDatabaseDriver() == null)
                throw new Exception("No Database Driver Class defined");
            if (getDatabasePassword() == null)
                throw new Exception("No Database Password defined");
            if (getDatabaseName() == null)
                throw new Exception("No Database Name defined");
            if (getDatabaseServername() == null)
                throw new Exception("No Database Servername defined");
            if (getDatabaseURLBase() == null)
                throw new Exception("No Database URL Base defined");
            if (getDatabaseUsername() == null)
                throw new Exception("No Database Username defined");
        
            // load the driver
            try {
                Class.forName(getDatabaseDriver()).newInstance();
            }
            catch (Exception e) {
                System.out.println("Error Loading DBDriver Class " + e);
                throw new Exception("Error Loading Database Driver " + e);
            }
        
            // Retrieve the connection URL from the database.
            String tempURL = getJDBCURL();
            
            // get an instance of the JDBCConnection Pool
            JDBCConnectionPool tempPool = JDBCConnectionPool.getInstance();
            mySelectConnection = tempPool.requestSelectConnection(tempURL, getDatabaseUsername(), getDatabasePassword());
        }
        // return the connection, use the update connection if possible so the user can see their updates.
        if (myUpdateConnection == null) return mySelectConnection;
        return myUpdateConnection;                
    }


    private static final String SERVER_NAME = "Servername";
    private static final String PORT_NUMBER = "PortNumber";
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";
    private static final String DATABASE_NAME = "DatabaseName";
    private static final String SHAPE_COLUMN = "ShapeColumn";
    private static final String DRIVER = "Driver";
    private static final String URLBASE = "URLBase";
    private static final String SPATIAL_REFERENCE_ID = "SpatialReferenceID";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("JDBCDataSource");
        
        // connection parameters
        tempRoot.addAttribute(SERVER_NAME, getDatabaseServername());
        tempRoot.addAttribute(PORT_NUMBER, ""+getDatabasePort());
        tempRoot.addAttribute(USERNAME, getDatabaseUsername());
        tempRoot.addAttribute(PASSWORD, getDatabasePassword());
        tempRoot.addAttribute(DATABASE_NAME, getDatabaseName());
        tempRoot.addAttribute(SHAPE_COLUMN, getDatabaseShapeColumn());
        tempRoot.addAttribute(DRIVER, getDatabaseDriver());
        tempRoot.addAttribute(URLBASE, getDatabaseURLBase());
        tempRoot.addAttribute(SPATIAL_REFERENCE_ID, ""+getDatabaseSpatialReferenceID());
        return tempRoot;
    }            
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("Can not set up JDBC Datasource configuration information is null");
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
            tempName = DRIVER;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseDriver(tempValue);
            tempName = SHAPE_COLUMN;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseShapeColumn(tempValue);
            tempName = URLBASE;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseURLBase(tempValue);
            tempName = SPATIAL_REFERENCE_ID;
            tempValue = inNode.getAttribute(tempName);
            setDatabaseSpatialReferenceID(tempValue);
        }
        catch (Exception e){
            throw new Exception("Can not read value for "+tempName+" to configure "+getName()+ " JDBC Datasource");
        }
    }
}
