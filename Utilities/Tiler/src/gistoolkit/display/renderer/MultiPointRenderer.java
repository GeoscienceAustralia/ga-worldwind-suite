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
 * Created to render shapes in a single colors.
 */
public class MultiPointRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "MultiPoint Renderer" */
    public String getRendererName(){ return "MultiPoint Renderer";}
    
    /**
     * MonoShader constructor comment.
     */
    public MultiPointRenderer() {
        super();
    }
    
    
    /**
     * Draws the Shapes
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof MultiPoint))
            return false;
        
        MultiPoint tempMultiPoint = (MultiPoint) inRecord.getShape();
                
        // draw the posative shape
        if (tempMultiPoint != null){
            Point[] tempPoints = tempMultiPoint.getPoints();
            if (tempPoints != null){
                
                inGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (inGraphics != null){
                    
                    // create a point Renderer for rendering the individual lines.
                    PointRenderer tempPointRenderer = new PointRenderer();
                    for (int i=0; i<tempPoints.length; i++){
                        tempPointRenderer.drawPoint(inGraphics,tempPoints[i], inConverter);
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof MultiPoint))
            return false;
        
        MultiPoint tempMultiPoint = (MultiPoint) inRecord.getShape();
        
        // draw the posative shape
        if (tempMultiPoint != null){
            Point[] tempPoints = tempMultiPoint.getPoints();
            if (tempPoints != null){
                
                inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (inGraphics != null){
                    
                    // create a point Renderer for rendering the individual lines.
                    PointRenderer tempPointRenderer = new PointRenderer();
                    for (int i=0; i<tempPoints.length; i++){
                        tempPointRenderer.drawPoint(inGraphics,tempPoints[i], inConverter);
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof MultiPoint))
            return false;
        
        MultiPoint tempMultiPoint = (MultiPoint) inRecord.getShape();
        
        // draw the posative shape
        if (tempMultiPoint != null){
            Point[] tempPoints = tempMultiPoint.getPoints();
            if (tempPoints != null){
                
                inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (inGraphics != null){
                    
                    // create a point Renderer for rendering the individual lines.
                    PointRenderer tempPointRenderer = new PointRenderer();
                    for (int i=0; i<tempPoints.length; i++){
                        tempPointRenderer.drawPointPoints(inGraphics,tempPoints[i], inConverter);
                    }
                }
            }
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
        return "MultiPoint Renderer";
    }
}