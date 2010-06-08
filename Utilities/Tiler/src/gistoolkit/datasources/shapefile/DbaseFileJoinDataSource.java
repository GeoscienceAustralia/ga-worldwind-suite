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

package gistoolkit.datasources.shapefile;

import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;

/**
 *
 * A class to allow records from a dbase 3 file to be concatinated with the records from another data source.
 *
 * There are many cases where a user will want to shade a map against data in a dbase 3 file.  The dbase file will contain
 * data aggrigated by fips code for example.  This data then needs to be joined against a shape file or database based on the fips code.
 * This class facilitates that process.
 *
 * Set the column from the source data that is to be used with the setDatasourceJoinColumn method, and
 * set the column for this data source with the setTableJoinColumn method.
 *
 * The data source first reads the data from the source datasource, then it compares the data from the
 * join data source to that of the source data source, and throws away any that do not have a match.  It 
 * also trims the source data source to the envelope.  This results in a standard inner join between the
 * source data, and the join data.
 *
 */
public class DbaseFileJoinDataSource extends SimpleJoinDataSource{
    
    /**Name of this datasource.*/
    private String myName = "DBaseFile";
    /** Return the name of this datasource for display to the user. */
    public String getName(){return myName;}
    /** Set the name of this datasource for display purposes. */
    public void setName(String inName){myName = inName;}
    
    /**The fully qualified file name (path and filename) of the dbase file..*/
    private String myDbaseFileName = "";
    /** Returns the url base to use when connecting through the JDBC driver. */
    public String getDbaseFileName() {return myDbaseFileName;}
    /** Sets the url base to use when connecting through the JDBC driver.*/
    public void setDbaseFileName(String inDbaseFileName){myDbaseFileName = inDbaseFileName;}
    
    
    /**
     * For use with configuration only where the source data source is to be set with the setNode() function.
     */
    public DbaseFileJoinDataSource(){};
    
    /**
     * Create a new DB2JoinDataSource with this data source as the source node.
     */
    public DbaseFileJoinDataSource(DataSource inSourceDataSource){
        super(inSourceDataSource);
    }
    
    
    /**
     * Reads the objects from the database that fall within the given Envelope.
     * If a null is sent in for the Envelope, all the objects in the shape file are read.
     */
    public synchronized GISDataset readShapes(Envelope inEnvelope) throws Exception {
        
        // retrieve the dataset from the source
        readDataSource(inEnvelope);
        String[] tempDatasourceAttributeNames = getJoinDataSourceAttributeNames();
        AttributeType[] tempDatasourceAttributeTypes = getJoinDataSourceAttributeTypes();
        int tempDatasourceCount = tempDatasourceAttributeNames.length;
        
        // read the header informatil.
        DbaseFileReader tempFileReader = new DbaseFileReader(myDbaseFileName);
        tempFileReader.readHeader();
        String[] tempAttributeNames = tempFileReader.getFieldNames();
        AttributeType[] tempAttributeTypes = tempFileReader.getFieldTypes();
        int tempCount = tempAttributeNames.length;
        
        String[] tempNames = new String[tempCount + tempDatasourceCount];
        AttributeType[] tempTypes = new AttributeType[tempCount+tempDatasourceCount];
        for (int i=0; i<tempDatasourceCount; i++){
            tempNames[i] = tempDatasourceAttributeNames[i];
            tempTypes[i] = tempDatasourceAttributeTypes[i];
        }
        
        int tempJoinIndex = -1;
        for (int i = 0; i < tempAttributeNames.length; i++) {
            String tempName = tempAttributeNames[i];
            if (tempName.equalsIgnoreCase(getTableJoinColumn())){
                tempJoinIndex = i;
            }
            tempNames[tempDatasourceCount+i] = tempName;
            AttributeType tempType = tempAttributeTypes[i];
            tempTypes[tempDatasourceCount+i] = tempType;
        }
        if (tempJoinIndex == -1){
            throw new Exception("Did not find Table Join Column "+getTableJoinColumn());
        }
        
        
        // add the records.
        GISDataset tempDataset = new GISDataset(tempNames, tempTypes);
        Record tempRecord = tempFileReader.read();
        Object[] tempObjects = new Object[tempNames.length];
        while (tempRecord != null){
            // add the records to the dataset.
            // create the attributes
            Object[] tempAttributes = tempRecord.getAttributes();
            for (int i = 0; i < tempCount; i++) {
                Object tempObject = tempAttributes[i];
                if (i == tempJoinIndex) {
                    // locate this row.
                    Record tempDSRecord = getDatasourceRecord(tempObject);
                    // If the record is not found, then do not continue.
                    if (tempDSRecord == null) continue;
                    Object[] tempDSAttributes = tempDSRecord.getAttributes();
                    for (int j=0; j<tempDSAttributes.length; j++){
                        tempObjects[j] = tempDSAttributes[j];
                    }
                    tempDataset.add(tempObjects, tempDSRecord.getShape());
                }
                tempObjects[tempDatasourceCount+i] = tempObject;
            }
            tempObjects = new Object[tempNames.length];
            //          fireRead(tempDataset.getRecord(tempDataset.getNumShapes()-1));
        }
        
        // return the completed dataset.
        System.out.println("Read "+tempDataset.size()+" Records");
        return tempDataset;
    }
    
    
    private static final String DBASE_FILENAME = "FILENAME";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("DbaseFileDatasource");
        tempRoot.addAttribute(DBASE_FILENAME, myDbaseFileName);
        
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("Can not set up DbaseJoinDatasource configuration information is null");
        super.setNode(inNode);
        String tempFilename = inNode.getAttribute(DBASE_FILENAME);
        if (tempFilename != null){
            myDbaseFileName = tempFilename;
        }
    }
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }  
}
