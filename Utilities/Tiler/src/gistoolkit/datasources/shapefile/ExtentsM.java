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
 * Extents to support the M value
 * @author head
 */
public class ExtentsM extends Envelope {
    public double myMinM = Double.NaN;
    public double myMaxM = Double.NaN;
    /**
     * ExtentsZ constructor comment.
     * @param inTopX double
     * @param inTopY double
     * @param inBottomX double
     * @param inBottomY double
     */
    public ExtentsM(double inTopX, double inTopY, double inBottomX, double inBottomY) {
        super(inTopX, inTopY, inBottomX, inBottomY);
    }
    /**
     * ExtentsZ constructor comment.
     * @param inTopX double
     * @param inTopY double
     * @param inBottomX double
     * @param inBottomY double
     */
    public ExtentsM(double inTopX, double inTopY, double inBottomX, double inBottomY, double inMinM, double inMaxM) {
        super(inTopX, inTopY, inBottomX, inBottomY);
        myMinM = inMinM;
        myMaxM = inMaxM;
    }
}
