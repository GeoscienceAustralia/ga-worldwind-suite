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
 * General calss for rendering all types of features in the GIS toolkit.
 */
public class FeatureRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "Feature Renderer" */
    public String getRendererName(){ return "Feature Renderer";}
    
    /**
     * MonoShader constructor comment.
     */
    public FeatureRenderer() {
        super();
    }
    
    /**
     * Draws the line by drawing lines between the points.
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        
        // if the record is null, then return false why I don't know, but they all do this.
        if (inRecord == null) return false;
        
        // Retrieve the shapes.
        Shape tempShape = inRecord.getShape();
        
        // oddly enough, if the shape is null return true.
        if (tempShape == null) return true;
        
        // render the shape in question.
        if (tempShape instanceof Point) return myPointRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPoint) return myMultiPointRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof LineString) return myLineRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiLineString) return myMultiLineRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof Polygon) return myPolygonRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPolygon) return myMultiPolygonRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof RasterShape) return myRasterRenderer.drawShape(inRecord, inGraphics, inConverter, inShader);
        return false;
    }
    
    /**
     * Draws the Line in the highlight color.
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        
        // if the record is null, then return false why I don't know, but they all do this.
        if (inRecord == null) return false;
        
        // Retrieve the shapes.
        Shape tempShape = inRecord.getShape();
        
        // oddly enough, if the shape is null return true.
        if (tempShape == null) return true;
        
        // render the shape in question.
        if (tempShape instanceof Point) return myPointRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPoint) return myMultiPointRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof LineString) return myLineRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiLineString) return myMultiLineRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof Polygon) return myPolygonRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPolygon) return myMultiPolygonRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof RasterShape) return myRasterRenderer.drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
        return false;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        
        // if the record is null, then return false why I don't know, but they all do this.
        if (inRecord == null) return false;
        
        // Retrieve the shapes.
        Shape tempShape = inRecord.getShape();
        
        // oddly enough, if the shape is null return true.
        if (tempShape == null) return true;
        
        // render the shape in question.
        if (tempShape instanceof Point) return myPointRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPoint) return myMultiPointRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof LineString) return myLineRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiLineString) return myMultiLineRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof Polygon) return myPolygonRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof MultiPolygon) return myMultiPolygonRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        if (tempShape instanceof RasterShape) return myRasterRenderer.drawShapePoints(inRecord, inGraphics, inConverter, inShader);
        return false;
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
    
    
    // render Lines
    private LineRenderer myLineRenderer = new LineRenderer();	
    // render MultiLines
    private MultiLineRenderer myMultiLineRenderer = new MultiLineRenderer();	
    // render MultiPoints
    private MultiPointRenderer myMultiPointRenderer = new MultiPointRenderer();	
    // render MultiPolygon
    private MultiPolygonRenderer myMultiPolygonRenderer = new MultiPolygonRenderer();	
    // render points
    private PointRenderer myPointRenderer = new PointRenderer();
    // render Polygons
    private PolygonRenderer myPolygonRenderer = new PolygonRenderer();
    // render Images
    private RasterRenderer myRasterRenderer = new RasterRenderer();
    
    /** For display in lists and such. */
    public String toString(){
        return "Default Feature Renderer";
    }
    
    /** Called before the layer is initially drawn to allow the renderer to prepare for rendering.
     *
     */
    public void beginDraw() {
    }
    
    /** Called after the layer has completed rendering.
     *
     */
    public void endDraw() {
    }
    
}