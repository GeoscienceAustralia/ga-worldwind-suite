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

package gistoolkit.features.featureutils;

import gistoolkit.features.*;

/**
 * Class to allow changes to an envelope.
 */
public class EnvelopeBuffer {
    
    /** Determines if this buffer is empty. */
    private boolean myIsEmpty = true;
    
    /** the minimum x-coordinate*/
    private double myMinX;
    /** Return the Minimum X coordinate. */
    public double getMinX(){return myMinX;}
    /** Set the Minimum X coordinate. */
    public void setMinX(double inMinX){myMinX = inMinX;}
    
    /** the maximum x-coordinate*/
    private double myMaxX;
    /** Return the Maximum X coordinate. */
    public double getMaxX(){return myMaxX;}
    /** Set the Maximum X coordinate. */
    public void setMaxX(double inMaxX){myMaxX = inMaxX;}
    
    /** the minimum y-coordinate*/
    private double myMinY;
    /** Return the minimum Y coordinate. */
    public double getMinY(){return myMinY;}
    /** Set the Minimum Y coordinate. */
    public void setMinY(double inMinY){myMinY = inMinY;}
    
    /** the maximum y-coordinate*/
    private double myMaxY;
    /** Return the Maximum Y coordinate. */
    public double getMaxY(){return myMaxY;}
    /** Set the Maximum Y coordinate. */
    public void setMaxY(double inMaxY){myMaxY = inMaxY;}
    
    /** Creates an empty EnvelopeBuffer. */
    public EnvelopeBuffer(){}
    
    /** Creates a new instance of EnvelopeBuffer */
    public EnvelopeBuffer(Envelope inEnvelope) {
        myMinX = inEnvelope.getMinX();
        myMinY = inEnvelope.getMinY();
        myMaxX = inEnvelope.getMaxX();
        myMaxY = inEnvelope.getMaxY();
        myIsEmpty=false;
    }
    
    /** Expands this envelope to include the envelope sent in. */
    public void expandToInclude(Envelope inEnvelope){
        if (inEnvelope == null) return;
        if (myIsEmpty){
            myMinX = inEnvelope.getMinX();
            myMinY = inEnvelope.getMinY();
            myMaxX = inEnvelope.getMaxX();
            myMaxY = inEnvelope.getMaxY();
            myIsEmpty = false;
        }
        else{
            if (inEnvelope.getMinX() < myMinX) {
                myMinX = inEnvelope.getMinX();
            }
            if (inEnvelope.getMaxX() > myMaxX) {
                myMaxX = inEnvelope.getMaxX();
            }
            if (inEnvelope.getMinY() < myMinY) {
                myMinY = inEnvelope.getMinY();
            }
            if (inEnvelope.getMaxY() > myMaxY) {
                myMaxY = inEnvelope.getMaxY();
            }
        }
    }
    
    /** Expands this envelope to include the Point sent in. */
    public void expandToInclude(Point inPoint){
        expandToInclude(inPoint.getX(), inPoint.getY());
    }
    /** Expands this envelope to include the Point sent in. */
    public void expandToInclude(double inX, double inY){
        if (myIsEmpty){
            myMinX = inX;
            myMinY = inY;
            myMaxX = inX;
            myMaxY = inY;
            myIsEmpty = false;
        }
        else{
            if (inX < myMinX) {
                myMinX = inX;
            }
            if (inX > myMaxX) {
                myMaxX = inX;
            }
            if (inY < myMinY) {
                myMinY = inY;
            }
            if (inY > myMaxY) {
                myMaxY = inY;
            }
        }
    }
    
    /** Translate this envelope buffer the given distance in X and Y. */
    public void translate(double diffX, double diffY){
        if (myIsEmpty) return;
        myMaxX = myMaxX + diffX;
        myMinX = myMinX + diffX;
        myMaxY = myMaxY + diffY;
        myMinY = myMinY + diffY;
    }
    
    /** Return the envelope from this EnvelopeBuffer. */
    public Envelope getEnvelope(){
        if (myIsEmpty) return null;
        return new Envelope(myMinX, myMinY, myMaxX, myMaxY);
    }
}
