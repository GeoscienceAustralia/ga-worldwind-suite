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

package gistoolkit.display.scalebar;

import java.awt.*;
import gistoolkit.common.*;
import gistoolkit.display.*;

/**
 * Class to display a scale bar on the map.
 */
public class MeterToMetricScaleBar extends SimpleScaleBar{
    /** 
     * A description to be displayed to the user that indicates what this scale bar does.
     * A good thing to indicates is that it takes a projection in meters and displays a scale in metric.
     * Or that it takes a projection in feed and displays a scale in in,ft,yd,and miles.
     * <p> This one returns kilometers, meters, centimters, milimeters, and micrometers </p>
     */
    public String getDescription(){return "From a meter projection, displays km,m,cm,mm";}
        
    /** Method to draw a scale bar on the graphics context. */
    public void drawScale(Graphics inGraphics, Converter inConverter, int inWidth, int inHeight){
        // find out how wide the display is
        Rectangle tempScreenRect = getAvailableSize(inWidth, inHeight);
        double tempWorldX1 = inConverter.toWorldX(0);
        double tempWidth = inConverter.toWorldX(tempScreenRect.width)-tempWorldX1;
        if (tempWidth < 0) return;
        
        // if it is huge, just display it.
        double tempDistance = tempWidth;
        String tempLabel = ""+tempWidth;
        double tempTestDistance = 1000000000; // billion
        while (tempTestDistance > 0.000000001){
            if (tempWidth < tempTestDistance*10){
                tempDistance = tempTestDistance*5;
            }
            if (tempWidth < tempTestDistance*5){
                tempDistance = tempTestDistance*3;
            }
            if (tempWidth < tempTestDistance*3){
                tempDistance = tempTestDistance*1;
            }
            if (tempWidth < tempTestDistance*2){
                tempDistance = tempTestDistance;
            }
            tempTestDistance = tempTestDistance/10;
        }
        // so the distance we are going to draw is tempDistance
        double tempScale = 1000000000;
        if (tempDistance < 1000000000){
            tempLabel = "kilometers";
            tempScale = 1000;
        }
        if (tempDistance < 1000000){
            tempLabel = "kilometers";
            tempScale = 1000;
        }
        if (tempDistance < 1000){
            tempLabel = "meters";
            tempScale = 1;
        }
        if (tempDistance < 1){
            tempLabel = "centimeters";
            tempScale = 0.01;
        }
        if (tempDistance < 0.01){
            tempLabel = "milimeters";
            tempScale = 0.001;
        }
        if (tempDistance < 0.001){
            tempLabel = "micrometers";
            tempScale = 0.000001;
        }
        if (tempDistance < 0.001){
            tempLabel = "nanometers";
            tempScale = 0.000000001;
        }
        int tempr=(int) (tempDistance/tempScale+0.5);
        tempLabel = ""+tempr+" "+tempLabel;

        // how tall are the uprights
        int tempUprightHeight = 10;
        
        // how big is the rectangle we need to use to draw this ScaleBar.
        int tempX2 = inConverter.toScreenX(tempWorldX1+tempDistance);
        Graphics tempLabelGraphics = getLabelGraphics(inGraphics);
        FontMetrics fm = tempLabelGraphics.getFontMetrics();
        int tempLabelWidth = fm.stringWidth(tempLabel)+tempUprightHeight;
        int tempLabelHeight = fm.getMaxAscent();

        // width of the rectangle
        int tempBarWidth = tempX2;
        if (tempLabelWidth > tempX2) tempBarWidth = tempLabelWidth;
        
        // height of the rectangle
        int tempBarHeight = tempLabelHeight + tempUprightHeight;
        
        // find the starting location
        Point tempStartPoint = getStart(inWidth, inHeight, tempBarWidth, tempBarHeight);
        
        // draw the main line
        Graphics tempLineGraphics = getLineGraphics(inGraphics);
        tempLineGraphics.drawLine(tempStartPoint.x, tempStartPoint.y+tempUprightHeight/2, tempStartPoint.x+tempX2, tempStartPoint.y+tempUprightHeight/2);
        
        // draw lines on the end of the line
        tempLineGraphics.drawLine(tempStartPoint.x, tempStartPoint.y, tempStartPoint.x, tempStartPoint.y+tempUprightHeight);
        tempLineGraphics.drawLine(tempStartPoint.x+tempX2, tempStartPoint.y, tempStartPoint.x+tempX2, tempStartPoint.y+tempUprightHeight);
        
        // label the line
        tempLabelGraphics = getLabelGraphics(inGraphics);
        tempLabelGraphics.drawString(tempLabel, tempStartPoint.x+tempUprightHeight, tempStartPoint.y+tempLabelHeight+tempUprightHeight);
    }
        
    /** Get the configuration information for this ScaleBar  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("MeterToMeterScaleBar");
        return tempRoot;
    }
    
    /** Set the configuration information for this ScaleBar  */
    public void setNode(Node inNode) throws Exception {
        super.setNode(inNode);
    }
}
