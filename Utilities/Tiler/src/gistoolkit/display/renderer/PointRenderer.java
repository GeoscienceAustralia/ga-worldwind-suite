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

package gistoolkit.display.renderer;

import java.util.Properties;
import java.awt.Graphics;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Created to render Points in a single color.
 */
public class PointRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "Point Renderer" */
    public String getRendererName(){ return "Point Renderer";}
    
    /**
     * MonoShader constructor comment.
     */
    public PointRenderer() {
        super();
    }
    
    /**
     * Draws the line by drawing lines between the points.
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Point))
            return false;
        
        Point tempPoint = (Point) inRecord.getShape();
        
        // Draw the point.
        Graphics tempGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
            drawPoint(tempGraphics, tempPoint, inConverter);
        }
        return true;
    }
    
    /**
     * Draws the Line in the highlight color.
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Point))
            return false;
        
        Point tempPoint = (Point) inRecord.getShape();
        
        // Draw the lines.
        Graphics tempGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
           drawPoint(tempGraphics, tempPoint, inConverter);
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Point))
            return false;
        
        Point tempPoint = (Point) inRecord.getShape();
        
        // Draw the lines.
        // Draw the lines.
        Graphics tempGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
            drawPointPoints(tempGraphics, tempPoint, inConverter);
        }
        return true;
    }
    
    /**
     * Draw the point.
     */
    protected boolean drawPoint(Graphics inGraphics, Point inPoint, Converter inConverter){
        if (inPoint == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
                
        if (inPoint != null){
            int x1 = inConverter.toScreenX(inPoint.getX());
            int y1 = inConverter.toScreenY(inPoint.getY());
            inGraphics.fillOval(x1-2, y1-2, 4,4);
            inGraphics.drawOval(x1-2, y1-2, 4,4);
        }
        return true;
    }
    
    /**
     * Draw the point.
     */
    protected boolean drawPointPoints(Graphics inGraphics, Point inPoint, Converter inConverter){
        if (inPoint == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        if (inPoint != null){
            int x1 = inConverter.toScreenX(inPoint.getX());
            int y1 = inConverter.toScreenY(inPoint.getY());
            inGraphics.drawOval(x1-2, y1-2, 4,4);
        }
        return true;
    }
    
    /**
     * Read the properties for the initialization of the rendere from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception {
    }
    
    /** Get the configuration information for this renderer  */
    public Node getNode() {
        return null;
    }
    
    /** Set the configuration information for this renderer  */
    public void setNode(Node inNode) throws Exception {
    }

    /** For display in lists and such. */
    public String toString(){
        return "Point Renderer";
    }
}