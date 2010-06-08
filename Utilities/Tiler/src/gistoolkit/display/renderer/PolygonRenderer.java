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
import java.util.Vector;
import java.awt.Graphics;
import java.awt.Graphics2D;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Created to render shapes in a single colors, default colors are black border, no fill.
 */
public class PolygonRenderer extends SimpleRenderer{
    /** The name for this renderer, always returns "Polygon Renderer" */
    public String getRendererName(){ return "Polygon Renderer";}
    
    private int[] myXs = new int[0];
    private int[] myYs = new int[0];
    
    /**
     * Class for holding the screen points
     */
    private class ScreenPoint{
        int x;
        int y;
        
        public ScreenPoint(){
        }
        
        public ScreenPoint(int inX, int inY){
            x = inX;
            y = inY;
        }
        
        boolean equals(ScreenPoint inScreenPoint){
            if (inScreenPoint.x != x) return false;
            if (inScreenPoint.y != y) return false;
            return true;
        }
    }
    
    /**
     * MonoShader constructor comment.
     */
    public PolygonRenderer() {
        super();
    }
    
    /**
     * Convert an array of points from World Coordinates to Screen coordinates.
     */
    private ScreenPoint[] convertToScreen(double[] inX, double[] inY, Converter inConverter){
        if (inX == null) return new ScreenPoint[0];
        if (inX.length  == 0) return new ScreenPoint[0];
        
        // convert the ponts
        Vector tempScreenVector = new Vector(inX.length);
        ScreenPoint tempScreenPoint = null;
        ScreenPoint tempPreviousScreenPoint = null;
        for (int i=0; i<inX.length; i++){
            tempScreenPoint = new ScreenPoint();
            tempScreenPoint.x = inConverter.toScreenX(inX[i]);
            tempScreenPoint.y = inConverter.toScreenY(inY[i]);
            
            if ((tempPreviousScreenPoint == null) || (!tempPreviousScreenPoint.equals(tempScreenPoint))){
                tempScreenVector.addElement(tempScreenPoint);
                tempPreviousScreenPoint = tempScreenPoint;
            }
            
        }
        
        if (tempScreenVector.size() < 3){ return null;}
        
        // test to enxure the shape is closed.
        if ( ((ScreenPoint)tempScreenVector.elementAt(0)).equals((ScreenPoint) tempScreenVector.elementAt(tempScreenVector.size()-1))){
            tempScreenVector.removeElementAt(tempScreenVector.size()-1);
        }
        ScreenPoint[] tempScreenPoints = new ScreenPoint[tempScreenVector.size()];
        tempScreenVector.copyInto(tempScreenPoints);
        return tempScreenPoints;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Polygon))
            return false;
        
        Polygon tempPolygon = (Polygon) inRecord.getShape();
        
        // draw the posative shape
        LinearRing tempRing = tempPolygon.getPosativeRing();
        double[] tempXs = tempRing.getXCoordinates();
        double[] tempYs = tempRing.getYCoordinates();
        if (myXs.length < tempXs.length){
            myXs = new int[tempXs.length];
            myYs = new int[tempXs.length];
        }
        for (int i = 0; i < tempXs.length; i++) {
            myXs[i] = inConverter.toScreenX(tempXs[i]);
            myYs[i] = inConverter.toScreenY(tempYs[i]);
        }
        
        // call to graphics routine to fill the posative shape.
        Graphics tempGraphics = inShader.getFillGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null) {
            if ((tempPolygon.getHoles() != null) && (tempPolygon.getHoles().length >0)){
                drawShapeWithHoles(inRecord, tempGraphics, inConverter, inShader);
            }
            else{
                tempGraphics.fillPolygon(myXs, myYs, tempXs.length);
            }
        }
        
        // call the graphics routine to draw the posative shape.'
        tempGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null) {
            tempGraphics.drawPolygon(myXs, myYs, tempXs.length);
        }
        
        // draw the negative shapes
        LinearRing[] tempHoles = tempPolygon.getHoles();
        if (tempHoles != null) {
            for (int i = 0; i < tempHoles.length; i++) {
                tempRing = tempHoles[i];
                tempXs = tempRing.getXCoordinates();
                tempYs = tempRing.getYCoordinates();
                if (myXs.length < tempXs.length){
                    myXs = new int[tempXs.length];
                    myYs = new int[tempXs.length];
                }
                for (int j = 0; j < tempXs.length; j++) {
                    myXs[j] = inConverter.convertX(tempXs[j]);
                    myYs[j] = inConverter.convertY(tempYs[j]);
                }
                
                // call the graphics routine to draw the negative shape.
                if (tempGraphics != null) tempGraphics.drawPolygon(myXs, myYs, tempXs.length);
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Polygon))
            return false;
        
        Polygon tempPolygon = (Polygon) inRecord.getShape();
        
        // draw the posative shape
        LinearRing tempRing = tempPolygon.getPosativeRing();
        double[] tempDXs = tempRing.getXCoordinates();
        double[] tempDYs = tempRing.getYCoordinates();
        int[] tempXs = new int[tempDXs.length];
        int[] tempYs = new int[tempDYs.length];
        for (int i = 0; i < tempDXs.length; i++) {
            tempXs[i] = inConverter.toScreenX(tempDXs[i]);
            tempYs[i] = inConverter.toScreenY(tempDYs[i]);
        }
        
        // call the graphics routine to draw the posative shape.'
        Graphics tempGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null) {
            tempGraphics.drawPolygon(tempXs, tempYs, tempDXs.length);
        }
        
        // draw the negative shapes
        LinearRing[] tempHoles = tempPolygon.getHoles();
        if (tempHoles != null) {
            for (int i = 0; i < tempHoles.length; i++) {
                tempRing = tempHoles[i];
                tempDXs = tempRing.getXCoordinates();
                tempDYs = tempRing.getYCoordinates();
                tempXs = new int[tempDXs.length];
                tempYs = new int[tempDYs.length];
                for (int j = 0; j < tempDXs.length; j++) {
                    tempXs[j] = inConverter.toScreenX(tempDXs[j]);
                    tempYs[j] = inConverter.toScreenY(tempDYs[j]);
                }
                
                // call the graphics routine to draw the negative shape.
                tempGraphics.drawPolygon(tempXs, tempYs, tempDXs.length);
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    public boolean drawShapePoints(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Polygon))
            return false;
        
        Polygon tempPolygon = (Polygon) inRecord.getShape();
        
        // draw the posative shape
        LinearRing tempRing = tempPolygon.getPosativeRing();
        double[] tempDXs = tempRing.getXCoordinates();
        double[] tempDYs = tempRing.getYCoordinates();
        int[] tempXs = new int[tempDXs.length];
        int[] tempYs = new int[tempDYs.length];
        for (int i = 0; i < tempDXs.length; i++) {
            tempXs[i] = inConverter.convertX(tempDXs[i]);
            tempYs[i] = inConverter.convertY(tempDYs[i]);
        }
        
        // call the graphics routine to draw the posative shape.'
        Graphics tempGraphics = inShader.getLabelHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null) {
            for (int i=0; i<tempXs.length; i++){
                tempGraphics.drawOval(tempXs[i]-4, tempYs[i]-4, 8,8);
            }
        }
        
        // draw the negative shapes
        LinearRing[] tempHoles = tempPolygon.getHoles();
        if (tempHoles != null) {
            for (int i = 0; i < tempHoles.length; i++) {
                tempRing = tempHoles[i];
                tempDXs = tempRing.getXCoordinates();
                tempDYs = tempRing.getYCoordinates();
                tempXs = new int[tempDXs.length];
                tempYs = new int[tempDYs.length];
                for (int j = 0; j < tempDXs.length; j++) {
                    tempXs[j] = inConverter.convertX(tempDXs[j]);
                    tempYs[j] = inConverter.convertY(tempDYs[j]);
                }
                
                // call the graphics routine to draw the negative shape.
                for (int j=0; j<tempXs.length; j++){
                    tempGraphics.drawOval(tempXs[j]-4, tempYs[j]-4, 8,8);
                }
            }
        }
        return true;
    }
    
    /**
     * Draws the Shapes
     */
    private boolean drawShapeWithHoles(Record inRecord,	Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Polygon))
            return false;
        
        Polygon tempPolygon = (Polygon) inRecord.getShape();
        
        // Create a shape to handle this case.
        java.awt.geom.GeneralPath tempPath = new java.awt.geom.GeneralPath(java.awt.geom.GeneralPath.WIND_EVEN_ODD);
        
        // convert the points to screen coordinates
        Vector tempRingVect = new Vector();
        ScreenPoint[] tempScreenPoints = null;
        LinearRing tempRing = tempPolygon.getPosativeRing();
        if (tempRing != null) {
            double[] tempDXs = tempRing.getXCoordinates();
            double[] tempDYs = tempRing.getYCoordinates();
            if (tempDXs != null) {
                tempScreenPoints = convertToScreen(tempDXs, tempDYs, inConverter);
                if ((tempScreenPoints != null) && (tempScreenPoints.length > 0)){
                    tempPath.moveTo(tempScreenPoints[0].x, tempScreenPoints[0].y);
                    for (int i=1; i<tempScreenPoints.length; i++){
                        tempPath.lineTo(tempScreenPoints[i].x, tempScreenPoints[i].y);
                    }
                    tempPath.closePath();
                    tempRingVect.addElement(tempScreenPoints);
                }
            }
        }
        
        // convert the holes to screen coordinates.
        LinearRing[] tempRings = tempPolygon.getHoles();
        if (tempRings != null) {
            for (int i = 0; i < tempRings.length; i++) {
                double[] tempDXs = tempRings[i].getXCoordinates();
                double[] tempDYs = tempRings[i].getYCoordinates();
                tempScreenPoints = convertToScreen(tempDXs, tempDYs, inConverter);
                if ((tempScreenPoints != null) && (tempScreenPoints.length > 0)){
                    tempPath.moveTo(tempScreenPoints[0].x, tempScreenPoints[0].y);
                    for (int j=1; j<tempScreenPoints.length; j++){
                        tempPath.lineTo(tempScreenPoints[j].x, tempScreenPoints[j].y);
                    }
                    tempPath.closePath();
                    tempRingVect.addElement(tempScreenPoints);
                }
            }
        }
        Graphics2D tempG2D = (Graphics2D) inGraphics;
        tempG2D.fill(tempPath);
        /*
        // Find the largest Y coordinate, and minimum X coordinate, the following routine
        // will scann from minX to MaxX finding intersections lower than the maximum Y.
        // it is important that the MaxY be higher than all the points in the shape.
        int tempMinX = inConverter.toScreenX(tempPolygon.getExtents().myTopX) - 1;
        int tempMaxX = inConverter.toScreenX(tempPolygon.getExtents().myBottomX);
        int tempMaxY = inConverter.toScreenY(tempPolygon.getExtents().myBottomY) + 1;
        int tempMinY = inConverter.toScreenY(tempPolygon.getExtents().myTopY);
         
        // loop through all lines on the screen
        for (int k = tempMinX; k <= tempMaxX; k++) {
         
            // loop through the rings finding the interssections
         
            // current values to check
            int x = k;
            int y = tempMaxY;
         
            // previous points
            Vector tempDrawPoints = new Vector();
         
            // loop through the points finding the crossings.
            for (int i = 0; i < tempRingVect.size(); i++) {
         
                // check all the rings
                tempScreenPoints = (ScreenPoint[]) tempRingVect.elementAt(i);
                for (int j = 0; j < tempScreenPoints.length; j++) {
         
                    // this algorythm depends on three points
                    int prevPoint = j - 1;
                    if (prevPoint < 0)
                        prevPoint = tempScreenPoints.length - 1;
         
                    int nextPoint = j+1;
                    if (nextPoint == tempScreenPoints.length){
                        nextPoint = 0;
                    }
         
                    int x0 = tempScreenPoints[prevPoint].x;
                    int y0 = tempScreenPoints[prevPoint].y;
                    int x1 = tempScreenPoints[j].x;
                    int y1 = tempScreenPoints[j].y;
                    int x2 = tempScreenPoints[nextPoint].x;
                    int y2 = tempScreenPoints[nextPoint].y;
         
                    // determine if the points cross
                    double cy = 0;
                    if (((x1 < x) && (x < x2)) || ((x1 > x) && (x > x2))) {
                        double t = ((double) (x - x2)) / ((double) (x1 - x2));
                        cy = t * y1 + (1 - t) * y2;
                        if (y > cy) {
                            tempDrawPoints.addElement(new ScreenPoint(x, (int) cy));
                        }
                    }
         
                    // check for a ray passing directly through a point.
                    if (x1 == x) {
                        if (((x0 < x) && (x2 >= x)) || ((x0 >= x) && (x2 < x))) {
                            tempDrawPoints.addElement(new ScreenPoint(x, y1));
                        }
                    }
                }
            }
         
            // order the points by their y axis
            for (int i = 0; i < tempDrawPoints.size(); i++) {
                ScreenPoint sc1 = (ScreenPoint) tempDrawPoints.elementAt(i);
                for (int j = i+1; j < tempDrawPoints.size(); j++) {
                    ScreenPoint sc2 = (ScreenPoint) tempDrawPoints.elementAt(j);
                    if (sc1.y > sc2.y) {
                        tempDrawPoints.setElementAt(sc2, i);
                        tempDrawPoints.setElementAt(sc1, j);
                        sc1 = sc2;
                    }
                }
            }
         
            // draw the points
            for (int i = 0; i < tempDrawPoints.size()-1; i+=2) {
                inGraphics.drawLine(
                ((ScreenPoint) tempDrawPoints.elementAt(i)).x,
                ((ScreenPoint) tempDrawPoints.elementAt(i)).y,
                ((ScreenPoint) tempDrawPoints.elementAt(i + 1)).x,
                ((ScreenPoint) tempDrawPoints.elementAt(i + 1)).y);
            }
         
        }
         */
        return true;
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
    
    /** For display in lists and such. */
    public String toString(){
        return "Polygon Renderer";
    }
}