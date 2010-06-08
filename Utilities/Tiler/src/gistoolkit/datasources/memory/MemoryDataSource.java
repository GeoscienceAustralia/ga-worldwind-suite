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

package gistoolkit.datasources.memory;
import java.util.*;
import gistoolkit.features.*;
import gistoolkit.common.*;
import gistoolkit.datasources.*;
/**
 * This data source only exists in memory.  It does not contain it's own persistent storage.
 * @author  ithaqua
 * @version
 */
public class MemoryDataSource extends SimpleDataSource {
    
    /** An Identifier string for this datasource */
    private String myName = "Temporary";
    /**Sets an identifier string for the datasource.*/
    public void setName(String inName) {myName = inName;}
    /** Returns the identifier string for the datasource.*/
    public String getName() {return myName;}
    
    /** The attribute names for this data source. */
    private String[] myAttributeNames = new String[0];
    /** Returns the list of attribute names for this data source. */
    public String[] getAttributeNames(){return myAttributeNames;}
    
    /** The attribute types for this data source. */
    private AttributeType[] myAttributeTypes = new AttributeType[0];
    /** Returns the list of attribute types for this data source. */
    public AttributeType[] getAttributeTypes(){return myAttributeTypes;}
    
    /** A dataset to contain all of the shapes currently committed to this memory data source */
    private GISDataset myCommittedDataset = null;
    
    /** A dataset to contain all of the shapes to be added since the last commit.*/
    private GISDataset myInsertDataset = null;
    
    /** A dataset to contain all of the shapes to be deleted since the last commit. */
    private GISDataset myDeleteDataset = null;
    
    /** A dataset to contain all of the shapes to be updated in this data set since the last commit. */
    private GISDataset myUpdateDataset = null;
    
    /** Creates new MemoryDataSource */
    public MemoryDataSource() {
        myCommittedDataset = new GISDataset(new String[0]);
    }
    
    /** Creates a new MemoryDataSource with the given name */
    public MemoryDataSource(String inName){
        myName = inName;
        myCommittedDataset = new GISDataset(new String[0]);
    }
    
    /** Creates new MemoryDataSource beginning with the given dataset*/
    public MemoryDataSource(String inName, GISDataset inDataset) {
        myName = inName;
        myCommittedDataset = new GISDataset();
        if (inDataset != null){
            for (int i=0; i<inDataset.size(); i++){
                myCommittedDataset.add(new MemoryRecord(inDataset.getRecord(i)));
            }
        }
    }
       
    /** Reconcile the attributes of the inserted record with that of the data source. 
     *
     * Does a case insensative comparison of attribute names from the record sent in to that
     * of the current table.  If there are records in the current table that do not exist in
     * the current record, then the current record is updated to add these attributes with 
     * default values.  If there are attributes in the current record that are not in the current
     * table, then the records in the current table are updated to include the attribute.
     */
    private MemoryRecord reconcileTypes(MemoryRecord inRecord){
        if (inRecord == null) return inRecord;
        
        // check for elements in the new record that are not in the current table.
        Vector tempNotFoundNameVect = new Vector();
        Vector tempNotFoundTypeVect = new Vector();
        String[] tempAttributeNames = inRecord.getAttributeNames();
        AttributeType[] tempAttributeTypes = inRecord.getAttributeTypes();
        for (int i=0; i<tempAttributeNames.length; i++){
            boolean tempNotFound = true;
            for (int j=0; j<myAttributeNames.length; j++){
                if (myAttributeNames[i].equalsIgnoreCase(myAttributeNames[i])){
                    tempNotFound = false;
                }
            }
            if (tempNotFound){
                tempNotFoundNameVect.addElement(tempAttributeNames[i]);
                tempNotFoundTypeVect.addElement(tempAttributeTypes[i]);
            }
        }
        
        // if there are elements that are not in the current record, then update the current table to add them.
        if (tempNotFoundNameVect.size() > 0){
            String[] tempNewNames = new String[myAttributeNames.length + tempNotFoundNameVect.size()];
            AttributeType[] tempNewTypes = new AttributeType[myAttributeNames.length + tempNotFoundNameVect.size()];
            for (int i=0; i<myAttributeNames.length; i++){
                tempNewNames[i] = myAttributeNames[i];
                tempNewTypes[i] = myAttributeTypes[i];
            }
            for(int i=0; i<tempNotFoundNameVect.size(); i++){
                tempNewNames[myAttributeNames.length + i] = (String) tempNotFoundNameVect.get(i);
                tempNewTypes[myAttributeTypes.length + i] = (AttributeType) tempNotFoundTypeVect.get(i);
            }
            
            setAttributeNamesAndTypes(tempNewNames, tempNewTypes);
        }
        
        // update the attribute types and values for the current record.
        Object[] tempAttributes = inRecord.getAttributes();
        Object[] tempNewAttributes = new Object[myAttributeNames.length];
        for (int i=0; i<myAttributeNames.length; i++){
            for (int j=0; j<tempAttributeNames.length; j++){
                if (myAttributeNames[i].equalsIgnoreCase(tempAttributeNames[j])){
                    tempNewAttributes[i] = tempAttributes[j];
                    break;
                }
            }
        }
        inRecord.setAttributeNames(myAttributeNames);
        inRecord.setAttributeTypes(myAttributeTypes);
        inRecord.setAttributes(tempNewAttributes);
        return inRecord;
    }
    
    /** Method to set the attributes, names and types for this data source.
     *  The existing records in the data source will be updated with the new table structure.
     *  As much of the existing data that can be converted will be.
     */
    public void setAttributeNamesAndTypes(String[] inAttributeNames, AttributeType[] inAttributeTypes){
        
        // set the types and names.
        myAttributeTypes = inAttributeTypes;
        myAttributeNames = inAttributeNames;
        
        // update the internal datasets.
        myCommittedDataset = updateDataset(myCommittedDataset);
        myDeleteDataset =updateDataset(myDeleteDataset);
        myInsertDataset =updateDataset(myInsertDataset);
        myUpdateDataset =updateDataset(myUpdateDataset);
    }
    
    /** Updates the dataset with the new attribute names and types.*/
    private GISDataset updateDataset(GISDataset inDataset){
        if (inDataset == null) return inDataset;
        GISDataset tempDataset = new GISDataset(myAttributeNames, myAttributeTypes);
        for (int i=0; i<inDataset.size(); i++){
            Record tempRecord = inDataset.getRecord(i);
            String[] tempAttributeNames = tempRecord.getAttributeNames();
            Object[] tempAttributes = tempRecord.getAttributes();
            Object[] tempNewAttributes = new Object[myAttributeNames.length];
            for (int j=0; j<myAttributeNames.length; j++){
                for (int k=0; k<tempAttributeNames.length; k++){
                    if (myAttributeNames[j].equalsIgnoreCase(tempAttributeNames[k])){
                        tempNewAttributes[j] = tempAttributes[k];
                        break;
                    }
                }
            }
            tempRecord.setAttributeNames(myAttributeNames);
            tempRecord.setAttributeTypes(myAttributeTypes);
            tempRecord.setAttributes(tempNewAttributes);
            tempDataset.add(tempRecord);
        }
        return tempDataset;
    }
        
    
    /**
     * Inserts the given record into the datasource.  Nulls are not allowed in the memory datasource.
     */
    public void doInsert(Record inRecord) throws Exception {
        // can not add nulls to the memory data source
        if (inRecord == null) return;
        if (myInsertDataset == null) myInsertDataset = new GISDataset();
        MemoryRecord tempRecord = null;
        if (inRecord instanceof MemoryRecord){
            tempRecord = (MemoryRecord) inRecord;
        }
        else{
            tempRecord = new MemoryRecord(inRecord);
        }
        reconcileTypes(tempRecord);
        myInsertDataset.add(tempRecord);
        clearCache();
    }
    
    /**
     * Commit all changes since the last commit.
     */
    public void doCommit() throws Exception {
        // this should get the entire set of records with the addition of the inserted set, and the removal of the deleted set, and with the updated set intacted.
        GISDataset tempDataset = readDataset();
        myCommittedDataset = new GISDataset();
        for (int i=0; i<tempDataset.size(); i++){
            // if this is not a memory record, then create one
            Record tempRecord = tempDataset.getRecord(i);
            if (tempRecord instanceof MemoryRecord){
                ((MemoryRecord) tempRecord).myOrrigionalShape = tempRecord.getShape();
                ((MemoryRecord) tempRecord).setOrrigionalAttributes(tempRecord.getAttributes());
            }
            else tempRecord = new MemoryRecord(tempRecord);
            
            // add the record to the committed set.
            myCommittedDataset.add(tempRecord);
        }
        myDeleteDataset = null;
        myInsertDataset = null;
        myUpdateDataset = null;
    }
    
    /**
     * Initialize the data source from the properties.
     */
    public void load(Properties inProperties) {
    }
    
    /**
     * Returns the bounding rectangle of all the shapes in the Data Source.
     */
    public Envelope readEnvelope() throws Exception {
        GISDataset tempDataset = readDataset();
        return tempDataset.getEnvelope();
    }
    
    /**
     * Determines if this datasource is updateable.
     */
    public boolean isUpdateable() {
        return true;
    }
        
    /**
     * Delete this record from the database.
     */
    public void doDelete(Record inRecord) throws Exception {
        // look through the updated set to find the record
        if (myUpdateDataset != null){
            for (int i=0; i<myUpdateDataset.size(); i++){
                MemoryRecord tempMRecord = (MemoryRecord) myUpdateDataset.getRecord(i);
                if ((tempMRecord == inRecord) || (tempMRecord.getShape() == inRecord.getShape())){
                    
                    // create a new dataset without the record.
                    GISDataset tempDataset = new GISDataset();
                    for (int j = 0; j<myUpdateDataset.size(); j++){
                        if (!(myUpdateDataset.getRecord(j) == inRecord)){
                            tempDataset.add(myUpdateDataset.getRecord(j));
                        }
                    }
                    myUpdateDataset = tempDataset;
                    break;
                }
            }
        }
        
        // look through the inserted set to find the record
        boolean tempFound = false;
        if (myInsertDataset != null){
            for (int i=0; i<myInsertDataset.size(); i++){
                if ((myInsertDataset.getRecord(i) == inRecord)||(myInsertDataset.getRecord(i).getShape() == inRecord.getShape())){
                    
                    // create a new dataset without the record.
                    GISDataset tempDataset = new GISDataset();
                    for (int j = 0; j<myInsertDataset.size(); j++){
                        if ((myInsertDataset.getRecord(j) == inRecord)||(myInsertDataset.getRecord(j).getShape() == inRecord.getShape())){
                            // do nothing
                        }
                        else {
                            tempDataset.add(myInsertDataset.getRecord(j));
                        }
                    }
                    myInsertDataset = tempDataset;
                    tempFound = true;
                    break;
                }
            }
        }
        
        // look through the committed dataset to find the record
        if ((tempFound == false) && (myCommittedDataset != null)){
            
            // look for it in the committed vector.
            for (int i=0; i<myCommittedDataset.size(); i++){
                if ((myCommittedDataset.getRecord(i) == inRecord)||(myCommittedDataset.getRecord(i).getShape() == inRecord.getShape())){
                    
                    // add the record to the list of deleted records
                    if (myDeleteDataset == null) myDeleteDataset = new GISDataset();
                    if (!(inRecord instanceof MemoryRecord)) inRecord = new MemoryRecord(inRecord);
                    myDeleteDataset.add(inRecord);
                    tempFound = true;
                    break;
                }
            }
            
            // if the record was not found, then throw an exception
            if (!tempFound) throw new Exception("Record not found in Data Source");            
            else clearCache();
        }
    }
    
    /**
     * Update the data source with the changed record.
     */
    public void doUpdate(Record inRecord) throws Exception {
        // if this is not a memory record, then bail
        if (!(inRecord instanceof MemoryRecord)) throw new Exception("Can not update this record, it did not come from this datasource"+inRecord);
        MemoryRecord tempInRecord = (MemoryRecord) inRecord;
        
        // This record may already have been updated.
        boolean tempFound = false;
        if (myUpdateDataset != null){
            for (int i=0; i<myUpdateDataset.size(); i++){
                MemoryRecord tempMRecord = (MemoryRecord) myUpdateDataset.getRecord(i);
                if ((tempMRecord == tempInRecord) || (tempInRecord.myOrrigionalShape == tempMRecord.myOrrigionalShape)){
                    // create the new Update Dataset with the new record instead of the old one.
                    GISDataset tempDataset = new GISDataset();
                    if (!(inRecord instanceof MemoryRecord)) inRecord = new MemoryRecord(inRecord);
                    for (int j=0; j<myUpdateDataset.size(); i++){
                        if (j == i) tempDataset.add(inRecord);
                        else tempDataset.add(myUpdateDataset.getRecord(j));
                    }
                    myUpdateDataset = tempDataset;
                    break;
                }
            }
        }
        
        // this record may have been inserted.
        if ((!tempFound) && (myInsertDataset != null)){
            for (int i=0; i<myInsertDataset.size(); i++){
                MemoryRecord tempMRecord = (MemoryRecord) myInsertDataset.getRecord(i);
                if ((tempMRecord == tempInRecord) || (tempInRecord.myOrrigionalShape == tempMRecord.myOrrigionalShape)){
                    
                    // create a new insert dataset with the new record.
                    GISDataset tempDataset = new GISDataset();
                    for (int j=0; j<myInsertDataset.size(); j++){
                        if (j==i) tempDataset.add(inRecord);
                        else tempDataset.add(myInsertDataset.getRecord(j));
                    }
                    myInsertDataset = tempDataset;
                    tempFound = true;
                    break;
                }
            }
        }
        
        // try to find the record in the main committed set
        if ((!tempFound) && (myCommittedDataset != null)){
            for (int i=0; i<myCommittedDataset.size(); i++){
                MemoryRecord tempMRecord = (MemoryRecord) myCommittedDataset.getRecord(i);
                if ((tempMRecord == tempInRecord) || (tempInRecord.myOrrigionalShape == tempMRecord.myOrrigionalShape)){
                    myUpdateDataset.add(tempInRecord);
                    tempFound = true;
                }
            }
        }
        
        if (!tempFound) throw new Exception("Record not found to be updated");
        else clearCache();
        return;
    }
    
    /**
     * Rollback any changes to this datasource since the last commit.
     */
    public void doRollback() throws Exception {
        myDeleteDataset = null;
        myUpdateDataset = null;
        myInsertDataset = null;
    }
    
    /**
     * Reads all the objects from the data source.
     */
    public GISDataset readDataset() throws Exception {
        return readDataset(null);
    }
    
    /** Value of the name tag in the configuration */
    private static final String NAME_TAG = "Name";
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempNode = new Node("Memory Dataset");
        tempNode.addAttribute(NAME_TAG, myName);
        return tempNode;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            myName = inNode.getAttribute(NAME_TAG);
        }
    }
        
    /** Reads only the objects from the data source that intersect these envelope. */
    protected GISDataset readShapes(Envelope inEnvelope) throws Exception {
        
        GISDataset tempDataset = new GISDataset();
        
        // add all the Items in the committed data set.
        if (myCommittedDataset != null){
            for (int i=0; i<myCommittedDataset.size(); i++){
                Record tempRecord = myCommittedDataset.getRecord(i);
                Shape tempShape = tempRecord.getShape();
                
                // is this shape to be added?
                boolean tempAdd = true;
                if (inEnvelope != null){
                    if (tempShape != null){
                        if (!inEnvelope.overlaps(tempShape.getEnvelope())) {
                            tempAdd = false;
                        }
                    }
                }
                
                if (tempAdd == true){
                    // Check that this shape is not in the deleted set
                    if (myDeleteDataset != null){
                        for (int j=0; j<myDeleteDataset.size(); j++){
                            if (myDeleteDataset.getRecord(i) == tempRecord) tempAdd = false;
                        }
                    }
                    
                    // check that this record is not in the update vector
                    if ((myUpdateDataset != null) && (tempAdd == true)){
                        for (int j=0; j<myUpdateDataset.size(); j++){
                            MemoryRecord tempMRecord = (MemoryRecord) myUpdateDataset.getRecord(j);
                            if ((tempMRecord == tempRecord)||(tempMRecord.myOrrigionalShape == tempShape)){
                                tempDataset.add(tempMRecord);
                                tempAdd = false;
                            }
                        }
                        if (tempAdd) tempDataset.add(tempRecord);
                    }
                }
            }
        }
        
        // add all the Items in the insert data set
        if (myInsertDataset != null){
            for (int i=0; i<myInsertDataset.size(); i++){
                Shape tempShape = myInsertDataset.getShape(i);
                Record tempRecord = myInsertDataset.getRecord(i);
                boolean tempAdd = true;
                if (inEnvelope != null){
                    if (tempShape != null){
                        if (!inEnvelope.overlaps(tempShape.getEnvelope())) {
                            tempAdd = false;
                        }
                    }
                }
                if (tempAdd == true) tempDataset.add(myInsertDataset.getRecord(i));
            }
        }
        
        // return the fully populated dataset
        return tempDataset;
    }    
    /** Get the style to use with this datasource.  */
    public gistoolkit.display.Style getStyle() {
        return null;
    }  
}
