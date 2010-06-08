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

import gistoolkit.features.*;
/**
 * Just like a MultiLineStringM, but with the addition of a z coordinate to each node.
 */
public class MultiLineStringZ extends MultiLineStringM {
    /**
     * The minimum Z value.
     */
    private double myMinZ = 0;
    
    /**
     * The maximum Z value.
     */
    private double myMaxZ = 0;
    /**
     * MultiLineStringM constructor comment.
     */
    public MultiLineStringZ() {
        super();
    }
    /**
     * MultiLineStringM constructor comment.
     * @param inLines gistoolkit.features.LineString[]
     */
    public MultiLineStringZ(gistoolkit.features.LineString[] inLines) {
        super(inLines);
    }
    /**
     * MultiLineStringM constructor comment.
     * @param inLines gistoolkit.features.LineString
     */
    public MultiLineStringZ(LineString inLines) {
        super(inLines);
    }
    /**
     * returns the maximum Z value.
     */
    public double getMaxZ(){
        return myMaxZ;
    }
    /**
     * returns the minimum Z value.
     */
    public double getMinZ(){
        return myMinZ;
    }
    /**
     * Sets the maximum Z value;
     */
    public void setMaxZ(double inMaxZ){
        myMaxZ = inMaxZ;
    }
    /**
     * Sets the minimum Z value;
     */
    public void setMinZ(double inMinZ){
        myMinZ = inMinZ;
    }
}
