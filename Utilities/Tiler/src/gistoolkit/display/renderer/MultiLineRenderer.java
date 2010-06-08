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
 * Created to render shapes in a single colors, default colors are black border, no fill.
 */
public class MultiLineRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "MultiLine Renderer" */
    public String getRendererName(){ return "MultiLine Renderer";}
    
    /**
     * MonoShader constructor comment.
     */
    public MultiLineRenderer() {
        super();
    }
    
    
    /**
     * Draws the Shapes
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof MultiLineString))
            return false;
        
        MultiLineString tempMultiLineString = (MultiLineString) inRecord.getShape();
        
        // set the Alpha Value
        inGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        
        
        // draw the posative shape
        if (inGraphics != null){
            if (tempMultiLineString != null){
                LineString[] tempLineStrings = tempMultiLineString.getLines();
                if (tempLineStrings != null){
                    // create a line Renderer for rendering the individual lines.
                    LineRenderer tempLineRenderer = new LineRenderer();
                    for (int i=0; i<tempLineStrings.length; i++){
                        tempLineRenderer.drawLine(inGraphics,tempLineStrings[i], inConverter);
                        
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
        if (!(inRecord.getShape() instanceof MultiLineString))
            return false;
        
        MultiLineString tempMultiLineString = (MultiLineString) inRecord.getShape();

        // draw the Line Strings
        if (tempMultiLineString != null){
            LineString[] tempLineStrings = tempMultiLineString.getLines();
            if (tempLineStrings != null){
                
                inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (inGraphics != null){                    
                    // create a line Renderer for rendering the individual lines.
                    LineRenderer tempLineRenderer = new LineRenderer();
                    for (int i=0; i<tempLineStrings.length; i++){
                        tempLineRenderer.drawLine(inGraphics,tempLineStrings[i], inConverter);
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
        if (!(inRecord.getShape() instanceof MultiLineString)) return false;
        
        // draw the Line Strings
        MultiLineString tempMultiLineString = (MultiLineString) inRecord.getShape();
        if (tempMultiLineString != null){
            LineString[] tempLineStrings = tempMultiLineString.getLines();
            if (tempLineStrings != null){
                
                inGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (inGraphics != null){

                    // create a line Renderer for rendering the individual lines.
                    LineRenderer tempLineRenderer = new LineRenderer();
                    for (int i=0; i<tempLineStrings.length; i++){
                        tempLineRenderer.drawLinePoints(inGraphics,tempLineStrings[i], inConverter);
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
        return "MultiLine Renderer";
    }    
}