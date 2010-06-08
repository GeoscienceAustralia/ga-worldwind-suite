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
 * Represents a collection of Polygons.
 */
public class MultiPolygon extends Shape {
    /**
     * List of polygons
     */
    private Polygon[] myPolygons;
    
    /**
     * Creates a new MultiPolygon
     */
    public MultiPolygon() {
    }
    
    /**
     * Creates a MultiPolygon from the Polygons
     */
    public MultiPolygon(Polygon[] inPolygons) {
        myPolygons = inPolygons;
        calculateEnvelope();
    }
    
    /**
     * Creates a MultiPolygon from the Polygons and the envelope
     */
    public MultiPolygon(Polygon[] inPolygons, Envelope inEnvelope) {
        myPolygons = inPolygons;
        myEnvelope = inEnvelope;
    }

    /** Return the type of shape this is */
    public String getShapeType(){return MULTIPOLYGON;}
    
    /** Return the number of points in this shape. */
    public int getNumPoints(){
        int tempNumPoints = 0;
        if (myPolygons != null){
            for (int i=0; i<myPolygons.length; i++){
                tempNumPoints = tempNumPoints + myPolygons[i].getNumPoints();
            }
        }
        return tempNumPoints;
    };
    
    /** Get the point at the given index. */
    public Point getPoint(int inIndex){
        Point tempPoint = null;
        if ((myPolygons != null)&&(inIndex > -1)){
            int tempIndex = inIndex;
            for (int i=0; i<myPolygons.length; i++){
                if (myPolygons[i].getNumPoints() >= tempIndex){
                    tempIndex = tempIndex - myPolygons[i].getNumPoints();
                }
                else{
                    tempPoint = myPolygons[i].getPoint(tempIndex);
                    break;
                }
            }
        }
        return tempPoint;
    }
    /** Set the point at the given index. */
    public void setPoint(int inIndex, double inXCoordinate, double inYCoordinate){
        if ((myPolygons != null)&&(inIndex > -1)){
            int tempIndex = inIndex;
            for (int i=0; i<myPolygons.length; i++){
                if (myPolygons[i].getNumPoints() <= tempIndex){
                    tempIndex = tempIndex - myPolygons[i].getNumPoints();
                }
                else{
                    myPolygons[i].setPoint(tempIndex, inXCoordinate, inYCoordinate);
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
            Polygon[] tempPolygons = getPolygons();
            for (int i=0; i<tempPolygons.length; i++){
                if (tempPolygons[i].getNumPoints() < tempIndex){
                    tempIndex = tempIndex - tempPolygons[i].getNumPoints();
                }
                else{
                    tempReturn = tempPolygons[i].add(tempIndex, inX, inY);
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
            Polygon[] tempPolygons = getPolygons();
            for (int i=0; i<tempPolygons.length; i++){
                if (tempPolygons[i].getNumPoints() <= tempIndex){
                    tempIndex = tempIndex - tempPolygons[i].getNumPoints();
                }
                else{
                    tempReturn = tempPolygons[i].remove(tempIndex);
                    break;
                }
            }
        }
        return tempReturn;
    }

    /**
     * Recalculates the emvelope for the shape.
     */
    public void calculateEnvelope() {
        // calculate the Envelope of this polygon
        myEnvelope = null;
    }
    
    /** Return the envelope of this multipolygon. */
    public Envelope getEnvelope(){
        if (myEnvelope == null){
            if (myPolygons != null){
                EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
                for (int i = 0; i < myPolygons.length; i++) {
                    tempEnvelopeBuffer.expandToInclude(myPolygons[i].getEnvelope());
                }
                myEnvelope = tempEnvelopeBuffer.getEnvelope();
            }
        }
        return myEnvelope;
    }
    
    /**
     * Creates a copy of the MultiPolygon.
     */
    public Object clone(){
        if (myPolygons != null){
            Polygon[] tempPolygons = new Polygon[myPolygons.length];
            for (int i=0; i<myPolygons.length; i++){
                tempPolygons[i] = (Polygon) myPolygons[i].clone();
            }
            return new MultiPolygon(tempPolygons);
        }
        return new MultiPolygon();
    }
    
    /**
     * returns true if any of the contained polygons contain this shape.
     */
    public boolean contains(Shape inShape) {
        if (inShape == null) return false;
                
        // determine if any of the polygons contain the shape.
        for (int i=0; i<myPolygons.length; i++){
            if (myPolygons[i].contains(inShape)) return true;
        }
        return false;
    }
    
    /**
     * return the list of Polygons contained within this MultiPolygon.
     */
    public Polygon[] getPolygons(){
        return myPolygons;
    }
    
    /**
     * Returns an array of points representing this shape.
     * used by things like projections that need to operate on all the points in the shape.
     */
    public Point[] getPoints(){
        if (myPolygons == null) return new Point[0];
        
        Vector tempPointVect = new Vector();
        int myNumPoints = 0;
        for (int i=0; i<myPolygons.length; i++){
            if (myPolygons[i] != null){
                Point[] tempPoints = myPolygons[i].getPoints();
                myNumPoints = myNumPoints + tempPoints.length;
                tempPointVect.addElement(tempPoints);
            }
        }
        
        Point[] tempPoints = new Point[myNumPoints];
        int tempIndex = 0;
        for (int i=0; i<tempPointVect.size(); i++){
            Point[] tempPoints1 = (Point[]) tempPointVect.elementAt(i);
            for (int j=0; j<tempPoints1.length; j++){
                tempPoints[tempIndex] = tempPoints1[j];
                tempIndex++;
            }
        }
        return tempPoints;
    }
    
    /** Returns the OGIS Well Know Text Representation of this shape */
    public String getWKT(){
        StringBuffer sb = new StringBuffer("MULTIPOLYGON(");
        if (myPolygons != null){
            for (int i=0; i<myPolygons.length; i++){
                LinearRing tempPolygon = myPolygons[i].getPosativeRing();
                
                // write the polygon
                if (tempPolygon != null){
                    if (i>0) sb.append(",");
                    sb.append("(");
                    // write the polygon
                    sb.append("(");
                    tempPolygon.ensureClosed();
                    Point[] tempPoints = tempPolygon.getRingPoints();
                    for (int j=0; j<tempPoints.length; j++){
                        if (j>0) sb.append(",");
                        sb.append(tempPoints[j].getX());
                        sb.append(" ");
                        sb.append(tempPoints[j].getY());
                    }
                    sb.append(")");
                    
                    // write the holes
                    LinearRing[] tempHoles = myPolygons[i].getHoles();
                    if (tempHoles != null){
                        for (int j=0; j<tempHoles.length; j++){
                            sb.append(",(");
                            tempHoles[j].ensureClosed();
                            tempPoints = tempHoles[j].getRingPoints();
                            for (int k=0; k<tempPoints.length; k++){
                                if (k>0) sb.append(",");
                                sb.append(tempPoints[k].getX());
                                sb.append(" ");
                                sb.append(tempPoints[k].getY());
                            }
                            sb.append(")");
                        }
                    }
                    sb.append(")");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }
       
    /** Add a Polygon to this MultiPolygonat the given location */
    public void addPolygon(int inIndex, Polygon inPolygon){
        // check for valid data and special cases.
        if (inPolygon == null) return;
        if (myPolygons == null){
            myPolygons = new Polygon[1];
            myPolygons[0] = inPolygon;
            return;
        }
        
        // create the new points array.
        Polygon[] tempPolygons = new Polygon[myPolygons.length+1];
        for (int i=0; i<myPolygons.length; i++){
            if (i < inIndex){
                tempPolygons[i] = myPolygons[i];
            }
            if (i == inIndex){
                tempPolygons[i] = inPolygon;
            }
            if (i >= inIndex){
                tempPolygons[i+1] = myPolygons[i];
            }
        }
        if (inIndex >= myPolygons.length) tempPolygons[myPolygons.length] = inPolygon;
        myPolygons = tempPolygons;
        // calculate the new envelope
        calculateEnvelope();
    }
    
    /**
     * Remove the indicated polygon from this multi polygon.
     */
    public void removePolygon(int inIndex){
        if ((inIndex >= 0) && (inIndex < myPolygons.length)){
            Polygon[] tempPolygons = new Polygon[myPolygons.length-1];
            for (int i=0; i<tempPolygons.length; i++){
                if (i >= inIndex){
                    tempPolygons[i-1] = myPolygons[i];
                }
                else{
                    tempPolygons[i] = myPolygons[i];
                }
            }
            myPolygons = tempPolygons;
        }
    }
        
    /**
     * Find the point within the MultiPolygon closest to this point.
     */
    public Point getClosestPoint(double inX, double inY) {
        Point tempPoint = null;
        
        int tempIndex = getClosestIndex(inX, inY);
        if (tempIndex > -1){
            tempPoint = getPoint(tempIndex);
        }
        return tempPoint;
    }
    
    /**
     * Find index of the point within the MultiPolygon closest to this point.
     */
    public int getClosestIndex(double inX, double inY) {
        int tempReturnIndex = -1;
        Polygon[] tempPolygons = getPolygons();
        int tempPolygonIndex = -1;
        int tempPointIndex = -1;
        double tempMinDistance = Double.MAX_VALUE;
        
        for (int i=0; i<tempPolygons.length ;i++){
            Polygon tempPolygon = tempPolygons[i];
            int tempIndex = tempPolygon.getClosestIndex(inX, inY);
            if (tempIndex > -1){
                Point tempPoint = tempPolygon.getPoint(tempIndex);
                if (tempPoint != null){
                    double tempDistance = getDistance(inX, inY, tempPoint.getX(), tempPoint.getY());
                    if (tempDistance < tempMinDistance){
                        tempMinDistance = tempDistance;
                        tempPolygonIndex = i;
                        tempPointIndex = tempIndex;
                    }
                }
            }
        }
        // if a point was found
        if (tempPolygonIndex != -1){
            tempReturnIndex = 0;
            for (int i=0; i<tempPolygonIndex; i++){
                tempReturnIndex = tempReturnIndex + tempPolygons[i].getNumPoints();
            }
            tempReturnIndex = tempReturnIndex + tempPointIndex;
        }
        return tempReturnIndex;
    }
    
    /** Translate the shape the given distance in the X and Y directions  */
    public void translate(double inXDistance, double inYDistance) {
        for (int i=0; i<myPolygons.length; i++){
            myPolygons[i].translate(inXDistance, inYDistance);
        }
        calculateEnvelope();
    }
    
    /** Get the distance from this shape to the given point */
    public double getDistanceToPoint(double inX, double inY){
        // special conditions
        if (myPolygons == null) return Double.NaN;
        if (myPolygons.length == 0) return Double.NaN;
        double tempMinDistance = Double.MAX_VALUE;
        
        // loop through all the polygons and return the distance.
        for (int i=0; i<myPolygons.length; i++){
            double tempDistance = myPolygons[i].getDistanceToPoint(inX, inY);
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
        if (inShape instanceof Point) return ((Point) inShape).intersectsMultiPolygon(this);
        
        // MultiPoints
        if (inShape instanceof MultiPoint) return ((MultiPoint) inShape).intersectsMultiPolygon(this);
        
        // LineStrings
        if (inShape instanceof LineString) return ((LineString) inShape).intersectsMultiPolygon(this);
        
        // MultiLineStrings
        if (inShape instanceof MultiLineString) return ((MultiLineString) inShape).intersectsMultiPolygon(this);
        
        // LinearRings
        if (inShape instanceof LinearRing) return ((LinearRing) inShape).intersectsMultiPolygon(this);
        
        // Polygons
        if (inShape instanceof Polygon) return ((Polygon) inShape).intersectsMultiPolygon(this);
        
        // MultiPolygons
        if (inShape instanceof MultiPolygon) return intersectsMultiPolygon((MultiPolygon) inShape);
        
        // RasterShapes
        if (inShape instanceof RasterShape) return intersectsRasterShape((RasterShape) inShape);
        
        // did not find the shape so return false
        return false;
    }
    
    /** A MultiPolygon intersects another MultiPolygon if any of the constituent polygons intersect. */
    public boolean intersectsMultiPolygon(MultiPolygon inMultiPolygon){
        if (myPolygons == null) return false;
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons == null) return false;
        for (int i=0; i<myPolygons.length; i++){
            for (int j=0; j<tempPolygons.length; j++){
                if (myPolygons[i].intersectsPolygon(tempPolygons[j])) return true;
            }
        }
        return false;
    }
    
    /** A MultiPolygon intersects a RasterShape if the MultiPolygon intersects the envelope of the raster */
    public boolean intersectsRasterShape(RasterShape inRasterShape){
        Polygon tempPolygon = inRasterShape.getEnvelope().getPolygon();
        if (tempPolygon.intersectsMultiPolygon(this)) return true;
        return false;
    }
}
