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
import java.awt.Rectangle;
import java.awt.Color;
import gistoolkit.display.*;
import gistoolkit.features.*;
/**
 * A model used for highlighting features on the map as the pointer is moved over it.
 * Creation date: (4/18/2001 3:20:16 PM)
 */
public class BoxDrawModel extends SimpleDrawModel{
    
    /**
     * Box to draw
     */
    private Rectangle myRectangle = null;
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public BoxDrawModel() {
        super();
    }
    
    /**
     * HighlightDrawModel constructor comment.
     */
    public BoxDrawModel(Command inCommand) {
        super();
        myCommand = inCommand;
    }
    
    /**
     * The function which is called to draw the image on the map.
     */
    public void draw() {
        // draw the map on the buffer
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;
        if (myGISDisplay.getMapImage() == null) return;
        myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        
        // draw the box on the buffer
        if (myRectangle != null){
            
            // draw the rectangle on the buffer
            myGISDisplay.getBufferImage().getGraphics().setColor(Color.red);
            myGISDisplay.getBufferImage().getGraphics().drawRect(myRectangle.x, myRectangle.y, myRectangle.width, myRectangle.height);
            
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
        
    }
    
    /**
     * Retrieves the Envelope associated with the box.
     * Converts the rectangle drawn on the screen to a valid box.  If this is not a valid rectangle, then it will return null.
     */
    public Envelope getEnvelope(){
        
        // convert the coordinates
        Converter c = myGISDisplay.getConverter();
        if (c == null) return null;
        double tempTopX = c.toWorldX(myRectangle.x);
        double tempTopY = c.toWorldY(myRectangle.y);
        double tempBottomX = c.toWorldX(myRectangle.x+myRectangle.width);
        double tempBottomY = c.toWorldY(myRectangle.y+myRectangle.height);
        
        // return the new Envelope
        return new Envelope(tempTopX, tempTopY, tempBottomX, tempBottomY);
    }
    
    /**
     * Retrieves the rectangle drawn on the screen.
     */
    public Rectangle getRectangle(){
        return myRectangle;
    }
    
    /**
     * Called when the user releases the mouse button.
     */
    public void mouseDragged(MouseEvent e){
        if (myRectangle != null){
            
            // calculate the smallest X coordinate and width
            if (myRectangle.x > e.getX()){
                myRectangle.width = myRectangle.x-e.getX();
                myRectangle.x = e.getX();
            }
            else {
                myRectangle.width = e.getX()-myRectangle.x;
            }
            
            // similarly calculate the smallest Y coordinate and width
            if (myRectangle.y > e.getY()){
                myRectangle.height = myRectangle.y-e.getY();
                myRectangle.y = e.getY();
            }
            else {
                myRectangle.height = e.getY()-myRectangle.y;
            }
            draw();
        }
    }
    
    /**
     * Called when the user presses the mouse butotn.
     */
    public void mousePressed(MouseEvent e){
        myRectangle = new Rectangle(e.getX(), e.getY(), 0,0);
    }
    
    /**
     * Called when the user releases the mouse butotn.
     */
    public void mouseReleased(MouseEvent e){
        if (myRectangle != null){
            if (myCommand != null){
                myCommand.executeDraw(this);
            }
        }
    }
    
    /**
     * Set the rectangle to use.
     */
    public void setRectangle(Rectangle inRectangle){
        myRectangle = inRectangle;
    }
    
    /** Reset the position of the display */
    public void reset(){
        myRectangle = null;
        draw();
    }
}