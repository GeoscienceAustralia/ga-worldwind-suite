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

package gistoolkit.features;

import java.awt.image.BufferedImage;
import gistoolkit.features.featureutils.*;
/**
 * Allows Bitmaps to be displayed on the screen.
 */
public class RasterShape extends Shape {

    /** Creates new RastorShape */
    public RasterShape() {
    }

    /** Creates new RastorShape */
    public RasterShape(Envelope inEnvelope, BufferedImage inImage) {
        myImage = inImage;
        myEnvelope = (inEnvelope);
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return RASTER;}
    
    /** Return the envelope. */
    public Envelope getEnvelope(){
        return myEnvelope;
    }
    
    /** Returns the number of points in the shape. */
    public int getNumPoints(){return 0;}
    
    /** Returns the point at the given index. */
    public Point getPoint(int inIndex){return null;}
    
    /** Sets the point at the given index to the given value. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){}
    
    /** Adds a point to this raster shape.  This is always illegal and will return -1. */
    public int add(double inX, double inY){return -1;}
    
    /** Adds a point to this raster shape at the given index.  This is always illegal and will return false. */
    public boolean add(int inIndex, double inX, double inY){return false;}

    /** Removes the point at the given index.  This is always illegal and will return false. */
    public boolean remove(int inIndex) {return false;}
    
    /** Image for holding information about this shape */
    private BufferedImage myImage = null;
    /** return the image to the calling routine */
    public BufferedImage getImage(){return myImage;}
    /** Set the image to be used with this shape. */
    public void setImage(Envelope inEnvelope, BufferedImage inImage){
        myImage = inImage;
        myEnvelope = inEnvelope;
    }
    
    /** clone this shape */
    public Object clone(){
        // create a new image
        Envelope tempEnvelope = (Envelope) getEnvelope().clone();
        if(myImage != null){
            BufferedImage tempImage = new BufferedImage(myImage.getWidth(), myImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            myImage.copyData(tempImage.getRaster());
            RasterShape tempRasterShape = new RasterShape(tempEnvelope, tempImage);
            return tempRasterShape;
        }
        return new RasterShape(tempEnvelope, null);
    }

    // return the points representing this shape 
    public Point[] getPoints(){
        return new Point[0];
    }
    
    // I don't think there is a wkt for rastors.
    public String getWKT(){
        return "";
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
        tempEnvelopeBuffer.translate(inXDistance, inYDistance);
        myEnvelope = tempEnvelopeBuffer.getEnvelope();
    }

    /** Get the index of the point within the shape nearest this location.
     *  Always returns -1.  
     */
    public int getClosestIndex(double inX, double inY){return -1;}

    /** Get the distance from this shape to the given point.  In the case of rastors, this is the same as the distance to the extents. */
    public double getDistanceToPoint(double inX, double inY){
        return getDistanceToEnvelope(inX, inY);
    }
    /** Determines if the two shapes intersect  */
    public boolean intersects(Shape inShape) {
        // if the shape sent in is null, then return false
        if (inShape == null) return false;
        
        // if the envelope do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return ((Point) inShape).intersectsRasterShape(this);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsRasterShape(this);
        
        // LineStrings
        if (inShape instanceof LineString) return ((LineString) inShape).intersectsRasterShape(this);
        
        // MultiLineStrings
        if (inShape instanceof MultiLineString) return ((MultiLineString) inShape).intersectsRasterShape(this);
        
        // LinearRings
        if (inShape instanceof LinearRing) return ((LinearRing) inShape).intersectsRasterShape(this);
        
        // Polygons
        if (inShape instanceof Polygon) return ((Polygon) inShape).intersectsRasterShape(this);
        
        // MultiPolygons
        if (inShape instanceof MultiPolygon) return ((MultiPolygon) inShape).intersectsRasterShape(this);
        
        // RasterShapes
        if (inShape instanceof RasterShape) return true; //The envelope overlap so they intersect
        
        // did not find the shape so return false
        return false;
    }
    
}
