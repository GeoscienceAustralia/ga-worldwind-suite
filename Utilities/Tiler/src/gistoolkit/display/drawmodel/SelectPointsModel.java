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

import java.util.*;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Handles the selection of multiple objects from the currently selected layer.
 */
public class SelectPointsModel extends SelectDrawModel {
    
    /** Creates new SelectDrawModel */
    public SelectPointsModel() {
    }
    /** Creates new SelectDrawModel */
    public SelectPointsModel(Command inCommand) {
        setCommand(inCommand);
    }
    
    /** The currently selected Points */
    private Point[] myPoints = null;
    
    /**
     * If the user presses the mouse button, then add the point to the list of points.
     */
    public void mousePressed(MouseEvent e){
        // determine where this event has occured.
        if (!e.isPopupTrigger()){
            Converter c = myGISDisplay.getConverter();
            if (c == null) return;
            gistoolkit.features.Point tempPoint = new gistoolkit.features.Point(c.toWorldX(e.getX()), c.toWorldY(e.getY()));
            if (myPoints == null) {
                myPoints = new Point[1];
                myPoints[0] = tempPoint;
            }
            else{
                Point[] tempPoints = new Point[myPoints.length+1];
                for (int i=0; i<myPoints.length; i++){
                    tempPoints[i] = myPoints[i];
                }
                tempPoints[myPoints.length] = tempPoint;
                myPoints = tempPoints;
            }
            doSelect();
        }
    }
    
    /**
     * If the mouse is dragged, then move the last point.
     */
    public void mouseDragged(MouseEvent e) {
        if (myPoints == null) return;
        
        // determine where this event has occured.
        Converter c = myGISDisplay.getConverter();
        if (c == null) return;
        
        // set the last point to the current coordinages.
        Point tempLastPoint = myPoints[myPoints.length-1];
        tempLastPoint.setX(c.toWorldX(e.getX()));
        tempLastPoint.setY(c.toWorldY(e.getY()));
        
        // recheck the selected shapes
        doSelect();
        draw();
    }
    
    private void doSelect(){
        if (getSelectedLayer() == null) return;
        if (myPoints == null) return;
        if (myPoints.length == 0) return;
        
        // determine where this event has occured.
        Converter c = myGISDisplay.getConverter();
        try {
            Layer tempCurrentLayer = getSelectedLayer();
            
            // Trim the records to just those that are significantly close to this one.
            Record[] tempRecords = tempCurrentLayer.getRecords();
            Vector tempSelectedVect = new Vector();
            
            // significantly close is within 4 pixels
            Envelope tempEnvelope = myGISDisplay.getEnvelope();
            int tempDisplayWidth = myGISDisplay.getWidth();
            double tempWidth = tempEnvelope.getWidth();
            double tempSignificantDistance = (tempWidth/tempDisplayWidth)*4;
            
            // loop through the points checking each one.
            for (int i=0; i<myPoints.length; i++){
                Record tempRecord = getNear(tempCurrentLayer, myPoints[i], tempSignificantDistance);
                if (tempRecord != null){
                    tempSelectedVect.addElement(tempRecord);
                }
            }
            
            // convert the records to an array and use that.
            if (tempSelectedVect.size() > 0){
                Record[] tempSetRecords = new Record[tempSelectedVect.size()];
                tempSelectedVect.copyInto(tempSetRecords);
                setSelectedRecordsNoNotify(tempSetRecords);
            }
            else{
                setSelectedRecordsNoNotify(null);
            }
            draw();
        }
        catch (Exception ex) {
            System.out.println(" " + ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Called when the user releases the mouse butotn.
     * Causes the display to keep the currently selected shape selected.
     */
    public void mouseReleased(MouseEvent e){
        if (myCommand != null){
            myCommand.executeDraw(this);
        }
    }
    
    /**
     * Draws the box on the screen, calls the draw in the super class to draw the shapes.
     */
    public synchronized void draw(){
        if (getGISDisplay() == null) return;
        if ((getGISDisplay().getBufferImage() != null) && (getGISDisplay().getMapImage() != null)){
            
            // draw the map on the buffer image
            getGISDisplay().getBufferImage().getGraphics().drawImage(getGISDisplay().getMapImage(), 0, 0, getGISDisplay());
            
            Layer tempLayer = getGISDisplay().getSelectedLayer();
            if (tempLayer != null){
                
                // highlight the currently selected record on the display
                if (myRecord != null) tempLayer.drawHighlight(myRecord, getGISDisplay().getBufferImage().getGraphics(), getGISDisplay().getConverter());
                
                // if there are records, then draw then on buffer image
                if ((getSelectedRecords() == null) || (getSelectedRecords().length == 0)){
                }
                else{
                    // draw the selected records on the display.
                    for (int i=0; i<getSelectedRecords().length; i++){
                        tempLayer.drawHighlight(getSelectedRecords()[i], getGISDisplay().getBufferImage().getGraphics(), getGISDisplay().getConverter());
                    }
                }
            }
            
            // Draw the points on the buffer image.
            Graphics tempGraphics = getGISDisplay().getBufferImage().getGraphics();
            Converter c = getGISDisplay().getConverter();
            if ((c != null) && (myPoints != null)){
                for (int i=0; i<myPoints.length; i++){
                    
                    // need these in screen coordinates
                    int x1 = c.toScreenX(myPoints[i].getX());
                    int y1 = c.toScreenY(myPoints[i].getY());
                    
                    // Point shadows
                    tempGraphics.setColor(Color.black);
                    tempGraphics.fillOval(x1-3, y1-3, 4,4);
                    
                    // Points
                    tempGraphics.setColor(Color.red);
                    tempGraphics.fillOval(x1-2, y1-2, 4,4);
                }
            }
            
            // draw the buffer image on the display
            getGISDisplay().getGraphics().drawImage(getGISDisplay().getBufferImage(), 0, 0, getGISDisplay());
        }
    }
    
    /** Recurd that is currently selected. */
    private Record myRecord = null;
    
    /** The layer that was selected the last time we tried to find a record */
    private Layer myLastSelectedLayer = null;
    
    /** Handle the selection when the mouse is moved */
    public void mouseMoved(MouseEvent e){
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
        
        // The currently selected layer
        Layer tempCurrentLayer = getSelectedLayer();
        if (tempCurrentLayer == null) return;
        
        // check the last object selected.  Most of the time this is it.
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
                fireRecordDeselected(myRecord);
                myRecord = null;
            }
        }
        
        // retrieve the record which contains this point, uses the first one, does not look for underlying layers.
        myLastSelectedLayer = tempCurrentLayer;
        Record tempRecord = getNear(tempCurrentLayer, tempPoint, tempSignificantDistance);
        if (tempRecord != null){
            fireRecordSelected(tempRecord);
            
            // save the record for the next loop
            myRecord = tempRecord;
        }
        draw();
    }
    
    /** sends the notification if the record is selected. */
    public void fireRecordSelected(Record tempRecord){
        Record[] tempRecords = new Record[1];
        tempRecords[0] = tempRecord;
        fireRecordsSelected(tempRecords);
    }
    
    /** sends the notification if the record is Deselected. */
    public void fireRecordDeselected(Record tempRecord){
        Record[] tempRecords = new Record[1];
        tempRecords[0] = tempRecord;
        fireRecordsDeselected(tempRecords);
    }
    
    /** Called when a key is pressed */
    public void keyPressed(java.awt.event.KeyEvent inKeyEvent){
        if ((inKeyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (inKeyEvent.getKeyCode() == KeyEvent.VK_DELETE)){
            if (myPoints != null){
                if (myPoints.length > 0){
                    Point[] tempPoints = new Point[myPoints.length-1];
                    for (int i=0; i<tempPoints.length; i++){
                        tempPoints[i] = myPoints[i];
                    }
                    myPoints = tempPoints;
                    doSelect();
                    draw();
                }
            }
        }
    }
    
    /** Reset the display */
    public void reset(){
        myPoints = null;
        myRecord = null;
        myLastSelectedLayer = null;
        setSelectedRecords(null);
        draw();
    }
    
}