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
 * Spheroid used to describe the International 1924 or Hayford 1909 sphere, I must confess I don't know what these are,
 * but I have the parameters, so they are immortalized here.
 */
public class I1924UTM extends SimpleEllipsoid {

    /** Creates new I1924UTM */
    public I1924UTM() {
        setMajorAxis(6378388.000); // meters
        setMinorAxis(6356911.946); // meters
        setName("International 1924");
        setDescriptiveName("International 1924, Hayford 1909: ED50, UTM");
    }
}
