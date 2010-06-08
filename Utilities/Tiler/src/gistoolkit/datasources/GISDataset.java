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
import gistoolkit.features.*;
import gistoolkit.features.featureutils.*;
import gistoolkit.display.Converter;
/**
 * Container for shapes and associated attribute data.
 * <p>
 * This class contains a collection of records. 
 * </p>
 */
public class GISDataset {
    /**
     * Collection of names for attribute data.
     */
    protected String[] myAttributeNames;

    /**
     * Collection of types for attribute data.
     */
    private AttributeType[] myAttributeTypes = null;
    
    /**
     * Vector of object arrays for the attributes.
     */
    protected Vector myVectRecords = new Vector();
    
    /**
     * Make a deep copy of this data set.  Could be a large amount of memory, so avoid if possible.
     */
    public Object clone(){
        GISDataset tempDataset = new GISDataset(myAttributeNames, myAttributeTypes);
        for (int i=0; i<myVectRecords.size(); i++){
            tempDataset.add((Record) ((Record) myVectRecords.elementAt(i)).clone());
        }
        return tempDataset;
    }
    
    /**
     * Bounds of the data in the geo dataset.
     */
    protected Envelope myEnvelope = null;
    
    /**
     * Layer constructor comment.
     */
    public GISDataset() {
        super();
    }
    
    /**
     * Create a new layer with the given number of attributes.
     */
    public GISDataset(String[] inAttributeNames){
        myAttributeNames = inAttributeNames;
        if (inAttributeNames != null){
            myAttributeTypes = new AttributeType[inAttributeNames.length];
            for (int i=0; i<inAttributeNames.length; i++){
                myAttributeTypes[i] = new AttributeType(AttributeType.STRING);
            }
        }
    }
    
    /**
     * Create a new Dataset with the given number and type of attributes.
     */
    public GISDataset(String[] inAttributeNames, AttributeType[] inAttributeTypes){
        myAttributeNames = inAttributeNames;
        myAttributeTypes = inAttributeTypes;
    }
    
    /**
     * Create a new Dataset with the given records.  They should all have the same names, types and number of attributes.
     */
    public GISDataset(Record[] inRecords){
        if (inRecords == null) return;
        for (int i=0; i<inRecords.length; i++){
            if (inRecords[i] != null){
                myAttributeNames = inRecords[i].getAttributeNames();
                myAttributeTypes = inRecords[i].getAttributeTypes();
                break;
            }
        }
        for (int i=0; i<inRecords.length; i++){
            if (inRecords[i] != null){
                myVectRecords.add(inRecords[i]);
            }
        }
    }
    
    /**
     * Add a data item to the list .
     */
    public void add(Object[] inAttributeValues, Shape inShape) {
        if (inAttributeValues == null)
            return;

        // remove the cache.
        myCacheRecords = null;
        
        // ensure that the list of attribute values is as long as the list of attribute names.
        Object[] tempArray;
        if (inAttributeValues.length < myAttributeNames.length) {
            
            // create a new array if the attribute values are not long enough.
            tempArray = new Object[myAttributeNames.length];
            for (int i = 0; i < myAttributeNames.length; i++) {
                if (i < inAttributeValues.length) {
                    tempArray[i] = inAttributeValues[i];
                }
                else {
                    tempArray[i] = null;
                }
            }
        }
        else {
            tempArray = inAttributeValues;
        }
        
        // create the record
        Record tempRecord = new Record();
        tempRecord.setAttributeNames(myAttributeNames);
        tempRecord.setAttributeTypes(myAttributeTypes);
        tempRecord.setAttributes(tempArray);
        tempRecord.setShape(inShape);
        
        // add the record to the list of records.
        myVectRecords.addElement(tempRecord);        
        myEnvelope = null;
    }
    
    /**
     * Add a data item to the list .
     */
    public void add(Record inRecord) {
        // remove the cache
        myCacheRecords = null;
        
        // add the record to the list of records.
        myVectRecords.addElement(inRecord);
        
        // ensure that the attribute names are populated.
        if (myAttributeNames == null){
            myAttributeNames = inRecord.getAttributeNames();
        }
        
        // ensure that the attribute types are populated.
        if (myAttributeTypes == null){
            myAttributeTypes = inRecord.getAttributeTypes();
        }
        myEnvelope = null;
    }
    
    /**
     * Return the number of attributes.
     */
    public int getAttributeCount(){
        return myAttributeNames.length;
    }
    
    /**
     * Return the attribute name at the given index.
     */
    public String getAttributeName(int inIndex){
        return myAttributeNames[inIndex];
    }
    
    /**
     * Return the attribute type at the given index.
     */
    public AttributeType getAttributeType(int inIndex){
        return myAttributeTypes[inIndex];
    }
    
    /**
     * Return the attribute names.
     */
    public String[] getAttributeNames(){
        return myAttributeNames;
    }
    
    /**
     * Return teh attribute types.
     */
    public AttributeType[] getAttributeTypes(){
        return myAttributeTypes;
    }
    
    /**
     * Return the attribute at the given offset.
     */
    public Object getAttributeValue(int inRow, int inCol){
        // retrieve the record
        Record tempRecord = (Record) myVectRecords.elementAt(inRow);
        return tempRecord.getAttributes()[inCol];
    }
    
    /**
     * Return the attributes at the given offset.
     */
    public Object[] getAttributeValues(int inRow){
        // retrieve the record
        Record tempRecord = (Record) myVectRecords.elementAt(inRow);
        return tempRecord.getAttributes();
    }
    
    /**
     * Return the Envelope of the data in the dataset.
     */
    public Envelope getEnvelope(){
        if (myEnvelope == null){
            if (myVectRecords.size() > 0){
                EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
                for (int i=0; i<myVectRecords.size(); i++){
                    Record tempRecord = (Record) myVectRecords.elementAt(i);
                    if (tempRecord != null){
                        Shape tempShape = tempRecord.getShape();
                        if (tempShape != null){
                            if (tempShape instanceof Point){
                                tempEnvelopeBuffer.expandToInclude((Point) tempShape);
                            }
                            else{
                                tempEnvelopeBuffer.expandToInclude(tempShape.getEnvelope());
                            }
                            
                        }
                    }
                }
                myEnvelope = tempEnvelopeBuffer.getEnvelope();
            }
        }
        return myEnvelope;
    }
    
    /**
     * Used to recalculate the envelope when the dataset is reprojected.
     */
    protected void resetEnvelope(){
        myEnvelope = null;
    }
    
    /**
     * @deprecated This method is being deprecated in vavor of size();
     */
     public int getNumShapes(){
        return myVectRecords.size();
    }
    /**
     *Return the number of recordsd in this layer.
     */
    public int getRecordCount(){
        return myVectRecords.size();
    }
    /**
     *Return the number of recordsd in this layer.
     */
    public int size(){
        return myVectRecords.size();
    }
    
    /**
     * Returns the record for the current selected dataset.
     */
    public Record getRecord(int inIndex){
        // retrieve the record
        Record tempRecord = (Record) myVectRecords.elementAt(inIndex);
        return tempRecord;
    }
    
    /** Cache so we don't have to create these all the time */
    private Record[] myCacheRecords = null;
    
    /**
     * Returns the records within this dataset
     */
    public Record[] getRecords(){
        if (myCacheRecords != null) return myCacheRecords;
        Record[] tempRecords = null;
        if (myVectRecords == null) tempRecords = new Record[0];
        else {
            tempRecords = new Record[myVectRecords.size()];
            myVectRecords.copyInto(tempRecords);
        }
        myCacheRecords = tempRecords;
        return myCacheRecords;
    }
    
    /**
     * Return the shape at the given index.
     */
    public Shape getShape(int index){
        // retrieve the record
        Record tempRecord = (Record) myVectRecords.elementAt(index);
        return tempRecord.getShape();
    }
    
    /** If the shape is contained within the data set, this returns the index of the first instance of that shape.  If the shape does not exist, then a -1 is returned.*/
    public int getIndex(Shape inShape){

        // I considered eliminating null, but this will return the first index of null in the set.
        for (int i=0; i<myVectRecords.size(); i++){
            Record tempRecord = (Record) myVectRecords.elementAt(i);
            if ( tempRecord.getShape() == inShape) return i;
        }
        return -1;
    }
/**
     * Return the snapped Point to this dataset
     */
    public Point getSnappedPoint(double worldX, double worldY, Converter tempConverter) {
        Record[] tmpRecords = getRecords();
        for(int i=0; i<tmpRecords.length; i++) {
            Shape myShape = tmpRecords[i].getShape();
            boolean snapPoints = false, snapVertexes = false, snapEdges = false;
            //snap vertexes for point shapes
            if((myShape.getShapeType().equals(Shape.POINT))||(myShape.getShapeType().equals(Shape.MULTIPOINT))) {
                snapPoints = true;
                snapVertexes = true;
                snapEdges = true;
            }
            //snap vertexes and edges for line shapes
            if((myShape.getShapeType().equals(Shape.LINESTRING))||(myShape.getShapeType().equals(Shape.MULTILINESTRING))
                                                                  ||(myShape.getShapeType().equals(Shape.LINEARRING))) {
                snapPoints = true;
                snapVertexes = true;
                snapEdges = true;
            }
            //snap vertexes and edges for line shapes
            if((myShape.getShapeType().equals(Shape.POLYGON))||(myShape.getShapeType().equals(Shape.MULTIPOLYGON))) {
                snapPoints = true;
                snapVertexes = true;
                snapEdges = true;
            }
            if(snapPoints) {
                Point[] tmpPoints = myShape.getPoints();
                double dist;
                dist = Shape.getDistance(worldX, worldY,tmpPoints[0].getX(),tmpPoints[0].getY());
                if(dist < tempConverter.getWorldWidth()/100) return tmpPoints[0];
                dist = Shape.getDistance(worldX, worldY,tmpPoints[tmpPoints.length-1].getX(),tmpPoints[tmpPoints.length-1].getY());
                if(dist < tempConverter.getWorldWidth()/100) return tmpPoints[tmpPoints.length-1];
            }
            if(snapVertexes) {
                Point[] tmpPoints = myShape.getPoints();
                for(int j=0;j<tmpPoints.length;j++) {
                    double dist = Shape.getDistance(worldX, worldY,tmpPoints[j].getX(),tmpPoints[j].getY());
                    if(dist < tempConverter.getWorldWidth()/100) return tmpPoints[j];
                }
            }
            if(snapEdges) {
                Point pP = new Point(worldX,worldY);
                Envelope pPExtents = new Envelope(pP.x,pP.y,pP.x,pP.y);
                if(myShape.getEnvelope().contains(pPExtents)) {
                    Point[] tmpPoints = myShape.getPoints();
                    double minDist = Double.MAX_VALUE;
                    Point minDistPoint = null;
                    for(int j=0;j<tmpPoints.length-1;j++) {
                        Point pA = tmpPoints[j];
                        Point pB = tmpPoints[j+1];
                        Envelope lineExtents = new LineString(new Point[] {pA,pB}).getEnvelope();
                        if(lineExtents.contains(pPExtents)) {
                            double a1 = (pB.y-pA.y)/(pB.x-pA.x);
                            double b1 = pB.y-(a1*pB.x);
                            double a2 = -1/a1;
                            double b2 = pP.y - (a2*pP.x);
                            double ptX = (b2-b1)/(a1-a2);
                            double ptY = (a1*ptX)  + b1;
                            Point onLinePt = new Point(ptX,ptY);
                            double dist = onLinePt.getDistanceToPoint(pP.x,pP.y);
/*
                            System.out.println(pA+"_"+pB+"_"+pP+"_"+onLinePt);
                            System.out.println(dist+" < "+tempConverter.getWorldWidth()/tempConverter.getScreenWidth()*20);
 */
                            if(dist < tempConverter.getWorldWidth()/tempConverter.getScreenWidth()*20) {
                                if(dist < minDist) {
                                    minDistPoint = onLinePt;
                                    minDist = dist;
                                }
                            }
                        }
                    }
                    if(minDistPoint != null) return minDistPoint;
                }
            }
        }
        return null;
    }
}