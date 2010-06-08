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
import java.awt.image.ImageObserver;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;
/**
 * Created to render rastor images on the screen, typically used for imagery.
*/
public class RasterRenderer extends SimpleRenderer implements ImageObserver {
    /** The name for this renderer, always returns "Raster Renderer" */
    public String getRendererName(){ return "Raster Renderer";}
    
    /** Creates new RastorRenderer */
    public RasterRenderer() {
    }
    
    /**
     * Draw the shape.  The renderer should return true if it successfully drew the shape, and false if it did not.
     */
    public boolean drawShape(Record inRecord,Graphics inGraphics,Converter inConverter,Shader inShader) {
        if (inRecord == null) return false;
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof RasterShape)) return false;
        RasterShape tempShape = (RasterShape) inRecord.getShape();
        
        // convert the extents of the shape to map coordinates
        Envelope tempEnvelope = tempShape.getEnvelope();
        int tempTopX = inConverter.toScreenX(tempEnvelope.getMinX());
        int tempTopY = inConverter.toScreenY(tempEnvelope.getMaxY());
        int tempBottomX = inConverter.toScreenX(tempEnvelope.getMaxX());
        int tempBottomY = inConverter.toScreenY(tempEnvelope.getMinY());
        
        // draw the rastor
        Graphics tempGraphics = inShader.getFillGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
            tempGraphics.drawImage(tempShape.getImage(), tempTopX, tempTopY, (tempBottomX-tempTopX), tempBottomY-tempTopY, this);
        }
        return true;
    }
    
    /**
     * Highlight the shape.  The renderer should return true if it successfully drew the shape, and false if it did not.
     */
    public boolean drawShapeHighlight(Record inRecord,Graphics inGraphics,Converter inConverter,Shader inShader) {
        if (inRecord == null) return false;
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof RasterShape)) return false;
        RasterShape tempShape = (RasterShape) inRecord.getShape();
        
        // convert the extents of the shape to map coordinates
        Envelope tempEnvelope = tempShape.getEnvelope();
        int tempTopX = inConverter.toScreenX(tempEnvelope.getMinX());
        int tempTopY = inConverter.toScreenY(tempEnvelope.getMaxY());
        int tempBottomX = inConverter.toScreenX(tempEnvelope.getMaxX());
        int tempBottomY = inConverter.toScreenY(tempEnvelope.getMinY());
        
        // draw a nice box around this shape.
        Graphics tempGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
            
            tempGraphics.drawLine(tempTopX, tempTopY, tempBottomX, tempTopY);
            tempGraphics.drawLine(tempBottomX, tempTopY, tempBottomX, tempBottomY);
            tempGraphics.drawLine(tempTopX, tempTopY, tempTopX, tempBottomY);
            tempGraphics.drawLine(tempTopX, tempBottomY, tempBottomX, tempBottomY);
            
            // draw the points marking the corners
            tempGraphics.drawOval(tempTopX, tempTopY, 4,4);
            tempGraphics.drawOval(tempTopX, tempBottomY, 4,4);
            tempGraphics.drawOval(tempBottomX, tempTopY, 4,4);
            tempGraphics.drawOval(tempBottomX, tempBottomY, 4,4);
        }
        
        return true;
        
    }
    
    // draw the points of the bounding box of the image.
    public boolean drawShapePoints(Record inRecord,Graphics inGraphics,Converter inConverter,Shader inShader) {
        return drawShapeHighlight(inRecord, inGraphics, inConverter, inShader);
    }
    
    /**
     * Read the properties for the initialization of the rendere from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception {
    }
    
    /** I am really hoping that this never gets called */
    public boolean imageUpdate(java.awt.Image p1,int p2,int p3,int p4,int p5,int p6) {
        // just tell me about it.
        System.out.println("Image Update Called in RastorRenderer");
        return false;
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
        return "Image (Raster) Renderer";
    }
}
