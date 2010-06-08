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

import gistoolkit.features.featureutils.*;

/**
 * Represents a collection of points.
 */
public class MultiPoint extends Shape {
    
    /** The X Coordinates of all the points in the line string. */
    private double[] myXCoordinates;
    /** Return the array of X Coordinates.  This is private data, and should be used carefully. */
    public double[] getXCoordinates(){return myXCoordinates;}
    
    /** The Y Coordinates of all the points in the line string. */
    private double[] myYCoordinates;
    /** Return the array of Y Coordinates.  This is private data, and should be used carefully. */
    public double[] getYCoordinates(){return myYCoordinates;}
    
    /**
     * Point constructor comment.
     */
    public MultiPoint() {
        super();
    }
    
    /**
     * Create a new multi point which contains a single point.
     */
    public MultiPoint(double[] inXs, double[] inYs) {
        // validate input data
        if (inXs == null)
            return;
        if (inYs == null)
            return;
        if (inXs.length == 0){
            myXCoordinates = new double[0];
            myYCoordinates = new double[0];
            return;
        }
        if (inYs.length == 0){
            myXCoordinates = new double[0];
            myYCoordinates = new double[0];
            return;
        }
        
        // find the minimum length, if these do not match
        int tempMinLength = inXs.length;
        if (inYs.length < tempMinLength)
            tempMinLength = inYs.length;
        
        // create the points array
        myXCoordinates = new double[tempMinLength];
        myYCoordinates = new double[tempMinLength];
        for (int i = 0; i < tempMinLength; i++) {
            myXCoordinates[i] = inXs[i];
            myYCoordinates[i] = inYs[i];
        }
        calculateEnvelope();
    }
    
    /**
     * Create a new multi point which contains a single point.
     */
    public MultiPoint(Point[] inPoints) {
        if (inPoints == null){
            myXCoordinates = new double[0];
            myYCoordinates = new double[0];
            return;
        }
        
        // create the points array
        myXCoordinates = new double[inPoints.length];
        myYCoordinates = new double[inPoints.length];
        for (int i = 0; i < inPoints.length; i++) {
            myXCoordinates[i] = inPoints[i].getX();
            myYCoordinates[i] = inPoints[i].getY();
        }
        calculateEnvelope();
    }
    
    /**
     * Create a new multi point which contains a single point.
     */
    public MultiPoint(double inX, double inY) {
        myXCoordinates = new double[1];
        myYCoordinates = new double[1];
        myXCoordinates[0] = inX;
        myYCoordinates[0] = inY;        
        calculateEnvelope();
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return MULTIPOINT;}
    
    /**
     * Creates a copy of the MultiPoint
     */
    public Object clone(){
        // if this is an uninitialized multipoint, then return an uninitialized multipoint.
        if (myXCoordinates == null) return new MultiPoint();
        
        // create a copy of this multipoint and return that one.
        double[] tempXCoordinates = new double[myXCoordinates.length];
        double[] tempYCoordinates = new double[myYCoordinates.length];
        return new MultiPoint(tempXCoordinates, tempYCoordinates);
    }
    
    /** Return the number of points in this shape. */
    public int getNumPoints(){return myXCoordinates.length;};
    
    /** Get the point at the given index. */
    public Point getPoint(int inIndex){
        if ((inIndex >=0) && (inIndex < myXCoordinates.length)) return new Point(myXCoordinates[inIndex], myYCoordinates[inIndex]);
        return null;
    }
    /** Set the point at the given index. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){
        if ((inIndex >=0) && (inIndex < myXCoordinates.length)) {
            myXCoordinates[inIndex] = inXCoordinate;
            myYCoordinates[inIndex] = inYCoordinate;
        }
    }
    
    /**
     * Add a point to the MultiPoint returns the index of the newly added point, or -1 if it failed to add the point.
     */
    public int add(double inX, double inY) {
        int tempIndex = getClosestIndex(inX, inY);
        if (add(tempIndex, inX, inY)) return tempIndex;
        return -1;
    }
    
    /**
     * Adds a point to the MultiPoint at the given point index. Returns true if the add succeeded, and false if it failed.
     */
    public boolean add(int inIndex, double inX, double inY) {
        
        // check for valid data and special cases.
        if (myXCoordinates == null){
            if (inIndex == 0){
                myXCoordinates = new double[1];
                myXCoordinates[0] = inX;
                myYCoordinates = new double[1];
                myYCoordinates[0] = inY;
                return true;
            }
            else{
                return false;
            }
        }
        
        if ((inIndex <= myXCoordinates.length) && (inIndex >=0)){
            // create the new points array.
            double[] tempXCoordinates = new double[myXCoordinates.length+1];
            double[] tempYCoordinates = new double[myYCoordinates.length+1];
            for (int i=0; i<myXCoordinates.length; i++){
                if (i < inIndex){
                    tempXCoordinates[i] = myXCoordinates[i];
                    tempYCoordinates[i] = myYCoordinates[i];
                }
                if (i == inIndex){
                    tempXCoordinates[i] = inX;
                    tempYCoordinates[i] = inY;
                }
                if (i >= inIndex){
                    tempXCoordinates[i+1] = myXCoordinates[i];
                    tempYCoordinates[i+1] = myYCoordinates[i];
                }
            }
            if (inIndex >= tempXCoordinates.length){
                tempXCoordinates[myXCoordinates.length] = inX;
                tempYCoordinates[myYCoordinates.length] = inY;
            }
            myXCoordinates = tempXCoordinates;
            myYCoordinates = tempYCoordinates;
            // calculate the new envelope
            calculateEnvelope();
            return true;
        }
        return false;
    }

    /**
     * Removes the point at the given index from the MultiPoint.
     */
    public boolean remove(int inIndex) {
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 4) return false;
        if (inIndex >= myXCoordinates.length) return false;
        if (inIndex < 0 ) return false;
        
        // create the new points array.
        double[] tempXCoordinates = new double[myXCoordinates.length-1];
        double[] tempYCoordinates = new double[myYCoordinates.length-1];
        for (int i=0; i<myXCoordinates.length; i++){
            if (i < inIndex){
                tempXCoordinates[i] = myXCoordinates[i];
                tempYCoordinates[i] = myYCoordinates[i];
            }
            if (i > inIndex){
                tempXCoordinates[i-1] = myXCoordinates[i];
                tempYCoordinates[i-1] = myYCoordinates[i];
            }
        }
        myXCoordinates = tempXCoordinates;
        myYCoordinates = tempYCoordinates;
        calculateEnvelope();
        return true;
    }
    
    /**
     * Recalculates the envelope for the shape.
     */
    public void calculateEnvelope() {
        myEnvelope = null;
    }
    
    /** Retrieve the envelope for this shape. */
    public Envelope getEnvelope(){
        if (myEnvelope == null){
            if ((myXCoordinates != null) && (myXCoordinates.length > 0)) {
                EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
                // loop through the points calculating the envelope
                for (int i = 0; i < myXCoordinates.length; i++) {
                    tempEnvelopeBuffer.expandToInclude(myXCoordinates[i], myYCoordinates[i]);
                }
                myEnvelope = tempEnvelopeBuffer.getEnvelope();
            }
        }
        return myEnvelope;
   }
        
    /**
     * Returns the list of points from this multi point.
     */
    public Point[] getPoints(){
        if (myXCoordinates == null) return new Point[0];
        if (myXCoordinates.length == 0) return new Point[0];
        Point[] tempPoints = new Point[myXCoordinates.length];
        for (int i=0; i<myXCoordinates.length; i++){
            tempPoints[i] = new Point(myXCoordinates[i], myYCoordinates[i]);
        }
        return tempPoints;
    }
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public String getWKT(){
        StringBuffer sb = new StringBuffer("multipoint(");
        for (int i=0; i<myXCoordinates.length; i++){
            if (i>0) sb.append(",");
            sb.append(myXCoordinates[i]);
            sb.append(" ");
            sb.append(myYCoordinates[i]);
        }
        sb.append(")");
        return sb.toString();
    }
            
    /**
     * Return the point in the MultiPoint that is the closest to this point.
     */
    public Point getClosestPoint(double inX, double inY) {
        Point tempPoint = null;

        int tempPointIndex = getClosestIndex(inX, inY);
        // if a point was found
        if (tempPointIndex != -1){
            tempPoint = new Point(myXCoordinates[tempPointIndex], myYCoordinates[tempPointIndex]);
        }
        return tempPoint;
    }

    /**
     * Return the point in the LineString that is the closest to this point.
     */
    public int getClosestIndex(double inX, double inY) {
        
        // loop through the points finding the closest line segment.
        double tempMinDistance = Double.MAX_VALUE;
        int tempPointIndex = -1;
        for (int i=0; i<myXCoordinates.length; i++){
            double tempDistance = getDistance(myXCoordinates[i], myYCoordinates[i], inX, inY);
            if (tempDistance < tempMinDistance){
                tempMinDistance = tempDistance;
                tempPointIndex = i;
            }
        }
        
        // if a point was found
        return tempPointIndex;
    }
        
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        for (int i=0; i<myXCoordinates.length; i++){
            myXCoordinates[i] = myXCoordinates[i] + inXDistance;
            myYCoordinates[i] = myYCoordinates[i] + inYDistance;
        }
        calculateEnvelope();
    }
    
    /** Get the distance from this shape to the given point */
    public double getDistanceToPoint(double inX, double inY){
        // special conditions
        if (myXCoordinates == null) return Double.NaN;
        if (myXCoordinates.length == 0) return Double.NaN;
        double tempMaxDistance = getDistance(inX, inY, myXCoordinates[0], myYCoordinates[0]);
        if (myXCoordinates.length == 1) return tempMaxDistance;
        
        // loop through all the points and return the distance.
        for (int i=1; i<myXCoordinates.length; i++){
            double tempDistance = getDistanceToLine(myXCoordinates[i-1], myYCoordinates[i-1], myXCoordinates[i], myYCoordinates[i], inX, inY);
            if (tempDistance < tempMaxDistance) tempMaxDistance = tempDistance;
        }
        
        return tempMaxDistance;
    }
    
    /** Determines if the two shapes intersect  */
    public boolean intersects(Shape inShape) {
        // if the shape sent in is null, then return false
        if (inShape == null) return false;
        
        // if the envelope do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return ((Point) inShape).intersectsMultiPoint(this);
        
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
        
        // did not find the shape so return false
        return false;
    }
    
    /** A MultiPoint will intersect another MultiPoint if any one of the points is within the minimum distance (EQUAL_LIMIT).*/
    public boolean intersectsMultiPoint(MultiPoint inMultiPoint){
        double[] inXCoordinates = inMultiPoint.getXCoordinates();
        double[] inYCoordinates = inMultiPoint.getYCoordinates();
        if ((inXCoordinates != null) && (myXCoordinates != null)){
            for (int i=0; i<inXCoordinates.length; i++){
                for (int j=0; j<myXCoordinates.length; j++){
                    if ((Math.abs(inXCoordinates[i]-myXCoordinates[j]) < EQUAL_LIMIT) &&
                        (Math.abs(inYCoordinates[i]-myYCoordinates[j]) < EQUAL_LIMIT)){
                           return true;
                    }
                }
            }
        }
        return false;
    }
    
    /** A MultiPoint will intersect a LineString if any one of the points intersects a verticie of the line string, or is on one of the lines. */
    public boolean intersectsLineString(LineString inLineString){
        double[] inXCoordinates = inLineString.getXCoordinates();
        double[] inYCoordinates = inLineString.getYCoordinates();
        if ((inXCoordinates != null) && (myXCoordinates != null)){
            for (int i=0; i<inXCoordinates.length; i++){
                for (int j=0; j<myXCoordinates.length; j++){
                    if ((Math.abs(inXCoordinates[i]-myXCoordinates[j]) < EQUAL_LIMIT) &&
                        (Math.abs(inYCoordinates[i]-myYCoordinates[j]) < EQUAL_LIMIT)){
                           return true;
                    }
                    if (i>0){
                        if (pointOnLine(myXCoordinates[j], myYCoordinates[j],
                        inXCoordinates[i-1], inYCoordinates[i-1],
                        inXCoordinates[i], inYCoordinates[i]
                        )) return true;
                    }
                }
            }
        }
        return false;
    }
    
    /** A MultiPoint will intersect a MultiLineString if any one of the points intersects one of the constituent line strings. */
    public boolean intersectsMultiLineString(MultiLineString inMultiLineString){
        LineString[] tempLines = inMultiLineString.getLines();
        if (tempLines != null){
            Point[] tempPOints = getPoints();
            if (myXCoordinates != null){
                for (int i=0; i<myXCoordinates.length; i++){
                    for (int j=0; j<tempLines.length; j++){
                        if (tempLines[i].intersectsPoint(myXCoordinates[j], myYCoordinates[j])) return true;
                    }
                }
            }
        }
        return false;
    }
    
    /** A MultiPoint will intersect a LinearRing if any one of the points is contained within the linear ring. */
    public boolean intersectsLinearRing(LinearRing inLinearRing){
        if (inLinearRing != null){
            if (myXCoordinates != null){
                for (int i=0; i<myXCoordinates.length; i++){
                    if (inLinearRing.containsPoint(myXCoordinates[i], myYCoordinates[i])) return true;
                }
            }
        }
        return false;
    }
    
    /** A MUltiPoing will intersect a polygon if any one of the points is contained within the polygon. */
    public boolean intersectsPolygon(Polygon inPolygon){
        if (inPolygon != null){
            if (myXCoordinates != null){
                for (int i=0; i<myXCoordinates.length; i++){
                    if (Polygon.isPointInPolygon(inPolygon, new Point(myXCoordinates[i], myYCoordinates[i]))) return true;
                }
            }
        }
        return false;
    }
    
    /** A MultiPoint will intersect a MultiPolygon if any one of the points is contained within the MultiPolygon. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        if (inMultiPolygon != null){
            if (myXCoordinates != null){
                for (int i=0; i<myXCoordinates.length; i++){
                    if (inMultiPolygon.contains(new Point(myXCoordinates[i], myYCoordinates[i]))) return true;
                }
            }
        }
        return false;
    }
    
    /** A MultiPoint will intersect a RasterShape if any one of the points is contained within the envelope of the Raster. */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        if (inRasterShape != null){
            if (myXCoordinates != null){
                for (int i=0; i<myXCoordinates.length; i++){
                    if (inRasterShape.contains(new Point(myXCoordinates[i], myYCoordinates[i]))) return true;
                }
            }
        }
        return false;
    }
}