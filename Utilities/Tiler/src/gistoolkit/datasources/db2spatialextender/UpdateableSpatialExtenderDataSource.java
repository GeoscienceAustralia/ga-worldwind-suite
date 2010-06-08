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
import java.sql.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;
/**
 * Allowing joins in a map is very powerfull, but it gets into some very harry issues when
 * it comes to updating, deleting, and otherwise modifying the base data.  To facilitate a
 * more direct and intuative approach to the updateing data, this datasource was created.
 */
public class UpdateableSpatialExtenderDataSource extends SpatialExtenderDataSource {
    
    // max 32 = 4,294,967,295
    int tempMaxInt = Integer.MAX_VALUE;
    
    /** Records if all the shapes have been read.*/
    private boolean myAllRead = false;
    
    /**
     * Table to select the information from.
     */
    private String myDatabaseTableName = "States";
    /** Return the table name from which to retrieve data from spatial extender */
    public String getDatabaseTableName(){return myDatabaseTableName;}
    /** set the table name from which to retrieve data from spatial extender */
    public void setDatabaseTableName(String inDatabaseTableName){myDatabaseTableName = inDatabaseTableName;}
    
    /** The types of the data */
    private AttributeType[] myAttributeTypes = null;
    /** The Names of the data */
    private String[] myAttributeNames = null;
    /** The Column types of the data */
    private Integer[] myColumnTypes = null;
    
    /**
     * Stores the datatype of the Shape column in the database.
     */
    private String myShapeDataType;
    
    /** Creates new UpdatableSpatialExtenderDataSource */
    public UpdateableSpatialExtenderDataSource() {
        super();
    }
    
    /**
     * Connects this datasource to the database, calls the connect in the super class.
     */
    public Connection connect() throws Exception{
        Connection tempConnection = super.connect();
        setName(getDatabaseTableName());
        SQLConverter tempConverter = getSQLConverter();
        
        // find the types of the data.
        if (myAttributeTypes == null){
            // retrieve the column names from the table
            String tempQuery = "select * from "+getDatabaseTableName()+" where 0=1";
            
            // Send the sql Query to the database
            Statement tempStmt = tempConnection.createStatement();
            ResultSet tempResultSet = tempStmt.executeQuery(tempQuery);
            
            // construct the attribute names and types
            ResultSetMetaData tempResultSetMetaData = tempResultSet.getMetaData();
            int tempColumnNum = tempResultSetMetaData.getColumnCount();
            ArrayList tempNameList = new ArrayList(tempColumnNum-1);
            ArrayList tempTypeList = new ArrayList(tempColumnNum-1);
            ArrayList tempJDBCList = new ArrayList(tempColumnNum-1);
            for (int i=0; i<tempColumnNum; i++){
                String tempColumnName = tempResultSetMetaData.getColumnName(i+1);
                String tempTypeName = tempResultSetMetaData.getColumnTypeName(i+1);
                int tempJDBCType = tempResultSetMetaData.getColumnType(i+1);
                if (!tempColumnName.equalsIgnoreCase(getDatabaseShapeColumn())){
                    AttributeType tempColumnType = tempConverter.getAttributeType(tempResultSetMetaData, i);
                    tempNameList.add(tempColumnName);
                    tempTypeList.add(tempColumnType);
                    tempJDBCList.add(new Integer(tempJDBCType));
                }
                else{
                    // find the shape data type
                    int tempIndex = tempTypeName.indexOf(".");
                    if (tempIndex != -1){
                        myShapeDataType = tempTypeName.substring(tempIndex+1);
                    }
                    System.out.println("ShapeColumnType="+tempTypeName+" DataType="+myShapeDataType);
                }
            }
            myAttributeNames = new String[tempNameList.size()];
            tempNameList.toArray(myAttributeNames);
            myAttributeTypes = new AttributeType[tempTypeList.size()];
            tempTypeList.toArray(myAttributeTypes);
            myColumnTypes = new Integer[tempJDBCList.size()];
            tempJDBCList.toArray(myColumnTypes);
            
            // close the connection
            tempStmt.close();
            tempStmt = null;
        }        
        return tempConnection;
    }
        
    /**
     * Inserts the given record into the datasource.
     */
    public void doInsert(Record inRecord) throws Exception {
        
        // insert the data into DB2
        SQLConverter tempConverter = getSQLConverter();
        String[] tempNames = inRecord.getAttributeNames();
        Object[] tempValues = inRecord.getAttributes();
        AttributeType[] tempTypes = inRecord.getAttributeTypes();
        
        // create the SQL statement
        String tempQuery = "INSERT INTO "+myDatabaseTableName+"(\n\t";
        for (int i=0; i<myAttributeNames.length; i++){
            tempQuery = tempQuery + myAttributeNames[i] + ",\n\t";
        }
        tempQuery = tempQuery + getDatabaseShapeColumn();
        
        tempQuery = tempQuery +"\n)\nVALUES(\n";
        for (int i=0; i<myAttributeNames.length; i++){
            String tempValue = null;
            for (int j=0; j<tempNames.length; j++){
                if (myAttributeNames[i].equalsIgnoreCase(tempNames[j])){
                    tempValue = tempConverter.toSQL(tempValues[j], tempTypes[j], myColumnTypes[i].intValue());
                }
            }
            if (tempValue == null)
                tempValue = tempConverter.toSQL(null, myAttributeTypes[i], myColumnTypes[i].intValue());
            if (tempValue.length() == 0) tempValue = "null";
            tempQuery += "\t"+tempValue + ",\n";
        }
        tempQuery = tempQuery + "\t" + WKBParser.getSQL(myShapeDataType, getDatabaseSpatialReferenceID());
        tempQuery = tempQuery + "\n)";
        
        // Send the SQL statement to the database
        try{
            Connection tempConnection = requestUpdateConnection();
            PreparedStatement tempStmt = tempConnection.prepareStatement(tempQuery);
            Shape tempShape = inRecord.getShape();
            tempStmt.setBytes(1, WKBParser.writeWKB(tempShape));
            tempStmt.executeUpdate();
            tempStmt.close();
        }
        catch (Exception e){
            System.out.println(tempQuery);
            e.printStackTrace();
            throw e;
        }        
    }
    
    /**
     * Update the data source with the changed record.
     */
    public void doUpdate(Record inRecord) throws Exception {
        if (!(inRecord instanceof SpatialExtenderRecord)) throw new Exception("Can Not Update Record, Redord Type is not Spatial Extender, record was not read from Spatial Extender Data Source.");
        
        // insert the data into DB2
        Object[] tempValues = inRecord.getAttributes();
        SQLConverter tempConverter = getSQLConverter();
        
        // create the SQL statement
        String tempQuery = "UPDATE "+myDatabaseTableName+" SET\n\t";
        String[] tempNames = inRecord.getAttributeNames();
        for (int i=0; i<tempNames.length; i++){
            String tempValue = tempConverter.toSQL(tempValues[i], myAttributeTypes[i], myColumnTypes[i].intValue());
            if ((tempValue != null) && (tempValue.length() > 0)){
                // NAME
                tempQuery = tempQuery + tempNames[i] + " = ";

                // VALUE
                tempQuery = tempQuery + tempValue;
                tempQuery = tempQuery + ",\n\t";
            }
        }
        tempQuery = tempQuery + getDatabaseShapeColumn() + " = " + WKBParser.getSQL(myShapeDataType, getDatabaseSpatialReferenceID());
        
        // Where clause
        String tempWhere = "WHERE\n\t";
        Object[] tempOldValues = ((SpatialExtenderRecord) inRecord).getPreviousAttributes();
        boolean tempStart = true;
        
        for (int i=0; i<tempOldValues.length; i++){
            String tempValue = tempConverter.toSQL(tempOldValues[i], myAttributeTypes[i], myColumnTypes[i].intValue());
            if ((tempValue != null) && (tempValue.length() > 0)){
                if (!tempStart) tempWhere = tempWhere + " and\n\t";
                tempStart = false;
                tempWhere = tempWhere + tempNames[i] +" = ";

                // VALUE
                tempWhere = tempWhere + tempValue;
            }
        }        
        tempQuery = tempQuery + "\n"+ tempWhere;
        
        // Send the SQL statement to the database
        Connection tempConnection = requestUpdateConnection();
        PreparedStatement tempStmt = tempConnection.prepareStatement(tempQuery);
        Shape tempShape = WKBParser.convert(inRecord.getShape(), myShapeDataType);
        byte[] tempByte = WKBParser.writeWKB(tempShape);
        tempStmt.setBytes(1, tempByte);
        tempStmt.executeUpdate();
        myAllRead = false;
    }
    
    /**
     * Delete this record from the database.
     */
    public void doDelete(Record inRecord) throws Exception {
        // Delete the data from DB2
        // Where clause
        String[] tempNames = inRecord.getAttributeNames();
        String tempQuery = "DELETE FROM\n\t"+getDatabaseTableName();
        String tempWhere = "WHERE\n\t";
        Object[] tempValues = inRecord.getAttributes();
        SQLConverter tempConverter = getSQLConverter();
        
        for (int i=0; i<tempValues.length; i++){
            if (i>0) tempWhere = tempWhere + " and\n\t";
            tempWhere = tempWhere + tempNames[i] +" = ";
            
            // VALUE
            String tempValue = tempConverter.toSQL(tempValues[i], myAttributeTypes[i], myColumnTypes[i].intValue());
            tempWhere = tempWhere + tempValue;
        }
        
        tempQuery = tempQuery + "\n"+ tempWhere;
        
        // Send the SQL statement to the database
        Connection tempConnection = requestUpdateConnection();
        Statement tempStmt = tempConnection.createStatement();
        tempStmt.executeUpdate(tempQuery);
        tempStmt.close();
        myAllRead = false;
    }
    
    /**
     * Commit all changes since the last commit.
     */
    public void doCommit() throws Exception {
        Connection tempConnection = requestUpdateConnection();
        Statement tempStmt = tempConnection.createStatement();
        tempConnection.commit();
        releaseUpdateConnection();
        myAllRead = false;
    }
    
    /**
     * Rollback any changes to this datasource since the last commit.
     */
    public void doRollback() throws Exception {
        Connection tempConnection = requestUpdateConnection();
        Statement tempStmt = tempConnection.createStatement();
        tempConnection.rollback();
        releaseUpdateConnection();
    }
    
    /**
     * Reads the objects from the database that fall within the given Envelope.
     * If a null is sent in for the Envelope, all the objects in the shape file are read.
     */
    public synchronized GISDataset readShapes(Envelope inEnvelope) throws Exception {

        if (myAllRead){
            if (inEnvelope != null){
                return queryFromCache(inEnvelope);
            }
            else{
                return getCacheDataset();
            }
        }
        // retrieve the connection
        Connection tempCon = connect();
        SQLConverter tempConverter = getSQLConverter();
        
        // create the statement
        Statement tempStatement = tempCon.createStatement();
        
        // construct the SQL String
        boolean tempFirst = true;
        Vector tempTypeVect = new Vector();
        String tempQuery = "SELECT\n";
        
        // select the attributes.
        for (int i=0; i<myAttributeNames.length; i++){
            String tempString = myAttributeNames[i];
            if (tempFirst){ tempQuery = tempQuery + "\t"; tempFirst = false;}
            else tempQuery = tempQuery + ",\n\t";
            tempQuery = tempQuery + tempString;
        }
        
        // select the shape
        if (tempFirst){ tempQuery = tempQuery + "\t"; tempFirst = false;}
        else tempQuery = tempQuery + ",\n\t";
        tempQuery = tempQuery + "db2gse.st_AsBinary("+getDatabaseShapeColumn().toLowerCase()+") "+getDatabaseShapeColumn().toLowerCase();

        // add the from tablename
        tempQuery = tempQuery + "\nFROM\n\t"+myDatabaseTableName.toLowerCase();
        
        // add the where clause to include the Envelope.
        if (inEnvelope != null){
            String tempWhereString = new String("\nWHERE\n");
            tempWhereString = tempWhereString + getWhereString(inEnvelope);
            tempQuery = tempQuery + "\t"+ tempWhereString;
        }
        else{
            myAllRead = true;
        }
        
        // send the SQLString to the database
        ResultSet tempResultSet = tempStatement.executeQuery(tempQuery);
        
        // read the attribute headers
        ResultSetMetaData tempMetaData = tempResultSet.getMetaData();
        int tempShapeCol = Integer.MAX_VALUE;
        
        int tempCount = tempMetaData.getColumnCount();
        ArrayList tempNameList = new ArrayList(tempCount);
        ArrayList tempTypeList = new ArrayList(tempCount);
        
        for (int i = 0; i < tempCount; i++) {
            String tempString = tempMetaData.getColumnName(i + 1);
            if (getDatabaseShapeColumn().equalsIgnoreCase(tempString)) {
                tempShapeCol = i;
            } else {
                tempNameList.add(tempString);
                tempTypeList.add(tempConverter.getAttributeType(tempMetaData, i));
            }
        }
        String[] tempNames = new String[tempNameList.size()];
        tempNameList.toArray(tempNames);
        AttributeType[] tempTypes = new AttributeType[tempTypeList.size()];
        tempTypeList.toArray(tempTypes);
        
        // create the dataset
        GISDataset tempDataset = new GISDataset(tempNames);
        
        try{
            // add the records to the dataset.
            boolean tempContinue = true;
            while (tempContinue) {
                try{
                    tempContinue = tempResultSet.next();
                }
                catch (Exception e){
                    System.out.println("The ResultSet.next() threw "+e);
                    tempContinue = false;
                }
                
                if (tempContinue){
                    
                    try{
                        // create the attributes
                        Object[] tempObjects = new Object[tempNames.length];
                        Shape tempShape = null;
                        for (int i = 0; i < tempCount; i++) {
                            if (i == tempShapeCol) {
                                
                                // read the shape.
                                InputStream in = tempResultSet.getBinaryStream(i + 1);
                                tempShape = WKBParser.parseShape(in);
                            } else {
                                if (i < tempShapeCol) {
                                    tempObjects[i] = tempResultSet.getObject(i + 1);
                                } else
                                    tempObjects[i - 1] = tempResultSet.getObject(i + 1);
                            }
                        }
                        
                        // add the record to the dataset
                        SpatialExtenderRecord tempRecord = new SpatialExtenderRecord();
                        tempRecord.setAttributeTypes(tempTypes);
                        tempRecord.setAttributeNames(tempNames);
                        tempRecord.setAttributes(tempObjects);
                        tempRecord.setShape(tempShape);
                        tempDataset.add(tempRecord);
//                        fireRead(tempRecord);
                    }
                    catch (Exception e){
                        System.out.println("Inner Loop "+e);
                        e.printStackTrace();
                    }
                    catch (Throwable t){
                        System.out.println("Inner Loop "+t);
                        t.printStackTrace();
                    }
                }
            }
            tempStatement.close();               
        }
        catch (Exception e){
            System.out.println("Outer Loop "+e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println("Outer Loop "+t);
            t.printStackTrace();
        }
                        
        // return the completed dataset.
        return tempDataset;
    }
    
    /**
     * Returns true.
     */
    public boolean isUpdateable(){
        return true;
    }
    
    /** Constant for saving the tablename to the configuration file. */    
    private static final String TABLE_NAME = "TableName";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.addAttribute(TABLE_NAME, myDatabaseTableName);
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        super.setNode(inNode);
        String tempString = inNode.getAttribute(TABLE_NAME);
        myDatabaseTableName = tempString;
        connect();
    }
    
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }    
}
