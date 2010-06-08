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

package gistoolkit.display;

import java.awt.*;
import gistoolkit.common.*;

/**
 * Class to display a scale bar on the map.
 */
public interface ScaleBar {
    /** 
     * A description to be displayed to the user that indicates what this scale bar does.
     * A good thing to indicates is that it takes a projection in meters and displays a scale in metric.
     * Or that it takes a projection in feed and displays a scale in in,ft,yd,and miles.
     */
    public String getDescription();
    
    
    /** Method to draw a scale bar on the graphics context. */
    public void drawScale(Graphics inGraphics, Converter inConverter, int inWidth, int inHeight);
    
    /** Get the configuration information for this ScaleBar  */
    public Node getNode();
    
    /** Set the configuration information for this ScaleBar  */
    public void setNode(Node inNode) throws Exception ;
    
}
