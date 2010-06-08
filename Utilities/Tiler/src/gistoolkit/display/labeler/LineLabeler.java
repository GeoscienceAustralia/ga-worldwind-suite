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

package gistoolkit.display.labeler;

import java.awt.*;
import java.awt.geom.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Labeler for drawing line labels on the screen.
 */
public class LineLabeler extends SimpleLabeler {
    /** The name for this labeler, returns "Line Labeler"*/
    public String getLabelerName(){ return "Line Labeler";}
    
    /** Creates new PolygonLabeler */
    public LineLabeler() {
    }
    protected boolean drawLabel(String inString, gistoolkit.features.Shape inShape, Graphics inGraphics, Converter inConverter, OverlapManager inOverlapManager){
        if (inString == null) return false;
        if (inShape == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        // find where to
        gistoolkit.features.Shape tempShape = inShape;
        if (tempShape != null){
            // the label to apply.
            if (tempShape instanceof gistoolkit.features.MultiLineString){
                LineString[] tempLines = ((MultiLineString) tempShape).getLines();
                if (tempLines.length > 0){
                    tempShape = tempLines[0];
                }
            }
            if (tempShape instanceof gistoolkit.features.LineString){
                FontMetrics fm = inGraphics.getFontMetrics();
                Graphics2D g2d = (Graphics2D) inGraphics;
                Rectangle2D r2d = getBounds(inString, g2d);
                int tempWidth = (int) r2d.getWidth();
                int tempHeight = (int) r2d.getHeight();
                
                // find the center point.
                Envelope e = tempShape.getEnvelope();
                double centerx = (e.getMinX() + e.getMaxX())/2;
                double centery = (e.getMaxY() + e.getMinY())/2;
                
                // find the linestring closest to the centerpoint.
                gistoolkit.features.Point[] tempPoints = tempShape.getPoints();
                gistoolkit.features.Point tempPoint1 = null;
                gistoolkit.features.Point tempPoint2 = null;
                double distance = Double.POSITIVE_INFINITY;
                for (int i=0; i<tempPoints.length-1; i++){
                    double tempDistance = gistoolkit.features.Shape.getDistanceToLine(tempPoints[i].getX(), tempPoints[i].getY(), tempPoints[i+1].getX(), tempPoints[i+1].getY(), centerx, centery);
                    if (tempDistance < distance){
                        distance = tempDistance;
                        tempPoint1 = tempPoints[i];
                        tempPoint2 = tempPoints[i+1];
                    }
                }
                
                // find the angle between the points.
                double angle = 0;
                if ((tempPoint1 != null) && (tempPoint2 != null)){
                    centerx = (tempPoint1.getX() + tempPoint2.getX())/2;
                    centery = (tempPoint1.getY() + tempPoint2.getY())/2;
                    if (!(tempPoint2.getX() == tempPoint1.getX())){
                        double tan = -(tempPoint2.getY() - tempPoint1.getY())/(tempPoint2.getX() - tempPoint1.getX());
                        angle = Math.atan(tan);
                    }
                }
                
                // find the point to label.
                int tempLocX = inConverter.toScreenX(centerx);
                int tempLocY = inConverter.toScreenY(centery);
                
                // if this point is off screen, do not label it
                if ((tempLocX > inConverter.getScreenWidth())
                || (tempLocX < 0)
                || (tempLocY > inConverter.getScreenHeight())
                || (tempLocY < 0)){
                    return false;
                }
                java.awt.Point tempPoint = getLabelPosition(tempLocX, tempLocY, tempWidth, tempHeight);
                AffineTransform tempRotateTransform = AffineTransform.getRotateInstance(angle, (double) tempLocX, (double) tempLocY);
                java.awt.Shape tempRotateShape = tempRotateTransform.createTransformedShape(new Rectangle(tempPoint.x-tempWidth/2, tempPoint.y-tempHeight/2, tempWidth, tempHeight));
                
                // determine if it is overlaped.
                boolean tempDraw = false;
                if (inOverlapManager != null){
                    if (!inOverlapManager.isOverLaps(tempRotateShape)){
                        tempDraw = true;
                    }
                }
                else{
                    tempDraw = true;
                }
                
                // draw the text.
                if (tempDraw){
                    AffineTransform tempTransform = g2d.getTransform();
                    g2d.setTransform(tempRotateTransform);
                    drawString(inString, g2d, tempPoint.x, tempPoint.y, tempWidth, tempHeight);
                    g2d.setTransform(tempTransform);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Draw the label for the record on the graphics context
     */
    protected synchronized boolean drawLabel(Record inRecord,Graphics inGraphics,Converter inConverter) {
        
        String[] tempAttributeNames = inRecord.getAttributeNames();
        if (inRecord.getAttributes()[getLabelColumn()] != null){
            gistoolkit.features.Shape tempShape = inRecord.getShape();
            String tempString = inRecord.getAttributes()[getLabelColumn()].toString();
            if (tempString != null){
                if (!isDuplicate(tempString)){
                    boolean tempDrawn = drawLabel(tempString, tempShape, inGraphics, inConverter, getOverlapManager());
                    if (!tempDrawn){
                        removeDuplicate(tempString);
                    }
                }
            }
        }
        return false;
    }
    
    /** get the configuration information for this labeler  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        return tempRoot;
    }
    
    /** Set the configuration information for this labeler  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        super.setNode(inNode);
    }
}
