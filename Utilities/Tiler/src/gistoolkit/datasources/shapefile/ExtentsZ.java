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
 * Insert the type's description here.
 * @author head
 */
public class ExtentsZ extends ExtentsM {
    public double myMinZ = Double.NaN;
    public double myMaxZ = Double.NaN;
    /**
     * ExtentsZ constructor comment.
     * @param inTopX double
     * @param inTopY double
     * @param inBottomX double
     * @param inBottomY double
     */
    public ExtentsZ(double inTopX, double inTopY, double inBottomX, double inBottomY) {
        super(inTopX, inTopY, inBottomX, inBottomY);
    }
    /**
     * ExtentsZ constructor comment.
     * @param inTopX double
     * @param inTopY double
     * @param inBottomX double
     * @param inBottomY double
     */
    public ExtentsZ(double inTopX, double inTopY, double inBottomX, double inBottomY, double inMinZ, double inMaxZ) {
        super(inTopX, inTopY, inBottomX, inBottomY);
        myMinZ = inMinZ;
        myMaxZ = inMaxZ;
    }
    /**
     * ExtentsZ constructor comment.
     * @param inTopX double
     * @param inTopY double
     * @param inBottomX double
     * @param inBottomY double
     */
    public ExtentsZ(double inTopX, double inTopY, double inBottomX, double inBottomY, double inMinZ, double inMaxZ, double inMinM, double inMaxM) {
        super(inTopX, inTopY, inBottomX, inBottomY);
        myMinZ = inMinZ;
        myMaxZ = inMaxZ;
        myMinM = inMinM;
        myMaxM = inMaxM;
    }
}
