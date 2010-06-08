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

import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.projection.ellipsoid.*;
/**
 * <p>
 * A conformal conic projection. Also known as the Conic Orthomorphic projection.
 * This conic projection was designed by Lambert (1772) and has been used extensively for mapping of regions
 * with predominantly east-west orientation, just like the Albers projection. Unlike the Albers projection,
 * Lambert's conformal projection is not equal-area. The parallels are arcs of circles with a common origin,
 * and meridians are the equally spaced radii of these circles. As with Albers projection, it is only the two
 * standard parallels that are distortion-free.
 * </p>
 * <p>
 * from http://gmt.soest.hawaii.edu/gmt/doc/html/GMT_Docs/node43.html
 * </p>
 * <p>
 * Must specify the longitude and latitude of the oragin, as well as two standard parallels and the map scale in inches.
 * As a rule of thumb, these parallels can be placed at one-sixth
 * and five-sixths of the range of latitudes, but there are more refined means of selection.
 * </p>
 * @author  ithaqua
 */
public class LambertConicConformalProjection extends SimpleProjection implements EditableProjection{
    
    /** Creates new LambertConnicConvormalProjection */
    public LambertConicConformalProjection() {
        setEllipsoid(new WGS84());
        setLatOragin(27.833333);
        setLonOragin(-99.000000);
    }
    
    /**
     * Set the scale factor
     */
    private double myScaleFactor = 1;
    /** return the scale factor */
    public double getScaleFactor(){return myScaleFactor;}
    /** Set the scale factor */
    public void setScaleFactor(double inScaleFactor){myScaleFactor = inScaleFactor;}
    
    /**
     * The two parallel lambert connic conformal projection provides two parallels of zero distortion.
     * This is the first of those latitudes where the distortion is zero.
     */
    private double myLat1 = 0;
    /** return the first latitude where the distortion is zero */
    public double getLatitude1(){return myLat1;}
    /** Set the first latitude where the distortion is zero */
    public void setLatitude1(double inLat1){myLat1 = inLat1;}
    
    /**
     * The two parallel lambert connic conformal projection provides two parallels of zero distortion.
     * This is the first of those latitudes where the distortion is zero.
     */
    private double myLat2 = 0;
    /** return the first latitude where the distortion is zero */
    public double getLatitude2(){return myLat2;}
    /** Set the first latitude where the distortion is zero */
    public void setLatitude2(double inLat2){myLat2 = inLat2;}
    
    /** determination of how close this projection should be. */
    public static final double EPS10 = 1.e-10;
    
    /** Project the data in the forward direction  */
    public void projectForward(Point inPoint) throws Exception {
        // performs the setup stuff. to calculate r0, F, n, phi0, and lam0
        setup();
        double phi = Math.toRadians(inPoint.y); // latitude
        double lam = Math.toRadians(inPoint.x); // longitude
        double es = getESquared();
        double e = getEccentricity();
        double k0 = getScaleFactor();
        
        
        double theta = n*(lam-lam0);
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        
        double m = cosphi / Math.sqrt (1. - es * sinphi * sinphi);
        double t = Math.tan(FORTPI-phi/2)/Math.pow(((1-e*sinphi)/(1+e*sinphi)), e/2);
        double r = getEllipsoid().getMajorAxis() * F * Math.pow(t, n);
        double x = getEasting() + r*Math.sin(theta);
        double y = getNorthing() + r0-r*Math.cos(theta);
        
        inPoint.setX(x);
        inPoint.setY(y);
    }
    
    /** Project the data in the reverse direction  */
    public void projectBackward(Point inPoint) throws Exception {
        setup();
        double E = inPoint.getX();
        double N = inPoint.getY();
        double a = getEllipsoid().getMajorAxis();
        double A1 = E-getEasting();
        double A2 = r0-(N-getNorthing());
        double theta = Math.atan(A1/A2);
        double rp = Math.sqrt(A1*A1+A2*A2);
        double tp = Math.pow(rp/(a*F), 1/n);
        double e = getEccentricity();
        
        double phiinit = 0;
        double phi = phi0;
        do{
            phiinit = phi;
            double sinphi = Math.sin(phiinit);
            phi = HALFPI-2*Math.atan(tp* Math.pow( (1-e*sinphi)/(1+e*sinphi), e/2));
        }
        while(Math.abs(phiinit-phi) > 1e-6);

        double lam = theta/n+lam0;
        inPoint.setX(Math.toDegrees(lam));
        inPoint.setY(Math.toDegrees(phi));
    }
        
    /** return the name of the projection  */
    public String getProjectionName() {
        return "Lambert Conformal Conic";
    }
    
    /** Precalculated values used in the routine, but only calculated once */
    private double lam0 = 0;
    private double phi0 = 0;
    private double n = 0;
    private double c = 0;
    private double r0 = 0;
    private double F = 0;
    
    /** set up the projection */
    protected void doSetup() throws Exception{
        double cosphi, sinphi;
        boolean secant;
        lam0 = Math.toRadians(getLonOragin());
        phi0 = Math.toRadians(getLatOragin());
        double phi1 = Math.toRadians(myLat1);
        double phi2 = Math.toRadians(myLat2);
        
        if (Math.abs(phi1 + phi2) < EPS10) throw new Exception ("Latitudes are too small ");
        double sinphi0 = Math.sin(phi0);
        double cosphi0 = Math.cos(phi0);
        double sinphi1 = Math.sin(phi1);
        double cosphi1 = Math.cos(phi1);
        double sinphi2 = Math.sin(phi2);
        double cosphi2 = Math.cos(phi2);
        
        double es = getESquared();
        double e = Math.sqrt(es);
        double m0 = cosphi0 / Math.sqrt (1. - es * sinphi0 * sinphi0);
        double m1 = cosphi1 / Math.sqrt (1. - es * sinphi1 * sinphi1);
        double m2 = cosphi2 / Math.sqrt (1. - es * sinphi2 * sinphi2);
        double t0 = Math.tan(FORTPI-phi0/2)/Math.pow(((1-e*sinphi0)/(1+e*sinphi0)), e/2);
        double t1 = Math.tan(FORTPI-phi1/2)/Math.pow(((1-e*sinphi1)/(1+e*sinphi1)), e/2);
        double t2 = Math.tan(FORTPI-phi2/2)/Math.pow(((1-e*sinphi2)/(1+e*sinphi2)), e/2);
        n = (Math.log(m1)-Math.log(m2))/(Math.log(t1)-Math.log(t2));
        F = m1/(n*Math.pow(t1,n));
        r0 = getEllipsoid().getMajorAxis() * F * Math.pow(t0, n);        
    }    
    
    
    /** Get the panel used to edit this projection */
    public ProjectionPanel getEditPanel(){
        LambertConicConformalProjectionPanel lccp = new LambertConicConformalProjectionPanel();
        lccp.setProjection(this);
        return lccp;
    }
    
    /** Main routine for testing this projection */
    public static void main(String[] inArgs){
        LambertConicConformalProjection lcc = new LambertConicConformalProjection();
        
        // The example is worked in feet.
        SimpleEllipsoid se = new SimpleEllipsoid();
        se.setMajorAxis(20925874.016); // feet 20925832.16
        se.setMinorAxis(20854933.727); // feet
        se.setName("Clarke 1866 Feet");
        se.setDescriptiveName(se.getName());
        lcc.setEllipsoid(se);
        
        lcc.setLatitude1(28.3833);
        lcc.setLatitude2(30.28334);
        lcc.setLatOragin(27.833333);
        lcc.setLonOragin(-99.000000);
        lcc.setEasting(2000000);
        lcc.setNorthing(2000000.00);
        gistoolkit.features.Point tempPoint = new gistoolkit.features.Point(-96.00, 28.5);
        try{
            lcc.projectForward(tempPoint);
            System.out.println("Easting = "+tempPoint.getX()+" Should be 2963505.83");
            System.out.println("Northing = "+tempPoint.getY()+" Should be 254760.42");
            lcc.projectBackward(tempPoint);
            System.out.println("Latitude = "+ tempPoint.getY()+" Should be 28.5");
            System.out.println("Longitude = "+ tempPoint.getX()+" Should be -96.00");
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    private static final String LATITUDE_ONE = "Latitude1";
    private static final String LATITUDE_TWO = "Latitude2";
    private static final String SCALE_FACTOR = "ScaleFactor";
    /** Return the configuration information for this projection */
    public Node getNode(){
        Node tempRoot = super.getNode();
        tempRoot.addAttribute(LATITUDE_ONE, ""+myLat1);
        tempRoot.addAttribute(LATITUDE_TWO, ""+myLat2);
        tempRoot.addAttribute(SCALE_FACTOR, ""+myScaleFactor);
        return tempRoot;
    }
    
    /** Setup this projection using the configuration information in the node */
    public void setNode(Node inNode) throws Exception{
        super.setNode(inNode);
        
        String tempName = "";
        String tempValue = "";
        try{
            tempName = LATITUDE_ONE;
            tempValue = (String) inNode.getAttribute(tempName);
            myLat1 = Double.parseDouble(tempValue);
            tempName = LATITUDE_TWO;
            tempValue = (String) inNode.getAttribute(tempName);
            myLat2 = Double.parseDouble(tempValue);
            tempName = SCALE_FACTOR;
            tempValue = (String) inNode.getAttribute(tempName);
            myScaleFactor = Double.parseDouble(tempValue);
        }
        catch (Exception e){
            throw new Exception("Can not read value for "+tempName+" for "+getProjectionName());
        }
    }
    
    /** clone this object*/
    public Object clone(){
        LambertConicConformalProjection tempProjection = new LambertConicConformalProjection();
        tempProjection.setEasting(getEasting());
        tempProjection.setNorthing(getNorthing());
        tempProjection.setEllipsoid(getEllipsoid());
        tempProjection.setLatOragin(getLatOragin());
        tempProjection.setLonOragin(getLonOragin());
        tempProjection.setLatitude1(getLatitude1());
        tempProjection.setLatitude2(getLatitude2());
        tempProjection.setScaleFactor(getScaleFactor());
        return tempProjection;
    }    
}
