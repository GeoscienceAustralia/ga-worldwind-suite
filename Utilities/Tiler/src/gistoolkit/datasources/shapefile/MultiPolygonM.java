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
 * Just like MultiPolygon, but with points with m values.
 */
public class MultiPolygonM extends MultiPolygon {
    /**
     * The minimum M value.
     */
    private double myMinM = 0;
    
    /**
     * The maximum M value.
     */
    private double myMaxM = 0;
    
    /**
     * MultiPolygonM constructor comment.
     */
    public MultiPolygonM() {
        super();
    }
    /**
     * MultiPolygonM constructor comment.
     * @param inPolygons gistoolkit.features.Polygon[]
     */
    public MultiPolygonM(gistoolkit.features.Polygon[] inPolygons) {
        super(inPolygons);
    }
    /**
     * MultiPolygonM constructor comment.
     * @param inPolygons gistoolkit.features.Polygon[]
     * @param inEnvelope gistoolkit.features.Envelope
     */
    public MultiPolygonM(gistoolkit.features.Polygon[] inPolygons, Envelope inEnvelope) {
        super(inPolygons, inEnvelope);
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
