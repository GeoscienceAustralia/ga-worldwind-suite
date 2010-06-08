/* */
package gistoolkit.datasources.db2spatialextender;

import java.io.*;
import java.sql.*;

/**
 * Class to test the DB2 Connection to determine if it supports concurrent transactions.
 * Db2 7.1 Service Pack 10.
 */
public class TestDB2Connection {
    private Connection myConnection = null;
    private boolean myStateDone = false;
    private boolean myCountyDone = false;
    
    public static void main(String[] inString){
        TestDB2Connection tempTestDB2Connection = new TestDB2Connection();
        tempTestDB2Connection.doTest();
    }
        
    public void doTest(){
        try{
            // load the driver
            try {
                Class.forName("COM.ibm.db2.jdbc.net.DB2Driver").newInstance();
            }
            catch (Exception e) {
                System.out.println("Error Loading DBDriver Class " + e);
                throw new Exception("Error Loading Database Driver " + e);
            }
            
            // Set up the connection.
            // if the connection was not found, then attempt to create the connection.
            String tempURL = "jdbc:db2://db2server:6789/db2";
            myConnection = DriverManager.getConnection(tempURL, "ithaqua", "boggle1");
            myCounty.start();
            try{
                Thread.sleep(100);
            }
            catch (InterruptedException e){
            }            
            myState.start();
            
            // sleep for some time to wait for the other threads to complete.
            while ((myStateDone == false) || (myCountyDone == false)){
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    /** Create a thread for selecting from the County table. */
    private class MyCountyClass extends Thread {
        public void run(){
            try{
                // create a statement, and select the names of the counties from the database.
                String tempCountyQuery = "SELECT county, db2gse.st_asBinary(shape) as shape FROM counties WHERE state_fips = '12'";
                Statement tempCountyStatement = myConnection.createStatement();
                ResultSet tempCountyResultSet = tempCountyStatement.executeQuery(tempCountyQuery);
            
                // select some records from the county result set there are some 6000 records in this table.
                System.out.println();
                int tempCountyCount = 0;
                while(tempCountyResultSet.next()){
                    tempCountyResultSet.getString(1);
                    InputStream in = tempCountyResultSet.getBinaryStream(2);
                    byte[] buff = new byte[1000];
                    int tempLength = in.read(buff);
                    while (tempLength != -1){
                        tempLength = in.read(buff);
                    }
                    tempCountyCount++;
                }
                System.out.println("Read "+tempCountyCount+" Counties");
            
                // close the tempCountyStatement.
                tempCountyStatement.close();
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
            myCountyDone = true;
        }
    }
    MyCountyClass myCounty = new MyCountyClass();
    
    /** Create a thread for selecting from the State table. */
    private class MyStateClass extends Thread {
        public void run(){
            try{

                // Select some data from the states table
                String tempStateQuery = "SELECT state_name, db2gse.st_asBinary(shape) as shape FROM states";
                Statement tempStateStatement = myConnection.createStatement();
                ResultSet tempStateResultSet = tempStateStatement.executeQuery(tempStateQuery);
            
                // select all the records from the States table.
                int tempStateCount = 0;
                while (tempStateResultSet.next()){
                    tempStateResultSet.getString(1);
                    InputStream in = tempStateResultSet.getBinaryStream(2);
                    byte[] buff = new byte[1000];
                    int tempLength = in.read(buff);
                    while (tempLength != -1){
                        tempLength = in.read(buff);
                    }
                    tempStateCount++;
                }
                System.out.println("Read "+tempStateCount+" States");
              
                // close the tempCountyStatement.
                tempStateStatement.close();
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
            myStateDone = true;
        }
    }
    MyStateClass myState = new MyStateClass();
}
