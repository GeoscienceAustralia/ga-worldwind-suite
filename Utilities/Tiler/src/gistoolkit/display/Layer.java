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

package gistoolkit.display;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.projection.*;
import gistoolkit.datasources.*;
import gistoolkit.datasources.filter.Filter;
/**
 * Represents a layer of the rendered map.
 */
public class Layer{
    /** Keeps Track of if the Layer is dirty or not */
    private boolean myIsDirty = false;
    /** Returns true if the layer has been updated and not committed or rolled back. */
    public boolean isDirty(){return myIsDirty;}
    
    /**
     * Internal class to handle events from the datasource.
     */
    private class DataSourceEventHandler implements DataSourceListener{
        private boolean inRead = false;
        /**
         * Called when the datasource reads a record.
         */
        public void recordRead(DataSourceEvent inEvent) {
            fireReadOccured();
        }
        
        /**
         * Called when the datasource inserts a record.
         */
        public void recordInserted(DataSourceEvent inEvent) {
        }
        
        /**
         * Called when the datasource updates a record.
         */
        public void recordUpdated(DataSourceEvent inEvent) {
        }
        
        /**
         * Called when the datasource deletes a record.
         */
        public void recordDeleted(DataSourceEvent inEvent) {
        }
        
        /**
         * Called when the datasource commits the data.
         */
        public void transactionCommitted(DataSourceEvent inEvent) {
        }
        
        /**
         * Called when the datasource rollsback the data.
         */
        public void transactionRolledBack(DataSourceEvent inEvent) {
        }
        
        public void fireReadOccured(){
            if (!inRead){
                if (myVectListeners.size() > 0){
                    LayerEvent tempEvent = new LayerEvent(LayerEvent.READ_OCCURED);
                    for (int i=0; i<myVectListeners.size(); i++){
                        ((LayerListener) myVectListeners.elementAt(i)).recordRead(tempEvent);
                    }
                }
            }            
        }
        public void fireReadBegin(){
            inRead = true;
            if (myVectListeners.size() > 0){
                LayerEvent tempEvent = new LayerEvent(LayerEvent.READ_BEGIN);
                for (int i=0; i<myVectListeners.size(); i++){
                    ((LayerListener) myVectListeners.elementAt(i)).recordRead(tempEvent);
                }
            }
        }
        public void fireReadComplete(){
            inRead = false;
            if (myVectListeners.size() > 0){
                LayerEvent tempEvent = new LayerEvent(LayerEvent.READ_COMPLETE);
                for (int i=0; i<myVectListeners.size(); i++){
                    ((LayerListener) myVectListeners.elementAt(i)).recordRead(tempEvent);
                }
            }
        }
    }
    private DataSourceEventHandler myDataSourceEventHandler = new DataSourceEventHandler();
    
    // Shader to be used to give this layer some color and style.
    private Style myStyle = new Style();
    
    // Name of this layer.
    private String myLayerName = "None";
    
    // Most Recently queried dataset
    private GISDataset myDataset = null;
    
    // Most Recently queried Envelope
    private Envelope myEnvelope = null;
    
    // Determines if this layer is updatable or not.
    private boolean myUpdateable = false;
    
    /**
     * Supports the listener model on the layer.
     */
    private Vector myVectListeners = new Vector();
    /**
     * Add a Layer Listener to the vector of listeners.
     */
    public void addLayerListener(LayerListener inListener){
        if (!myVectListeners.contains(inListener)){
            myVectListeners.add(inListener);
        }
    }
    /**
     * removes a Layer Listener from the vector of Lisetners.
     */
    public void removeLayerListener(LayerListener inListener){
        myVectListeners.removeElement(inListener);
    }
    
    /**
     * Creates a new layer without a datasource.  This layer will be invalid until it receives or generates
     * a datasource.  This can be done with the load() method.
     */
    public Layer(){
    }
        
    /** Create the layer with a new datasource.*/
    public Layer(DataSource inDataSource) {
        myDataSource = inDataSource;
        myUpdateable = inDataSource.isUpdateable();
        inDataSource.addDataSourceListener(myDataSourceEventHandler);
    }
    
    // Location where this layer gets its data.
    private DataSource myDataSource;
    /** Retrieve the data source from the layer */
    public DataSource getDataSource(){return myDataSource;}
    /** Set the data source for the layer */
    public void setDataSource(DataSource inDataSource){
        myDataSource = inDataSource;
    }
    
    /** if set to false, the layer will not draw.*/
    private boolean myVisible = true;
    /** returns true if the layer is currently visible.*/
    public boolean isVisible(){return myVisible;}
    /** Sets the value of the layer is visible flag.  If set to false, the layer will not draw.*/
    public void setVisible(boolean inVisible){myVisible = inVisible;}
    
    // maximum and minimum labeling distances.
    /* Determines the maximum distance at which this layer should be drawn. If this value is 0, then the layer is always drawn.*/
    private double myMaxDistance = 0;
    /**
     * Sets the maximum distance at which this layer is drawn.  If the
     * Envelope of the map are between the maximum and minimum distances, then this
     * layer will be drawn. Otherwise it will not be drawn.  If this value is set to 0,
     * then the layer is always drawn.
     */
    public void setMaxDistance(double inMaxDistance){
        myMaxDistance = inMaxDistance;
    }
    /**
     * Gets the maximum distance this at which this layer is drawn.  If the
     * Envelope of the map are between the maximum and minimum distances, then this
     * layer will be drawn. Otherwise it will not be drawn.  If this value is set to 0,
     * then the layer is always drawn.
     */
    public double getMaxDistance(){
        return myMaxDistance;
    }
    
    /** Determines the minimum distance at which this layer should be drawn.*/
    private double myMinDistance = 0;
    /**
     * Sets the minimum distance at which this layer is drawn.  If the
     * Envelope of the map are between the maximum and minimum distances, then this
     * layer will be drawn. Otherwise it will not be drawn.
     */
    public void setMinDistance(double inMinDistance){
        myMinDistance = inMinDistance;
    }
    /**
     * Gets the minimum distance at which this layer is drawn.  If the
     * Envelope of the map are between the maximum and minimum distances, then this
     * layer will be drawn. Otherwise it will not be drawn.
     */
    public double getMinDistance(){
        return myMinDistance;
    }
    /**
     * Checks to determine if this layer should be drawn, returns true
     * if the Envelope fall within the max and min, or if the distances are set to 0.
     */
    private boolean checkDraw(Envelope inEnvelope){
        if (!isVisible()) return false;
        if (inEnvelope == null) return false;
        
        if (myMaxDistance != 0){
            if (Math.abs(inEnvelope.getMaxX()-inEnvelope.getMinX()) < (Math.abs(myMinDistance))) return false;
            if (Math.abs(inEnvelope.getMinY()-inEnvelope.getMaxY()) > (Math.abs(myMaxDistance))) return false;
        }
        return true;
    }
    
    /**
     * The maximum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.  If this value is set to 0, then this layer is never labeled.
     */
    private double myMaxLabelDistance = 0;
    /**
     * Get the maximum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.  If this value is set to 0, then this layer is never labeled.
     */
    public double getMaxLabelDistance(){return myMaxLabelDistance;}
    /**
     * Set the maximum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.  If this value is set to 0, then this layer is never labeled.
     */
    public void setMaxLabelDistance(double inMaxLabelDistance){myMaxLabelDistance = inMaxLabelDistance;}
    /**
     * The minimum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.
     */
    private double myMinLabelDistance = 0;
    /**
     * Get the minimum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.
     */
    public double getMinLabelDistance(){return myMinLabelDistance;}
    /**
     * Set the minimum distance at which this layer should allow labeling.  If the Envelope of the
     * map are between the maximum and minimum distances, then this layer will be labeled.  Otherwise it will not
     * be labeled.
     */
    public void setMinLabelDistance(double inMinLabelDistance){myMinLabelDistance = inMinLabelDistance;}
    /**
     * Checks to determine if labels for this layer should be drawn, returns true
     * if the Envelope fall within the max and min, or fals if they don't, or max distance == 0.
     */
    private boolean checkLabel(Envelope inEnvelope){
        if (!isVisible()) return false;
        if (inEnvelope == null) return false;
        if (myMaxLabelDistance == 0){
            return false;
        }
        else{
            if (Math.abs(inEnvelope.getMaxX()-inEnvelope.getMinX()) < (Math.abs(myMinLabelDistance))) return false;
            if (Math.abs(inEnvelope.getMaxY()-inEnvelope.getMinY()) > (Math.abs(myMaxLabelDistance))) return false;
        }
        return true;
    }
    
    /**
     * Deletes the data in the data source.
     */
    public void delete(Record inRecord) throws Exception{
        if (!isUpdateable()) return;
        if (myDataSource != null){
            myDataSource.delete(inRecord);
            myIsDirty = true;
        }
    }
    
    /**
     * Draw the layer on the map with the given bounds
     */
    public void drawHighlight(Record inRecord, Graphics inGraphics, Converter inConverter){
        if (inConverter == null) return;
        if (inGraphics == null) return;
        
        // read the dataset for the bounds sent in
        try{
            myStyle.drawHighlight(inRecord, inGraphics, inConverter);
        }
        catch (Exception e){
            System.out.println(e);
        }
        
    }
    
    /**
     * Draw the layer on the map with the given bounds
     */
    public void drawLayer(Graphics g, Converter inConverter){
        // check for good data.
        if (inConverter == null) return;
        if (g == null) return;
        
        // if the world Envelope are too big or too small, then do not draw this layer.
        if (!checkDraw(inConverter.getWorldEnvelope())) return;
        
        int i=0;
        // read the dataset for the bounds sent in
        try{
            if (myDataSource instanceof RasterDatasource) {
                ((RasterDatasource) myDataSource).setImageWidth((int)inConverter.getScreenWidth());
                ((RasterDatasource) myDataSource).setImageHeight((int)inConverter.getScreenHeight());
            }
            myDataSourceEventHandler.fireReadBegin();
            myDataset = myDataSource.readDataset(inConverter.getWorldEnvelope());
            myDataSourceEventHandler.fireReadComplete();
            
            if (myDataset != null){
                myEnvelope = inConverter.getWorldEnvelope();
                
                String[] tempAttributeNames = myDataset.getAttributeNames();
                
                // draw the shape.
                myStyle.beginDraw();
                for (i=0; i<myDataset.size(); i++){
                    Record tempRecord = myDataset.getRecord(i);
                    if (myStyle != null){
                        myStyle.drawShape(tempRecord, g, inConverter);
                    }
                }
                for (i=0; i<myDataset.size(); i++){
                    if (myStyle.getTopShader() != null){
                        Record tempRecord = myDataset.getRecord(i);
                        if (myStyle != null){
                            myStyle.drawShape(tempRecord, g, inConverter, true);
                        }
                    }
                }
                myStyle.endDraw();
            }
        }
        catch (Exception e){
            System.out.println("Layer "+e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println("Layer "+t);
            t.printStackTrace();
        }
    }
    
    /**
     * Draw the label on the map with the given bounds
     */
    public void labelLayer(Graphics g, Converter inConverter){
        // check for good data.
        if (inConverter == null) return;
        if (g == null) return;
        
        // if the world Envelope are too big or too small, then do not draw this layer.
        if (!checkDraw(inConverter.getWorldEnvelope())) return;
        if (!checkLabel(inConverter.getWorldEnvelope())) return;
        
        int i=0;
        // read the dataset for the bounds sent in
        try{
            myDataSourceEventHandler.fireReadBegin();
            myDataset = myDataSource.readDataset(inConverter.getWorldEnvelope());
            myDataSourceEventHandler.fireReadComplete();
            
            if (myDataset != null){
                myEnvelope = inConverter.getWorldEnvelope();
                
                String[] tempAttributeNames = myDataset.getAttributeNames();
                
                myStyle.beginLabel();
                for (i=0; i<myDataset.size(); i++){
                    Record tempRecord = myDataset.getRecord(i);
                    if (myStyle != null){
                        myStyle.drawLabel(tempRecord, g, inConverter);
                    }
                }
                myStyle.endLabel();
            }
        }
        catch (Exception e){
            System.out.println("Layer "+e);
            e.printStackTrace();
        }
        catch (Throwable t){
            System.out.println("Layer "+t);
            t.printStackTrace();
        }
    }
    
    /**
     * Returns the bounding rectangle of the layer
     */
    public Envelope getEnvelope() throws Exception{
        if (myDataSource == null)
            return null;
        
        return myDataSource.getEnvelope();
    }
    
    /**
     * Returns the name of this layer.
     */
    public java.lang.String getLayerName() {
        return myLayerName;
    }
    
    /**
     * Returns the record of the shape containing this shape.
     * Returns a -1 if there are no points
     */
    public Record getObjectContaining(gistoolkit.features.Shape inShape) throws Exception{
        if (myDataset == null) return null;
        for (int i = 0; i < myDataset.size(); i++) {
            gistoolkit.features.Shape tempShape = myDataset.getShape(i);
            if (tempShape != null){
                if (tempShape.contains(inShape)) {
                    return myDataset.getRecord(i);
                }
            }
        }
        return null;
    }
    
    /**
     * Insert the method's description here.
     */
    public Style getStyle() {
        return myStyle;
    }
    
    /**
     * Inserts the data in the data source.
     */
    public void insert(Record inRecord) throws Exception{
        if (!isUpdateable()) return;
        if (inRecord.getShape() != null){
            inRecord.getShape().calculateEnvelope();
        }
        myDataSource.insert(inRecord);
        myIsDirty = true;
    }
    
    /**
     * Sets the name of this layer.
     */
    public void setLayerName(java.lang.String newLayerName) {
        myLayerName = newLayerName;
    }
    
    /**
     * Sets the style for this layer to use.
     */
    public void setStyle(Style newStyle) {
        myStyle = newStyle;
    }
    
    /**
     * Insert the method's description here.
     */
    public String toString() {
        return getLayerName();
    }
    
    /**
     * Updates the data in the data source.
     */
    public void update(Record inRecord) throws Exception{
        if (!isUpdateable()) return;
        if (inRecord.getShape() != null){
            inRecord.getShape().calculateEnvelope();
        }
        myDataSource.update(inRecord);
        myIsDirty = true;
    }
    
    /**
     * Commit any changes to this layer.
     */
    public void commit() throws Exception{
        if (!isUpdateable()) return;
        if (myDataSource != null){
            myDataSource.commit();
            myIsDirty = false;
        }
    }
    
    /**
     * Rollback any changes to this layer.
     */
    public void rollback() throws Exception{
        if (!isUpdateable()) return;
        if (myDataSource != null){
            myIsDirty = false;
            myDataSource.rollback();
        }
    }
    
    /**
     * Determines if this layer is currently updateable or not.
     */
    public boolean isUpdateable(){
        return myUpdateable;
    }
    
    /**
     * Sets the flag to indicate that this layer is not updateable.
     */
    public void setUpdateable(boolean inUpdateable){
        myUpdateable = inUpdateable;
    }
    
    /**
     * The projection to use when rendering this particular layer.  This layer
     * just hands this off to the data source to request the data in that projection.
     */
    private Projection myProjection = null;
    /** The projection to use when retrieving data. */
    private Projection myFromProjection = null;
    
    /**
     * Sets the projection to use when rendering this particular layer.  This layer
     * just hands this off to the data source to request the data in that projection.
     */
    public void setProjection(Projection inProjection, boolean inCacheProjected) throws Exception{
        myProjection = inProjection;
        if (myDataSource != null){
            myDataSource.setToProjection(myProjection, inCacheProjected);
        }
    }
    /**
     * Sets the projection to use when rendering this particular layer.  This layer
     * just hands this off to the data source to request the data in that projection.
     */
    public void setFromProjection(Projection inProjection) throws Exception{
        myFromProjection = inProjection;
        if (myDataSource != null){
            myDataSource.setFromProjection(inProjection);
        }
    }
    /** Retrieve the projection in use with this layer */
    public Projection getProjection(){return myProjection;}
    /** Retrieve the from projection in use with this layer */
    public Projection getFromProjection(){return myFromProjection;}
        
    /** Retrieve the array of attribute names */
    public String[] getAttributeNames(){
        if (myDataset != null) return myDataset.getAttributeNames();
        return new String[0];
    }
    /** Retrieve the array of attribute types */
    public AttributeType[] getAttributeTypes(){
        if (myDataset != null) return myDataset.getAttributeTypes();
        return new AttributeType[0];
    }
    
    /** Retrieve the legend from the style */
    public BufferedImage getLegend(){
        if (myStyle != null){
            if (myStyle.getShader() != null){
                return myStyle.getShader().getLegend();
            }
        }
        return null;
    }
    
    
    private static final String LAYER_NAME="LayerName";
    private static final String VISIBLE = "Visible";
    private static final String MAX_DISPLAY_DISTANCE = "MaxDisplayDistance";
    private static final String MIN_DISPLAY_DISTANCE = "MinDisplayDistance";
    private static final String MAX_LABEL_DISTANCE = "MaxLabelDistance";
    private static final String MIN_LABEL_DISTANCE = "MinLabelDistance";
    private static final String DATA_SOURCE_CLASS = "DataSourceClass";
    private static final String STYLE_CLASS = "StyleClass";
    private static final String NODE_DATASOURCE="DataSourceInformation";
    private static final String NODE_STYLE = "StyleInformation";
    
    /** Retrieve the configuration information for this layer. */
    public Node getNode(){
        Node tempRoot = new Node("Layer");
        tempRoot.addAttribute(LAYER_NAME, myLayerName);
        tempRoot.addAttribute(VISIBLE, ""+myVisible);
        
        // distances at which things are displayed.
        tempRoot.addAttribute(MAX_DISPLAY_DISTANCE, ""+myMaxDistance);
        tempRoot.addAttribute(MIN_DISPLAY_DISTANCE, ""+myMinDistance);
        tempRoot.addAttribute(MAX_LABEL_DISTANCE, ""+myMaxLabelDistance);
        tempRoot.addAttribute(MIN_LABEL_DISTANCE, ""+myMinLabelDistance);
        
        // Data Source
        Node tempDataSource = new Node(NODE_DATASOURCE);
        tempDataSource.addAttribute(DATA_SOURCE_CLASS, myDataSource.getClass().getName());
        tempDataSource.addChild(myDataSource.getNode());
        tempRoot.addChild(tempDataSource);
        
        // Style
        Node tempStyle = new Node(NODE_STYLE);
        tempRoot.addChild(tempStyle);
        tempStyle.addAttribute(STYLE_CLASS, myStyle.getClass().getName());
        tempStyle.addChild(myStyle.getNode());
        
        return tempRoot;
    }
    
    /** Set the configuraiton information for this layer. */
    public void setNode(Node inNode) throws Exception{
        if (inNode == null) throw new Exception ("Configuration information does not exist");
        // read the layer name
        if (inNode.getAttribute(LAYER_NAME) != null) myLayerName = inNode.getAttribute(LAYER_NAME);
        
        // Layer Visible
        if (inNode.getAttribute(VISIBLE) != null) {
            String tempString = inNode.getAttribute(VISIBLE);
            tempString = tempString.toUpperCase().trim();
            if (tempString.length() > 0){
                myVisible = tempString.startsWith("T");
            }
        }
        
        // the Display levels
        String tempName = MAX_DISPLAY_DISTANCE;
        String tempValue = inNode.getAttribute(tempName);
        try{
            myMaxDistance = Double.parseDouble(tempValue);
        }
        catch (NumberFormatException e){
            System.out.println("NumberFormatException parsing "+tempName+" Value = "+tempValue+" For Layer");
        }
        tempName = MIN_DISPLAY_DISTANCE;
        tempValue = inNode.getAttribute(tempName);
        try{
            myMinDistance = Double.parseDouble(tempValue);
        }
        catch (NumberFormatException e){
            System.out.println("NumberFormatException parsing "+tempName+" Value = "+tempValue+" For Layer");
        }
        tempName = MAX_LABEL_DISTANCE;
        tempValue = inNode.getAttribute(tempName);
        try{
            myMaxLabelDistance = Double.parseDouble(tempValue);
        }
        catch (NumberFormatException e){
            System.out.println("NumberFormatException parsing "+tempName+" Value = "+tempValue+" For Layer");
        }
        tempName = MIN_LABEL_DISTANCE;
        tempValue = inNode.getAttribute(tempName);
        try{
            myMinLabelDistance = Double.parseDouble(tempValue);
        }
        catch (NumberFormatException e){
            System.out.println("NumberFormatException parsing "+tempName+" Value = "+tempValue+" For Layer");
        }
        
        // read the Data Source.
        Node tempDataSourceNode = inNode.getChild(NODE_DATASOURCE);
        if (tempDataSourceNode == null) throw new Exception("DataSource Configuration information is null");
        String tempString = tempDataSourceNode.getAttribute(DATA_SOURCE_CLASS);
        if (tempString == null) throw new Exception("DataSource class name is null");
        DataSource tempDataSource = (DataSource) Class.forName(tempString).newInstance();
        Node[] tempNodes = tempDataSourceNode.getChildren();
        if (tempNodes.length > 0) tempDataSource.setNode(tempNodes[0]);
        
        // read the Style information
        Node tempStyleNode = inNode.getChild(NODE_STYLE);
        if (tempStyleNode == null) throw new Exception("Style configuration information is null");
        tempString = tempStyleNode.getAttribute(STYLE_CLASS);
        if (tempString == null) throw new Exception("Style class name is null");
        Style tempStyle = (Style) Class.forName(tempString).newInstance();
        tempNodes = tempStyleNode.getChildren();
        if (tempNodes.length > 0) tempStyle.setNode(tempNodes[0]);
        
        // set the data source
        setDataSource(tempDataSource);
        setUpdateable(tempDataSource.isUpdateable());

        // set the style
        setStyle(tempStyle);
    }
    
    /** Return the currently displayed dataset. */
    public GISDataset getDataset(){return myDataset;}
    /** Sets the filter to use with this dataset. */
    public void setFilter(Filter inFilter){myDataSource.setFilter(inFilter);}
    /** Gets the filter in use with this datasource. */
    public Filter getFilter(){ return myDataSource.getFilter();}
    
    /** Returns all the currently displayed records */
    public Record[] getRecords(){
        if (myDataset == null) return new Record[0];
        return myDataset.getRecords();
    }
}