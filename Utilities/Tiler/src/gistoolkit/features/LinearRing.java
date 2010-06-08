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

import java.util.*;
import gistoolkit.features.featureutils.*;
/**
 * A ring is a closed group of line segments.  These are usefull for implementing polygons.
 * The rules for rings are that they can not overlap (No Self Intersections), there can be no shared line segments, although single points can be shared.
 */
public class LinearRing extends Shape {
    
    /** The X Coordinates of all the points in the line string. */
    private double[] myXCoordinates;
    /** Return the array of X Coordinates.  This is private data, and should be used carefully. */
    public double[] getXCoordinates(){return myXCoordinates;}
    
    /** The Y Coordinates of all the points in the line string. */
    private double[] myYCoordinates;
    /** Return the array of Y Coordinates.  This is private data, and should be used carefully. */
    public double[] getYCoordinates(){return myYCoordinates;}
    
    /**
     * Create a new empty linear ring.
     */
    public LinearRing() {
        super();
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return LINEARRING;}
    
    /**
     * Create a new linear ring from the points.
     */
    public LinearRing(double[] inXs, double[] inYs) {
        
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
        
        // ensure that the shape is closed
        ensureClosed();
    }
    
    /**
     * Create a new linear ring from the points.
     */
    public LinearRing(Point[] inPoints) {
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
        // ensure that the shape is closed
        ensureClosed();
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
     * Adds a point to the LineString at the given point index.
     */
    public int add(double inX, double inY) {
        int tempIndex = getClosestIndex(inX, inY);
        if (add(tempIndex, inX, inY)) return tempIndex;
        return -1;
    }
    
    /**
     * Adds a point to the LineString at the given point index.
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
            if (inIndex >= myXCoordinates.length){
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
     * Removes the point at the given index from the linear ring.
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
     * Creates a copy of the Linear Ring
     * Creation date: (5/3/2001 8:45:46 AM)
     * @return gistoolkit.features.Shape
     */
    public Object clone(){
        return new LinearRing(myXCoordinates, myYCoordinates);
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     * Creation date: (4/18/2001 5:36:23 PM)
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
        // check for LineString
        if (inShape instanceof MultiLineString){
            return containsMultiLine((MultiLineString) inShape);
        }
        // check for linear rings
        if (inShape instanceof LinearRing){
            return containsLinearRing((LinearRing) inShape);
        }
        
        // check for polygon
        if (inShape instanceof Polygon){
            return containsPolygon((Polygon) inShape);
        }
        
        // check for multipolygon
        if (inShape instanceof MultiPolygon){
            return containsMultiPolygon((MultiPolygon) inShape);
        }
        return false;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsPoint(Point inPoint) {
        // check that the point is within the polygon.
        
        // on the boundary value
        boolean tempOnBoundary = true;
        if (inPoint == null) return false;
        return containsPoint(inPoint.getX(), inPoint.getY());
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsPoint(double inX, double inY) {
        // check that the point is within the polygon.
        
        // on the boundary value
        boolean tempOnBoundary = true;
        
        // number of times the polygon crosses the ray
        int crossings = 0;
        double x = inX;
        double y = inY;
        for (int i = 0; i < myXCoordinates.length - 1; i++) {
            if (((myXCoordinates[i] < x) && (x < myXCoordinates[i + 1]))
            ||  ((myXCoordinates[i] > x) && (x > myXCoordinates[i + 1]))) {
                double t =
                (x - myXCoordinates[i + 1])
                / (myXCoordinates[i] - myXCoordinates[i + 1]);
                double cy = t * myYCoordinates[i] + (1 - t) * myYCoordinates[i + 1];
                if (y == cy)
                    return (tempOnBoundary); // on the boundary is considered inside
                else
                    if (y > cy)
                        crossings++;
            }
            if ((myXCoordinates[i] == x && myYCoordinates[i] <= y)) {
                if (myYCoordinates[i] == y)
                    return (tempOnBoundary); // on the boundary
                if (myXCoordinates[i + 1] == x) {
                    if ((myYCoordinates[i] <= y && y <= myYCoordinates[i + 1])
                    || (myYCoordinates[i] >= y && y >= myYCoordinates[i + 1])) {
                        return (tempOnBoundary);
                    }
                }
                else
                    if (myXCoordinates[i + 1] > x)
                        crossings++;
                if (i>0) if (myXCoordinates[i - 1] > x)
                    crossings++;
            }
        }
        if (Math.IEEEremainder(crossings, 2.0) == 0)
            return false;
        return true;
    }
    
    /**
     * Return the Points which make up the linear ring.  Creates a new copy of all the points, so this is an expensive operation.
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
    
    /**
     * Return the Points which make up the linear ring.  The ring is closed with the last point being the same as the first.
     */
    public Point[] getRingPoints(){
        if (myXCoordinates == null) return new Point[0];
        if (myXCoordinates.length == 0) return new Point[0];
        Point[] tempPoints = new Point[myXCoordinates.length];
        for (int i=0; i<myXCoordinates.length; i++){
            tempPoints[i] = new Point(myXCoordinates[i], myYCoordinates[i]);
        }
        return tempPoints;
    }
    
    /**
     * checkorient tests the clockwise or counter-clockwise orientation of a polygon
     * returned values:
     *	>0 clockwise
     *	=0 if points are in a straight line
     *  <0 counter-clockwise
     * Creation date: (4/12/2001 5:36:54 PM)
     * @author:
     */
    private static double checkOrientation(Point[] inPoints) {
        
        // if an invalid polygon is sent in, return invalid polygon.
        if (inPoints == null) return 0;
        if (inPoints.length <3 ) return 0;
        
        int j, index, n1, n2;
        double z = 0;
        
        // Find the index of the point with the largest X value
        index = 0;
        for (j = 1; j < inPoints.length; j++) {
            if (inPoints[j].x > inPoints[index].x) {
                index = j;
            }
        }
        
        // Check the polygon path orientation (clockwise or counter-countwise)
        if (index == 0){
            n1 = inPoints.length-1;
        }
        else {
            n1 = index - 1;
        }
        
        // loop around the polygon checking each node.
        for (j = 1; j < inPoints.length - 1; j++) {
            
            // Start the second point at the point after the largest point,
            // if the index of the point is beyond the length of the polygon,
            // then loop back to 0 (The beginning) and increment from there.
            n2 = (index + j) % inPoints.length;
            
            // perform the dot product to determine the direction of the polygon
            z = (inPoints[n1].x - inPoints[index].x) * (inPoints[n2].y - inPoints[index].y);
            z = z - (inPoints[n2].x - inPoints[index].x) * (inPoints[n1].y - inPoints[index].y);
            if (Math.abs(z) > 10E-15)
                break;
            
            
            // Check for duplicate points
            if (Math.abs(Math.abs(inPoints[n1].x - inPoints[index].x) + Math.abs(inPoints[n1].y -inPoints[index].y)) < 10E-15){
                n1 = n1-1;
                if (n1 < 0) n1 = inPoints.length-1;
                
                // check for triangles with duplicate points
                if (n1 == n2) break;
                j--;
            }
        }
        return z;
    }
    
    /**
     * checkorient tests the clockwise or counter-clockwise orientation of a polygon
     * returned values:
     *	>0 clockwise
     *	=0 if points are in a straight line
     *  <0 counter-clockwise
     * Creation date: (4/12/2001 5:36:54 PM)
     * @author:
     */
    private static double checkOrientation(double[] inXCoordinates, double[] inYCoordinates) {
        
        // if an invalid polygon is sent in, return invalid polygon.
        int j, index, n1, n2;
        double z = 0;
        
        // Find the index of the point with the largest X value
        index = 0;
        for (j = 1; j < inXCoordinates.length; j++) {
            if (inXCoordinates[j] > inXCoordinates[index]) {
                index = j;
            }
        }
        
        // Check the polygon path orientation (clockwise or counter-countwise)
        if (index == 0){
            n1 = inXCoordinates.length-1;
        }
        else {
            n1 = index - 1;
        }
        
        // loop around the polygon checking each node.
        for (j = 1; j < inXCoordinates.length - 1; j++) {
            
            // Start the second point at the point after the largest point,
            // if the index of the point is beyond the length of the polygon,
            // then loop back to 0 (The beginning) and increment from there.
            n2 = (index + j) % inXCoordinates.length;
            
            // perform the dot product to determine the direction of the polygon
            z = (inXCoordinates[n1] - inXCoordinates[index]) * (inYCoordinates[n2] - inYCoordinates[index]);
            z = z - (inXCoordinates[n2] - inXCoordinates[index]) * (inYCoordinates[n1] - inYCoordinates[index]);
            if (Math.abs(z) > 10E-15)
                break;
            
            
            // Check for duplicate points
            if (Math.abs(Math.abs(inXCoordinates[n1] - inXCoordinates[index]) + Math.abs(inYCoordinates[n1] -inYCoordinates[index])) < 10E-15){
                n1 = n1-1;
                if (n1 < 0) n1 = inXCoordinates.length-1;
                
                // check for triangles with duplicate points
                if (n1 == n2) break;
                j--;
            }
        }
        return z;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsLine(LineString inLineString) {
        // degenerate conditions for both objects.
        if (inLineString == null) return false;
        if (inLineString.getPoints() == null) return false;
        if (inLineString.getPoints().length == 0) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length < 2) return false;
        if (inLineString.getXCoordinates().length < 2) return containsPoint(inLineString.getXCoordinates()[0], inLineString.getYCoordinates()[0]);
        
        
        // check point in polygon for the first point in the line for speed
        if (!this.containsPoint(inLineString.getXCoordinates()[0], inLineString.getYCoordinates()[0])) return false;
        
        // check for intersections of the line string
        double[] tempXCoordinates = inLineString.getXCoordinates();
        double[] tempYCoordinates = inLineString.getYCoordinates();
        for (int i=0; i<tempXCoordinates.length-1; i++){
            for (int j=0; j<myXCoordinates.length-1; j++){
                if (linesIntersect( tempXCoordinates[i], tempYCoordinates[i],
                tempXCoordinates[i+1], tempYCoordinates[i+1],
                myXCoordinates[j], myYCoordinates[j],
                myXCoordinates[j+1], myYCoordinates[j+1])){
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsLinearRing(LinearRing inRing) {
        // degenerate conditions for both objects.
        if (inRing == null) return false;
        if (inRing.getPoints() == null) return false;
        if (inRing.getPoints().length == 0) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length < 2) return false;
        if (inRing.getXCoordinates().length < 2) return containsPoint(inRing.getXCoordinates()[0], inRing.getYCoordinates()[0]);
        
        
        // check point in polygon for the first point in the line for speed
        if (!this.containsPoint(inRing.getXCoordinates()[0], inRing.getYCoordinates()[0])) return false;
        
        // check for intersections of the line string
        double[] tempXCoordinates = inRing.getXCoordinates();
        double[] tempYCoordinates = inRing.getYCoordinates();
        for (int i=0; i<tempXCoordinates.length-1; i++){
            for (int j=0; j<myXCoordinates.length-1; j++){
                if (linesIntersect( tempXCoordinates[i], tempYCoordinates[i],
                tempXCoordinates[i+1], tempYCoordinates[i+1],
                myXCoordinates[j], myYCoordinates[j],
                myXCoordinates[j+1], myYCoordinates[j+1])){
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsMultiLine(MultiLineString inMultiLineString) {
        // degenerate conditions for both objects.
        if (inMultiLineString == null) return false;
        if (inMultiLineString.getLines() == null) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length < 2) return false;
        
        LineString[] tempLineStrings = inMultiLineString.getLines();
        if (tempLineStrings.length == 0) return false;
        for (int i=0; i<tempLineStrings.length; i++){
            if (!containsLine(tempLineStrings[i])){
                return false;
            }
        }
        return true;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
    */
    public boolean containsMultiPolygon(MultiPolygon inMultiPolygon) {
        if (inMultiPolygon == null) return false;
        
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons == null) return false;
        
        for (int i=0; i<tempPolygons.length; i++){
            
            // skip null polygons
            if (tempPolygons[i] != null){
                if (tempPolygons[i].getPosativeRing() != null){
                    if (tempPolygons[i].getPosativeRing().getPoints() != null){
                        if (tempPolygons[i].getPosativeRing().getPoints().length > 2){
                            if (!containsPolygon(tempPolygons[i])){
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * For linear rings, since the shape is representing a simple polygon, if the shape is contained within the interior of the
     * ring, then it is said to be within the simple polygon.
     */
    public boolean containsPolygon(Polygon inPolygon) {
        if (inPolygon == null) return false;
        LinearRing tempRing = inPolygon.getPosativeRing();
        return contains(tempRing);
    }
    
    /**
     * Returns true if this is a valid ring, and it is oriented clockwise.
     */
    public boolean isClockwise(){
        if (checkOrientation(myXCoordinates, myYCoordinates) > 0) return true;
        return false;
    }
    
    /**
     * Exactly changes the orientation of the nodes in the ring
     */
    public void reorder(){
        if (myXCoordinates == null) return;
        if (myXCoordinates.length == 0) return;
        if (myXCoordinates.length == 1) return;
        
        // reorder the nodes.
        double tempX = 0;
        double tempY = 0;
        for (int i=0; i<myXCoordinates.length/2; i++){
            tempX = myXCoordinates[i];
            tempY = myYCoordinates[i];
            
            myXCoordinates[i] = myXCoordinates[myXCoordinates.length-i-1];
            myYCoordinates[i] = myYCoordinates[myYCoordinates.length-i-1];
            
            myXCoordinates[myXCoordinates.length-i-1] = tempX;
            myYCoordinates[myYCoordinates.length-i-1] = tempY;
        }
    }
    
    /**
     * Returns the string representation of this Ring.
    */
    public String toString() {
        if (myXCoordinates == null) return "LinearRing No Points";
        return "LinearRing NumPoints="+myXCoordinates.length + " Envelope="+getEnvelope();
    }
    
    /** Ensure that the ring is closed */
    public void ensureClosed(){
        
        // if the first and last points are not the same, then ensure that they are the same.
        if (myXCoordinates == null) return;
        if (myXCoordinates.length < 3) return;
        
        // index of the last element
        int tempLastPoint = myXCoordinates.length-1;
        if (Point.pointsEqual(myXCoordinates[0], myYCoordinates[0], myXCoordinates[tempLastPoint], myYCoordinates[tempLastPoint])) return;
        
        // add the new point at the end.
        add(myXCoordinates.length, myXCoordinates[0], myYCoordinates[0]);
    }
    
    /** There is no WKT for a linear ring */
    public String getWKT(){
        return null;
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        if (myXCoordinates == null) return;
        for (int i=0; i<myXCoordinates.length; i++){
            myXCoordinates[i] = myXCoordinates[i] + inXDistance;
            myYCoordinates[i] = myYCoordinates[i] + inYDistance;
        }
        calculateEnvelope();
    }
    
    /** Class to represent a point of intersection of two lines.*/
    private class IntersectionPoint extends Point {
        Point myLine1PointA;
        Point myLine1PointB;
        Point myLine2PointA;
        Point myLine2PointB;
        double myDistanceFromLine1PointA;
        
        /** constructor */
        public IntersectionPoint(Point inPoint, Point inLine1PointA, Point inLine1PointB, Point inLine2PointA, Point inLine2PointB){
            super(inPoint.getX(), inPoint.getY());
            myLine1PointA = inLine1PointA;
            myLine1PointB= inLine1PointB;
            myLine2PointA = inLine2PointA;
            myLine2PointB = inLine2PointB;
            myDistanceFromLine1PointA = distance(this, myLine1PointA);
        }
        
        /** Return the point on line 2 to the left of line 2 */
        public Point getLeftOnePoint(){
            // if this is a vertical line
            if (myLine1PointB.x == myLine1PointA.x) {
                // if we are going up
                if (myLine1PointA.y < myLine1PointB.y){
                    if (myLine2PointA.x < myLine1PointA.x) return myLine2PointA;
                    else return myLine2PointB;
                }
                // The operation is flipped if we are going down.
                else {
                    if (myLine2PointA.x < myLine1PointA.x) return myLine2PointB;
                    else return myLine2PointA;
                }
            }
            // if this is a horizontal line
            if (myLine1PointB.y == myLine1PointA.y) {
                // if we are going right
                if (myLine1PointA.x < myLine1PointB.x){
                    if (myLine2PointA.y < myLine1PointA.y) return myLine2PointB;
                    else return myLine2PointA;
                }
                // The operation is flipped if we are going down.
                else {
                    if (myLine2PointA.y < myLine1PointA.y) return myLine2PointA;
                    else return myLine2PointB;
                }
            }
            
            // calculate the slope of the line 1.
            double slope = (myLine1PointB.y - myLine1PointA.y) * (myLine1PointB.x-myLine1PointB.x);
            
            // calculate the intercept of the line 1.
            double intercept = myLine1PointA.y - slope*myLine1PointA.x;
            
            double intercept2 = myLine2PointA.y - slope*myLine2PointA.x;
            if (slope > 0){
                if (intercept < intercept2) return myLine2PointA;
                else return myLine2PointB;
            }
            else{
                if (intercept < intercept2) return myLine2PointB;
                else return myLine2PointA;
            }
        }
        
        // return the point on line one to the left of line 2.
        public Point getLeftTwoPoint(){
            // if this is a vertical line
            if (myLine2PointB.x == myLine2PointA.x) {
                // if we are going up
                if (myLine2PointA.y < myLine2PointB.y){
                    if (myLine1PointA.x < myLine2PointA.x) return myLine1PointA;
                    else return myLine1PointB;
                }
                // The operation is flipped if we are going down.
                else {
                    if (myLine1PointA.x < myLine2PointA.x) return myLine1PointB;
                    else return myLine1PointA;
                }
            }
            // if this is a horizontal line
            if (myLine2PointB.y == myLine2PointA.y) {
                // if we are going right
                if (myLine2PointA.x < myLine2PointB.x){
                    if (myLine1PointA.y < myLine2PointA.y) return myLine1PointB;
                    else return myLine1PointA;
                }
                // The operation is flipped if we are going down.
                else {
                    if (myLine1PointA.y < myLine2PointA.y) return myLine1PointA;
                    else return myLine1PointB;
                }
            }
            
            // calculate the slope of the line 2.
            double slope = (myLine2PointB.y - myLine2PointA.y) / (myLine2PointB.x-myLine2PointB.x);
            
            // calculate the intercept of the line 1.
            double intercept = myLine2PointA.y - slope*myLine2PointA.x;
            
            // return the point
            double intercept2 = myLine1PointA.y - slope*myLine1PointA.x;
            if (slope > 0){
                if (intercept < intercept2) return myLine1PointA;
                else return myLine1PointB;
            }
            else{
                if (intercept < intercept2) return myLine1PointB;
                else return myLine1PointA;
            }
        }
    };
    
    /** Create the union of the two objects */
    public Shape union(Shape inShape) {
        if (inShape == null) return null;
        if (inShape instanceof LinearRing){
            // if the two lines intersect
            LinearRing tempLinearRing = (LinearRing) inShape;
            if (overlaps(tempLinearRing)){
                // ensure that the shapes are oriented properly
                if (!isClockwise()) reorder();
                if (!tempLinearRing.isClockwise()) tempLinearRing.reorder();
                
                // create some storage
                ArrayList tempPointList1 = new ArrayList();
                ArrayList tempPointList2 = new ArrayList();
                ArrayList tempIntersects = new ArrayList();
                ArrayList tempArrayList = new ArrayList();
                
                // retrieve the necissary points
                Point[] tempPoints1 = getRingPoints();
                Point[] tempPoints2 = tempLinearRing.getRingPoints();
                for (int i=0; i<tempPoints2.length; i++) tempPointList2.add(tempPoints2[i]);
                
                // Add the intersections of the two rings to both rings.
                boolean tempFoundIntersection = false;
                for (int i=1; i<tempPoints1.length; i++){
                    tempArrayList.clear();
                    tempPointList1.add(tempPoints1[i-1]);
                    if (tempFoundIntersection == true){
                        tempPoints2 = (Point[]) tempPointList2.toArray();
                    }
                    tempFoundIntersection = false;
                    for (int j=1; j<tempPoints2.length; j++){
                        Point tempPoint = getLinesIntersect(tempPoints1[i-1], tempPoints1[i], tempPoints2[j-1], tempPoints2[j]);
                        if (tempPoint != null){
                            IntersectionPoint tempIntPoint = new IntersectionPoint(tempPoint, tempPoints1[i-1], tempPoints1[i], tempPoints2[j-1], tempPoints2[j]);
                            tempFoundIntersection = true;
                            boolean tempFound = false;
                            for (int k=0; k<tempArrayList.size(); k++){
                                IntersectionPoint tempIntPoint2 = (IntersectionPoint) tempArrayList.get(k);
                                if (tempIntPoint2.myDistanceFromLine1PointA > tempIntPoint.myDistanceFromLine1PointA){
                                    tempArrayList.add(k,tempIntPoint);
                                    tempFound = true;
                                }
                            }
                            if (!tempFound) tempArrayList.add(tempIntPoint);
                            
                            tempIntersects.add(tempIntPoint);
                            int tempIndex = tempPointList2.indexOf(tempPoints2[j]);
                            tempPointList2.add(tempIndex, tempIntPoint);
                        }
                    }
                    for (int j=0; j<tempArrayList.size(); j++){
                        IntersectionPoint tempIntPoint = (IntersectionPoint) tempArrayList.get(j);
                        if (j > 0) tempIntPoint.myLine2PointA = (IntersectionPoint) tempArrayList.get(j-1);
                        if (j+1 < (tempArrayList.size())) tempIntPoint.myLine2PointB = (IntersectionPoint) tempArrayList.get(j+1);
                        tempPointList1.add(tempIntPoint);
                    }
                }
                
                if (tempIntersects.size() > 0){
                    // Start at an intersection point and begin making circles.
                    ArrayList tempRing = new ArrayList();
                    ArrayList tempRings = new ArrayList();
                    while (tempIntersects.size() > 0){
                        tempRing.clear();
                        boolean tempPosDir = true;
                        
                        // start with any intersection point
                        IntersectionPoint tempStartPoint = (IntersectionPoint) tempIntersects.get(0);
                        tempIntersects.remove(tempStartPoint);
                        tempRing.add(tempStartPoint);
                        
                        // the next point is the one that is to the left of this line.
                        Point tempNextPoint = null;
                        tempNextPoint = tempStartPoint.getLeftOnePoint();
                        if (tempStartPoint.myLine2PointB == tempNextPoint) tempPosDir = true;
                        else tempPosDir = false;
                        boolean tempPoly1 = false;
                        
                        while (tempNextPoint != tempStartPoint){
                            tempRing.add(tempNextPoint);
                            
                            // move to the left of all intersection points.
                            if (tempNextPoint instanceof IntersectionPoint){
                                tempIntersects.remove(tempNextPoint);
                                IntersectionPoint tempOldPoint = (IntersectionPoint) tempNextPoint;
                                if (tempPoly1){
                                    tempNextPoint = tempOldPoint.getLeftOnePoint();
                                    if (tempOldPoint.myLine2PointB == tempNextPoint) tempPosDir = true;
                                    else tempPosDir = false;
                                    tempPoly1 = false;
                                }
                                else{
                                    tempNextPoint = tempOldPoint.getLeftTwoPoint();
                                    if (tempOldPoint.myLine1PointB == tempNextPoint) tempPosDir = true;
                                    else tempPosDir = false;
                                    tempPoly1 = true;
                                }
                            }
                            
                            // follow the ring around in the given direction.
                            else{
                                if (tempPoly1){
                                    int tempIndex = tempPointList1.indexOf(tempNextPoint);
                                    if (tempPosDir){
                                        tempIndex = tempIndex + 1;
                                        if (tempIndex == tempPointList1.size()) tempIndex = 1;
                                        tempNextPoint = (Point) tempPointList1.get(tempIndex);
                                    }
                                    else{
                                        tempIndex = tempIndex -1;
                                        if (tempIndex < 0) tempIndex = tempPointList1.size()-2;
                                        tempNextPoint = (Point) tempPointList2.get(tempIndex);
                                    }
                                }
                                else{
                                    int tempIndex = tempPointList2.indexOf(tempNextPoint);
                                    if (tempPosDir){
                                        tempIndex = tempIndex + 1;
                                        if (tempIndex == tempPointList2.size()) tempIndex = 1;
                                        tempNextPoint = (Point) tempPointList1.get(tempIndex);
                                    }
                                    else{
                                        tempIndex = tempIndex -1;
                                        if (tempIndex < 0) tempIndex = tempPointList2.size()-2;
                                        tempNextPoint = (Point) tempPointList2.get(tempIndex);
                                    }
                                }
                            }
                        }
                        // add the last point, this is a duplicate
                        tempRing.add(tempNextPoint);
                        
                        // create the ring
                        Point[] tempRingPoints = (Point[]) tempRing.toArray();
                        tempRings.add(new LinearRing(tempRingPoints));
                    }
                    
                    // order the rings
                    for(int i=1; i<tempRings.size(); i++){
                        LinearRing tempRing1 = (LinearRing) (tempRings.get(i-1));
                        LinearRing tempRing2 = (LinearRing) (tempRings.get(i));
                        if (tempRing2.contains(tempRing1)) {
                            tempRings.remove(tempRing2);
                            tempRings.add(0, tempRing2);
                        }
                    }
                    
                    // create the polygon.
                    LinearRing tempPosativeRing = (LinearRing) tempRings.get(0);
                    LinearRing tempHoles[] = (LinearRing[]) tempRings.toArray();
                    return new Polygon(tempPosativeRing, tempHoles);
                }
                else{
                    // the two shapes do not intersect.
                    // if this shape contains the other one, then return this shape
                    if (contains(tempLinearRing)) return this;
                    
                    // if the other shape contains this one, then return the other shape.
                    else return tempLinearRing;
                    
                }
            }
            else{
                Polygon[] tempPolygons = {
                    new Polygon(this),
                    new Polygon(tempLinearRing)
                };
                return new MultiPolygon(tempPolygons);
            }
        }
        else {
            return null;
        }
    }
    
    /**
     * Checks if the shape overlaps this shape, and returns true if it does.
     */
    public boolean overlaps(Shape inShape){
        // if the inputs shape is null, then these two shapes cannot overlap
        if (inShape == null) return false;
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length < 2) return false;
        
        // if the envelopes do not overlap, then the shapes cannot, so return false
        {
            Envelope tempMyEnvelope = getEnvelope();
            if (tempMyEnvelope == null) return false;
            Envelope tempInEnvelope = inShape.getEnvelope();
            if (tempInEnvelope == null) return false;
            if (!tempMyEnvelope.overlaps(tempInEnvelope)) return false;
        }
        
        // if the shape is a point, then check if this shape contains the point
        if (inShape instanceof Point) return containsPoint((Point) inShape);
        
        // if the shape is a multi point then check if this shape contains any of the points
        if (inShape instanceof MultiPoint){
            MultiPoint tempMultiPoint = (MultiPoint) inShape;
            Point[] tempPoints = tempMultiPoint.getPoints();
            for (int i=0; i<tempPoints.length; i++){
                if (overlaps(tempPoints[i])) return true;
            }
            return false;
        }
        
        // if the shape coming in is a line string, then check for an intersection
        if (inShape instanceof LineString){
            LineString tempLineString = (LineString) inShape;
            double[] tempXCoordinates = tempLineString.getXCoordinates();
            double[] tempYCoordinates = tempLineString.getYCoordinates();
            if (tempXCoordinates.length == 0) return false;
            if (tempXCoordinates.length == 1) return containsPoint(tempXCoordinates[0], tempYCoordinates[0]);
            
            // loop through all the elements of the line string to check for overlaps.
            for (int i=1; i<tempXCoordinates.length; i++){
                for (int j=1; j<myXCoordinates.length; j++){
                    if (linesIntersect(tempXCoordinates[i-1], tempYCoordinates[i-1],
                    tempXCoordinates[i], tempYCoordinates[i],
                    myXCoordinates[j-1], myYCoordinates[j-1],
                    myXCoordinates[j], myYCoordinates[j])){
                        return true;
                    }
                }
            }
            
            // there are no Intersections, so if the line is completely within return true, else return false
            return containsPoint(tempXCoordinates[0], tempYCoordinates[0]);
        }
        
        // if the shape coming in is a multi line string, then check each line string.
        if (inShape instanceof MultiLineString){
            MultiLineString tempMultiLineString = (MultiLineString) inShape;
            
            LineString[] tempLineStrings = tempMultiLineString.getLines();
            if (tempLineStrings == null) return false;
            for (int i=0; i<tempLineStrings.length; i++){
                if (overlaps(tempLineStrings[i])) return true;
            }
            return false;
        }
        
        // if the shape coming in is a linear ring, then check for overlaps.
        if (inShape instanceof LinearRing){
            LinearRing tempLinearRing = (LinearRing) inShape;
            double[] tempXCoordinates = tempLinearRing.getXCoordinates();
            double[] tempYCoordinates = tempLinearRing.getYCoordinates();
            if (tempXCoordinates.length == 0) return false;
            if (tempXCoordinates.length == 1) return containsPoint(tempXCoordinates[0], tempYCoordinates[0]);
            
            // loop through all the elements of the line string to check for overlaps.
            for (int i=1; i<tempXCoordinates.length; i++){
                for (int j=1; j<myXCoordinates.length; j++){
                    if (linesIntersect(tempXCoordinates[i-1], tempYCoordinates[i-1],
                    tempXCoordinates[i], tempYCoordinates[i],
                    myXCoordinates[j-1], myYCoordinates[j-1],
                    myXCoordinates[j], myYCoordinates[j])){
                        return true;
                    }
                }
            }
            
            // there are no Intersections, so if the line is completely within return true, else return false
            if (containsPoint(tempXCoordinates[0], tempYCoordinates[0])) return true;
            
            // there are no Intersections, so if the LinearRing is completely within return true, else return false
            if (tempLinearRing.containsPoint(myXCoordinates[0], myYCoordinates[0])) return true;
            
            return false;
        }
        
        // if the shape coming in is a Polygon, then check for overlap
        if (inShape instanceof Polygon){
            Polygon tempPolygon = (Polygon) inShape;
            LinearRing tempLinearRing = tempPolygon.getPosativeRing();
            if ( overlaps(tempLinearRing) ) {
                LinearRing[] tempHoles = tempPolygon.getHoles();
                if (tempHoles != null){
                    for (int i=0; i<tempHoles.length; i++){
                        if (overlaps(tempHoles[i])) {
                            if (!tempHoles[i].contains(this)){
                                return true;
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }
        
        // if the shape coming in is a multi polygon, then check each of it's polygons
        if (inShape instanceof MultiPolygon){
            MultiPolygon tempMultiPolygon = (MultiPolygon) inShape;
            Polygon[] tempPolygons = tempMultiPolygon.getPolygons();
            if (tempPolygons != null){
                for (int i=0; i<tempPolygons.length; i++){
                    if (overlaps(tempPolygons[i])) return true;
                }
            }
            return false;
        }
        
        // if the shape coming in is a rastor image, then check for intersections with the envelope
        if (inShape instanceof RasterShape){
            RasterShape tempRasterShape = (RasterShape) inShape;
            Envelope tempEnvelope = tempRasterShape.getEnvelope();
            Point[] tempPoints = tempEnvelope.getPolygon().getPoints();
            
            // loop through all the elements of the line string to check for overlaps.
            for (int i=1; i<tempPoints.length; i++){
                for (int j=1; j<myXCoordinates.length; j++){
                    if (linesIntersect(tempPoints[i-1].getX(), tempPoints[i-1].getY(),
                    tempPoints[i].getX(), tempPoints[i].getY(),
                    myXCoordinates[j-1], myYCoordinates[j-1],
                    myXCoordinates[j], myYCoordinates[j])){
                        return true;
                    }
                }
            }
            
            // there are no Intersections, so if the raster is completely within return true, else return false
            if (contains(tempPoints[0])) return true;
            LinearRing tempLinearRing = new LinearRing(tempPoints);
            // check the condition where the linear ring is contained within the raster.
            if (tempLinearRing.containsPoint(myXCoordinates[0], myYCoordinates[0])) return true;
            return false;
        }
        
        // this is a shape I do not know about, so return false.
        return false;
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
        
        // if the envelopes do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return intersectsPoint((Point) inShape);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsLinearRing(this);
        
        // LineStrings
        if (inShape instanceof LineString) return ((LineString) inShape).intersectsLinearRing(this);
        
        // MultiLineStrings
        if (inShape instanceof MultiLineString) return ((MultiLineString) inShape).intersectsLinearRing(this);
        
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
     * A Linear Ring will intersect a point if the point falls on the line, or within the boundary of the linear ring.
     * Calls containsPoint().
     */
    public boolean intersectsPoint(Point inPoint) {
        return containsPoint(inPoint.getX(), inPoint.getY());
    }
    
    /**
     * A Linear Ring will intersect a point if the point falls on the line, within the boundary of the linear ring.
     * Calls containsPoint().
     */
    public boolean intersectsPoint(double inX, double inY) {
        return containsPoint(inX, inY);
    }
    
    /** A LinearRing intersects another LinearRing if the boundaries of the LinearRing cross, or one LinearRing is entirely within the other */
    public boolean intersectsLinearRing(LinearRing inLinearRing){
        // check for containment
        if (inLinearRing.contains(this)) return true;
        if (contains(inLinearRing)) return true;
        
        // check for intersections.
        if (myXCoordinates == null) return false;
        if (myXCoordinates.length == 0) return false;
        if (myXCoordinates.length == 1) return new Point(myXCoordinates[0], myYCoordinates[0]).intersectsLinearRing(inLinearRing);
        
        double[] tempXCoordinates = inLinearRing.getXCoordinates();
        double[] tempYCoordinates = inLinearRing.getYCoordinates();
        if (tempXCoordinates == null) return false;
        if (tempXCoordinates.length == 0) return false;
        if (tempXCoordinates.length == 1) return new Point(tempXCoordinates[0], tempYCoordinates[0]).intersectsLinearRing(this);
        
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
                if (Point.pointsEqual(myXCoordinates[i], myYCoordinates[i], tempXCoordinates[j], tempYCoordinates[j])) return true;
            }
        }
        return false;
    }
    
    /** A LinearRing intersects a Polygon if it intersects any of the rings of the polygon, or if it is contained within the posative ring of the polygon. */
    public boolean intersectsPolygon(Polygon inPolygon){
        LinearRing tempPosativeRing = inPolygon.getPosativeRing();
        if (tempPosativeRing != null){
            if (tempPosativeRing.contains(this)) return true;
            
            // look for crosses
            if (intersectsLinearRing(tempPosativeRing)) return true;
        }
        
        LinearRing[] tempHoles = inPolygon.getHoles();
        if (tempHoles != null){
            for (int i=0; i<tempHoles.length; i++){
                if (intersectsLinearRing(tempHoles[i])) return true;
            }
        }
        return false;
    }
    
    /** A LinearRing intersects a MultiPolygon if it intersects any of the constituent Polygons contained within the MultiPolygon. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons != null){
            for (int i=0; i<tempPolygons.length; i++){
                if (intersectsPolygon(tempPolygons[i])) return true;
            }
        }
        return false;
    }
    
    /** A LinearRing intersects a RasterShape if it intersects the envelope of the raster, or if it is contained within the raster. */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        Polygon tempPolygon = inRasterShape.getEnvelope().getPolygon();
        if (intersectsPolygon(tempPolygon)) return true;
        return false;
    }
}