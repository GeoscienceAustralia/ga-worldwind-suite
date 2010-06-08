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
 */
package gistoolkit.datasources;

import java.sql.*;

/**
 * Handles the connection pooling for the JDBC Datasources.
 * 
 */
public class JDBCConnectionPool {
 
    /** A reference to the static JDBCConnectionPool. */
    private static JDBCConnectionPool myJDBCConnectionPool = null;
    /** Return a reference to the JDBCConnectionPool. */
    public static JDBCConnectionPool getInstance(){
        if (myJDBCConnectionPool == null) myJDBCConnectionPool = new JDBCConnectionPool();
        return myJDBCConnectionPool;
    }
        
    /** The Vector of available connections used for Selecting data only. */
    private java.util.Vector myVectorSelectAvailable = new java.util.Vector();
        
    /** The Vector of available connections used for Selecting data only. */
    private java.util.Vector myVectorUpdateAvailable = new java.util.Vector();
    
    /** The Vector of in use connections used for selecting data only. */
    private java.util.Vector myVectorUpdatetInUse = new java.util.Vector();

    /** Create a new JDBCConnectionPool. */
    private JDBCConnectionPool(){
        myReaper.start();
    }

    /** Retrieve a connection from the connection pool. */
    public Connection requestSelectConnection(String inURL, String inUsername, String inPassword) throws SQLException{
        // look for a connection of this type within the SelectAvailable queue.
        Connection tempConnection = findInSelectAvailableQueue(inURL, inUsername, inPassword);
        
        if (tempConnection == null){
            // if the connection was not found, then attempt to create the connection.
            tempConnection = DriverManager.getConnection(inURL, inUsername, inPassword);
            MyConnection tempMyConnection = new MyConnection(inURL, inUsername, inPassword, tempConnection);
            addSelectAvailable(tempMyConnection);
        }
        
        // return the connection.
        return tempConnection;        
    }
    /** Return a connection to the connection pool. */
    public void releaseSelectConnection(Connection inConnection) throws SQLException{
        // if there is a null sent in, just return
        if (inConnection == null) return;
        
        // look for the connection in the SelectInUse queue.
        MyConnection tempConnection = findInSelectQueue(inConnection);
        if (tempConnection == null){        
            // if it was not found, close it.
            inConnection.close();
        }
    }
    
    /** Retrieve a connection from the connection pool. */
    public Connection requestUpdateConnection(String inURL, String inUsername, String inPassword) throws SQLException{        
        // look for a connection of this type within the UpdateAvailable queue.
        Connection tempConnection = findInUpdateAvailableQueue(inURL, inUsername, inPassword);
        
        if (tempConnection == null){
            // if the connection was not found, then attempt to create the connection.
            tempConnection = DriverManager.getConnection(inURL, inUsername, inPassword);
            MyConnection tempMyConnection = new MyConnection(inURL, inUsername, inPassword, tempConnection);
            addUpdateInUse(tempMyConnection);
            tempConnection.setAutoCommit(false);
        }
        
        // return the connection.
        return tempConnection;        
    }
    /** Return a connection to the connection pool. */
    public void releaseUpdateConnection(Connection inConnection) throws SQLException{
        // if there is a null sent in, just return
        if (inConnection == null) return;

        // look for the connection in the UpdatetInUse queue.
        MyConnection tempConnection = removeFromUpdateInUseQueue(inConnection);
        if (tempConnection == null){
            // if it was not found, close it.
            inConnection.close();
        }
    }
        
    /** Synchronized methods.*/
    /** Add this connection to the Select Available queue. */
    private synchronized void addSelectAvailable(MyConnection inConnection){
        myVectorSelectAvailable.add(inConnection);
    }
    /** Add this connection to the Update InUse queue. */
    private synchronized void addUpdateInUse(MyConnection inConnection){
        myVectorUpdatetInUse.add(inConnection);
    }
    
    /**
     * Look for a connection in the SelectAvailableQueue.
     * <p>
     * If a connection is found in the select available queue, then it is
     * returned as the result.  If no connection was found, then null is returned.
     * </p>
     */
    private synchronized Connection findInSelectAvailableQueue(String inURL, String inUsername, String inPassword){        
        // loop through all the connections finding one that matches these parameters.
        for (int i=0; i<myVectorSelectAvailable.size(); i++){
            MyConnection tempConnection = (MyConnection) myVectorSelectAvailable.get(i);            
            // if the connection URL's are the same.
            if (tempConnection.isEqual(inURL, inUsername, inPassword)){                                                
                // return the connection.
                tempConnection.updateConnectDate();
                return tempConnection.getConnection();
            }            
        }
        
        // no connection was found, so return null;
        return null;
    }
    
    /**
     * Look for a connection in the UpdateAvailableQueue.
     * <p>
     * If a connection is found in the update available queue, then it is moved to the inuse queue, and
     * returned as the result.  If no connection was found, then null is returned.
     * </p>
     */
    private synchronized Connection findInUpdateAvailableQueue(String inURL, String inUsername, String inPassword){        
        // loop through all the connections finding one that matches these parameters.
        for (int i=0; i<myVectorUpdateAvailable.size(); i++){
            MyConnection tempConnection = (MyConnection) myVectorUpdateAvailable.get(i);            
            // if the connection URL's are the same.
            if (tempConnection.isEqual(inURL, inUsername, inPassword)){                        
                //Remvoe the connection from the available queue, and add it to the in Use queue.
                myVectorUpdateAvailable.remove(i);
                myVectorUpdatetInUse.add(tempConnection);                        
                // return the connection.
                
                tempConnection.updateConnectDate();
                return tempConnection.getConnection();
            }            
        }        
        // no connection was found, so return null;
        return null;
    }
    
    /**
     * Find the connection in the select queue.
     */
    private synchronized MyConnection findInSelectQueue(Connection inConnection){
        for (int i=0; i<myVectorSelectAvailable.size(); i++){
            MyConnection tempConnection = (MyConnection) myVectorSelectAvailable.get(i);            
            // if the connection URL's are the same.
            if (tempConnection.getConnection() == inConnection){                        
                // return the connection.
                return tempConnection;
            }            
        }        
        // no connection was found, so return null;
        return null;
    }
    
    /**
     * Find the connection in the Update InUse queue.
     * <p>
     * If the connection it is removed from the inUse Queue, and put back into the available queue.
     * </p>
     */
    private synchronized MyConnection removeFromUpdateInUseQueue(Connection inConnection){
        for (int i=0; i<myVectorUpdatetInUse.size(); i++){
            MyConnection tempConnection = (MyConnection) myVectorUpdatetInUse.get(i);            
            // if the connection URL's are the same.
            if (tempConnection.getConnection() == inConnection){                        
                // return the connection.
                myVectorUpdatetInUse.remove(i);
                myVectorUpdateAvailable.add(tempConnection);
                tempConnection.updateConnectDate();
                return tempConnection;
            }            
        }        
        // no connection was found, so return null;
        return null;
    }    
     
    /**
     * Keep the URL, Username and Password information.
     * <p>
     * This class holds the Username, password, and URL information, as well as holding the
     * database connection itself.  In addition, it determines if connections are eqial, and keeps track of
     * how long they have been open.
     * </p>
     */
    private class MyConnection{
        /** The URL this connection is using to connect.*/
        private String myURL = null;        
        /** Get the URL this connection is using to connect.*/
        public String getURL() {return myURL;}        

        /** The Username this connection is using to connect.*/
        private String myUsername = null;
        /** Get the Username this connection is using to connect.*/
        public String getUsername() {return myUsername;}        

        /** The Password this connection is using to connect.*/
        private String myPassword = null;
        /** Get the Password this connection is using to connect.*/
        public String getPassword() {return myPassword;}
        
        /** The date when this Connection was connected. */
        private long myConnectDate = 0;
        /** Get the time this connection was last used. */
        public long getConnectDate(){return myConnectDate;}
        /** Reset the connect date. */
        public void updateConnectDate(){myConnectDate = new java.util.Date().getTime();}
        
        /** The Connection. */
        private Connection myConnection = null;        
        /** Return the JDBC Connection for this connection.*/
        public Connection getConnection(){return myConnection;}
        
        /** Construct a new MyConnection with these parameters. */
        public MyConnection(String inURL, String inUsername, String inPassword, Connection inConnection){
            myURL = inURL;
            myUsername = inUsername;
            myPassword = inPassword;
            myConnection = inConnection;
            myConnectDate = new java.util.Date().getTime();
        }
        
        /** Check if this connections parameters are the same as the ones sent in. */
        public boolean isEqual(String inURL, String inUsername, String inPassword){
            // if the connection URL's are the same.
            if ((myURL == inURL) || (myURL.equalsIgnoreCase(inURL))){
                // and the Usernames are the same.
                if ((myUsername == inUsername) || (myUsername.equalsIgnoreCase(inUsername))){
                    // and the passwords are the same.
                    if ((myPassword == inPassword) || (myPassword.equalsIgnoreCase(inPassword))){
                        return true;
                    }
                }
            }
            return false;
        }        
    }
    
    /** Look through the update available queue, and find connections that have been out there for too
     * long and terminate them.
     */
    private synchronized void reapUpdateAvailable(){
        if (myVectorUpdateAvailable.size() == 0) return;
        long tempTimeDifference = 5*1000*60; // five minutes
        long tempCurrentDate = new java.util.Date().getTime();

        // Loop through the available connections finding the ones to delete.
        java.util.Vector tempDeleteConnections = new java.util.Vector();        
        for (int i=0; i<myVectorUpdateAvailable.size(); i++){
            MyConnection tempConnection = (MyConnection) myVectorUpdateAvailable.get(i);
            if ((tempCurrentDate - tempConnection.getConnectDate()) > tempTimeDifference){
                tempDeleteConnections.add(tempConnection);
            }
        }
        
        // Close the connections and remove them from the available queue.
        for (int i=0; i<tempDeleteConnections.size(); i++){
            MyConnection tempConnection = (MyConnection) tempDeleteConnections.get(i);
            try{
                myVectorUpdateAvailable.remove(tempConnection);
                tempConnection.getConnection().close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    /** Class for cleaning up connections if they have out lived their life span. */
    private class MyReaper extends Thread{
        public void run(){
            while(true){
                try{
                    Thread.sleep(1000*60); // One Minute.
                    reapUpdateAvailable();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    /** Thread to cleanup old available connections. */
    private MyReaper myReaper = new MyReaper();  
}
