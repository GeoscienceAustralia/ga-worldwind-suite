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

import java.awt.event.*;
import gistoolkit.display.*;
/**
 * A model to offset the map by a particular amount.
 */
public class PanDrawModel	extends SimpleDrawModel{
    
    /**
     * Point where the initial click of the mouse was located.
     */
    private java.awt.Point myStartPoint = null;
    
    /**
     * Point where the mouse was dragged to.
     */
    private java.awt.Point myEndPoint = null;
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public PanDrawModel() {
        super();
    }
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public PanDrawModel(Command inCommand) {
        super();
        myCommand = inCommand;
    }
    
    /**
     * The function which is called to draw the image on the map.
     */
    public void draw() {
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;

        // draw the map on the buffer
        if ((myStartPoint == null) || (myEndPoint == null)){
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        }
        else {
            int dx = myEndPoint.x - myStartPoint.x;
            int dy = myEndPoint.y - myStartPoint.y;
            myGISDisplay.getBufferImage().getGraphics().clearRect(0,0, myGISDisplay.getWidth(), myGISDisplay.getHeight());
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), dx, dy, myGISDisplay);
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
        
    }
    
    /**
     * Return the point to which the mouse was dragged.
     */
    public java.awt.Point getEndPoint(){
        return myEndPoint;
    }
    
    /**
     * Return the point where the mouse was clicket.
     */
    public java.awt.Point getStartPoint(){
        return myStartPoint;
    }
    
    /**
     * Called when the user releases the mouse button.
     */
    public void mouseDragged(MouseEvent e){
        myEndPoint = new java.awt.Point(e.getX(), e.getY());
        draw();
    }
    
    /**
     * Called when the user releases the mouse butotn.
     */
    public void mousePressed(MouseEvent e){
        
        myStartPoint = new java.awt.Point(e.getX(), e.getY());
        myEndPoint = myStartPoint;
    }
    
    /**
     * Called when the user releases the mouse butotn.
     */
    public void mouseReleased(MouseEvent e){
        if (myCommand != null){
            if ((myStartPoint != null) && (myEndPoint != null)){
                myCommand.executeDraw(this);
                myStartPoint = null;
                myEndPoint = null;
                draw();
            }
        }
    }
    
    /**
     * Sets the starting point for the Draw Model
     */
    public void setStartPoint(java.awt.Point inPoint){
        myStartPoint = inPoint;
    }
    
    /** Reset the position of the display */
    public void reset(){
        myStartPoint = null;
        myEndPoint = null;
        draw();
    }
}