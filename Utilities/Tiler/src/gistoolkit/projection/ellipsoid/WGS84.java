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

/**
 * The Ellipsoid used to describe the WGS84 lat long conversion same as GRS80.
 */
public class WGS84 extends SimpleEllipsoid {

    /** Creates new WGS84 */
    public WGS84() {
        setMajorAxis(6378137.000); // meters
        setMinorAxis(6356752.3141); // meters
        setName("WGS 84");
        setDescriptiveName("WGS84, GRS80: WGS84, ITRS, ETRS89");
    }
}
