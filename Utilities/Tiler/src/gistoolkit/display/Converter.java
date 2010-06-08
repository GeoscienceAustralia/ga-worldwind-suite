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

package gistoolkit.display;

import gistoolkit.features.Envelope;
/**
 * Class to convert from world coordinates to screen coordinates.
 * The individual layers will have data in their respective projections.
 * Typically, this will all be one projection.
 * The converter will then convertfrom that projection to screen coordinates.
 */
public class Converter {
    private Envelope myWorldEnvelope;
    private Envelope myScreenEnvelope;
    private double myXMultiplier;
    private double myYMultiplier;
    
    /**
     * Create a converter to convert from the given screen Envelope to the given
     * world Envelope, and vice versa. 
     */
    public Converter(Envelope inScreenEnvelope, Envelope inWorldEnvelope){
        this(inScreenEnvelope, inWorldEnvelope, true);
    }
    /**
     * Create a converter to convert from the given screen Envelope to the given
     * world Envelope, and vice versa. The Square parameter indicates if the converter needs to maintain
     * The aspect ratio of the image.  If true is sent in, then the converter will modify the bounds of the world
     * to create an image at the correct aspect ratio.  If a false is sent in, then the converter will not change the screen dimensions, or
     * change the world dimensions.  In this case, the resulting map may be compressed some what.
     */
    public Converter(Envelope inScreenEnvelope, Envelope inWorldEnvelope, boolean inSquare){
        // save the bounds
        myWorldEnvelope = inWorldEnvelope;
        myScreenEnvelope = inScreenEnvelope;
        
        // figure out the multipliers
        myXMultiplier = (inScreenEnvelope.getMaxX()-inScreenEnvelope.getMinX())/(inWorldEnvelope.getMaxX()-inWorldEnvelope.getMinX());
        myYMultiplier = (inScreenEnvelope.getMaxY()-inScreenEnvelope.getMinY())/(inWorldEnvelope.getMinY()-inWorldEnvelope.getMaxY());
        
        if (inSquare){
            // Decide whether the xdirection or y direction is limiting.
            if (myXMultiplier < -myYMultiplier){
                myYMultiplier = -myXMultiplier;
            }

            // Decide wheather the xdirection or y direction is limiting.
            if (-myYMultiplier <= myXMultiplier){
                myXMultiplier = -myYMultiplier;                
            }
            
            //X coordinate.
            double tempNewXwidth = (inScreenEnvelope.getMaxX() - inScreenEnvelope.getMinX())/myXMultiplier;
            double tempOldXwidth = (inWorldEnvelope.getMaxX() - inWorldEnvelope.getMinX());
            double tempXDiff = (tempNewXwidth - tempOldXwidth)/2;

            // Y coordinate
            double tempNewYHeight = (inScreenEnvelope.getMinY() - inScreenEnvelope.getMaxY())/myYMultiplier;
            double tempOldYHeight = (inWorldEnvelope.getMinY() - inWorldEnvelope.getMaxY());
            double tempYDiff = (tempNewYHeight - tempOldYHeight)/2;

            // calculate the new world Envelope.
            myWorldEnvelope = new Envelope(
                inWorldEnvelope.getMinX() - tempXDiff,
                inWorldEnvelope.getMaxY() - tempYDiff,
                inWorldEnvelope.getMaxX() + tempXDiff,
                inWorldEnvelope.getMinY() + tempYDiff
                );                
        }
    }
    
    /**
     * Convert the given world x coordinate to a corrisponding screen coordinate.
     */
    public int convertX(double inX) {
        return (int) ((inX - myWorldEnvelope.getMinX()) * myXMultiplier);
    }
    public int convertY(double inY){
        return (int) ((inY-myWorldEnvelope.getMaxY())*myYMultiplier);
    }
    
    /**
     * Returns a copy of the world Envelope used to create this converter.
     */
    public Envelope getWorldEnvelope() {
        if (myWorldEnvelope == null)
            return myWorldEnvelope;
        else
            return (Envelope) myWorldEnvelope.clone();
    }
    
    /**
     * Returns a copy of the screen Envelope used to create this converter.
     */
    public Envelope getScreenEnvelope() {
        if (myScreenEnvelope == null)
            return myScreenEnvelope;
        else
            return (Envelope) myScreenEnvelope.clone();
    }

    /**
     * Converts the given world coordinate in X to a screen pixel coordinate in X.
     */
    public int toScreenX(double inX) {
        return (int) ((inX - myWorldEnvelope.getMinX())*myXMultiplier);
    }
    
    /**
     * Converts the given world coordinate in Y to a screen pixel coordinate in Y.
     */
    public int toScreenY(double inY) {
        return (int) ((inY - myWorldEnvelope.getMaxY()) * myYMultiplier);
    }
    
    /**
     * Converts the given screen X pixel coordinate to a world X coordinate.
     */
    public double toWorldX(int inX) {
        return inX/myXMultiplier +myWorldEnvelope.getMinX();
    }
    
    /**
     * Converts the given screen Y pixel coordinate to a world Y coordinate.
     */
    public double toWorldY(int inY) {
        return inY/myYMultiplier +myWorldEnvelope.getMaxY();
    }
    
    /** Return the width of the world */
    public double getWorldWidth(){ return myWorldEnvelope.getWidth();}
    /** Retrieve the height of the world */
    public double getWorldHeight(){ return myWorldEnvelope.getHeight();}
    /** Return the width of the screen*/
    public double getScreenWidth(){ return myScreenEnvelope.getWidth();}
    /** Return the height of the screen*/
    public double getScreenHeight(){ return myScreenEnvelope.getHeight();}
}