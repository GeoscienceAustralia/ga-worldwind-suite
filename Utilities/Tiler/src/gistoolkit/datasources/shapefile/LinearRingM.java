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
 * Just like a linear ring but with M values for the points
 */
public class LinearRingM extends LinearRing {
    /**
     * The minimum M value.
     */
    private double myMinM = 0;
    
    /**
     * The maximum M value.
     */
    private double myMaxM = 0;
    
    /**
     * LinearRingM constructor comment.
     */
    public LinearRingM() {
        super();
    }
    /**
     * LinearRingM constructor comment.
     * @param inXs double[]
     * @param inYs double[]
     */
    public LinearRingM(double[] inXs, double[] inYs) {
        super(inXs, inYs);
    }
    /**
     * LinearRingM constructor comment.
     * @param inPoints gistoolkit.features.Point[]
     */
    public LinearRingM(gistoolkit.features.Point[] inPoints) {
        super(inPoints);
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
