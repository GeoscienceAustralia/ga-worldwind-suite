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
import gistoolkit.features.Record;
import gistoolkit.display.Layer;

/**
 * Super class for handling selections from the interface.
 */
public class SelectDrawModel extends SimpleDrawModel {
    
    /** Creates new SelectDrawModel */
    public SelectDrawModel() {
    }

    /**Objects who have registered interest with this model.*/
    private Vector myListeners = new Vector();
    
    /**Adds the Listeners interest to events from this HighlightDrawModel.*/
    public void add(SelectDrawModelListener inListener){
        myListeners.addElement(inListener);
    }
    /**Removes this listener from the list of listeners interested in events from this HighlightDrawModel.*/
    public void remove(SelectDrawModelListener inListener){
        myListeners.removeElement(inListener);
    }
    
    /**Notifies the listener that a record was Deselected.*/
    protected void fireRecordsDeselected(Record[] inRecords){
        for (int i=0; i<myListeners.size(); i++){
            SelectDrawModelListener tempListener = (SelectDrawModelListener) myListeners.elementAt(i);
            tempListener.recordsDeselected(inRecords);
        }
    }
    
    /**Notifies the listener that a record was selected.*/
    protected void fireRecordsSelected(Record[] inRecords){
        for (int i=0; i<myListeners.size(); i++){
            SelectDrawModelListener tempListener = (SelectDrawModelListener) myListeners.elementAt(i);
            tempListener.recordsSelected(inRecords);
        }
    }
    
    /** These are the selected records.*/
    public Record[] mySelectedRecords = new Record[0];
    /** Get the currently selected records. */
    public Record[] getSelectedRecords(){return mySelectedRecords;}
    /** Set the currently selected records, and do not notify the listeners of the event. */
    public synchronized void setSelectedRecordsNoNotify(Record[] inSelectedRecords){mySelectedRecords = inSelectedRecords;}
    /** Set the currently selected records. */
    public synchronized void setSelectedRecords(Record[] inSelectedRecords){
        if (mySelectedRecords == inSelectedRecords) return;
        if (mySelectedRecords != null) fireRecordsDeselected(mySelectedRecords);
        mySelectedRecords = inSelectedRecords;
        if (mySelectedRecords != null) fireRecordsSelected(mySelectedRecords);
    }
    
    /** The currently selected layer */
    private Layer mySelectedLayer = null;
    /** Set the currently selected layer */
    public void setSelectedLayer(Layer inLayer){mySelectedLayer = null;}
    /** Get the currently selected layer */
    public Layer getSelectedLayer() {
        if (mySelectedLayer == null) {
            if (getGISDisplay() != null) return getGISDisplay().getSelectedLayer();
        }
        else{
            return mySelectedLayer;
        }
        return null;
    }
    
    /** Draw the selected records */
    public synchronized void draw(){
        if (getGISDisplay() == null) return;
        
        if ((mySelectedRecords == null) || (mySelectedRecords.length == 0)) super.draw();
        else{
            if ((getGISDisplay().getBufferImage() != null) && (getGISDisplay().getMapImage() != null)){
                getGISDisplay().getBufferImage().getGraphics().drawImage(getGISDisplay().getMapImage(), 0, 0, getGISDisplay());
                Layer tempLayer = getGISDisplay().getSelectedLayer();
                if (tempLayer != null){
                    for (int i=0; i<mySelectedRecords.length; i++){
                        tempLayer.drawHighlight(mySelectedRecords[i], getGISDisplay().getBufferImage().getGraphics(), getGISDisplay().getConverter());
                    }
                }
                getGISDisplay().getGraphics().drawImage(getGISDisplay().getBufferImage(), 0, 0, getGISDisplay());
            }
        }
    }
    /** Loops through the layer to find an object near this point */
    protected Record getNear(Layer inLayer, gistoolkit.features.Point inPoint, double inDistance){
        if (inLayer == null) return null;
        if (inPoint == null) return null;
        Record[] tempRecords = inLayer.getRecords();
        if (tempRecords == null) return null;
        
        // loop through the dataset finding a shape significantly close to this point
        gistoolkit.features.Shape tempShape;
        for (int i=0; i<tempRecords.length; i++){
            tempShape = tempRecords[i].getShape();
            if (tempShape != null){
                if ((tempShape instanceof gistoolkit.features.Polygon) || (tempShape instanceof gistoolkit.features.MultiPolygon)){
                    if (tempShape.contains(inPoint)){
                        return tempRecords[i];
                    }
                }
                // if the distance to the extents is less than the significant distance then check the distance to the point.
                if (tempShape.getDistanceToEnvelope(inPoint.getX(), inPoint.getY()) < inDistance){
                    if (tempShape.getDistanceToPoint(inPoint.getX(), inPoint.getY()) < inDistance){
                        return tempRecords[i];
                    }
                }
            }
        }
        return null;
    }
    
    /** Reset the display */
    public void reset(){
        mySelectedRecords = null;
        draw();
    }
}
