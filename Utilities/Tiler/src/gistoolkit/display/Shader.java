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

import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import gistoolkit.common.*;
/**
 * Determines which color to use when drawing the feature.
 */
public interface Shader {
        
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for drawing of lines.
     */
    public Graphics getLineGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    /**
     * Set up the graphics context for the highlighting of lines.
     * Always return the graphics context sent in after modifying it for highlighting of lines.
     */
    public Graphics getLineHighlightGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    public Graphics getFillGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of polygons.
     */
    public Graphics getFillHighlightGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for labeling of features.
     */
    public Graphics getLabelGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for highlighting of features.
     */
    public Graphics getLabelHighlightGraphics(Graphics inGraphics, Object[] inAttributes,String[] inNames);
    
    /**
     * Read the properties for the initialization of the rendere from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception;
    
    /**
     * Returns the legend for the shader.
     */
    public BufferedImage getLegend();
    
    /** Get the configuration information for this shader */
    public Node getNode();
    
    /** Set the configuration information for this shader */
    public void setNode(Node inNode) throws Exception;
}