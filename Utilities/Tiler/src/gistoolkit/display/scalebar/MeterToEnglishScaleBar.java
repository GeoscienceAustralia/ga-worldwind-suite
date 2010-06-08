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
public class MeterToEnglishScaleBar extends SimpleScaleBar{
    /** 
     * A description to be displayed to the user that indicates what this scale bar does.
     * A good thing to indicates is that it takes a projection in meters and displays a scale in metric.
     * Or that it takes a projection in feed and displays a scale in in,ft,yd,and miles.
     * <p> This one returns kilometers, meters, centimters, milimeters, and micrometers </p>
     */
    public String getDescription(){return "From a meter projection, displays mi,yd,ft,in";}
        
    /** Method to draw a scale bar on the graphics context. */
    public void drawScale(Graphics inGraphics, Converter inConverter, int inWidth, int inHeight){
        // find out how wide the display is
        Rectangle tempScreenRect = getAvailableSize(inWidth, inHeight);
        double tempWorldX1 = inConverter.toWorldX(0);
        double tempWorldWidth = inConverter.toWorldX(tempScreenRect.width)-tempWorldX1;
        if (tempWorldWidth < 0) return;
        
        // convert the world width to feet.
        double tempWorldFeet = (tempWorldWidth * 3.28084);
        
        // check if this is in the range of miles.
        String tempLabel = "";
        double tempDistance = tempWorldFeet;
        if (tempWorldFeet > 5280){
            double tempWorldMiles = tempWorldFeet/5280;
            double tempTestDistance = 1000000000; // billion
            while (tempTestDistance > 0.1){
                if (tempWorldMiles < tempTestDistance*10){
                    tempDistance = tempTestDistance*5;
                }
                if (tempWorldMiles < tempTestDistance*5){
                    tempDistance = tempTestDistance*3;
                }
                if (tempWorldMiles < tempTestDistance*3){
                    tempDistance = tempTestDistance*1;
                }
                if (tempWorldMiles < tempTestDistance*2){
                    tempDistance = tempTestDistance;
                }
                tempTestDistance = tempTestDistance/10;
            }
            // the distance to draw in Feet
            int tempr=(int) (tempDistance+0.5);
            tempLabel = ""+tempr+" miles";
            tempDistance = tempDistance * 5280;
        }
        else if( tempWorldFeet > 3){
            double tempWorldYards = tempWorldFeet/3;
            double tempTestDistance = 100000;
            while (tempTestDistance > 0.1){
                if (tempWorldYards < tempTestDistance*10){
                    tempDistance = tempTestDistance*5;
                }
                if (tempWorldYards < tempTestDistance*5){
                    tempDistance = tempTestDistance*3;
                }
                if (tempWorldYards < tempTestDistance*3){
                    tempDistance = tempTestDistance*1;
                }
                if (tempWorldYards < tempTestDistance*2){
                    tempDistance = tempTestDistance;
                }
                tempTestDistance = tempTestDistance/10;
            }
            // the distance to draw in Feet
            int tempr=(int) (tempDistance+0.5);
            tempLabel = ""+tempr + " yards";
            tempDistance = tempDistance * 3;
        }
        else if (tempWorldFeet > 1){
            if (tempWorldFeet < 3){
                tempDistance = 2;
            }
            if (tempWorldFeet < 2){
                tempDistance = 1;
            }
            int tempr=(int) (tempDistance+0.5);
            tempLabel = tempr + " feet";
        }
        else if (tempWorldFeet > 0.0833){
            double tempWorldInches = tempWorldFeet/12;
            double tempTestDistance = 1.2;
            if (tempWorldFeet < tempTestDistance*10){
                tempDistance = tempTestDistance*5;
            }
            if (tempWorldFeet < tempTestDistance*5){
                tempDistance = tempTestDistance*3;
            }
            if (tempWorldFeet < tempTestDistance*3){
                tempDistance = tempTestDistance*1;
            }
            if (tempWorldFeet < tempTestDistance*2){
                tempDistance = tempTestDistance;
            }
            // the distance to draw in feet
            int tempr=(int) (tempDistance+0.5);
            tempLabel = tempr+" inches";
            tempDistance = tempDistance/12;
        }
        else{
            tempLabel = (tempWorldFeet/12)+"inches";
        }

        // how tall are the uprights
        int tempUprightHeight = 10;
        
        // how big is the rectangle we need to use to draw this ScaleBar.
        int tempX2 = inConverter.toScreenX(tempWorldX1+tempDistance/3.28084);
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
