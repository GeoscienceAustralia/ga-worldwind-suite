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
 * Used for handling general things like lat oragin, long oragin, and elipsoid.
 */
public abstract class SimpleProjection implements Projection, EllipsoidProjection {
    
    /** PI /2 */
    public static final double HALFPI=1.5707963267948966;
    /** PI /4 */
    public static final double FORTPI=0.78539816339744833;
    /** Ratio of the circumference of a circle to its diameter*/
    public static final double PI=3.14159265358979323846;
    /** Pi * 2 */
    public static final double TWOPI=6.2831853071795864769;
    
    /*
     * Latitude of true oragin.
     */
    private double myLatOragin = 0;
    /** Retrieve the latitude location of the true oragin of the map. */
    public double getLatOragin(){return myLatOragin;}
    /** Set the latitude location of the true oragin of the map. */
    public void setLatOragin(double inLatOragin){myLatOragin = inLatOragin; mySetup = false;}
    
    /**
     * Longatude of true oragin.
     */
    private double myLonOragin = 3;
    /** Retrieve the longatude location of the true oragin of the map. */
    public double getLonOragin(){return myLonOragin;}
    /** Set the longatude location of the true oragin of the map. */
    public void setLonOragin(double inLonOragin){myLonOragin = inLonOragin; mySetup = false;}
    
    /**
     * Constant to apply to northing of true oragin default is 0
     * Northing is just an addition to the map coordinates to move the map
     * up or down (in the Y direction) of where the transform lands you.
     */
    private double myNorthing = 0;
    /** Retrieve the northing of true oragin for this projection */
    public double getNorthing(){return myNorthing;}
    /** Set the northing of true oragin for this projection */
    public void setNorthing(double inNorthing){myNorthing = inNorthing;}
    
    /**
     * Constant to apply to easting of true oragin default is 500,000 meters.
     * Easting is just an addition to the map coordinates to move the map to the
     * right or left (in the X direction) of where the transform lands you.
     */
    private double myEasting = 0.0;
    /** Retrieve the easting of true oragin for this projection */
    public double getEasting(){return myEasting;}
    /** Set the easting of true oragin for this projection */
    public void setEasting(double inEasting){myEasting = inEasting;}
    
    
    /**
     * This method of generating a UTM projection relies on the presence of an
     * ellipsoid to model the earth.  By default, the ellipsoid used is WGS84.
     */
    private Ellipsoid myEllipsoid = new WGS84();
    /** Retrieve the ellipsoid used to model the lat long coordinates of the earth */
    public Ellipsoid getEllipsoid(){return myEllipsoid;}
    /** Set the ellipsoid used to model the lat long coordinates of the earth */
    public void setEllipsoid(Ellipsoid inEllipsoid){
        myEllipsoid = inEllipsoid;
        gotESquared = false;
        gotEccentricity = false;
        mySetup = false;
    }
    
    /** return the units for this projection. */
    public String getUnitOfMeasure(){
        if (myEllipsoid == null) return "Map Units";
        else return myEllipsoid.getUnitOfMeasure();
    }
    
    /** Return the eccentricity squared value of the elipse*/
    private double myESquared = Double.NaN;
    private boolean gotESquared = false;
    /** get the esquared number */
    public double getESquared(){
        if (!gotESquared){
            if (myEllipsoid != null){
                double a = myEllipsoid.getMajorAxis();
                double b = myEllipsoid.getMinorAxis();
                myESquared = (a*a-b*b)/(a*a); // esentricity squared
                gotESquared = true;
            }
        }
        return myESquared;
    }
    
    /** Return the eccentricityvalue of the elipse*/
    private double myEccentricity = Double.NaN;
    private boolean gotEccentricity = false;
    /** get the eccentricity number */
    public double getEccentricity(){
        if (!gotEccentricity){
            double es = getESquared();
            myEccentricity = Math.sqrt(es);
            gotEccentricity = true;
        }
        return myEccentricity;
    }
    
    /** boolean to determine if setup has been run */
    private boolean mySetup = false;
    
    /** Run setup */
    protected final void setup() throws Exception{
        if (!mySetup){
            doSetup();
            mySetup = true;
        }
    }
    
    /** allow insertable setup data */
    protected void doSetup()throws Exception{
        
    }
    
    
    /** Project the data in the forward direction  */
    public abstract void projectForward(Point inPoint) throws Exception;
    
    /** Project the data in the reverse direction  */
    public abstract void projectBackward(Point inPoint) throws Exception;
        
    /** return the name of the projection  */
    public abstract String getProjectionName();
    
    /**
     * return the euclidian hypotenuse of a right triangle
     * Math.sqrt(x*x+y*y);
     */
    public double hypot(double x, double y){
        return Math.sqrt(x*x+y*y);
    }
    
    /**
     * meridinal distance for ellipsoid and inverse
     * 8th degree - accurate to < 1e-5 meters when used in conjuction
     * with typical major axis values.
     * Inverse determines phi to EPS (1e-11) radians, about 1e-6 seconds.
     * Calculate the series expansion.
     * eSquared = (a^2-b^2)/(a^2) where a and b are the major and minor axes
     * of the ellipsoid in question.
     */
    public static double[] pj_enfn(double inESquared){
        double C00 = 1.;
        double C02 = .25;
        double C04 = .046875;
        double C06 = .01953125;
        double C08 = .01068115234375;
        double C22 = .75;
        double C44 = .46875;
        double C46 = .01302083333333333333;
        double C48 = .00712076822916666666;
        double C66 = .36458333333333333333;
        double C68 = .00569661458333333333;
        double C88 = .3076171875;
        int EN_SIZE = 5;
        
        double[] en = new double[EN_SIZE];
        double es = inESquared;
        double t;
        en[0] = C00 - es * (C02 + es * (C04 + es * (C06 + es * C08)));
        en[1] = es * (C22 - es * (C04 + es * (C06 + es * C08)));
        en[2] = (t = es * es) * (C44 - es * (C46 + es * C48));
        en[3] = (t *= es) * (C66 - es * C68);
        en[4] = t * es * C88;
        return en;
    }
    
    /** determine latitude angle phi-2*/
    public static double pj_tsfn(double phi, double sinphi, double e) {
        sinphi *= e;
        return (Math.tan (.5 * (HALFPI - phi)) /
        Math.pow((1. - sinphi) / (1. + sinphi), .5 * e));
    }
    /** determine latitude angle phi-2*/
    public static double pj_phi2(double ts, double e) throws Exception{
        double eccnth, Phi, con, dphi;
        double TOL=1.0e-10;
        int N_ITER=15;
        
        int i;
        
        eccnth = .5 * e;
        Phi = HALFPI - 2. * Math.atan (ts);
        i = N_ITER;
        do {
            con = e * Math.sin (Phi);
            dphi = HALFPI - 2. * Math.atan (ts * Math.pow((1. - con) /
            (1. + con), eccnth)) - Phi;
            Phi += dphi;
        } while ( (Math.abs(dphi) > TOL) && (--i > 0) );
        if (i <= 0) throw new Exception ("Error finding pj_phi2");
        return Phi;
    }
    
    /* determine constant small m */
    double pj_msfn(double sinphi, double cosphi, double es) {
        return (cosphi / Math.sqrt (1.0 - es * sinphi * sinphi));
    }
    
    /** determine small q */
    double pj_qsfn(double sinphi, double e, double one_es) {
        double EPSILON = 1.0e-7;
        double con;
        
        if (e >= EPSILON) {
            con = e * sinphi;
            return (one_es * (sinphi / (1. - con * con) - (.5 / e) * Math.log ((1. - con) / (1. + con))));
        }
        else return (sinphi + sinphi);
    }
    
    double aasin(double v) throws Exception{
        double ONE_TOL = 1.00000000000001;
        double TOL = 0.000000001;
        
        double av = Math.abs(v);
        if (av  >= 1.) {
            if (av > ONE_TOL) throw new Exception("aasin failed av is larger than "+ONE_TOL);
            if (v < 0) return -HALFPI;
            else return HALFPI;
        }
        return Math.asin(v);
    }
    
    double aacos(double v) throws Exception{
        double ONE_TOL = 1.00000000000001;
        double av = Math.abs(v);
        if (av >= 1.) {
            if (av > ONE_TOL) throw new Exception("aacos failed av is larger than "+ONE_TOL);
            if (v < 0) return PI;
            else return 0;
        }
        return Math.acos(v);
    }
    
    double asqrt(double v) {
        if (v <=0) return 0;
        return Math.sqrt(v);
    }
    
    double aatan2(double n, double d) {
        double ATOL = 1e-50;
        if ((Math.abs(n) < ATOL) && (Math.abs(d) < ATOL)) return 0;
        return Math.atan2(n,d);
    }
    
    private static final String ELLIPSOID_NAME = "Ellipsoid";
    private static final String ELLIPSOID_CLASS = "EllipsoidClass";
    private static final String LAT_ORAGIN = "LatitudeOfOragin";
    private static final String LON_ORAGIN = "LongitudeOfOragin";
    private static final String NORHTING = "Northing";
    private static final String EASTING = "Easting";
    private static final String CENTRAL_SCALE = "CentralScale";
    
    /** Return the configuration information for this projection  */
    public Node getNode() {
        Node tempRoot = new Node("Projection");
        tempRoot.addAttribute("ProjectionName", getProjectionName());
        tempRoot.addAttribute(ELLIPSOID_NAME, myEllipsoid.getName());
        tempRoot.addAttribute(ELLIPSOID_CLASS, myEllipsoid.getClass().getName());
        tempRoot.addAttribute(LAT_ORAGIN, ""+myLatOragin);
        tempRoot.addAttribute(LON_ORAGIN, ""+myLonOragin);
        tempRoot.addAttribute(NORHTING, ""+myNorthing);
        tempRoot.addAttribute(EASTING, ""+myEasting);
        return tempRoot;
    }
    
    /** Setup this projection using the configuration information in the node  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        
        // find the ellipsoid
        String tempName = ELLIPSOID_CLASS;
        String tempString = inNode.getAttribute(tempName);
        if (tempString != null) myEllipsoid = (Ellipsoid) Class.forName(tempString).newInstance();
        
        try{
            tempName = LAT_ORAGIN;
            tempString = inNode.getAttribute(tempName);
            myLatOragin = Double.parseDouble(tempString);
            tempName = LON_ORAGIN;
            tempString = inNode.getAttribute(tempName);
            myLonOragin = Double.parseDouble(tempString);
            tempName = NORHTING;
            tempString = inNode.getAttribute(tempName);
            myNorthing = Double.parseDouble(tempString);
            tempName = EASTING;
            tempString = inNode.getAttribute(tempName);
            myEasting = Double.parseDouble(tempString);
        }
        catch (Exception e){
            throw new Exception("Can not read the value for "+tempName+" for Projection"+getProjectionName());
        }
    }
    
    /** Return the name of the projection */
    public String toString(){
        return getProjectionName();
    }

    /** clone this object*/
    public abstract Object clone();
    
    /** set the envelope of the map.  The projection may do some initialization bassed on this value  */
    public void setEnvelope(Envelope inEnvelope) throws Exception {
    }
    
}
