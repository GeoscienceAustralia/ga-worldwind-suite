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

package gistoolkit.projection;

/**
 * Class to contain the parameters associated with an elipsoid used in modeling the lat long
 * measurements of the earth.
 */
public interface Ellipsoid {
    /**
     * Returns the units of measure for the ellipsoid.
     */
    public String getUnitOfMeasure();
    
    /**
     * Returns the semi-major axis of the elipsoid.
     */
    public double getMajorAxis();
    
    /**
     * Returns the semi-minor axis.
     */
    public double getMinorAxis();
        
    /**
     * Returns the name of the elipsoid.
     */
    public String getName();
    
    /**
     * Returns a description of the elipsoid, good place for any aliases.
     */
    public String getDescriptiveName();
}

