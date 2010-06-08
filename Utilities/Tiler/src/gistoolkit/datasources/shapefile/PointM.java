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
 * This point contains an additional double precision measure attribute.
 */
public class PointM extends Point {
    /**
     * Place to contain the double value for the M coordinate.
     */
    private double myM;
    
    /**
     * return the M coordinate.
     */
    public double getM(){
        return myM;
    }
    /**
     * Sets the M coordinate.
     */
    public void setM(double inM){
        myM = inM;
    }
    
    public PointM(double inX, double inY){
        super(inX, inY);
    }
}
