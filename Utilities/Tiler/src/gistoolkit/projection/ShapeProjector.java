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

package gistoolkit.projection;

import gistoolkit.features.*;
/**
 * Projects the shape in the given direction.
 */
public class ShapeProjector extends Object {
    
    /** Creates new ShapeProjector */
    public ShapeProjector() {
    }
    
    /**
     * Handles projection of shapes
     */
    public static void projectForward(Projection inProjection, Shape inShape) throws Exception{
        if (inProjection == null) return;
        if (inProjection instanceof NoProjection) return;
        if (inShape == null) return;
        if (inShape instanceof Point){
            projectForwardPoint(inProjection, (Point) inShape);
        }
        else if (inShape instanceof MultiPoint){
            projectForwardMultiPoint(inProjection, (MultiPoint) inShape);
        }
        else if (inShape instanceof LineString){
            projectForwardLineString(inProjection, (LineString) inShape);
        }
        else if (inShape instanceof MultiLineString){
            projectForwardMultiLineString(inProjection, (MultiLineString) inShape);
        }
        else if (inShape instanceof LinearRing){
            projectForwardLinearRing(inProjection, (LinearRing) inShape);
        }
        else if (inShape instanceof Polygon){
            projectForwardPolygon(inProjection, (Polygon) inShape);
        }
        else if (inShape instanceof MultiPolygon){
            projectForwardMultiPolygon(inProjection, (MultiPolygon) inShape);
        }        
        else if (inShape instanceof RasterShape){
            ImageProjector.projectForward(inProjection, (RasterShape) inShape);
        }
    }
    
    /**
     * Handles projection of Points.
     */
    public static void projectForwardPoint(Projection inProjection, Point inPoint) throws Exception{
        if (inProjection == null) return;
        if (inPoint == null) return;
        inProjection.projectForward(inPoint);
        inPoint.calculateEnvelope();
    }

    /**
     * Handles projection of MultiPoints.
     */
    public static void projectForwardMultiPoint(Projection inProjection, MultiPoint inMultiPoint) throws Exception{
        if (inProjection == null) return;
        if (inMultiPoint == null) return;
        Point[] tempPoints = inMultiPoint.getPoints();
        if (tempPoints == null) return;
        for (int i=0; i<tempPoints.length; i++){
            projectForward(inProjection, tempPoints[i]);
        }
        inMultiPoint.calculateEnvelope();
    }
    /**
     * Handles projection of LineStrings.
     */
    public static void projectForwardLineString(Projection inProjection, LineString inLineString) throws Exception{
        if (inProjection == null) return;
        if (inLineString == null) return;
        Point tempPoint = new Point(0,0);
        double[] tempXs = inLineString.getXCoordinates();
        double[] tempYs = inLineString.getYCoordinates();
        if (tempXs == null) return;
        for (int i=0; i<tempXs.length; i++){
            tempPoint.setX(tempXs[i]);
            tempPoint.setY(tempYs[i]);
            inProjection.projectForward(tempPoint);
            tempXs[i] = tempPoint.getX();
            tempYs[i] = tempPoint.getY();
        }
        inLineString.calculateEnvelope();
    }
    /**
     * Handles projection of MultiLineStrings.
     */
    public static void projectForwardMultiLineString(Projection inProjection, MultiLineString inMultiLineString) throws Exception{
        if (inProjection == null) return;
        if (inMultiLineString == null) return;
        LineString[] tempLineStrings = inMultiLineString.getLines();
        for (int i=0; i<tempLineStrings.length; i++){
            projectForward(inProjection, tempLineStrings[i]);
        }
        inMultiLineString.calculateEnvelope();
    }

    /**
     * Handles projection of LinearRings.
     */
    public static void projectForwardLinearRing(Projection inProjection, LinearRing inLinearRing) throws Exception{
        if (inProjection == null) return;
        Point tempPoint = new Point(0,0);
        double[] tempXs = inLinearRing.getXCoordinates();
        double[] tempYs = inLinearRing.getYCoordinates();
        if (tempXs == null) return;
        for (int i=0; i<tempXs.length; i++){
            tempPoint.setX(tempXs[i]);
            tempPoint.setY(tempYs[i]);
            inProjection.projectForward(tempPoint);
            tempXs[i] = tempPoint.getX();
            tempYs[i] = tempPoint.getY();
        }
        inLinearRing.calculateEnvelope();
    }

    /**
     * Handles projection of Polygons.
     */
    public static void projectForwardPolygon(Projection inProjection, Polygon inPolygon) throws Exception{
        if (inProjection == null) return;
        if (inPolygon == null) return;
        LinearRing tempLinearRing = inPolygon.getPosativeRing();
        projectForward(inProjection, tempLinearRing);
        LinearRing[] tempLinearRings = inPolygon.getHoles();
        for (int i=0; i<tempLinearRings.length; i++){
            projectForward(inProjection, tempLinearRings[i]);
        }
        inPolygon.calculateEnvelope();
    }
     
    /**
     * Handles projection of MultiPolygons.
     */
    public static void projectForwardMultiPolygon(Projection inProjection, MultiPolygon inMultiPolygon) throws Exception{
        if (inProjection == null) return;
        if (inMultiPolygon == null) return;
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons == null) return;
        for (int i=0; i<tempPolygons.length; i++){
            projectForward(inProjection, tempPolygons[i]);
        }
        inMultiPolygon.calculateEnvelope();
    }

    /**
     * Handles reverse projection of shapes.
     */
    public static void projectBackward(Projection inProjection, Shape inShape) throws Exception{
        if (inProjection == null) return;
        if (inProjection instanceof NoProjection) return;
        if (inShape == null) return;
        if (inShape instanceof Point){
            projectBackwardPoint(inProjection, (Point) inShape);
        }
        else if (inShape instanceof MultiPoint){
            projectBackwardMultiPoint(inProjection, (MultiPoint) inShape);
        }
        else if (inShape instanceof LineString){
            projectBackwardLineString(inProjection, (LineString) inShape);
        }
        else if (inShape instanceof MultiLineString){
            projectBackwardMultiLineString(inProjection, (MultiLineString) inShape);
        }
        else if (inShape instanceof LinearRing){
           projectBackwardLinearRing(inProjection, (LinearRing) inShape);
        }
        else if (inShape instanceof Polygon){
            projectBackwardPolygon(inProjection, (Polygon) inShape);
        }
        else if (inShape instanceof MultiPolygon){
            projectBackwardMultiPolygon(inProjection, (MultiPolygon) inShape);
        }        
        else if (inShape instanceof RasterShape){
            ImageProjector.projectBackward(inProjection, (RasterShape) inShape);
        }
    }
    /**
     * Handles reverse projection of Points.
     */
    public static void projectBackwardPoint(Projection inProjection, Point inPoint) throws Exception{
        if (inProjection == null) return;
        if (inPoint == null) return;
        inProjection.projectBackward(inPoint);
        inPoint.calculateEnvelope();
    }

    /**
     * Handles reverse projection of MultiPoints.
     */
    public static void projectBackwardMultiPoint(Projection inProjection, MultiPoint inMultiPoint) throws Exception{
        if (inProjection == null) return;
        if (inMultiPoint == null) return;
        Point[] tempPoints = inMultiPoint.getPoints();
        if (tempPoints == null) return;
        for (int i=0; i<tempPoints.length; i++){
            projectBackward(inProjection, tempPoints[i]);
        }
        inMultiPoint.calculateEnvelope();
    }
    /**
     * Handles reverse projection of LineStrings.
     */
    public static void projectBackwardLineString(Projection inProjection, LineString inLineString) throws Exception{
        if (inProjection == null) return;
        if (inLineString == null) return;
        Point tempPoint = new Point(0,0);
        double[] tempXs = inLineString.getXCoordinates();
        double[] tempYs = inLineString.getYCoordinates();
        if (tempXs == null) return;
        for (int i=0; i<tempXs.length; i++){
            tempPoint.setX(tempXs[i]);
            tempPoint.setY(tempYs[i]);
            inProjection.projectBackward(tempPoint);
            tempXs[i] = tempPoint.getX();
            tempYs[i] = tempPoint.getY();
        }
        inLineString.calculateEnvelope();
    }
    /**
     * Handles reverse projection of MultiLineStrings.
     */
    public static void projectBackwardMultiLineString(Projection inProjection, MultiLineString inMultiLineString) throws Exception{
        if (inProjection == null) return;
        if (inMultiLineString == null) return;
        LineString[] tempLineStrings = inMultiLineString.getLines();
        for (int i=0; i<tempLineStrings.length; i++){
            projectBackward(inProjection, tempLineStrings[i]);
        }
        inMultiLineString.calculateEnvelope();
    }

    /**
     * Handles reverse projection of LinearRings.
     */
    public static void projectBackwardLinearRing(Projection inProjection, LinearRing inLinearRing) throws Exception{
        if (inProjection == null) return;
        Point tempPoint = new Point(0,0);
        double[] tempXs = inLinearRing.getXCoordinates();
        double[] tempYs = inLinearRing.getYCoordinates();
        if (tempXs == null) return;
        for (int i=0; i<tempXs.length; i++){
            tempPoint.setX(tempXs[i]);
            tempPoint.setY(tempYs[i]);
            inProjection.projectBackward(tempPoint);
            tempXs[i] = tempPoint.getX();
            tempYs[i] = tempPoint.getY();
        }
        inLinearRing.calculateEnvelope();
    }

    /**
     * Handles reverse projection of Polygons.
     */
    public static void projectBackwardPolygon(Projection inProjection, Polygon inPolygon) throws Exception{
        if (inProjection == null) return;
        if (inPolygon == null) return;
        LinearRing tempLinearRing = inPolygon.getPosativeRing();
        projectBackward(inProjection, tempLinearRing);
        LinearRing[] tempLinearRings = inPolygon.getHoles();
        for (int i=0; i<tempLinearRings.length; i++){
            projectBackward(inProjection, tempLinearRings[i]);
        }
        inPolygon.calculateEnvelope();
    }
     
    /**
     * Handles reverse projection of MultiPolygons.
     */
    public static void projectBackwardMultiPolygon(Projection inProjection, MultiPolygon inMultiPolygon) throws Exception{
        if (inProjection == null) return;
        if (inMultiPolygon == null) return;
        Polygon[] tempPolygons = inMultiPolygon.getPolygons();
        if (tempPolygons == null) return;
        for (int i=0; i<tempPolygons.length; i++){
            projectBackward(inProjection, tempPolygons[i]);
        }
        inMultiPolygon.calculateEnvelope();
    }

    /** Project the envelope Foreward */
    public static Envelope projectForward(Projection inProjection, Envelope inEnvelope) throws Exception{
        if (inProjection == null) return inEnvelope;
        if (inEnvelope == null) return inEnvelope;
        Point[] tempPoint = new Point[8];
        // corners
        tempPoint[0] = new Point(inEnvelope.getMinX(), inEnvelope.getMaxY());
        tempPoint[1] = new Point(inEnvelope.getMaxX(), inEnvelope.getMaxY());
        tempPoint[2] = new Point(inEnvelope.getMaxX(), inEnvelope.getMinY());
        tempPoint[3] = new Point(inEnvelope.getMinX(), inEnvelope.getMinY());
        // sides
        tempPoint[4] = new Point((inEnvelope.getMinX() + inEnvelope.getMaxX())/2, inEnvelope.getMaxY());
        tempPoint[5] = new Point(inEnvelope.getMaxX(), (inEnvelope.getMaxY()+inEnvelope.getMinY())/2);
        tempPoint[6] = new Point((inEnvelope.getMinX() + inEnvelope.getMaxX())/2, inEnvelope.getMinY());
        tempPoint[7] = new Point(inEnvelope.getMinX(), (inEnvelope.getMaxY()+inEnvelope.getMinY())/2);
        
        // project the points
        for (int i=0; i<tempPoint.length; i++) inProjection.projectForward(tempPoint[i]);
        
        // find the bounding box of all the points.
        double minX = tempPoint[0].getX();
        double maxX = tempPoint[0].getX();
        double minY = tempPoint[0].getY();
        double maxY = tempPoint[0].getY();
        for (int i=0; i<tempPoint.length; i++){
            if (minX > tempPoint[i].getX()) minX = tempPoint[i].getX();
            if (maxX < tempPoint[i].getX()) maxX = tempPoint[i].getX();
            if (minY > tempPoint[i].getY()) minY = tempPoint[i].getY();
            if (maxY < tempPoint[i].getY()) maxY = tempPoint[i].getY();
        }
        return new Envelope(minX,minY,maxX,maxY);
    }
    
    /** Project the envelope Backward */
    public static Envelope projectBackward(Projection inProjection, Envelope inEnvelope) throws Exception{
        if (inProjection == null) return inEnvelope;
        if (inEnvelope == null) return inEnvelope;
        Point[] tempPoint = new Point[8];
        // corners
        tempPoint[0] = new Point(inEnvelope.getMinX(), inEnvelope.getMaxY());
        tempPoint[1] = new Point(inEnvelope.getMaxX(), inEnvelope.getMaxY());
        tempPoint[2] = new Point(inEnvelope.getMaxX(), inEnvelope.getMinY());
        tempPoint[3] = new Point(inEnvelope.getMinX(), inEnvelope.getMinY());
        // sides
        tempPoint[4] = new Point((inEnvelope.getMinX() + inEnvelope.getMaxX())/2, inEnvelope.getMaxY());
        tempPoint[5] = new Point(inEnvelope.getMaxX(), (inEnvelope.getMaxY()+inEnvelope.getMinY())/2);
        tempPoint[6] = new Point((inEnvelope.getMinX() + inEnvelope.getMaxX())/2, inEnvelope.getMinY());
        tempPoint[7] = new Point(inEnvelope.getMinX(), (inEnvelope.getMaxY()+inEnvelope.getMinY())/2);
        
        // project the points
        for (int i=0; i<tempPoint.length; i++) inProjection.projectBackward(tempPoint[i]);
        
        // find the bounding box of all the points.
        double minX = tempPoint[0].getX();
        double maxX = tempPoint[0].getX();
        double minY = tempPoint[0].getY();
        double maxY = tempPoint[0].getY();
        for (int i=0; i<tempPoint.length; i++){
            if (minX > tempPoint[i].getX()) minX = tempPoint[i].getX();
            if (maxX < tempPoint[i].getX()) maxX = tempPoint[i].getX();
            if (minY > tempPoint[i].getY()) minY = tempPoint[i].getY();
            if (maxY < tempPoint[i].getY()) maxY = tempPoint[i].getY();
        }
        return new Envelope(minX,minY,maxX,maxY);
    }
}
