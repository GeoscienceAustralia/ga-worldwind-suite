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

/**
 * A point is a feature that has no dimension, it only represents a point in two dimension space.
 */
public class Point extends Shape {
    public double x;
    public double y;
    
    /**
     * Point constructor comment.
     */
    public Point(double inX, double inY) {
        x = inX;
        y = inY;
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return POINT;}
    
    /**
     * Creates a copy of the Point
     */
    public Object clone(){
        return new Point(x,y);
    }
    
    /** Returns the number of points in the shape. */
    public int getNumPoints(){return 1;};
    
    /** Returns the point at the given index. */
    public Point getPoint(int inIndex){if (inIndex == 0) return this; else return null;}
    
    /** Sets the point at the given index to the given value. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){
        if (inIndex == 0){
            x = inXCoordinate;
            y = inYCoordinate;
        };
    }
    
    /** Adds the point to the Shape. Always returns -1 because points can not be added to a point.*/
    public int add(double inX, double inY){
        return -1;
    }
    
    /** Adds the point to the Shape. Always returns -1 because points can not be added to a point.*/
    public boolean add(int inIndex, double inX, double inY){
        return false;
    }

    /** Removes the point at the given index.  Since there is only one point, this method will always return false, the point can not be removed. */
    public boolean remove(int inIndex){
        return false;
    }
    
    /**
     * Determines if this point contains the shape sent in;
     * This Point can only contain the shape sent in if they are the same point.
     * @return boolean
     * @param inShape features.Shape
     */
    public boolean contains(Shape inShape) {
        if (inShape instanceof Point){
            Point tempPoint = (Point) inShape;
            if (Math.abs(getX()-tempPoint.getX()) > EQUAL_LIMIT) return false;
            if (Math.abs(getY()-tempPoint.getY()) > EQUAL_LIMIT) return false;
            return true;
        }
        return false;
    }
    
    /**
     * return the bounding rectangle of this shape.
     */
    public Envelope getEnvelope() {
        if (myEnvelope == null){
            myEnvelope = new Envelope(x,y,x,y);
        }
        return myEnvelope;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(double inX) {
        myEnvelope = null;
        x = inX;
    }
    public void setY(double inY) {
        myEnvelope = null;
        y = inY;
    }
    public boolean equals(Object inObject) {
        if (inObject instanceof Point){
            Point tempPoint = (Point) inObject;
            if ((getX() == tempPoint.getX() ) && (getY() == tempPoint.getY())) return true;
            return false;
        }
        else return (this == inObject);
    }
    public boolean equals(double inX, double inY) {
        if (( Math.abs(getX() - inX) < EQUAL_LIMIT ) && (Math.abs(getY() - inY) < EQUAL_LIMIT)) return true;
        return false;
    }
    
    /**
     * Returns the string representation of this point.
     * Creation date: (5/17/2001 10:16:24 AM)
     * @return java.lang.String
     */
    public String toString() {
        return "Point X="+getX()+" Y="+getY();
    }
    
    /** return a point array of one point for this point */
    public Point[] getPoints(){
        Point[] tempPoints = new Point[1];
        tempPoints[0] = this;
        return tempPoints;
    }
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public String getWKT(){
        return "point("+getX()+" "+getY()+")";
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        myEnvelope = null;
        x = x + inXDistance;
        y = y + inYDistance;
    }
    
    /** Return the distance from the particular point */
    public double distance(Point inPoint){
        return distance(this, inPoint);
    }
        
    /** Get the point nearest this location.  Always returns this point. */
    public Point getClosestPoint(double inX, double inY){return this;}
    
    /** Get the point nearest this location.  Always returns this point. */
    public int getClosestIndex(double inX, double inY){return 0;}

    /** Get the distance from this shape to the given point */
    public double getDistanceToPoint(double inX, double inY){
        return getDistance(x,y,inX,inY);
    }
    
    /** Determines if the two shapes intersect  */
    public boolean intersects(Shape inShape) {
        // if the shape sent in is null, then return false
        if (inShape == null) return false;
        
        // if the envelope do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return intersectsPoint((Point) inShape);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return intersectsMultiPoint((MultiPoint) inShape);
        
        // LineStrings
        if (inShape instanceof LineString) return intersectsLineString((LineString) inShape);
        
        // MultiLineStrings
        if (inShape instanceof MultiLineString) return intersectsMultiLineString((MultiLineString) inShape);
        
        // LinearRings
        if (inShape instanceof LinearRing) return intersectsLinearRing((LinearRing) inShape);
        
        // Polygons
        if (inShape instanceof Polygon) return intersectsPolygon((Polygon) inShape);
        
        // MultiPolygons
        if (inShape instanceof MultiPolygon) return intersectsMultiPolygon((MultiPolygon) inShape);
        
        // RasterShapes
        if (inShape instanceof RasterShape) return intersectsRasterShape((RasterShape) inShape);
        
        // did not find the shape so return false;
        return false;
    }
    
    /** A point intersects another point if they are within the equal limit, like an equal calculation. */
    public boolean intersectsPoint(Point inPoint){
        if (pointsEqual(inPoint.getX(), x, inPoint.getY(), y)) return true;
        return false;
    }

    /** A point intersects another point if they are within the equal limit, like an equal calculation. */
    public static boolean pointsEqual(double inX1, double inY1, double inX2, double inY2){
        if ((Math.abs(inX1 - inX2) < EQUAL_LIMIT) && (Math.abs(inY1 - inY2) < EQUAL_LIMIT)) return true;
        return false;
    }
    
    /** A point intersects a MultiPoint if it intersects any one of it's constituent points */
    public boolean intersectsMultiPoint(MultiPoint inMultiPoint){
        Point[] tempPoints = inMultiPoint.getPoints();
        if (tempPoints != null){
            for (int i=0; i<tempPoints.length; i++){
                if (intersectsPoint(tempPoints[i])) return true;
            }
        }
        return false;
    }
    
    /** A point intersects a LineString if it intersects any one if the LineStrings' points, or if it is on the line between any two of the LineStrings' points. */
    public boolean intersectsLineString(LineString inLineString){
        Point[] tempPoints = inLineString.getPoints();
        if (tempPoints != null){
            for (int i=0; i<tempPoints.length; i++){
                Point tempPoint = tempPoints[i];
                if (((tempPoint.getX() - x) < EQUAL_LIMIT) && ((tempPoint.getY() - y) < EQUAL_LIMIT)) return true;
                if (i>0){
                    if (pointOnLine(this, tempPoints[i], tempPoints[i-1])) return true;
                }
            }
        }
        return false;
    }

    /** A point intersects a MultiLineString if it intersects any one of the constituent LineStrings. */
    public boolean intersectsMultiLineString(MultiLineString inMultiLineString){
        LineString[] tempLineStrings = inMultiLineString.getLines();
        if (tempLineStrings != null){
            for (int i=0; i<tempLineStrings.length; i++){
                if (intersects(tempLineStrings[i])) return true;
            }
        }
        return false;
    }
    
    /** A point intersects a LinearRing if it is contained within the interior of the linear ring. */
    public boolean intersectsLinearRing(LinearRing inLinearRing){
        return inLinearRing.contains(this);
    }
    
    /** A point intersects a Polygon if it is contained within the polygon. */
    public boolean intersectsPolygon(Polygon inPolygon){
        return inPolygon.contains(this);
    }
    
    /** A point intersects a MultiPolygon if it is contained within the MultiPolygon. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        return inMultiPolygon.contains(this);
    }
    
    /** A point intersects a RasterShape if it is contained within the envelope of the raster shape */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        return inRasterShape.contains(this);
    }    
}