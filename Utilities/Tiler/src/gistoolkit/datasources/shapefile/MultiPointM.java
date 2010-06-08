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
 * A collection of multi points of the type M
 */
public class MultiPointM extends MultiPoint {
    /**
     * The minimum M value.
     */
    private double myMinM = 0;
    
    /**
     * The maximum M value.
     */
    private double myMaxM = 0;
    
    /**
     * MultiPointM constructor comment.
     */
    public MultiPointM() {
        super();
    }
    /**
     * MultiPointM constructor comment.
     * @param inXs double[]
     * @param inYs double[]
     */
    public MultiPointM(double[] inXs, double[] inYs) {
        super(inXs, inYs);
    }
    /**
     * MultiPointM constructor comment.
     * @param inPoints gistoolkit.features.Point[]
     */
    public MultiPointM(gistoolkit.features.Point[] inPoints) {
        super(inPoints);
    }
    /**
     * MultiPointM constructor comment.
     * @param inPoints gistoolkit.features.Point[]
     */
    public MultiPointM(gistoolkit.features.Point[] inPoints, double inMmin, double inMmax, double[] inMs) {
        super(inPoints);
    }
    /**
     * MultiPointM constructor comment.
     * @param inX double
     * @param inY double
     */
    public MultiPointM(double inX, double inY) {
        super(inX, inY);
    }
    /**
     * returns the maximum M value.
     */
    public double getMaxM(){
        return myMaxM;
    }
    /**
     * returns the minimum M value.
     */
    public double getMinM(){
        return myMinM;
    }
    /**
     * Sets the maximum M value;
     */
    public void setMaxM(double inMaxM){
        myMaxM = inMaxM;
    }
    /**
     * Sets the minimum M value;
     */
    public void setMinM(double inMinM){
        myMinM = inMinM;
    }
}
