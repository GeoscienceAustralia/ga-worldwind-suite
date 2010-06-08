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

package gistoolkit.display.drawmodel;

import java.util.Vector;
import java.awt.event.*;
import gistoolkit.display.*;
import gistoolkit.features.*;
import gistoolkit.datasources.*;
/**
 * A model used for highlighting features on the map as the pointer is moved over it.
 */
public class HighlightDrawModel	extends SimpleDrawModel {
    
    /**
     * The layer to search for objects.
     */
    private Layer myLayer = null;
    
    /**
     * The last selected object.  It is used to optimize the screen redraw.
     */
    private Record myRecord = null;
    
    /**
     * The layer for the last selected object.
     */
    private Layer myLastSelectedLayer = null;
    
    
    /**
     * Integer to keep track of which layer the current record came from
     */
    private int myLayerIndex = 0;
    
    
    /**
     * Objects who have registered interest with this model.
     */
    private Vector myListeners = new Vector();
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public HighlightDrawModel() {
        super();
    }
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public HighlightDrawModel(Command inCommand) {
        super();
        myCommand = inCommand;
    }
    
    /**
     * Adds the Listeners interest to events from this HighlightDrawModel.
     */
    public void add(HighlightDrawModelListener inListener){
        myListeners.addElement(inListener);
    }
    
    /**
     * The method called to draw the images on the map.
     */
    public void draw() {
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;

        if (myStuck && (myRecord != null)){
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
            myLastSelectedLayer.drawHighlight(myRecord, myGISDisplay.getBufferImage().getGraphics(), myGISDisplay.getConverter());
            myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
        }
        else{
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
            myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
        }
    }
    
    /**
     * Notifies the listener that a record was Deselected.
     */
    private void fireRecordDeselected(Record inRecord){
        for (int i=0; i<myListeners.size(); i++){
            HighlightDrawModelListener tempListener = (HighlightDrawModelListener) myListeners.elementAt(i);
            tempListener.recordDeselected(inRecord);
        }
    }
    
    /**
     * Notifies the listener that a record was selected.
     */
    private void fireRecordSelected(Record inRecord){
        for (int i=0; i<myListeners.size(); i++){
            HighlightDrawModelListener tempListener = (HighlightDrawModelListener) myListeners.elementAt(i);
            tempListener.recordSelected(inRecord);
        }
    }
        /**
         * Return the last layer an object was selected from.
         */
    public Layer getLastSelectedLayer(){
        return myLastSelectedLayer;
    }
    
    /**
     * Returns the last selected record.  If there is not record selected, then it returns null.
     */
    public Record getRecord(){
        return myRecord;
    }
    
    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     */
    public void mouseMoved(MouseEvent e) {
        if (!myStuck){
            
            // determine where this event has occured.
            Converter c = myGISDisplay.getConverter();
            if (c == null) return;
            gistoolkit.features.Point tempPoint = new gistoolkit.features.Point(c.toWorldX(e.getX()), c.toWorldY(e.getY()));

            // significantly close is within 4 pixels
            Envelope tempEnvelope = myGISDisplay.getEnvelope();
            int tempDisplayWidth = myGISDisplay.getWidth();
            double tempWidth = tempEnvelope.getWidth();
            double tempSignificantDistance = (tempWidth/tempDisplayWidth)*4;
            
            // find a feature close to this point
            Layer[] tempLayers = myGISDisplay.getLayers();
            if (tempLayers == null) return;
            if (tempLayers.length == 0) return;
           
            // if there is a record selected
            boolean tempWasSelected = true;
            if (myRecord == null) tempWasSelected = false;
            else tempWasSelected = true;
            
            // this highlight draw model uses all the layers in the display
            for (int i = tempLayers.length-1; i >= 0; i--) {
                try {
                    
                    // check the last object selected.  Most of the time this is it.
                    if (i<=myLayerIndex){
                        if (myRecord != null){
                            if (myRecord.getShape() != null){
                                if (myRecord.getShape().getDistanceToPoint(tempPoint.getX(), tempPoint.getY()) < tempSignificantDistance){
                                    return;
                                }
                                if (myRecord.getShape().contains(tempPoint)){
                                    return;
                                }
                                fireRecordDeselected(myRecord);
                                myRecord = null;
                            }
                        }
                    }
                    
                    // retrieve the record which contains this point, uses the first one, does not look for underlying layers.
                    Record tempRecord = getNear(tempLayers[i], tempPoint, tempSignificantDistance);
                    if (tempRecord != null){
                        
                        // if this shape is the correct one.
                        if ((myRecord == null) || (myRecord.getShape() != tempRecord.getShape())){
                            myLastSelectedLayer = tempLayers[i];
                            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
                            tempLayers[i].drawHighlight(tempRecord, myGISDisplay.getBufferImage().getGraphics(), c);
                            myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
                            myLayerIndex = i;
                        }
                        
                        // fire the notification events.
                        if (myRecord != null){
                            if (myRecord.getShape() != tempRecord.getShape()){
                                fireRecordDeselected(myRecord);
                                myLayer = tempLayers[i];
                                fireRecordSelected(tempRecord);
                            }
                        }
                        else {
                            fireRecordSelected(tempRecord);
                        }
                        
                        // save the record for the next loop
                        myRecord = tempRecord;
                        myLayer = tempLayers[i];
                        return;
                    }
                }
                catch (Exception ex) {
                    System.out.println("HighlightDrawModel-MouseMoved " + ex);
                    ex.printStackTrace();
                }
            }
            
            // if there was a record selected, and now there isn't, then repaint the screen.
            if (tempWasSelected){
                if (myRecord == null){
                    draw();
                }
            }
        }
        else {
            // the display is stuck, so draw the selected record.
            if (myRecord != null){
                myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
                myLastSelectedLayer.drawHighlight(myRecord, myGISDisplay.getBufferImage().getGraphics(), myGISDisplay.getConverter());
                myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
            }
        }
    }
    
    /** Loops through the layer to find an object near this point */
    private Record getNear(Layer inLayer, gistoolkit.features.Point inPoint, double inDistance){
        if (inLayer == null) return null;
        if (inPoint == null) return null;
        GISDataset tempDataset = inLayer.getDataset();
        if (tempDataset == null) return null;
                
        // loop through the dataset finding a shape significantly close to this point
        gistoolkit.features.Shape tempShape;
        for (int i=0; i<tempDataset.size(); i++){
            tempShape = tempDataset.getShape(i);
            if (tempShape != null){
                if ((tempShape instanceof gistoolkit.features.Polygon) || (tempShape instanceof gistoolkit.features.MultiPolygon)){
                    if (tempShape.contains(inPoint)){
                        return tempDataset.getRecord(i);
                    }
                }
                // if the distance to the extents is less than the significant distance then check the distance to the point.
                if (tempShape.getDistanceToEnvelope(inPoint.getX(), inPoint.getY()) < inDistance){
                    if (tempShape.getDistanceToPoint(inPoint.getX(), inPoint.getY()) < inDistance){
                        return tempDataset.getRecord(i);
                    }
                }
            }
        }
        return null;
    }
        
    /**
     * Called when the user releases the mouse butotn.
     */
    public void mouseReleased(MouseEvent e){
        if (myRecord != null){
            if (myCommand != null){
                myCommand.executeDraw(this);
            }
            myStuck = true;
        }
    }
    
    /**
     * Removes this listener from the list of listeners interested in events from this HighlightDrawModel.
     */
    public void remove(HighlightDrawModelListener inListener){
        myListeners.removeElement(inListener);
    }
    
    /**
     * boolean to determine if this select is stuck.
     */
    private boolean myStuck = false;
    
    /**
     * Return the layer associated with the record that was just selected.
     */
    public Layer getLayer(){
        return myLayer;
    }
    
    /** Reset the position of the display */
    public void reset(){
        myStuck = false;
    }
    
}