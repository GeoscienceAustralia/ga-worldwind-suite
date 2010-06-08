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
 * Labeler for drawing polygon labels on the screen.
 */
public class PolygonLabeler extends SimpleLabeler {
    /** The name for this labeler, returns "Polygon Labeler"*/
    public String getLabelerName(){ return "Polygon Labeler";}
    
    /** Creates new PolygonLabeler */
    public PolygonLabeler() {
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
    
    /**
     * Draw the label for the record on the graphics context
     */
    public boolean drawLabel(String inString, gistoolkit.features.Shape inShape,Graphics inGraphics,Converter inConverter, OverlapManager inOverlapManager) {
        if (inString == null) return false;
        if (inShape == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        gistoolkit.features.Shape tempShape = inShape;
        if (tempShape != null){
            Graphics2D g2d = (Graphics2D) inGraphics;
            Rectangle2D r2d = getBounds(inString, g2d);
            int tempWidth = (int) r2d.getWidth();
            int tempHeight = (int) r2d.getHeight();
            if (tempShape instanceof gistoolkit.features.MultiPolygon){
                gistoolkit.features.Polygon[] tempPolygons = ((gistoolkit.features.MultiPolygon) tempShape).getPolygons();
                if (tempPolygons.length > 0){
                    gistoolkit.features.Point tempPoint = tempPolygons[0].getCentroid();
                    int tempLocX = inConverter.toScreenX(tempPoint.getX());
                    int tempLocY = inConverter.toScreenY(tempPoint.getY());
                    java.awt.Point p = getLabelPosition(tempLocX, tempLocY, tempWidth, tempHeight);
                    boolean tempDraw = false;
                    if (inOverlapManager == null){
                        tempDraw = true;
                    }
                    else{
                        if (!inOverlapManager.isOverLaps( p.x, p.y, tempWidth, tempHeight)){
                            tempDraw = true;
                        }
                    }
                    if (tempDraw){
                        drawString(inString, g2d, p.x, p.y, tempWidth, tempHeight);
                        return true;
                    }
                }
            }
            else if (tempShape instanceof gistoolkit.features.Polygon){
                gistoolkit.features.Point tempPoint=((gistoolkit.features.Polygon) tempShape).getCentroid();
                int tempLocX = inConverter.toScreenX(tempPoint.getX());
                int tempLocY = inConverter.toScreenY(tempPoint.getY());
                java.awt.Point p = getLabelPosition(tempLocX, tempLocY, tempWidth, tempHeight);
                boolean tempDraw = false;
                if (inOverlapManager == null){
                    tempDraw = true;
                }
                else{
                    if (!inOverlapManager.isOverLaps(p.x, p.y, tempWidth, tempHeight)){
                        tempDraw = true;
                    }
                }
                if (tempDraw){
                    drawString(inString, g2d, p.x, p.y, tempWidth, tempHeight);
                    return true;
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
