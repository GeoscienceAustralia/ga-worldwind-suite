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

import java.util.*;
import java.awt.Graphics;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Created to render shapes in a single colors, default colors are black border, no fill.
 */
public class MultiPolygonRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "MultiPolygon Renderer" */
    public String getRendererName(){ return "MultiPolygon Renderer";}
     
    /**
     * Polygon renderer for rendering the sub polygons of these multi polygons
     */
    private PolygonRenderer myPolygonRenderer = new PolygonRenderer();
    
    /**
     * MonoShader constructor comment.
     */
    public MultiPolygonRenderer() {
        super();
    }
    
    /**
     * Draws shapes in a single color,
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (inRecord.getShape() == null) return false;
        if (! (inRecord.getShape() instanceof MultiPolygon))
            return false;
        
        /**
         * Return the list of multipolygons and draw them individually.
         */
        MultiPolygon tempMultiPolygon = (MultiPolygon) inRecord.getShape();
        Polygon[] tempPolygons = tempMultiPolygon.getPolygons();
        
        
        // loop through the polygons drawing them.
        Record tempRecord = new Record();
        for (int i = 0; i < tempPolygons.length; i++) {
            tempRecord.setAttributeNames(inRecord.getAttributeNames());
            tempRecord.setAttributes(inRecord.getAttributes());
            tempRecord.setShape(tempPolygons[i]);
            myPolygonRenderer.drawShape(tempRecord, inGraphics, inConverter, inShader);
        }
        
        return true;
    }
    
    /**
     * Draws shapes in a single color,
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        // check that the shape is valid
        if (inRecord == null) return false;
        if (! (inRecord.getShape() instanceof MultiPolygon))
            return false;
        
        // check that the shader is valid
        if (inShader == null) return false;
        
        /**
         * Return the list of multipolygons and draw them individually.
         */
        MultiPolygon tempMultiPolygon = (MultiPolygon) inRecord.getShape();
        Polygon[] tempPolygons = tempMultiPolygon.getPolygons();
        
        
        // loop through the polygons drawing them.
        for (int i = 0; i < tempPolygons.length; i++) {
            Record tempRecord = (Record) inRecord.clone();
            tempRecord.setShape(tempPolygons[i]);
            myPolygonRenderer.drawShapeHighlight(tempRecord, inGraphics, inConverter, inShader);
        }
        
        return true;
    }
    
    /**
     * Draws shapes in a single color,
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        // check that the shape is valid
        if (inRecord == null) return false;
        if (! (inRecord.getShape() instanceof MultiPolygon))
            return false;
        
        // check that the shader is valid
        if (inShader == null) return false;
        
        /**
         * Return the list of multipolygons and draw them individually.
         */
        MultiPolygon tempMultiPolygon = (MultiPolygon) inRecord.getShape();
        Polygon[] tempPolygons = tempMultiPolygon.getPolygons();
        
        // loop through the polygons drawing them.
        for (int i = 0; i < tempPolygons.length; i++) {
            Record tempRecord = (Record) inRecord.clone();
            tempRecord.setShape(tempPolygons[i]);
            myPolygonRenderer.drawShapePoints(tempRecord, inGraphics, inConverter, inShader);
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
        return "MultiPolygon Renderer";
    }    
}