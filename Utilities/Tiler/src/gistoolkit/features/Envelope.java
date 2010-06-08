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

package gistoolkit.features;

/**
 * Class to represent a 2 dimensional rectangular area in space.
 * <p>
 * This class is immutable, and can not be changed after instantiated.  To create an envelope that can be changed,
 * create an EnvelopeBuffer from the shapeutils package, and use it for dynamic expansions and contractions of envelopes.
 * </p>
 */
public class Envelope {
    
    /** The minimum x-coordinate.*/
    private double myMinX;
    /** Returns the minimum X coordinate. */
    public double getMinX(){return myMinX;}
    
    /** The maximum x-coordinate*/
    private double myMaxX;
    /** Returns the maximum X coordinate. */
    public double getMaxX(){return myMaxX;}
    
    /** The minimum y-coordinate*/
    private double myMinY;
    /** Returns the minimum Y coordinate. */
    public double getMinY(){return myMinY;}
    
    /** The maximum y-coordinate*/
    private double myMaxY;
    /** Returns the maximum Y coordinate. */
    public double getMaxY(){return myMaxY;}
    
    /**
     * Create a new Envelope with the given values.  The bottomX is the maximum X, and the Top X is the minimum X.
     */
    public Envelope(double inMinX, double inMinY, double inMaxX, double inMaxY) {
        if (inMinX > inMaxX){
            myMaxX = inMinX;
            myMinX = inMaxX;
        }
        else{
            myMinX = inMinX;
            myMaxX = inMaxX;
        }
        if (inMinY > inMaxY){
            myMaxY = inMinY;
            myMinY = inMaxY;
        }
        else{
            myMinY = inMinY;
            myMaxY = inMaxY;
        }
    }
    
    /**
     * Returns the width of this envelope
     */
    public double getWidth(){
        return Math.abs(myMaxX-myMinX);
    }
    
    /**
     * Returns the height of this envelope
     */
    public double getHeight(){
        return Math.abs(myMaxY-myMinY);
    }
    
    /**
     * Create a copy of the envelope.
     */
    public Object clone() {
        return new Envelope(myMinX, myMinY, myMaxX, myMaxY);
    }
    
    /**
     * Check if the two envelopes are equal. This is checked against the EqualLimit in Shape.
     */
    public boolean isEqual(Envelope inEnvelope){
        if (inEnvelope == null) return false;
        if (Math.abs(myMinX-inEnvelope.getMinX()) > Shape.EQUAL_LIMIT) return false;
        if (Math.abs(myMinY-inEnvelope.getMinY()) > Shape.EQUAL_LIMIT) return false;
        if (Math.abs(myMaxX-inEnvelope.getMaxX()) > Shape.EQUAL_LIMIT) return false;
        if (Math.abs(myMaxY-inEnvelope.getMaxY()) > Shape.EQUAL_LIMIT) return false;
        return true;
    }
    
    /**
     * Determines if the Extents sent in is within the current Extents.
     * Compares with equality, such that if the Extents sent in is the same as the current Extents,
     * then the result will be true.
     */
    public boolean overlaps(Envelope inEnvelope) {
        if (inEnvelope == null)
            return false;
        
        // check the sides.
        if (inEnvelope.getMaxX() < myMinX)
            return false;
        if (inEnvelope.getMinX() > myMaxX)
            return false;
        if (inEnvelope.getMinY() > myMaxY)
            return false;
        if (inEnvelope.getMaxY() < myMinY)
            return false;
        return true;
    }
    /**
     * Return the overlap of these two extents, returns null if they do not overap.
     * Compares with equality, such that if the Extents sent in is the same as the current Extents,
     * then the result will be true.
     */
    public Envelope getOverlap(Envelope inEnvelope) {
        if (!overlaps(inEnvelope)) return null;

        double tempMinX = inEnvelope.getMinX();
        if (tempMinX < myMinX) tempMinX = myMinX;
        double tempMinY = inEnvelope.getMinY();
        if (tempMinY < myMinY) tempMinY = myMinY;
        double tempMaxX = inEnvelope.getMaxX();
        if (tempMaxX > myMaxX) tempMaxX = myMaxX;
        double tempMaxY = inEnvelope.getMaxY();
        if (tempMaxY > myMaxY) tempMaxY = myMaxY;
        return new Envelope(tempMinX, tempMinY, tempMaxX, tempMaxY);        
    }
    /**
     * Determines if the Extents sent in overlap the current extents.
     * Compares with equality, such that if the Extents sent in shares a partial side with these extents, then it will return true.
     * then the result will be true.
     */
    public boolean intersects(Envelope inEnvelope) {
        return overlaps(inEnvelope);
    }
    
    /**
     * Determines if the Extents sent in is within the current Extents.
     * Compares with equality, such that if the Extents sent in is the same as the current Extents,
     * then the result will be true.
     */
    public boolean contains(Envelope inEnvelope) {
        return inEnvelope.getMinX() >= myMinX &&
        inEnvelope.getMaxX() <= myMaxX &&
        inEnvelope.getMinY() >= myMinY &&
        inEnvelope.getMaxY() <= myMaxY;
    }

    /** Determines if the point sent in is within the current envelope. */
    public boolean contains(double inX, double inY) {
        return inX >= myMinX &&
        inX <= myMaxX &&
        inY >= myMinY &&
        inY <= myMaxY;
    }
    
    /**
     * Return the polygon for this Shape.
     */
    public Polygon getPolygon(){
        Point[] tempPoints = new Point[5];
        tempPoints[0] = new Point(myMinX, myMaxY);
        tempPoints[1] = new Point(myMaxX, myMaxY);
        tempPoints[2] = new Point(myMaxX, myMinY);
        tempPoints[3] = new Point(myMinX, myMinY);
        tempPoints[4] = new Point(myMinX, myMaxY);
        return new Polygon(new LinearRing(tempPoints));
    }
    
    /**
     * The to string method for debugging.
     */
    public String toString() {
        return "MinX="+myMinX+" MinY="+myMinY+" MaxX="+ myMaxX+" MaxY="+myMaxY;
    }
}
