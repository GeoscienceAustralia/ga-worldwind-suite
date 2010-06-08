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

import gistoolkit.features.Point;
import gistoolkit.projection.ellipsoid.*;


/*
 * Class to convert latitude/longitude to British National Grid.
 */
public class BritishNationalGridProjection extends TransversMercatorProjection {
        
    /** return the name of the projection  */
    public String getProjectionName() {
        return "British National Grid";
    }
    
    /**
     * Default constructor.
     */
    public BritishNationalGridProjection() {
        setEllipsoid(new Airy1830());
        setLonOragin(-2);
        setLatOragin(49);
        setCentralScale(0.9996012717);
        setEasting(400000);
        setNorthing(100000);
    }
        
    public Object[] degreeToSquare(double lat, double lon) {
        Point tempPoint = new Point(lon, lat);
        projectForward(tempPoint);
        int easting = (int) (tempPoint.getX() + 0.5);
        int northing = (int) (tempPoint.getY() + 0.5);
        int posx = easting / 500000;
        int posy = northing / 500000;
        String gridSquares = "VWXYZQRSTULMNOPFGHJKABCDE";
        StringBuffer returnSquare = new StringBuffer();
        
        returnSquare.append(gridSquares.charAt(posx + posy * 5 + 7));
        posx = (easting % 500000) / 100000;
        posy = (northing % 500000) / 100000;
        returnSquare.append(gridSquares.charAt(posx + posy * 5));
        
        return (new Object[] {
            returnSquare.toString(),
            new Integer(easting % 100000),
            new Integer(northing % 100000)
        });
    }
        
    /**
     * For testing. Call the class from the command line with two parameters,
     * first latitude, then longitude.
     */
    public static void main(String arg[]) {
        BritishNationalGridProjection tempBNP = new BritishNationalGridProjection();
        if (arg.length != 2) {
            System.out.println("Usage: java GridConverter lat lon");
            System.out.println("  where lat, lon = latitude and longitude to convert");
            System.out.println("  (use negative longitude for west)");
            System.exit(1);
        }
        
        double lat = Double.parseDouble(arg[0]);
        double lon = Double.parseDouble(arg[1]);
        
        Point tempPoint = new Point(lon, lat);
        tempBNP.projectForward(tempPoint);
        
        System.out.println("latitude:          " + lat);
        System.out.println("longitude:         " + lon);
        System.out.println();
        System.out.println("full easting:      " + tempPoint.getX());
        System.out.println("full northing:     " + tempPoint.getY());
        System.out.println();
        
        Object[] square = tempBNP.degreeToSquare(lat, lon);
        System.out.println("square designator: " + square[0]);
        System.out.println("square easting:    " + square[1]);
        System.out.println("square northing:   " + square[2]);
        
    }    
}
