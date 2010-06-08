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
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Handles the selection of multiple objects from the currently selected layer.
 */
public class SelectBoxModel extends SelectDrawModel {
    
    /** Creates new SelectDrawModel */
    public SelectBoxModel() {
    }
    /** Creates new SelectDrawModel */
    public SelectBoxModel(Command inCommand) {
        setCommand(inCommand);
    }
    
    /** The currently selected box */
    private Point myNorthWestPoint = null;
    private Point mySouthEastPoint = null;
    
    /**
     * If the user moves the mouse, then set the first point
     */
    public void mousePressed(MouseEvent e){
        // determine where this event has occured.
        Converter c = myGISDisplay.getConverter();
        if (c == null) return;
        gistoolkit.features.Point tempPoint = new gistoolkit.features.Point(c.toWorldX(e.getX()), c.toWorldY(e.getY()));
        
        myNorthWestPoint = tempPoint;
        mySouthEastPoint = (Point) tempPoint.clone();
    }
    
    /**
     * As the user draws a rectangle, the selected objects will be displayed.
     */
    public void mouseDragged(MouseEvent e) {
        if (getSelectedLayer() == null) return;
        
        // determine where this event has occured.
        Converter c = myGISDisplay.getConverter();
        if (c == null) return;
        gistoolkit.features.Point tempPoint = new gistoolkit.features.Point(c.toWorldX(e.getX()), c.toWorldY(e.getY()));
//        if (tempPoint.getX() < myNorthWestPoint.getX()) myNorthWestPoint.setX(tempPoint.getX());
//        else mySouthEastPoint.setX(tempPoint.getX());
//        if (tempPoint.getY() > myNorthWestPoint.getY()) myNorthWestPoint.setY(tempPoint.getY());
//        else mySouthEastPoint.setY(tempPoint.getY());
        mySouthEastPoint = tempPoint;
        
        try {
            Layer tempCurrentLayer = getSelectedLayer();
            
            // Trim the records to just those that intersect this layer.
            Record[] tempRecords = tempCurrentLayer.getRecords();
            Vector tempSelectedVect = new Vector();
            
            // construct the polygon
            Point[] tempPoints = new Point[5];
            tempPoints[0] = new Point(myNorthWestPoint.getX(), myNorthWestPoint.getY());
            tempPoints[1] = new Point(mySouthEastPoint.getX(), myNorthWestPoint.getY());
            tempPoints[2] = new Point(mySouthEastPoint.getX(), mySouthEastPoint.getY());
            tempPoints[3] = new Point(myNorthWestPoint.getX(), mySouthEastPoint.getY());
            tempPoints[4] = tempPoints[0];
            LinearRing tempLinearRing = new LinearRing(tempPoints);
            tempLinearRing.ensureClosed();
            if (!tempLinearRing.isClockwise()) tempLinearRing.reorder();
            Polygon tempBox = new Polygon(new LinearRing(tempPoints));
            
            for (int i=0; i<tempRecords.length; i++){
                Shape tempShape = tempRecords[i].getShape();
                if (tempShape != null){
                    if (tempBox.intersects(tempShape)){
                        tempSelectedVect.addElement(tempRecords[i]);
                    }
                }
            }
            if (tempSelectedVect.size() > 0){                
                Record[] tempSetRecords = new Record[tempSelectedVect.size()];
                tempSelectedVect.copyInto(tempSetRecords);
                setSelectedRecords(tempSetRecords);
            }
            else{
                setSelectedRecords(null);
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
            
            // if there are records, then draw then on buffer image
            if ((getSelectedRecords() == null) || (getSelectedRecords().length == 0)){
            }
            else{
                Layer tempLayer = getGISDisplay().getSelectedLayer();
                if (tempLayer != null){
                    for (int i=0; i<getSelectedRecords().length; i++){
                        tempLayer.drawHighlight(getSelectedRecords()[i], getGISDisplay().getBufferImage().getGraphics(), getGISDisplay().getConverter());
                    }
                }
            }
            
            // Draw the points and the box on the buffer image
            if ((myNorthWestPoint != null) && (mySouthEastPoint != null)){
                // convert the points to the screen representations
                Converter c = getGISDisplay().getConverter();
                if (c != null){
                    
                    // need these in screen coordinates
                    int x1 = c.toScreenX(myNorthWestPoint.getX());
                    int y1 = c.toScreenY(myNorthWestPoint.getY());
                    int x2 = c.toScreenX(mySouthEastPoint.getX());
                    int y2 = c.toScreenY(mySouthEastPoint.getY());
                    
                    // draw the lines
                    Graphics tempGraphics = getGISDisplay().getBufferImage().getGraphics();
                    // Line Shadows
                    tempGraphics.setColor(Color.black);
                    tempGraphics.drawLine(x1-1, y1-1,x2-1,y1-1);
                    tempGraphics.drawLine(x2-1, y1-1,x2-1,y2-1);
                    tempGraphics.drawLine(x1-1, y2-1,x2-1,y2-1);
                    tempGraphics.drawLine(x1-1, y1-1,x1-1,y2-1);
                    
                    // Lines
                    tempGraphics.setColor(Color.red);
                    tempGraphics.drawLine(x1, y1,x2,y1);
                    tempGraphics.drawLine(x2, y1,x2,y2);
                    tempGraphics.drawLine(x1, y2,x2,y2);
                    tempGraphics.drawLine(x1, y1,x1,y2);
                    
                    // Point shadows
                    tempGraphics.setColor(Color.black);
                    tempGraphics.fillOval(x1-3, y1-3, 4,4);
                    tempGraphics.fillOval(x2-3, y1-3, 4,4);
                    tempGraphics.fillOval(x2-3, y2-3, 4,4);
                    tempGraphics.fillOval(x1-3, y2-3, 4,4);
                    
                    // Points
                    tempGraphics.setColor(Color.red);
                    tempGraphics.fillOval(x1-2, y1-2, 4,4);
                    tempGraphics.fillOval(x2-2, y1-2, 4,4);
                    tempGraphics.fillOval(x2-2, y2-2, 4,4);
                    tempGraphics.fillOval(x1-2, y2-2, 4,4);
                }
            }
            
            // draw the buffer image on the display
            getGISDisplay().getGraphics().drawImage(getGISDisplay().getBufferImage(), 0, 0, getGISDisplay());
        }
    }
    /** Reset the display */
    public void reset(){
        myNorthWestPoint = null;
        mySouthEastPoint = null;
        setSelectedRecords(null);
        draw();
    }
}