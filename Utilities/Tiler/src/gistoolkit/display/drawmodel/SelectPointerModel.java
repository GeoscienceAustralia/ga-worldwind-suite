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

import java.awt.event.MouseEvent;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Handles the selection of a single object from the currently selected layer.
 */
public class SelectPointerModel extends SelectDrawModel {
    
    /** Creates new SelectDrawModel */
    public SelectPointerModel() {
    }
    /** Creates new SelectDrawModel */
    public SelectPointerModel(Command inCommand) {
        setCommand(inCommand);
    }
    
    /**
     * The last selected object.  It is used to optimize the screen redraw.
     */
    private Record myRecord = null;
    
    /**
     * boolean to determine if the selection follows the mouse, or if it is stuck with the currently selected feature.
     */
    private boolean myStuck = false;
    
    /**
     * Last layer selected.  Ensures that the last layer selected is the current one.
     */
    private Layer myLastSelectedLayer = null;
    
    /**
     * When the mouse moves around the display with no buttons down, then the item under the pointer will be selected.
     * (with no buttons down).
     */
    public void mouseMoved(MouseEvent e) {
        if (getSelectedLayer() == null) return;
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
            
            // if there is a record selected
            boolean tempWasSelected = true;
            if (myRecord == null) tempWasSelected = false;
            else tempWasSelected = true;
            
            try {
                Layer tempCurrentLayer = getSelectedLayer();
                // check the last object selected.  Most of the time this is it.
                if (myLastSelectedLayer != null){
                    if (myRecord != null){
                        if (myRecord.getShape() != null){
                            if (myLastSelectedLayer == tempCurrentLayer){
                                if (myRecord.getShape().getDistanceToPoint(tempPoint.getX(), tempPoint.getY()) < tempSignificantDistance){
                                    return;
                                }
                                if (myRecord.getShape().contains(tempPoint)){
                                    return;
                                }
                            }
                            setSelectedRecords(null);
                            myRecord = null;
                        }
                    }
                }
                myLastSelectedLayer = tempCurrentLayer;                
                // retrieve the record which contains this point, only looks in the currently selected layer.
                Record tempRecord = getNear(tempCurrentLayer, tempPoint, tempSignificantDistance);
                if (tempRecord != null){
                                        
                    // save the record for the next loop
                    Record[] tempRecords = {tempRecord};
                    setSelectedRecords(tempRecords);
                    myRecord = tempRecord;
                    draw();
                }
            }
            catch (Exception ex) {
                System.out.println("" + ex);
                ex.printStackTrace();
            }
            
            // if there was a record selected, and now there isn't, then repaint the screen.
            if (tempWasSelected){
                if (myRecord == null){
                    draw();
                }
            }
            else{
                if (myRecord != null){
                    draw();
                }
            }
        }
        else {
            // the display is stuck, so draw the selected record.
            if (myRecord != null){
                draw();
            }
        }
    }
    
    /**
     * Called when the user releases the mouse butotn.
     * Causes the display to keep the currently selected shape selected.
     */
    public void mouseReleased(MouseEvent e){
        if (myRecord != null){
            if (myCommand != null){
                myCommand.executeDraw(this);
            }
            myStuck = true;
        }
    }
    
    /** Reset the display */
    public void reset(){
        myLastSelectedLayer = null;
        myRecord = null;
        myStuck = false;
        setSelectedRecords(null);
        draw();
    }
}
