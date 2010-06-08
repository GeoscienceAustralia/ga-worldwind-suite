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

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import gistoolkit.display.*;

/**
 * Handles the selection of a single object from the currently selected layer.
 */
public class SelectPointModel extends SelectDrawModel {
    
    /** Creates new SelectDrawModel */
    public SelectPointModel() {
    }
    /** Creates new SelectDrawModel */
    public SelectPointModel(Command inCommand) {
        setCommand(inCommand);
    }
    
    /** The point where the user released the mouse. */
    private gistoolkit.features.Point myPoint = null;
    /** Retrieve the point where the user released the mounse. */
    public gistoolkit.features.Point getPoint(){return myPoint;}
    
    /** Draw the selected records */
    public synchronized void draw(){
        if (getGISDisplay() == null) return;
        if (getGISDisplay().getBufferImage() == null) return;
        try{
            super.draw();
        }
        catch (Exception e){
            System.out.println(e);
        }
        
        // Draw the points on the buffer image.
        Graphics tempGraphics = getGISDisplay().getBufferImage().getGraphics();
        Converter c = getGISDisplay().getConverter();
        if ((c != null) && (myPoint != null)){
            
            // need these in screen coordinates
            int x1 = c.toScreenX(myPoint.getX());
            int y1 = c.toScreenY(myPoint.getY());
            
            // Point shadows
            tempGraphics.setColor(Color.black);
            tempGraphics.fillOval(x1-3, y1-3, 6,6);
            
            // Points
            tempGraphics.setColor(Color.red);
            tempGraphics.fillOval(x1-2, y1-2, 6,6);
        }        
    }
    
    /**
     * Called when the user releases the mouse butotn.
     * Causes the display to keep the currently selected shape selected.
     */
    public void mouseReleased(MouseEvent e){
        if (myCommand != null){
            if (getGISDisplay() == null) return;
            int x = e.getX();
            int y = e.getY();
            Converter c = getGISDisplay().getConverter();
            if (c == null) return;
            myPoint = new gistoolkit.features.Point(c.toWorldX(x), c.toWorldY(y));
            myCommand.executeDraw(this);
            draw();
        }
    }
    
    /** Reset the display */
    public void reset(){
        super.reset();
        myPoint = null;
        draw();
    }
}
