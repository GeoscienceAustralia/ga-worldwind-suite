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
 * Simple draw model, good for subclassing.
 */
public class SimpleDrawModel implements DrawModel, MouseListener, MouseMotionListener, KeyListener{

	/**
	 * Handle to the GISDisplay for modifying.
	 */
	protected GISDisplay myGISDisplay = null;		

	/**
	 * Handle to the Command to call
	 */
	protected Command myCommand = null;

	/**
	 * SimpleDrawModel constructor comment.
	 */
	public SimpleDrawModel() {
		super();
	}
	/**
	 * The function which is called to draw the image on the map.
	 */
	public void draw() {
            if (myGISDisplay == null) return;
            if (myGISDisplay.getBufferImage() == null) return;
            if (myGISDisplay.getMapImage() == null) return;
            
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
            myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
	}
 	/**
 	 * Called when the user clicks the mouse button.
 	 */
 	public void mouseClicked(MouseEvent e){
 	}       
	/**
	 * Invoked when the mouse button has been moved on a component
	 * (with no buttons no down).
	 */
	public void mouseDragged(MouseEvent e) {
	}
 	/**
 	 * Called when the user clicks the mouse button.
 	 */
 	public void mouseEntered(MouseEvent e){
 	}         
 	/**
 	 * Called when the user clicks the mouse button.
 	 */
 	public void mouseExited(MouseEvent e){
 	}        
	/**
	 * Invoked when the mouse button has been moved on a component
	 * (with no buttons no down).
	 */
	public void mouseMoved(MouseEvent e) {
	}
 	/**
 	 * Called when the user presses the mouse button.
 	 */
 	public void mousePressed(MouseEvent e){
 	}        
 	/**
 	 * Called when the user releases the mouse butotn.
 	 */
 	public void mouseReleased(MouseEvent e){
 	}            
	/**
	 * Set the command to call at the completion of this draw.
	 */
	public void setCommand(Command inCommand){
		myCommand = inCommand;
	}
	/**
	 * Sets the parent GISDisplay for this draw model.
	 */
	public void setGISDisplay(GISDisplay inGISDisplay){
		myGISDisplay = inGISDisplay;
	}
        /**
         * Retrieves the parent GISDisplay for this draw mdoel.
         */
        public GISDisplay getGISDisplay(){
            return myGISDisplay;
        }
	/**
	 * Function called to indicate that this draw model will be removed.
	 */
	public void remove(){
		if (myCommand != null) myCommand.removeDraw(this);
	}
        
        /** Called when the DrawModel should quit doing what it is doing and reset to the initial state  */
        public void reset() {
        }

        /** Called when a key on the keyboard is pressed */
        public void keyPressed(java.awt.event.KeyEvent keyEvent) {
        }
        /** Called when a key on the key board is released */
        public void keyReleased(java.awt.event.KeyEvent keyEvent) {
        }
        /** Called when a key on the key board is pressed and then released */
        public void keyTyped(java.awt.event.KeyEvent keyEvent) {
        }
        
}