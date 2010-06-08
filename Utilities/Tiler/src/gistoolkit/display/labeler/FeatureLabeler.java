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
import gistoolkit.display.*;
import gistoolkit.features.*;
/**
 * Simple Labeler to be extended by more advanced renderers.
 * @author  ithaqua
 */
public class FeatureLabeler extends SimpleLabeler {
    /** A name for this labeler, returns "Feature Labeler". */
    public String getLabelerName(){ return "Feature Labeler";}
    
    /** Set the number of the column to label by.*/
    public void setLabelColumn(int inLabelColumn){ super.setLabelColumn(inLabelColumn);reset();}
    
    /** Set the distance from the label point that the label will be placed. */
    public void setLabelOffset(int inOffset){super.setLabelOffset(inOffset); reset();}
    
    /** Set the orientation of the labels relative to their anchor. */
    public void setLabelOrientation(int inOrientation){super.setLabelOrientation(inOrientation);reset();}
    
    /** reference to a polygon labeler. */
    private PolygonLabeler myPolygonLabeler = null;
    /** get a polygon labeler bassed on this labeler. */
    private PolygonLabeler getPolygonLabeler(){
        if (myPolygonLabeler != null) return myPolygonLabeler;
        myPolygonLabeler = new PolygonLabeler();
        myPolygonLabeler.setLabelColumn(getLabelColumn());
        myPolygonLabeler.setLabelOffset(getLabelOffset());
        myPolygonLabeler.setLabelOrientation(getLabelOrientation());
        myPolygonLabeler.setAllowDuplicates(getAllowDuplicates());
        myPolygonLabeler.setAllowOverlaps(getAllowOverlaps());
        return myPolygonLabeler;
    }
    /** reference to a line labeler. */
    private LineLabeler myLineLabeler = null;
    /** get a line labeler bassed on this labeler. */
    private LineLabeler getLineLabeler(){
        if (myLineLabeler != null) return myLineLabeler;
        myLineLabeler = new LineLabeler();
        myLineLabeler.setLabelColumn(getLabelColumn());
        myLineLabeler.setLabelOffset(getLabelOffset());
        myLineLabeler.setLabelOrientation(getLabelOrientation());
        myLineLabeler.setAllowDuplicates(getAllowDuplicates());
        myLineLabeler.setAllowOverlaps(getAllowOverlaps());
        return myLineLabeler;
    }
    private void reset(){
        myPolygonLabeler = null;
        myLineLabeler = null;
    }
    
    /** Creates new BasicLabeler */
    public FeatureLabeler() {
    }
    
    
    /**
     * Draw the label for the record on the graphics context
     */
    protected boolean drawLabel(Record inRecord,Graphics inGraphics,Converter inConverter) {
        
        String[] tempAttributeNames = inRecord.getAttributeNames();
        if (inRecord.getAttributes()[getLabelColumn()] != null){
            gistoolkit.features.Shape tempShape = inRecord.getShape();
            if (tempShape != null){
                // draw polygons
                if ((tempShape instanceof gistoolkit.features.Polygon) || (tempShape instanceof gistoolkit.features.MultiPolygon)){
                    PolygonLabeler tempPolygonLabeler = getPolygonLabeler();
                    tempPolygonLabeler.drawLabel(inRecord, inGraphics, inConverter);
                    return true;
                }
                if ((tempShape instanceof gistoolkit.features.LineString) || (tempShape instanceof gistoolkit.features.MultiLineString)){
                    LineLabeler tempLineLabeler = getLineLabeler();
                    tempLineLabeler.drawLabel(inRecord, inGraphics, inConverter);
                    return true;
                }
                // draw everything else.
                else{
                    String tempString = inRecord.getAttributes()[getLabelColumn()].toString();
                    if (!isDuplicate(tempString)){
                        Graphics2D g2d = (Graphics2D) inGraphics;
                        Rectangle2D r2d = getBounds(tempString, g2d);
                        int tempWidth = (int) r2d.getWidth();
                        int tempHeight = (int) r2d.getHeight();
                        Envelope e = inRecord.getShape().getEnvelope();
                        double x = (e.getMinX() + e.getMaxX())/2;
                        double y = (e.getMinY() + e.getMaxY())/2;
                        int tempLocX = inConverter.toScreenX(x);
                        int tempLocY = inConverter.toScreenY(y);
                        java.awt.Point p = getLabelPosition(tempLocX, tempLocY, tempWidth, tempHeight);
                        if (!isOverLaps(p.x, p.y, tempWidth, tempHeight)){
                            // draw the string
                            drawString(tempString, g2d, p.x, p.y, tempWidth, tempHeight);
//                            g2d.drawString(tempString, p.x-tempWidth/2, p.y+tempHeight/2);
                            return true;
                        }
                        else{
                            removeDuplicate(tempString);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /** Return the edit panel used to edit this labeler  */
    public LabelerPanel getEditPanel() {
        SimpleLabelerPanel tempPanel = new SimpleLabelerPanel();
        tempPanel.setLabeler(this);
        return tempPanel;
    }
    /** Name for the Feature Renderer for the configuration file. */
    public static final String FEATURE_RENDERER_NODE = "FeatureRenderer";
    
    /** get the configuration information for this labeler  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName(FEATURE_RENDERER_NODE);
        return tempRoot;
    }
    
    /** Set the configuration information for this labeler  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        super.setNode(inNode);
    }
    
    /**
     * Called before the layer is initially labeled to allow the labeler to prepare for labeling.
     */
    public void beginLabel(){
        super.beginLabel();
        getLineLabeler().beginLabel();
        getPolygonLabeler().beginLabel();
    }
    
    /**
     * Called after the layer has completed labeling.
     */
    public void endLabel() {
        super.beginLabel();
        getLineLabeler().endLabel();
        getPolygonLabeler().endLabel();
    }
}

