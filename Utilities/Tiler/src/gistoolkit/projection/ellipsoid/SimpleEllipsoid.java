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

package gistoolkit.projection.ellipsoid;

import gistoolkit.projection.*;
/**
 * Simple class to be used as a super class of sub Ellipsoids.
 */
public class SimpleEllipsoid implements Ellipsoid{

    /** Creates new SimpleEllipsoid */
    public SimpleEllipsoid() {
    }

    /** the semi major axis for WGS84 in meters */
    private double mySemiMajor = 6378137.000;
    /** set the major axis for this Ellipsoid */
    public void setMajorAxis(double inMajorAxis){mySemiMajor = inMajorAxis;}
    
    /** the semi minor axis for WGS84 in meters */
    private double mySemiMinor = 6356752.3141;
    /** set the minor axis for this Ellipsoid */
    public void setMinorAxis(double inMinorAxis){mySemiMinor = inMinorAxis;}
    
    /** The units of measure for this ellipsoid */
    private String myUnitOfMeasure = "meter";
    /** Sset the unit of measure of this ellipsoid */
    public void setUnitOfMeasure(String inUnitOfMeasure){myUnitOfMeasure = inUnitOfMeasure;}
    
    /** The name of the Ellipsoid, to be displayed to the user*/
    private String myName = "WGS84";
    /** set the name of the Ellipsoid to be displayed to the user */
    public void setName(String inName){myName = inName;}
    
    /** A more descriptive name of the Ellipsoid */
    private String myDescriptiveName = "WGS84, GRS80: WGS84, ITRS, ETRS89";
    /** Set the descriptive name to be displayed to the user */
    public void setDescriptiveName(String inDescriptiveName){myDescriptiveName = inDescriptiveName;}
        
    /**
     * Returns the semi-major axis of the Ellipsoid.
     */
    public double getMajorAxis() {
        return mySemiMajor;
    }
    
    /**
     * Returns the semi-minor axis.
     */
    public double getMinorAxis() {
        return mySemiMinor;
    }
    
    /**
     * Returns the unit of measure for this ellipsoid.
     */
    public String getUnitOfMeasure(){return myUnitOfMeasure;}
    
    /**
     * Returns the name of the Ellipsoid.
     */
    public String getName() {
        return myName;
    }
    
    /**
     * Returns a description of the Ellipsoid, good place for any aliases.
     */
    public String getDescriptiveName() {
        return myDescriptiveName;
    }
    
    /**
     * Return the name of the Ellipsoid as the to string value.
     **/
    public String toString(){return getName();}
}
