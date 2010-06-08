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

package gistoolkit.projection.ellipsoid.transform;

/**
 * Class to represent a point in three dimensional space.
 */
public class CartesianPoint extends Object {

    /** Creates new CartesianPoint */
    public CartesianPoint() {
    }
    
    /** The X coordinate of this point */
    private double myX = 0;
    /** Return the X coordinate of this point */
    public double getX(){return myX;}
    /** Set the X coordinate of this point */
    public void setX(double inX){myX = inX;}

    /** The Y coordinate of this point */
    private double myY = 0;
    /** Return the Y coordinate of this point */
    public double getY(){return myY;}
    /** Set the Y coordinate of this point */
    public void setY(double inY){myY = inY;}

    /** The Z coordinate of this point */
    private double myZ = 0;
    /** Return the Z coordinate of this point */
    public double getZ(){return myZ;}
    /** Set the Z coordinate of this point */
    public void setZ(double inZ){myZ = inZ;}

    
}
