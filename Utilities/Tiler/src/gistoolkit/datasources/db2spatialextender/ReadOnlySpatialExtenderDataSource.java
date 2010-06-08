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
import gistoolkit.projection.*;
import gistoolkit.datasources.*;
/**
 * Allows connections to a DB2 Database given an arbitrary SQL String.
 *<p>
 *This allows the datasource to be configured to select data from a db2 join, and even
 *allows selecting data from temporary tables.  An Example may be:
 *</p>
 *<p>
 *SELECT mktregion.profit Profit, db2gse.st_AsBinary(County.shape) FROM mktregion, county WHERE mktregion.fips = county.fips
 *</p>
 */
public class ReadOnlySpatialExtenderDataSource extends SpatialExtenderDataSource implements DataSource{    
    /**
     * The string to apply to select the data from DB2.
     */
    public String myDatabaseQuery;
    /** retrieve the sql string that defines the layer.*/
    public String getSQLString(){return myDatabaseQuery;}
    /**sets the sql string that defines this layer.*/
    public void setSQLString(String inQuery){myDatabaseQuery = inQuery;}
    
    /** Indicates whether to preread this data source, keeping the contents in memory until the data source is disposed. Sending a readDataset(null) will refresh the cache.*/
    private boolean myPreread = false;
    /** Sets the preread flag to tell this data source to preread the entire contents of the layer, and keep them in memory, essentially caching them, until the data source is disposed. */
    public void setPreread(boolean inPreread){ myPreread = inPreread;}
    /** Returns the preread flag.  This flag tells the data source to preread the entire contents of the layer, and to cach that information in memory until the data source is disposed. */
    public boolean getPreread(){return myPreread;}
    
    /**
     * SpatialExtenderDataSource constructor comment.
     */
    public ReadOnlySpatialExtenderDataSource() {
        super();
    }
    
    /**
     * Connects this datasource to the database, calls the connect in the super class.
     */
    public Connection connect() throws Exception{
        //setName(getDatabaseServername()+":"+getDatabaseShapeColumn());
        return super.connect();
    }
    
    /**
     * Reads only the objects from the data source that intersect these Envelope.
     * This is heare instead of in the super class because of the preread logic.
     */
    public synchronized GISDataset readDataset(Envelope inEnvelope) throws Exception {
        Envelope tempEnvelope = inEnvelope;
        if (getCacheEnvelope() != null){
            if (isCachedProjected() == false){
                tempEnvelope = ShapeProjector.projectBackward(getToProjection(), inEnvelope);
                if ((getPreread()) || (getCacheEnvelope().contains(tempEnvelope))){
                    GISDataset tempDataset = queryFromCache(tempEnvelope);
                    tempDataset = (GISDataset) tempDataset.clone();
                    projectForward(getToProjection(), tempDataset);
                    return tempDataset;
                }
            }
            else {
                if ((getPreread()) || getCacheEnvelope().contains(tempEnvelope)){
                    return queryFromCache(tempEnvelope);
                }
            }
        }
        return super.readDataset(inEnvelope);
    }
    
    /**
     * Reads the objects from the database that fall within the given Envelope.
     * If a null is sent in for the Envelope, all the objects in the shape file are read.
     */
    public synchronized GISDataset readShapes(Envelope inEnvelope) throws Exception {
        
        // retrieve the connection
       Connection tempCon = connect();
        
        // create the statement
        Statement tempStatement = tempCon.createStatement();
        SQLConverter tempConverter = getSQLConverter();
        
        // add the where clause to include the Envelope.
        String tempWhereString = "";
        StringBuffer sb = new StringBuffer(myDatabaseQuery);
        String tempUpper = myDatabaseQuery.toUpperCase();
        String tempWhere = null;
        if (inEnvelope != null){
            // create the where clause
            tempWhere = getWhereString(inEnvelope);
        }
        String tempFilterSQL = getFilterSQL();
        if ((tempFilterSQL != null) && (tempFilterSQL.length() > 0)){
            if (tempWhere != null){
                tempWhere = tempWhere + " AND " + tempFilterSQL;
            }
            else {
                tempWhere = tempFilterSQL;
            }
        }
        
        if (tempWhere != null){
            // check for shapewhere
            int tempIndex = tempUpper.indexOf("SHAPEWHERE");
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
        
        int tempCount = tempMetaData.getColumnCount();
        ArrayList tempNameList = new ArrayList(tempCount);
        ArrayList tempTypeList = new ArrayList(tempCount);
        for (int i = 0; i < tempCount; i++) {
            String tempString = tempMetaData.getColumnName(i + 1);
            if (getDatabaseShapeColumn().equalsIgnoreCase(tempString)) {
                tempShapeCol = i;
            } else {
                AttributeType tempType = tempConverter.getAttributeType(tempMetaData, i);
                tempTypeList.add(tempType);
                tempNameList.add(tempString);
            }
        }
        String[] tempNames = new String[tempNameList.size()];
        tempNameList.toArray(tempNames);
        AttributeType[] tempTypes = new AttributeType[tempTypeList.size()];
        tempTypeList.toArray(tempTypes);
        
        // create the dataset
        myTempDataset = new GISDataset(tempNames, tempTypes);
        
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
                                try{
                                    // read the binary stream.
                                    InputStream in = tempResultSet.getBinaryStream(i + 1);
                                    tempShape = WKBParser.parseShape(in);
                                    if (tempShape == null) System.out.println("Null Shape "+getName()+" i = "+i+" tempShapeCol = "+tempShapeCol+" Record = "+myTempDataset.size());
                                    in.close();
                                }
                                catch (Exception e){
                                    System.out.println(getName()+" i = "+i+" tempShapeCol = "+tempShapeCol+" Record = "+myTempDataset.size());
                                    e.printStackTrace();
                                }
                                
                                // indicate that the value has been read
                            } else {
                                if (i < tempShapeCol) {
                                    tempObjects[i] = tempResultSet.getObject(i + 1);
                                } else
                                    tempObjects[i - 1] = tempResultSet.getObject(i + 1);
                            }
                        }
                        myTempDataset.add(tempObjects, tempShape);
                        fireRead(myTempDataset.getRecord(myTempDataset.size()-1));
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
            }// end for
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
        
        // return the completed dataset.
        System.out.println(getName() +" Read "+myTempDataset.size()+" Records");
        return myTempDataset;
    }
    
    
    /**
     * For the Multi Threading
     */
    private GISDataset myTempDataset= null;
    
    
    private static final String QUERY = "QUERY";
    private static final String PREREAD = "Preread";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.addAttribute(QUERY, myDatabaseQuery);
        tempRoot.addAttribute(PREREAD, ""+getPreread());
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        super.setNode(inNode);
        String tempString = inNode.getAttribute(QUERY);
        myDatabaseQuery = tempString;
        String tempPrereadString = inNode.getAttribute(PREREAD);
        if (tempPrereadString == null) setPreread(false);
        else{
            if (tempPrereadString.toUpperCase().trim().startsWith("T")){
                setPreread(true);
            }
        }
        if (getPreread()){
            this.readDataset();
        }
    }
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }  
}