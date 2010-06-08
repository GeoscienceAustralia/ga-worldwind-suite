/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2003, Ithaqua Enterprises Inc.
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

package gistoolkit.display.shader.stroke;

import java.awt.*;
import java.awt.geom.*;

/**
 * A class to implement the railroad stroking of lines that is often desired in maps.
 *
 * This class draws a line as a single line, with cross bars to represent the railroad ties.
 */
public class RailRoadStroke implements Stroke{
    private BasicStroke myLineStroke;
    private BasicStroke myTwoLineStroke;
    private BasicStroke myTieStroke;
    
    /** Use two lines the same distance apart as the continuious lines of the rail road.*/
    private boolean myTwoLine = false;
    /** Use two lines the same distance apart as the continuious lines of the rail road.*/
    public boolean getTwoLine(){return myTwoLine;}
    
    /** The width of the railroad ties.  These are the lines that stick out beyond the ends of the
     * actual rails.
     */
    float myTieWidth;
    /** The width of the railroad ties.  These are the lines that stick out beyond the ends of the
     * actual rails.
     */
    public float getTieWidth(){return myTieWidth;}
    
    /** The width of the "line" that is to be used as the single rail of the rail road. */
    float myLineWidth;
    /** Return the width of the "Line" that is to be used as the single rail of the rail road. */
    public float getLineWidth(){return myLineWidth;}
    
    /** The dash array to use with this RailRoadStroke. */
    private float[] myTieDashArray = {(float)2.0,(float)10.0};
    public float[] getDashArray(){ return myTieDashArray;}
    
    /**
     * Creates a new instance of RailRoadStroke, the inLineWidth is the thickness of the line, the inTieWidth is the
     * Length of the ties of the rail road.
     */
    public RailRoadStroke(float inLineWidth, float inTieWidth) {
        myLineStroke = new BasicStroke(inLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        myTieStroke = new BasicStroke(inTieWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, myTieDashArray, (float) 0 );
        myTieWidth = inTieWidth;
        myLineWidth = inLineWidth;
    }
    
    /**
     * Creates a new instance of RailRoadStroke, the inLineWidth is the thickness of the line, the inTieWidth is the
     * Length of the ties of the rail road.
     */
    public RailRoadStroke(float inLineWidth, float inTieWidth, boolean inTwoLine) {
        myLineStroke = new BasicStroke(inLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        myTwoLine = inTwoLine;        
        if (myTwoLine){
            myTwoLineStroke = new BasicStroke(inLineWidth*2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        }
        myTieStroke = new BasicStroke(inTieWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, myTieDashArray, (float) 0 );
        myTieWidth = inTieWidth;
        myLineWidth = inLineWidth;
    }
    
    /**
     * Creates a new instance of RailRoadStroke, the inLineWidth is the thickness of the line, the inTieWidth is the
     * Length of the ties of the rail road. The Dash array describes the pattern of "ties" in the railroad.
     */
    public RailRoadStroke(float inLineWidth, float inTieWidth, float[] inTieDashArray, boolean inTwoLine) {
        myTieDashArray = inTieDashArray;
        myLineStroke = new BasicStroke(inLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        myTieStroke = new BasicStroke(inTieWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, myTieDashArray, (float) 0 );
        myTieWidth = inTieWidth;
        myLineWidth = inLineWidth;
        myTwoLine = inTwoLine;        
        if (myTwoLine){
            myTwoLineStroke = new BasicStroke(inLineWidth*2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND );
        }
    }
    
    public Shape createStrokedShape(Shape shape) {
        GeneralPath newPath = new GeneralPath(); //Start with an empty shape
        Shape newShape = null;
        if (myTwoLine){
            newShape = myTwoLineStroke.createStrokedShape(shape);
            newShape = myLineStroke.createStrokedShape(newShape);
        }
        else{
            newShape = myLineStroke.createStrokedShape(shape);
        }
        newPath.append(newShape, false);
        newShape = myTieStroke.createStrokedShape(shape);
        newPath.append(newShape, false);
        return newPath;
    }
}