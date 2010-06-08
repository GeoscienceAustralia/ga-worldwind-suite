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
import java.awt.Color;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Created to render Lines in a single color, default color is black.
 */
public class LineRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "Line Renderer" */
    public String getRendererName(){ return "Line Renderer";}
    
    /**
     * MonoShader constructor comment.
     */
    public LineRenderer() {
        super();
    }
    
    
    /**
     * Draws the line by drawing lines between the points.
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof LineString))
            return false;
        
        LineString tempLineString = (LineString) inRecord.getShape();
        
        // Draw the lines.
        double[] tempXPoints = tempLineString.getXCoordinates();
        if (tempXPoints != null){
            inGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
            if (inGraphics != null){
                drawLine(inGraphics, tempLineString, inConverter);
            }
        }
        return true;
    }
    
    /**
     * Draws the Line in the highlight color.
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof LineString))
            return false;
        
        LineString tempLineString = (LineString) inRecord.getShape();
        
        // Draw the lines.
        inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        
        if (inGraphics != null) {
            drawLine(inGraphics, tempLineString, inConverter);
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof LineString))
            return false;
        
        LineString tempLineString = (LineString) inRecord.getShape();
        
        // Draw the points.
        inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        
        if (inGraphics != null) {
            drawLinePoints(inGraphics, tempLineString, inConverter);
        }
        return true;
    }
    
    /**
     * Draws the actual line, the draw shape routine calls this routine.
     */
    protected boolean drawLine(Graphics inGraphics, LineString inLineString, Converter inConverter) {
        if (inLineString == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        double[] tempXPoints = inLineString.getXCoordinates();
        double[] tempYPoints = inLineString.getYCoordinates();
        if (tempXPoints != null){
            for (int i = 1; i < tempXPoints.length; i++) {
                int x1 = inConverter.toScreenX(tempXPoints[i - 1]);
                int y1 = inConverter.toScreenY(tempYPoints[i - 1]);
                int x2 = inConverter.toScreenX(tempXPoints[i]);
                int y2 = inConverter.toScreenY(tempYPoints[i]);
                
                inGraphics.drawLine(x1, y1, x2, y2);
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    protected boolean drawLinePoints(Graphics inGraphics, LineString inLineString, Converter inConverter) {
        if (inLineString == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        Color initColor = inGraphics.getColor();
        
        // Draw the Points
        double[] tempXPoints = inLineString.getXCoordinates();
        double[] tempYPoints = inLineString.getYCoordinates();
        if (tempXPoints != null){
            
            for (int i=0; i<tempXPoints.length; i++){
                int x1 = inConverter.toScreenX(tempXPoints[i]);
                int y1 = inConverter.toScreenY(tempYPoints[i]);
                inGraphics.setColor(Color.orange);
                inGraphics.drawOval(x1-2,y1-2,4,4);
                if((i==0)||(i==tempXPoints.length-1)) {
                    if(i==0)
                        inGraphics.setColor(Color.green);
                    else
                        inGraphics.setColor(Color.red);
                    inGraphics.fillOval(x1-3,y1-3,6,6);
                }
            }
        }
        inGraphics.setColor(initColor);
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
        return "Line Renderer";
    }
}