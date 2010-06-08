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
 * Represents a collection of Line Strings.
 */
public class MultiLineString extends Shape {
    /**
     * List of line strings
     */
    protected LineString[] myLines;
    
    /**
     * Create a new MultiLineString
     */
    public MultiLineString() {
    }
    
    /**
     * Creates a MultiLineString from the lines
     */
    public MultiLineString(LineString[] inLines) {
        myLines = inLines;
    }
    
    /**
     * Create a MultiLineString from the given line
     */
    public MultiLineString(LineString inLines) {
        if (inLines == null) {
            myLines = new LineString[0];
            return;
        }
        myLines = new LineString[1];
        myLines[0] = inLines;
    }
    
    /** Return the type of shape this is */
    public String getShapeType(){return MULTILINESTRING;}
    
    /**
     * Creates a copy of the MultiLineString
     */
    public Object clone(){
        if (myLines != null){
            LineString[] tempLines = new LineString[myLines.length];
            for (int i=0; i<myLines.length; i++){
                tempLines[i] = (LineString) myLines[i].clone();
            }
            MultiLineString tempMultiLineString = new MultiLineString(tempLines);
            return tempMultiLineString;
        }
        return new MultiLineString();
    }
    
    /** Return the number of points in this shape. */
    public int getNumPoints(){
        int tempNumPoints = 0;
        if (myLines != null){
            for (int i=0; i<myLines.length; i++){
                tempNumPoints = tempNumPoints + myLines[i].getNumPoints();
            }
        }
        return tempNumPoints;
    };
    
    /** Get the point at the given index. */
    public Point getPoint(int inIndex){
        Point tempPoint = null;
        if ((myLines != null)&&(inIndex > -1)){
            int tempIndex = inIndex;
            for (int i=0; i<myLines.length; i++){
                if (myLines[i].getNumPoints() <= tempIndex){
                    tempIndex = tempIndex - myLines[i].getNumPoints();
                }
                else{
                    tempPoint = myLines[i].getPoint(tempIndex);
                    break;
                }
            }
        }
        return tempPoint;
    }
    /** Set the point at the given index. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){
        if ((myLines != null)&&(inIndex > -1)){
            int tempIndex = inIndex;
            for (int i=0; i<myLines.length; i++){
                if (myLines[i].getNumPoints() <= tempIndex){
                    tempIndex = tempIndex - myLines[i].getNumPoints();
                }
                else{
                    myLines[i].setPoint(tempIndex, inXCoordinate, inYCoordinate);
                    break;
                }
            }
        }
    }
    
    /** Add the a point to this shape.  Returns the location where the point was added.  Returns -1 if it could not be added. */
    public int add(double inX, double inY) {
        int tempIndex = getClosestIndex(inX, inY);
        if (add(tempIndex+1, inX, inY)) return tempIndex+1;
        return -1;
    }
    
    /** Adds the point to this shape at the given index. */
    public boolean add(int inIndex, double inX, double inY){
        int tempIndex = inIndex;
        boolean tempReturn = false;
        if (tempIndex > -1){
            // check the Negative Rings.
            LineString[] tempLines = getLines();
            for (int i=0; i<tempLines.length; i++){
                if (tempLines[i].getNumPoints() < tempIndex){
                    tempIndex = tempIndex - tempLines[i].getNumPoints();
                }
                else{
                    tempReturn = tempLines[i].add(tempIndex, inX, inY);
                    break;
                }
            }
        }
        return tempReturn;
    }
    
    /** Delete the given point from the polygon. */
    public boolean remove(int inIndex){
        // find the point closes to this click location.
        int tempIndex = inIndex;
        boolean tempReturn = false;
        if (tempIndex > -1){
            
            // check the Negative Rings.
            LineString[] tempLines = getLines();
            for (int i=0; i<tempLines.length; i++){
                if (tempLines[i].getNumPoints() <= tempIndex){
                    tempIndex = tempIndex - tempLines[i].getNumPoints();
                }
                else{
                    tempReturn = tempLines[i].remove(tempIndex);
                    break;
                }
            }
        }
        return tempReturn;
    }
    
    /**
     * Calculates the envelope based on the data in the individual lines.
     */
    public void calculateEnvelope() {
        myEnvelope = null;
    }
    public Envelope getEnvelope(){
        if (myEnvelope == null){
            
            
            // loop through the lines calculating the max envelope.
            if (myLines != null){
                EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
                for (int i=0; i<myLines.length; i++){
                    
                    // add the envelope of this line to the current set for the multi line.
                    if (myLines[i] != null){
                        Point[] tempPoints = myLines[i].getPoints();
                        for (int j=0; j<tempPoints.length; j++){
                            tempEnvelopeBuffer.expandToInclude(tempPoints[j]);
                        }
                    }
                }
                myEnvelope = tempEnvelopeBuffer.getEnvelope();
            }
        }
        return myEnvelope;
    }
    /**
     * Retrieves the array of lines contained in this multi Line String.
     */
    public LineString[] getLines(){
        return myLines;
    }
    
    /** Return the points that comprise the object */
    public Point[] getPoints(){
        if (myLines == null) return new Point[0];
        
        // calculate how many points I need
        int tempNumPoints = 0;
        if (myLines != null){
            for (int i=0; i<myLines.length; i++){
                tempNumPoints = tempNumPoints + myLines[i].getPoints().length;
            }
        }
        
        // create the array of points
        Point[] tempPoints = new Point[tempNumPoints];
        int tempIndex = 0;
        if (myLines != null){
            for (int i=0; i<myLines.length; i++){
                Point[] tempPoints2 = myLines[i].getPoints();
                for (int j=0; j<tempPoints2.length; j++){
                    tempPoints[tempIndex] = tempPoints2[j];
                    tempIndex++;
                }
            }
        }
        return tempPoints;
    }
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public String getWKT(){
        StringBuffer sb = new StringBuffer("multilinestring(");
        if (myLines != null){
            for (int i=0; i<myLines.length; i++){
                if (i>0)sb.append(",");
                sb.append("(");
                Point[] tempPoints = myLines[i].getPoints();
                for (int j=0; j<tempPoints.length; j++){
                    if (j>0) sb.append(",");
                    sb.append(tempPoints[j].getX());
                    sb.append(" ");
                    sb.append(tempPoints[j].getY());
                }
                sb.append(")");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Return the point in the MultiLineString that is the closest to this point.
     */
    public Point getClosestPoint(double inX, double inY) {
        Point tempPoint = null;
        int tempIndex = getClosestIndex(inX, inY);
        if (tempIndex > -1){
            LineString[] tempLineStrings = getLines();
            for (int i=0; i<tempLineStrings.length; i++){
                if (tempIndex >= tempLineStrings[i].getNumPoints()){
                    tempIndex = tempIndex - tempLineStrings[i].getNumPoints();
                }
                else{
                    tempPoint = tempLineStrings[i].getPoint(tempIndex);
                    break;
                }
            }
        }
        return tempPoint;
    }

    /**
     * Return the point in the MultiLineString that is the closest to this point.
     */
    public int getClosestIndex(double inX, double inY) {
        LineString[] tempLineStrings = getLines();
        if (tempLineStrings == null) return -1;
        
        // loop through the points finding the closest line segment.
        double tempMinDistance = Double.MAX_VALUE;
        int tempPointIndex = -1;
        int tempLineIndex = -1;
        
        for (int i=0; i<tempLineStrings.length; i++){
            int tempIndex = tempLineStrings[i].getClosestIndex(inX, inY);
            Point tempPoint = tempLineStrings[i].getPoint(tempIndex);
            double tempDistance = getDistance(tempPoint.getX(), tempPoint.getY(), inX, inY);
            if (tempDistance < tempMinDistance){
                tempMinDistance = tempDistance;
                tempLineIndex = i;
                tempPointIndex = tempIndex;
            }
        }
        
        // if a point was found
        int tempReturnIndex = -1;
        if (tempPointIndex != -1){
            tempReturnIndex = 0;
            for (int i=0; i<tempLineIndex; i++){
                tempReturnIndex = tempReturnIndex + tempLineStrings[i].getNumPoints();
            }
            tempReturnIndex = tempReturnIndex + tempPointIndex;
        }
        return tempReturnIndex;
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        for (int i=0; i<myLines.length; i++){
            myLines[i].translate(inXDistance, inYDistance);
        }
        calculateEnvelope();
    }
    
    /** Add a line string to this MultiLineString at the given location */
    public void addLineString(int inIndex, LineString inLineString){
        // check for valid data and special cases.
        if (inLineString == null) return;
        if (myLines == null){
            myLines = new LineString[1];
            myLines[0] = inLineString;
            return;
        }
        
        // create the new points array.
        LineString[] tempLines = new LineString[myLines.length+1];
        for (int i=0; i<myLines.length; i++){
            if (i < inIndex){
                tempLines[i] = myLines[i];
            }
            if (i == inIndex){
                tempLines[i] = inLineString;
            }
            if (i >= inIndex){
                tempLines[i+1] = myLines[i];
            }
        }
        if (inIndex >= myLines.length) tempLines[myLines.length] = inLineString;
        myLines = tempLines;
        // calculate the new envelope
        calculateEnvelope();
    }
    
    /** Get the distance from this shape to the given point */
    public double getDistanceToPoint(double inX, double inY){
        // special conditions
        if (myLines == null) return Double.NaN;
        if (myLines.length == 0) return Double.NaN;
        double tempMinDistance = Double.MAX_VALUE;
        
        // loop through all the lines and return the distance.
        for (int i=0; i<myLines.length; i++){
            double tempDistance = myLines[i].getDistanceToPoint(inX, inY);
            if (tempDistance < tempMinDistance) tempMinDistance = tempDistance;
        }
        
        return tempMinDistance;
    }
    
    /** Determines if the two shapes intersect  */
    public boolean intersects(Shape inShape) {
        // if the shape sent in is null, then return false
        if (inShape == null) return false;
        
        // if the envelopes do not overlap, then the shapes cannot
        if (!getEnvelope().intersects(inShape.getEnvelope())) return false;
        
        // Points
        if (inShape instanceof Point) return ((Point) inShape).intersectsMultiLineString(this);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsMultiLineString(this);
        
        // LineStrings
        if (inShape instanceof LineString) return ((LineString) inShape).intersectsMultiLineString(this);
        
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
     * A MultiLineString will intersect another MultiLineString if any of the constituent LinesStrings within
     * the MultiLineStrings intersect.
     */
    public boolean intersectsMultiLineString(MultiLineString inMultiLineString){
        if (myLines == null) return false;
        LineString[] tempLines = inMultiLineString.getLines();
        if (tempLines == null) return false;
        
        for(int i=0; i<myLines.length; i++){
            for (int j=0; j<tempLines.length; j++){
                if (myLines[i].intersectsLineString(tempLines[j])) return true;
            }
        }
        return false;
    }
    
    /** A MultiLineString intersects a LinearRing if any one of it's constituent LineStrings intersect the Linear ring. */
    public boolean intersectsLinearRing(LinearRing inLinearRing){
        if (myLines == null) return false;
        
        for(int i=0; i<myLines.length; i++){
            for (int j=0; j<myLines.length; j++){
                if (myLines[i].intersectsLinearRing(inLinearRing)) return true;
            }
        }
        return false;
    }
    
    /** A MultiLineString intersects a Polygon if any of it's constituent LineStrings intersect the Polygon. */
    public boolean intersectsPolygon(Polygon inPolygon){
        if (myLines == null) return false;
        LinearRing tempLinearRing = inPolygon.getPosativeRing();
        if (tempLinearRing != null){
            if (intersectsLinearRing(tempLinearRing)) return true;
        }
        
        LinearRing[] tempHoles = inPolygon.getHoles();
        if (tempHoles != null){
            for (int i=0; i<tempHoles.length; i++){
                if (tempHoles[i].contains(this)) return false;
                if (intersectsLinearRing(tempHoles[i])) return true;
            }
        }
        return false;
    }
    
    /** A MultiLineString intersects a MultiPolygon if the any one of the MultiLineString intersects any of the constituent Polygons of the MultiPolygon. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons != null){
            for (int i=0; i<tempPolygons.length; i++){
                if (intersectsPolygon(tempPolygons[i])) return true;
            }
        }
        return false;
    }
    
    /** A MultiLineString intersects a RasterShape if any of the strings intersect the envelope or are contained within the envelope of the raster shape. */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        Polygon tempPolygon = inRasterShape.getEnvelope().getPolygon();
        if (intersectsPolygon(tempPolygon)) return true;
        return false;
    }
    
}
