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

import java.util.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;

/**
 * A data source bassed on a shape file.
 */
public class ShapeFileDataSource extends SimpleDataSource {    
    /**
     * The location of the shape file.
     */
    private String myFileName;
    
    /**
     * Shape File for reading and writing
     */
    private ShapeFile myShapeFile;

    /** Caches the envelope such that they can be redisplayed without re-querying */
    private Envelope myEnvelope = null;

    /**
     * This is the dataset for the entire shape file.
     */
    private ShapeFileDataset myDataset = null;
        
    /**
     * Records which have been deleted but not committed.
     */
    private ShapeFileDataset myDeleteDataset = null;
    
    /**
     * Records which have been updated but not committed
     */
    private ShapeFileDataset myUpdateDataset = null;
    
    /**
     * Records which have been inserted but not committed.
     */
    private ShapeFileDataset myInsertDataset = null;
        
    /** Name for the data source. */
    private java.lang.String myName;
        
    /**
     * create a new shape file, it is non functional until setFileName is called.
     */
    public ShapeFileDataSource() {
    }
    
    /**
     * create a new shape file with the given name.  Do not include the shp extension on the file name.
     */
    public ShapeFileDataSource(String inName) throws Exception {
        setFileName(inName);
    }
    
    /** Set the file name for the data source do not include the extension.*/
    public void setFileName(String inName) throws Exception{
        
        // String indicating the location of the shape file
        String tempName = inName;
        
        // check for the shp ending
        if ((inName.endsWith(".shp")) || (inName.endsWith(".SHP"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        // check for the shx ending
        if ((inName.endsWith(".shx")) || (inName.endsWith(".SHX"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        // check for the dbf ending
        if ((inName.endsWith(".dbf")) || (inName.endsWith(".DBF"))) {
            tempName = inName.substring(0, tempName.length() - 4);
        }
        
        // save the name
        myFileName = tempName;
        myName = tempName;
        if (myShapeFile != null) myShapeFile.setFile(inName);
    }
    
    /**
     * Commit the data since the last commit.
     */
    public void commit() throws Exception{
        // retrieve the attribute names from the current dataset
        ShapeFileDataset tempDataset = new ShapeFileDataset(myDataset.getAttributeNames());
        
        // construct a new dataset from the old with the update of the records.
        boolean tempFound = false;
        int tempIndex = 1;
        for (int i=0; i<myDataset.size(); i++){
            ShapeFileRecord rt = (ShapeFileRecord) myDataset.getRecord(i);
            
            // loop through the update dataset to find this record
            if (myUpdateDataset != null){
                
                for (int j=0; j<myUpdateDataset.size(); j++){
                    ShapeFileRecord ru = (ShapeFileRecord) myUpdateDataset.getRecord(j);
                    if (ru.getIndex() == rt.getIndex()){
                        tempFound = true;
                        myUpdateDataset.remove(ru);
                        rt = ru;
                        break;
                    }
                }
            }
            
            // search through the delete dataset and remove any records that match in the current dataset.
            if (myDeleteDataset != null){
                for (int j=0; j<myDeleteDataset.size(); j++){
                    ShapeFileRecord rd = (ShapeFileRecord) myDeleteDataset.getRecord(j);
                    if (rd.getIndex() == rt.getIndex()) {
                        rt = null;
                        myDeleteDataset.remove(rd);
                    }
                }
            }
                        
            if (rt != null){
                rt.setIndex(tempIndex);
                tempIndex++;
                tempDataset.add(rt);
            }
        }
        
        // if there are any records left in the update dataset, add them at the end
        if (myUpdateDataset != null){
            for (int i=0; i<myUpdateDataset.size(); i++){
                ShapeFileRecord ra = (ShapeFileRecord) myUpdateDataset.getRecord(i);
                ra.setIndex(tempIndex);
                tempIndex++;
                tempDataset.add(ra);
            }
        }
        // search through the delete dataset and remove any records that match in the current dataset.
        if (myDeleteDataset != null){
            for (int i=0; i<myDeleteDataset.size(); i++){
                ShapeFileRecord rd = (ShapeFileRecord) myDeleteDataset.getRecord(i);
                for (int j=0; j<tempDataset.size(); j++){
                    ShapeFileRecord rt = (ShapeFileRecord) tempDataset.getRecord(j);
                    if (rd.getIndex() == rt.getIndex()){
                        tempDataset.remove(rt);
                    }
                }
            }
        }
        
        // add all the records in the insert dataset
        if (myInsertDataset != null){
            for (int i=0; i<myInsertDataset.size(); i++){
                ShapeFileRecord ri = (ShapeFileRecord) myInsertDataset.getRecord(i);
                ri.setIndex(tempIndex);
                tempIndex++;
                tempDataset.add(ri);
            }
        }
        
        // clear the insert dataset
        myInsertDataset = null;
        myInsertRecordNumber = 0;
        
        // clear the update dataset
        myUpdateDataset = null;
        
        // set the new dataset
        myDataset = tempDataset;
        
        // store the dataset
        myShapeFile.setRecords(tempDataset.getShapeFileRecords());
        myShapeFile.writeRecords();
        fireCommit();
    }
    
    
    /**
     * Deletes the data from the datasource.
     */
    public void doDelete(Record inRecord) throws Exception {
        if (myDeleteDataset == null){
            myDeleteDataset = new ShapeFileDataset();
        }
        myDeleteDataset.add(inRecord);
        fireDelete(inRecord);
    }
    
    /**
     * Returns the bounding rectangle of all the shapes in the shape file.
     */
    public Envelope readEnvelope() {
        try{
            if (myEnvelope == null) {
                // read all the data in the shape file
                readShapes(null);
            }
            return myEnvelope;
        }
        catch (Exception e){
            System.out.println("Error Retrieving Envelope from Datasource "+myFileName + " e= "+e);
            return null;
        }
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/1/2001 3:32:59 PM)
     * @return java.lang.String
     */
    public java.lang.String getName() {
        return myName;
    }
    
    /** Keep track of the number and index of the inserted records. */
    private int myInsertRecordNumber = -1;
    
    /**
     * Insert the data source.
     */
    public void doInsert(Record inRecord)throws Exception{
        
        // check that this is a valid record
        ShapeFileRecord r = null;
        if (inRecord instanceof ShapeFileRecord){
            r = (ShapeFileRecord) inRecord;
        }
        else{
            r = new ShapeFileRecord();
            r.setAttributeNames(inRecord.getAttributeNames());
            r.setAttributes(inRecord.getAttributes());
            r.setShape(inRecord.getShape());
        }

        // ensure that the inserted record has a unique id.
        myInsertRecordNumber = myInsertRecordNumber -1;
        r.setIndex(myInsertRecordNumber);
        
        /** Insert the record */
        if (myInsertDataset == null) myInsertDataset = new ShapeFileDataset(myDataset.getAttributeNames());
        myInsertDataset.add(r);
        fireInsert(r);
    }
    
    /**
     * Performs some intelligent caching so it does not need to read the shape file on every
     * resize.
     */
    private GISDataset queryFromDataset(Envelope inEnvelope) {
        // retrieve the attribute names from the current dataset
        ShapeFileDataset tempDataset = new ShapeFileDataset(myDataset.getAttributeNames());
        
        /**
         * loop through the dataset retrieving the required shapes.
         */
        int tempRejectCount = 0;
        for (int i = 0; i < myDataset.size(); i++) {
            
            // determine if the shape belongs in the dataset
            Shape tempShape = myDataset.getShape(i);
            if (inEnvelope != null){
                if (tempShape != null){
                    if (tempShape instanceof Point){
                        Point tempPoint = (Point) tempShape;
                        if (inEnvelope.contains(tempPoint.getX(), tempPoint.getY())){
                            tempDataset.add(myDataset.getRecord(i));
                        }
                    }
                    else if (inEnvelope.overlaps(tempShape.getEnvelope())) {
                        tempDataset.add(myDataset.getRecord(i));
                    }
                    else {
                        tempRejectCount++;
                    }
                }
            }
            else {
                tempDataset.add(myDataset.getRecord(i));
            }
        }
        
        // search through the inserted dataset and insert any records.
        if (myInsertDataset != null){
            for (int i=0; i<myInsertDataset.size(); i++){
                Shape tempShape = myInsertDataset.getShape(i);
                if (tempShape != null){
                    if (inEnvelope.overlaps(tempShape.getEnvelope())) {
                        tempDataset.add(myInsertDataset.getRecord(i));
                    }
                    else {
                        tempRejectCount++;
                    }
                }
            }
                
        }

        // search through the updated dataset and update any records that match in the current dataset
        if (myUpdateDataset != null){
            for (int i=0; i<myUpdateDataset.size(); i++){
                ShapeFileRecord ru = (ShapeFileRecord) myUpdateDataset.getRecord(i);
                for (int j=0; j<tempDataset.size(); j++){
                    ShapeFileRecord rt = (ShapeFileRecord) tempDataset.getRecord(j);
                    if (ru.getIndex() == rt.getIndex()){
                        ru.getShape().calculateEnvelope();
                        tempDataset.setRecord(ru, j);
                    }
                }
            }
        }
        
        // search through the delete dataset and remove any records that match in the current dataset.
        if (myDeleteDataset != null){
            for (int i=0; i<myDeleteDataset.size(); i++){
                ShapeFileRecord rd = (ShapeFileRecord) myDeleteDataset.getRecord(i);
                for (int j=0; j<tempDataset.size(); j++){
                    ShapeFileRecord rt = (ShapeFileRecord) tempDataset.getRecord(j);
                    if (rd.getIndex() == rt.getIndex()){
                        tempDataset.remove(rt);
                    }
                }
            }
        }
        
        // return the new dataset
        return tempDataset;
    }
    
    /**
     * Reads the contents of the data file.
     */
    private ShapeFileDataset queryFromFile() throws Exception{
        if (myShapeFile == null){
            if (myFileName.toUpperCase().endsWith(".shp")) myShapeFile = new ShapeFile(myFileName);
            else myShapeFile = new ShapeFile(myFileName+".shp");
        }
        myShapeFile.readRecords();
        
        // construct the dataset
        ShapeFileRecord[] tempRecords = myShapeFile.getRecords();
        ShapeFileDataset tempDataset = new ShapeFileDataset();
        for (int i=0; i<tempRecords.length; i++){
            tempDataset.add(tempRecords[i]);
            fireRead(tempRecords[i]);
        }
        myEnvelope = tempDataset.getEnvelope();
        myDataset = tempDataset;
        return tempDataset;
    }
    
    
    /**
     * Set the name of the DataSource.
     */
    public void setName(java.lang.String newName) {
        myName = newName;
    }
    
    /**
     * Update the data source with the changed record.
     */
    public synchronized void doUpdate(Record inRecord) throws Exception{
        // check that this is a valid record
        if (inRecord instanceof ShapeFileRecord){
            ShapeFileRecord r = (ShapeFileRecord) inRecord;
            Shape s = (Shape) inRecord.getShape();
            if (s != null) s.calculateEnvelope();
             boolean found = false;
            
            // if this is a correct index, then look for it in the update vector.
            if (r.getIndex() >=0){
                                
                // Look for the record in the updatedataset, if it is there then update it.
                if (myUpdateDataset == null) myUpdateDataset = new ShapeFileDataset(inRecord.getAttributeNames());
                for (int i=0; i<myUpdateDataset.size(); i++){
                    ShapeFileRecord tempRecord = (ShapeFileRecord) myUpdateDataset.getRecord(i);
                    if (tempRecord.getIndex() == r.getIndex()){
                        
                        // if it is found in the update vector, then update it.
                        myUpdateDataset.setRecord(r, i);
                        found = true;
                        break;
                    }
                }
                if (found == false){
                    
                    // since the record is not in the update dataset, then update it.
                    myUpdateDataset.add(r);
                    found = true;
                }
            }
            // if the index is less than zero, then look for it in the insert vector.
            else if (r.getIndex() < 0){
                if (myInsertDataset != null){
                    for (int i=0; i<myInsertDataset.size(); i++){
                        ShapeFileRecord tempRecord = (ShapeFileRecord) myInsertDataset.getRecord(i);
                        if (tempRecord.getIndex() == r.getIndex()){
                            myInsertDataset.setRecord(r, i);
                            found = true;
                            break;
                        }
                    }
                }
            }
            
            if (!found) throw new Exception("Shape file update Failed, new or invalid index for record, may need to be inserted");
        }
        else {
            throw new Exception("Shape file update failed, invalid record Type, must be a ShapeFileRecord");
        }
    }
        
    /**
     * Rollback any changes to this datasource since the last commit.
     */
    public void rollback() throws Exception {
        myInsertDataset = null;
        myUpdateDataset = null;
        myDeleteDataset = null;
        fireRollBack();
        clearCache();
    }
    
    /**
     * Returns true
     */
    public boolean isUpdateable(){
        return true;
    }
    
    /**
     * set the properties of this datasource.
     */
    public void load(Properties inProperties) {
    }
    
    private static final String FILE_NAME = "FileName";
    
    /** Get the configuration information for this data source  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("ShapeFileDatasource");
        tempRoot.addAttribute(FILE_NAME, myFileName);
        return tempRoot;
    }
    
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) throw new Exception("No Configuration information for ShapeFileDataSource");
        super.setNode(inNode);
        String tempString = inNode.getAttribute(FILE_NAME);
        if (tempString == null) throw new Exception("No File specified for ShapeFileDataSource");
        myFileName = tempString;
        setFileName(tempString);
    }
    
    /** This method should return the shapes from the data source  */
    protected GISDataset readShapes(Envelope inEnvelope) throws Exception {
        if (myDataset == null) queryFromFile();
        
        if (inEnvelope == null) {
            return myDataset;
        }
        else{
            return queryFromDataset(inEnvelope);
        }
    }   
}