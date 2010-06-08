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
import gistoolkit.display.*;
import java.awt.event.MouseEvent;

/**
 * Draw model for capturing a click location on the map.
 */
public class ClickPointModel extends SimpleDrawModel{
    /** The point where the user released the mouse. */
    private gistoolkit.features.Point myPoint = null;
    
    public ClickPointModel() {
        super();
    }
    
    public ClickPointModel(Command inCommand) {
        super();
        setCommand(inCommand);
    }
    
    /** Retrieve the point where the user released the mounse. */
    public gistoolkit.features.Point getPoint() {
        return myPoint;
    }
    
    /**
     * Called when the user releases the mouse button.
     * Records the position of the click.
     */
    public void mouseReleased(MouseEvent e) {
        if (myCommand != null) {
            if (getGISDisplay() == null) {
                return;
            }
            
            int x = e.getX();
            int y = e.getY();
            Converter c = getGISDisplay().getConverter();
            
            if (c != null) {
                myPoint = new gistoolkit.features.Point(c.toWorldX(x), c.toWorldY(y));
                if (myCommand != null) myCommand.executeDraw(this);
                draw();
            }
        }
    }
    
    /** Reset the display */
    public void reset() {
        super.reset();
        myPoint = null;
        draw();
    }
}
