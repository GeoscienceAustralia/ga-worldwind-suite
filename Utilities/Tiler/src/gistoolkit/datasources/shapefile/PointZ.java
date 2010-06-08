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

package gistoolkit.datasources.shapefile;

/**
 * This point contains an additional double precision measure attribute.
 */
public class PointZ extends PointM {
    /**
     * Place to contain the double value for the Z coordinate.
     */
    private double myZ;
    
    /**
     * return the Z coordinate.
     */
    public double getZ(){
        return myZ;
    }
    /**
     * Sets the Z coordinate.
     */
    public void setZ(double inZ){
        myZ = inZ;
    }
    
    public PointZ(double inX, double inY){
        super(inX, inY);
    }
}
