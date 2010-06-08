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
 * A line string is a series of connected line segments.
 */
public class LineString extends Shape{
    /** The X Coordinates of all the points in the line string. */
    private double[] myXCoordinates;
    /** Return the array of X Coordinates.  This is private data, and should be used carefully. */
    public double[] getXCoordinates(){return myXCoordinates;}
    
    /** The Y Coordinates of all the points in the line string. */
    private double[] myYCoordinates;
    /** Return the array of Y Coordinates.  This is private data, and should be used carefully. */
    public double[] getYCoordinates(){return myYCoordinates;}
    
    /**
     * Create a new empty Line String.
     */
    public LineString() {
        super();
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return LINESTRING;}
    
    /**
     * Create a new LineString from the points.
     */
    public LineString(double[] inXs, double[] inYs) {
        
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
    }
    
    /**
     * Create a new LineString from the points.
     */
    public LineString(Point[] inPoints) {
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
    }
    
    /**
     * Creates a copy of the Line String
     * Creation date: (5/3/2001 8:45:46 AM)
     * @return gistoolkit.features.Shape
     */
    public Object clone(){
        return new LineString(myXCoordinates, myYCoordinates);
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
     * Add a point to the LineString returns the index of the newly added point, or -1 if it failed to add the point.
     */
    public int add(double inX, double inY) {
        int tempIndex = getClosestIndex(inX, inY);
        if (add(tempIndex, inX, inY)) return tempIndex;
        return -1;
    }
    
    /**
     * Adds a point to the LineString at the given point index. Returns true if the add succeeded, and false if it failed.
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
     * Removes the point at the given index from the LineString.
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
    
    /** return the bounding rectangle of this shape.*/
    public Envelope getEnvelope() {
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
     * Recalculates the envelope for the shape.
     */
    public void calculateEnvelope() {
        myEnvelope = null;
    }
    
    /**
     * For line strings, since the shape is a line, it can only contain points, and other line strings.
     * a point is contained within a line string if it falls on the line.  A line is contained in the line string if
     * it falls completely on the lines of the containing line string.  Polygons, and rings can not be contained in a line.
     * @return boolean
     * @param inShape features.Shape
     */
    public boolean contains(Shape inShape) {
        // check for points
        if (inShape instanceof Point){
            return containsPoint((Point) inShape);
        }
        // check for LineString
        if (inShape instanceof LineString){
            return containsLine((LineString) inShape);
        }
        // check for MultiString
        if (inShape instanceof MultiLineString){
            return containsMultiLine((MultiLineString) inShape);
        }
        return false;
    }
    
    /**
     * For line strings, since the shape is a line, it can only contain points, and other line strings.
     * <p>
     * A point is contained within a line string if it falls on the line.  A line is contained in the line string if
     * it falls completely on the lines of the containing line string.  Polygons, and rings can not be contained in a line.
     * </p>
     */
    public boolean containsLine(LineString inLineString) {
        if (inLineString == null) return false;
        if (inLineString.getPoints() == null) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 2) return false;
        
        double[] tempXCoordinates = inLineString.getXCoordinates();
        double[] tempYCoordinates = inLineString.getYCoordinates();
        for (int i=0; i<myXCoordinates.length-1; i++){
            boolean tempIsContained = false;
            for (int j=0; j<tempXCoordinates.length-1; j++){
                if ((pointOnLine(tempXCoordinates[j], tempYCoordinates[j], myXCoordinates[i], myYCoordinates[i], myXCoordinates[i+1], myYCoordinates[i+1]))
                && (pointOnLine(tempXCoordinates[j+1], tempYCoordinates[j+1], myXCoordinates[i], myYCoordinates[i], myXCoordinates[i+1], myYCoordinates[i+1]))){
                    tempIsContained = true;
                    break;
                }
            }
            if (tempIsContained == false) return false;
        }
        return true;
    }
    
    /**
     * For line strings, since the shape is a line, it can only contain points, and other line strings.
     * a point is contained within a line string if it falls on the line.  A line is contained in the line string if
     * it falls completely on the lines of the containing line string.  Polygons, and rings can not be contained in a line.
     */
    public boolean containsMultiLine(MultiLineString inMultiLineString) {
        if (inMultiLineString == null) return false;
        if (inMultiLineString.getLines() == null) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 2) return false;
        
        LineString[] tempLines = inMultiLineString.getLines();
        for (int i=0; i<tempLines.length; i++){
            if (!contains(tempLines[i])){
                return false;
            }
        }
        return true;
    }
    
    /**
     * For line strings, since the shape is a line, it can only contain points, and other line strings.
     * a point is contained within a line string if it falls on the line.  A line is contained in the line string if
     * it falls completely on the lines of the containing line string.  Polygons, and rings can not be contained in a line.
     */
    public boolean containsPoint(Point inPoint) {
        if (inPoint == null) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 2) return false;
        
        for (int i=0; i<myXCoordinates.length-1; i++){
            if (pointOnLine(inPoint.getX(), inPoint.getY(), myXCoordinates[i], myYCoordinates[i], myXCoordinates[i+1], myYCoordinates[i+1])) return true;
        }
        return false;
    }
    
    /**
     * Returns the points representing this line string.  Creates a new copy of all the points, so this is an expensive operation.
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
        StringBuffer sb = new StringBuffer("linestring(");
        for (int i=0; i<myXCoordinates.length; i++){
            if (i>0) sb.append(",");
            sb.append(myXCoordinates[i]);
            sb.append(" ");
            sb.append(myYCoordinates[i]);
        }
        sb.append(")");
        return sb.toString();
    }
                
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        for (int i=0; i<myXCoordinates.length; i++){
            myXCoordinates[i] = myXCoordinates[i] + inXDistance;
            myYCoordinates[i] = myYCoordinates[i] + inYDistance;
        }
        calculateEnvelope();
    }
    
    /**
     * Return the point in the LineString that is the closest to this point.
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
        
        // if the envelops do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return intersectsPoint( (Point) inShape);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsLineString(this);
        
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
    
    /**
     * A line string will intersect a point if the point falls on the line, or is the same as one of the verticies.
     */
    public boolean intersectsPoint(Point inPoint) {
        return intersectsPoint(inPoint.getX(), inPoint.getY());
    }

    /**
     * A line string will intersect a point if the point falls on the line, or is the same as one of the verticies.
     */
    public boolean intersectsPoint(double inX, double inY) {
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 2) return false;
        
        for (int i=0; i<myXCoordinates.length-1; i++){
            if (pointOnLine(inX, inY, myXCoordinates[i], myYCoordinates[i], myXCoordinates[i+1], myYCoordinates[i+1])) return true;
        }
        return false;
    }

    /** A LineString will intersect another line string if the two LineStrings cross at some point */
    public boolean intersectsLineString(LineString inLineString){
        
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length == 1) return new Point(myXCoordinates[0], myYCoordinates[0]).intersectsLineString(inLineString);
        
        double[] tempXCoordinates = inLineString.getXCoordinates();
        double[] tempYCoordinates = inLineString.getYCoordinates();
        if (tempXCoordinates == null) return false;
        if (tempXCoordinates.length == 0) return false;
        if (tempXCoordinates.length == 1) return new Point(tempXCoordinates[0], tempYCoordinates[0]).intersectsLineString(this);
        
        for (int i=0; i<myXCoordinates.length; i++){
            for (int j=0; j<tempXCoordinates.length; j++){
                if ((i>0) && (j>0)){
                    if (linesIntersect(myXCoordinates[i-1], myYCoordinates[i-1],
                    myXCoordinates[i], myYCoordinates[i],
                    tempXCoordinates[j-1], tempYCoordinates[j-1],
                    tempXCoordinates[j], tempYCoordinates[j])){
                        return true;
                    }
                }
                if (Point.pointsEqual(myXCoordinates[i], tempXCoordinates[j], myYCoordinates[i], tempYCoordinates[j])) return true;
            }
        }
        return false;
    }
    
    /** A LineString intersects a MultiLineString if any of the constituent LineStrings intersect this LineString. */
    public boolean intersectsMultiLineString(MultiLineString inMultiLineString){
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        
        LineString[] tempLines = inMultiLineString.getLines();
        for (int i=0; i<tempLines.length; i++){
            if (intersectsLineString(tempLines[i])) return true;
        }
        return false;
    }
    
    /** A LineString intersects a LinearRing if the LineString intersects the LinearRing, or if it is contained within the LinearRing. */
    public boolean intersectsLinearRing(LinearRing inLinearRing){
        
        // check for containing first.
        if (inLinearRing.contains(this)) return true;
        
        // check for intersections.
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length == 1) return new Point(myXCoordinates[0], myYCoordinates[0]).intersectsLinearRing(inLinearRing);
        
        double[] tempXCoordinates = inLinearRing.getXCoordinates();
        double[] tempYCoordinates = inLinearRing.getYCoordinates();
        if (tempXCoordinates == null) return false;
        if (tempXCoordinates.length == 0) return false;
        if (tempXCoordinates.length == 1) return new Point(tempXCoordinates[0], tempYCoordinates[0]).intersectsLineString(this);
        
        for (int i=0; i<myXCoordinates.length; i++){
            for (int j=0; j<tempXCoordinates.length; j++){
                if ((i>0) && (j>0)){
                    if (linesIntersect(myXCoordinates[i-1], myYCoordinates[i-1],
                    myXCoordinates[i], myYCoordinates[i],
                    tempXCoordinates[j-1], tempYCoordinates[j-1],
                    tempXCoordinates[j], tempYCoordinates[j])){
                        return true;
                    }
                }
                if (Point.pointsEqual(myXCoordinates[i], tempXCoordinates[j], myYCoordinates[i], tempYCoordinates[j])) return true;
            }
        }
        return false;
    }
    
    /** A LineString intersects a Polygon if it intersects any of the rings of the polygon, or if it is contained within the Polygon. */
    public boolean intersectsPolygon(Polygon inPolygon){
        if (inPolygon.contains(this)) return true;
        
        LinearRing tempLinearRing = inPolygon.getPosativeRing();
        if (intersectsLinearRing(tempLinearRing)) return true;
        
        LinearRing[] tempHoles = inPolygon.getHoles();
        if (tempHoles != null){
            for (int i=0; i<tempHoles.length; i++){
                if (intersectsLinearRing(tempHoles[i])) return true;
            }
        }
        return false;
    }
    
    /** A LineString intersects a MultiPolygon if it intersects any of the constituent Polygons */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        for (int i=0; i<tempPolygons.length; i++){
            if (intersectsPolygon(tempPolygons[i])) return true;
        }
        return false;
    }
    
    /** A LineString intersects a RasterShape if it intersects the envelope of the raster, or if it is contained within the envelope of the raster. */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        Polygon tempPolygon = inRasterShape.getEnvelope().getPolygon();
        if (intersectsPolygon(tempPolygon)) return true;
        return false;
    }
}
