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

package gistoolkit.datasources;

import java.util.*;
import gistoolkit.common.*;
import gistoolkit.features.*;

/**
 * Super class for joining tabular information to shape information.  The shape information will be
 * retrieved from the source data source.
 *
 * The data in the source data source will be read and joined in code with
 * tabular data retrieved by this data source.  The resulting data will be combined and returned to the client
 * as a single table with the attributes of both tables, and the shapes of the source data source.
 *
 * Set the column from the source data that is to be used with the setDatasourceJoinColumn method, and
 * set the column for this data source with the setTableJoinColumn method.
 *
 * The data source first reads the data from the source datasource, then it compares the data from the
 * join data source to that of the source data source, and throws away any that do not have a match.  It 
 * also trims the source data source to the envelope.  This results in a standard inner join between the
 * source data, and the join data.
 */
public abstract class SimpleJoinDataSource extends SimpleDataSource{
    
    /** The name of the DataSource column to join with. */
    private String myDatasourceJoinColumn = null;
    /** Set the name of the datasource column to join with. */
    public void setDatasourceJoinColumn(String inColumnName){
        myDatasourceJoinColumn = inColumnName;
    }
    /** Get the name of the DB2 column to join with. */
    public String getDatasourceJoinColumn(){return myDatasourceJoinColumn;}
    
    /** The name of the DB2 column to join with. */
    private String myTableJoinColumn = null;
    /** Set the name of the datasource column to join with. */
    public void setTableJoinColumn(String inColumnName){
        myTableJoinColumn = inColumnName;
    }
    /** Get the name of the DB2 column to join with. */
    public String getTableJoinColumn(){return myTableJoinColumn;}
    
    /**Boolean to indicate if the subject datasource should */
    private boolean myJoinedDataCached = false;
    /**
     * true to indicate that the joined datasource should be queried for all of it's records, the records cached, and then joined to the db2 data or
     * false to indicate that the joined datasource should be queried on the fly for it's records, and then those records joined to the DB2 data.
     */
    public void setJoinedDataCached(boolean inCached){myJoinedDataCached = true;}
    /**
     * true to indicate that the joined datasource should be queried for all of it's records, the records cached, and then joined to the db2 data or
     * false to indicate that the joined datasource should be queried on the fly for it's records, and then those records joined to the DB2 data.
     */
    public boolean getJoinedDataCached(){return myJoinedDataCached;}
    
    /** The data source to join to. */
    private DataSource myJoinDataSource = null;
    /** Retrieve the joined datasource. */
    public DataSource getJoinDataSource(){return myJoinDataSource;}
    
    /** Hash table for holding the returns from the datasource. */
    private Hashtable myHashtableCache = null;
    
    /** Attribute Names of the datasource data. */
    private String[] myJoinDataSourceAttributeNames = new String[0];
    /** return the datasource attribute names. */
    protected String[] getJoinDataSourceAttributeNames(){return myJoinDataSourceAttributeNames;}
    
    /** Attribute Types of the datasource data. */
    private AttributeType[] myJoinDataSourceAttributeTypes = new AttributeType[0];
    /** Method to return the attribute types for the data source. */
    protected AttributeType[] getJoinDataSourceAttributeTypes(){return myJoinDataSourceAttributeTypes;}
    
    /**
     * For use with configuration only where the source data source is to be set with the setNode() function.
     */
    public SimpleJoinDataSource(){};
    
    /**
     * Create a new DB2JoinDataSource with this data source as the source node.
     */
    public SimpleJoinDataSource(DataSource inSourceDataSource){
        myJoinDataSource = inSourceDataSource;
    }
    
    /** Read the data for the joined data source. */
    public synchronized void readDataSource(Envelope inEnvelope) throws Exception {
        if (myJoinDataSource != null){
            if ((myJoinedDataCached) && (myHashtableCache != null)){
                return;
            }
            
            // clear the current table
            if (myHashtableCache == null) myHashtableCache = new Hashtable();
            else myHashtableCache.clear();
            
            // read the data source.
            GISDataset tempDataset = null;
            if (!myJoinedDataCached) tempDataset = myJoinDataSource.readDataset(inEnvelope);
            else tempDataset = myJoinDataSource.readDataset();
            
            // populate the hash table.
            if (tempDataset != null){
                // store the dataset in the hash table
                int tempRowIndex = -1;
                String[] tempNames = tempDataset.getAttributeNames();
                for (int i=0; i<tempNames.length; i++){
                    if (tempNames[i].equalsIgnoreCase(getDatasourceJoinColumn())){
                        tempRowIndex = i;
                        break;
                    }
                }
                
                // if we did not find the column throw an exception
                if (tempRowIndex == -1) throw new Exception("Did not find the datasource join column "+getDatasourceJoinColumn());
                
                // add all of the records to the hash table
                for (int i=0; i<tempDataset.size(); i++){
                    Object[] tempObjects = tempDataset.getAttributeValues(i);
                    myHashtableCache.put(tempObjects[tempRowIndex], tempDataset.getRecord(i));
                }
                
                // set the attribute names and types
                myJoinDataSourceAttributeNames = tempDataset.getAttributeNames();
                myJoinDataSourceAttributeTypes = tempDataset.getAttributeTypes();
            }
        }
    }
    
    /** Return the data source record associated with this key. */
    public Record getDatasourceRecord(Object inKey){
        return (Record) myHashtableCache.get(inKey);
    }
    
    /** This method should return the shapes from the data source */
    protected abstract GISDataset readShapes(Envelope inEnvelope) throws Exception;
    
    /**Returns the bounding rectangle of all the shapes in the Data Source.*/
    public Envelope readEnvelope() throws Exception {
        return myJoinDataSource.getEnvelope();
    }
    private static final String DATASOURCE_NODE = "JoinDatasource";
    private static final String DATASOURCE_CLASS_NAME = "JoinDatasourceClass";
    private static final String DATASOURCE_JOIN_COLUMN = "JoinDatasourceColumn";
    private static final String TABLE_JOIN_COLUMN = "JoinTableColumn";
    private static final String DATASOURCE_CACHED = "DatasourceCached";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("SimpleJoinDataSource");
        
        // connection parameters
        tempRoot.addAttribute(TABLE_JOIN_COLUMN, getTableJoinColumn());
        tempRoot.addAttribute(DATASOURCE_JOIN_COLUMN, getDatasourceJoinColumn());
        tempRoot.addAttribute(DATASOURCE_CACHED, ""+getJoinedDataCached());
        
        // add the node from the Datasource
        if (myJoinDataSource != null){
            Node tempNode = new Node(DATASOURCE_NODE);
            tempNode.addAttribute(DATASOURCE_CLASS_NAME, myJoinDataSource.getClass().getName());
            tempRoot.addChild(tempNode);
            tempNode.addChild(myJoinDataSource.getNode());
        }
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("Can not set up DB2 Datasource configuration information is null");
        super.setNode(inNode);
        String tempName = null;
        String tempValue = null;
        
        // join parameters
        tempName = TABLE_JOIN_COLUMN;
        tempValue = inNode.getAttribute(tempName);
        setTableJoinColumn(tempValue);
        tempName = DATASOURCE_JOIN_COLUMN;
        tempValue = inNode.getAttribute(tempName);
        setDatasourceJoinColumn(tempValue);
        tempName = DATASOURCE_CACHED;
        tempValue = inNode.getAttribute(tempName);
        setJoinedDataCached(false);
        if (tempValue != null){
            if (tempValue.toUpperCase().startsWith("T")){
                setJoinedDataCached(true);
            }
        }
        
        // get the node for the joined data source.
        Node tempNode = inNode.getChild(DATASOURCE_NODE);
        if (tempNode == null) throw new Exception("No Joined Datasource configuration found.");
        
        String tempString = tempNode.getAttribute(DATASOURCE_CLASS_NAME);
        if (tempString == null) throw new Exception("No "+DATASOURCE_CLASS_NAME+" found for joined datasource.");
        
        myJoinDataSource = (DataSource) Class.forName(tempString).newInstance();
        Node[] tempNodes = tempNode.getChildren();
        if (tempNodes.length > 0){
            myJoinDataSource.setNode(tempNodes[0]);
        }
        
    }
    
}
