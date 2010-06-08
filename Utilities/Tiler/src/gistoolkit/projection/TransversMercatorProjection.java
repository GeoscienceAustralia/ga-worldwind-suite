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
 * Performs a Transvers Mercator projection from Latitude Longitude to  UTM or so.
 */
public class TransversMercatorProjection implements Projection, EllipsoidProjection {
    /**
     * Constant to apply to northing of true oragin default is 0
     * Northing is just an addition to the map coordinates to move the map
     * up or down (in the Y direction) of where the transform lands you.
     * For the Brittish national grid, this should be -100,000 meters,
     * For the Irish National grid this should be 250,000 meters.
     * For the UTM zones, it should be 0.
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
     * For the British National Grid, this should be 400,000 meters,
     * For the Irish National Grid, this should be 200,000 meters,
     * For the UTMZones, this should be 500,000 meters.
     */
    private double myEasting = 500000;
    /** Retrieve the easting of true oragin for this projection */
    public double getEasting(){return myEasting;}
    /** Set the easting of true oragin for this projection */
    public void setEasting(double inEasting){myEasting = inEasting;}
    
    /**
     * Scale factor to apply for the central meridian default is 0.9996.
     * The central scale factor us used to generate two parrallel lines of
     * zero distortion in the projection.  The area in the central of the projection
     * will be under projected by this amount, but the area on the edges will not experience
     * as much distortion.
     */
    private double myCentralScale = 0.9996;
    /** Retrieve the central scale factor, default is 0.9996*/
    public double getCentralScale(){return myCentralScale;}
    /** Set the central scale factor, default is 0.9996*/
    public void setCentralScale(double inCentralScale){myCentralScale = inCentralScale;}
    
    /**
     * Latitude of true oragin, usually set to 0 for UTM,
     * set to 49 N for the Brittish National grid
     * set to 53 degreese 30 min N for the Irish National Grid,
     */
    private double myLatOragin = 0;
    /** Retrieve the latitude location of the true oragin of the map. */
    public double getLatOragin(){return myLatOragin;}
    /** Set the latitude location of the true oragin of the map. */
    public void setLatOragin(double inLatOragin){myLatOragin = inLatOragin;}
    
    /**
     * Longatude of true oragin, usually set the nearest 6 degree increment,
     * set to 2 degreese W for the Brittish National grid
     * set to 8 degreese W for the Irish National Grid,
     * set to 9 degreese W for the UTM zone 29,
     * set to 3 degreese W for the UTM zone 30,
     * set to 3 degreese E for the UTM zone 31
     */
    private double myLonOragin = 3;
    /** Retrieve the longatude location of the true oragin of the map. */
    public double getLonOragin(){return myLonOragin;}
    /** Set the longatude location of the true oragin of the map. */
    public void setLonOragin(double inLonOragin){myLonOragin = inLonOragin;}
    
    /**
     * This method of generating a UTM projection relies on the presence of an
     * ellipsoid to model the earth.  By default, the ellipsoid used is WGS84.
     */
    private Ellipsoid myEllipsoid = new WGS84();
    /** Retrieve the ellipsoid used to model the lat long coordinates of the earth */
    public Ellipsoid getEllipsoid(){return myEllipsoid;}
    /** Set the ellipsoid used to model the lat long coordinates of the earth */
    public void setEllipsoid(Ellipsoid inEllipsoid){myEllipsoid = inEllipsoid;}
    /** Returns the units of measure for the projection. */
    public String getUnitOfMeasure(){
        if (myEllipsoid == null) return "Map Units";
        return myEllipsoid.getUnitOfMeasure();
    }
    /** Creates new TransversMercatorProjection */
    public TransversMercatorProjection() {
    }
    
    private static double FC1 = 1.0;
    private static double FC2 = 0.5;
    private static double FC3 = 0.16666666666666666666;
    private static double FC4 = 0.08333333333333333333;
    private static double FC5 = 0.05;
    private static double FC6 = 0.03333333333333333333;
    private static double FC7 = 0.02380952380952380952;
    private static double FC8 = 0.01785714285714285714;
    private static double HALFPI = 1.5707963267948966;
    
    
    /** Transform from from Lat Long to map coordinates */
    public void projectForward(Point inPoint){
        // parameters
        double phi0 = Math.toRadians(myLatOragin);
        double lam0 = Math.toRadians(myLonOragin);
        double k0 = myCentralScale;
        double phi = Math.toRadians(inPoint.y); // latitude
        double lam = Math.toRadians(inPoint.x); // longitude
        double x0 = myEasting;
        double y0 = -myNorthing;
        
        // correct for the latitude of oragin
        lam = lam-lam0;
        // correct for the longitude of oragin
        //phi = phi-phi0;
        
        // Ellipsoid parameters
        double a = myEllipsoid.getMajorAxis();
        double b = myEllipsoid.getMinorAxis();
        double es = (a*a-b*b)/(a*a); // esentricity squared
        double esp = es / (1. - es);
        
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double t = Math.abs(cosphi) > 1e-10 ? sinphi/cosphi : 0.0;
        t = t*t;
        double al = cosphi * lam;
        double als = al * al;
        al = al / Math.sqrt(1.0 - es * sinphi * sinphi);
        double n = esp * cosphi * cosphi;
        
        // coefficients of the power series.
        double[] tempCoefficients = getCoefficients(es);
        double m0 = forwardM(phi0, Math.sin(phi0), Math.cos(phi0), tempCoefficients);
        
        double x = k0 * al * (FC1 + FC3 * als * (1. - t + n + FC5 * als * (5. + t * (t - 18.) + n * (14. - 58. * t) + FC7 * als * (61. + t * ( t * (179. - t) - 479. ) ))));
        double y = k0 * (forwardM(phi, sinphi, cosphi, tempCoefficients) - m0 +
        sinphi * al * lam * FC2 * ( 1. +
        FC4 * als * (5. - t + n * (9. + 4. * n) +
        FC6 * als * (61. + t * (t - 58.) + n * (270. - 330 * t)
        + FC8 * als * (1385. + t * ( t * (543. - t) - 3111.) )
        ))));
        x = a * x + x0;
        y = a * y + y0;
        
        inPoint.setX(x);
        inPoint.setY(y);
        
    }
    
    /** Transform from from map coordinates to lat long*/
    public void projectBackward(Point inPoint){
        // parameters
        double phi0 = Math.toRadians(myLatOragin);
        double lam0 = Math.toRadians(myLonOragin);
        double k0 = myCentralScale;
        double x = inPoint.x;
        double y = inPoint.y;
        double x0 = myEasting;
        double y0 = -myNorthing;
        
        
        // Ellipsoid parameters
        double a = myEllipsoid.getMajorAxis();
        double b = myEllipsoid.getMinorAxis();
        double es = (a*a-b*b)/(a*a);
        double esp = es / (1. - es);
        
        double ra = 1/a;  //I have no idea what ra is.
        x = (x - x0) * ra; /* descale and de-offset */
        y = (y - y0) * ra;
        
        // coefficients of the power series.
        double[] tempCoefficients = getCoefficients(es);
        double m0 = forwardM(phi0, Math.sin(phi0), Math.cos(phi0), tempCoefficients);
        double phi = reverseM(m0 + y / k0, es, tempCoefficients);
        double lam = 0;
        if (Math.abs(phi) >= HALFPI) {
            phi = y < 0. ? -HALFPI : HALFPI;
            lam = 0.;
        } else {
            double sinphi = Math.sin(phi);
            double cosphi = Math.cos(phi);
            double t = Math.abs(cosphi) > 1e-10 ? sinphi/cosphi : 0.0;
            double n = esp * cosphi * cosphi;
            double con = 1. - es * sinphi * sinphi;
            double d = x * Math.sqrt(con) / k0;
            con = con * t;
            t = t * t;
            double ds = d * d;
            phi -= (con * ds / (1.-es)) * FC2 * (1. - ds * FC4 * (5. + t * (3. - 9. *  n) + n * (1. - 4 * n) - ds * FC6 * (61. + t * (90. - 252. * n + 45. * t) + 46. * n  - ds * FC8 * (1385. + t * (3633. + t * (4095. + 1574. * t)) ))));
            lam = d*(FC1 - ds*FC3*( 1. + 2.*t + n - ds*FC5*(5. + t*(28. + 24.*t + 8.*n) + 6.*n - ds * FC7 * (61. + t * (662. + t * (1320. + 720. * t)) )))) / cosphi;
        }
        
        lam = lam + lam0; /* reduce from del lp.lam */
        //      phi = phi + phi0;
        inPoint.setX(Math.toDegrees(lam));
        inPoint.setY(Math.toDegrees(phi));
    }
    
    // coefficients for the expansion calculations.
    private static final double C00 = 1.0;
    private static final double C02 = 0.25;
    private static final double C04 = 0.046875;
    private static final double C06 = 0.01953125;
    private static final double C08 = 0.01068115234375;
    private static final double C22 = 0.75;
    private static final double C44 = 0.46875;
    private static final double C46 = 0.01302083333333333333;
    private static final double C48 = 0.00712076822916666666;
    private static final double C66 = 0.36458333333333333333;
    private static final double C68 = 0.00569661458333333333;
    private static final double C88 = 0.3076171875;
    private static final double EPS = 1e-11;
    
    // kill the iterration if it gets stuck in an infinite loop
    private static double MAX_ITERATIONS = 10;
    
    /**
     * Calculate the series expansion.
     * eSquared = (a^2-b^2)/(a^2) where a and b are the major and minor axes
     * of the ellipsoid in question.
     */
    protected double[] getCoefficients(double es) {
        double t;
        
        double[] tempCoefficients = new double[5];
        tempCoefficients[0] = C00 - es * (C02 + es * (C04 + es * (C06 + es * C08)));
        tempCoefficients[1] = es * (C22 - es * (C04 + es * (C06 + es * C08)));
        tempCoefficients[2] = (t = es * es) * (C44 - es * (C46 + es * C48));
        tempCoefficients[3] = (t *= es) * (C66 - es * C68);
        tempCoefficients[4] = t * es * C88;
        return tempCoefficients;
    }
    
    /**
     * Utility functions for calculating M in forward direction.
     * meridinal distance for ellipsoid and inverse
     * 8th degree - accurate to < 1e-5 meters when used in conjuction
     * with typical major axis values.
     */
    protected double forwardM(double phi, double sphi, double cphi, double[] inCoefficients) {
        cphi = cphi*sphi;
        sphi = sphi*sphi;
        return(inCoefficients[0] * phi - cphi * (inCoefficients[1] + sphi*(inCoefficients[2] + sphi*(inCoefficients[3] + sphi*inCoefficients[4]))));
    }
    
    /**
     * Utility function for calculating M in the reverse direction.
     * This is an itterative approach that rairly goes beyond 2 itterations,
     * However it is designed to break at MAX_ITERATIONS and return the best guess at that point.
     * Inverse determines phi to EPS (1e-11) radians, about 1e-6 seconds.
     */
    protected double reverseM(double arg, double es, double[] inCoefficients) {
        double s=0;
        double k = 1./(1.-es);
        
        double t=0;
        double phi = arg;
        for (int i=0; i<MAX_ITERATIONS; i++){ /* rarely goes over 2 iterations */
            s = Math.sin(phi);
            t = 1. - es * s * s;
            t = (forwardM(phi, s, Math.cos(phi), inCoefficients) - arg) * (t * Math.sqrt(t)) * k;
            phi = phi - t;
            if (Math.abs(t) < EPS)
                return phi;
        }
        System.out.println("Max Itteration Hit, t = "+t+" Must be less than "+EPS);
        return phi;
    }
    
    
    public static void main(String[] inArgs){
        TransversMercatorProjection tm = new TransversMercatorProjection();
        tm.setEllipsoid(new Airy1830());
        tm.setCentralScale(0.9996012717);
        tm.setLatOragin(49.0);
        tm.setLonOragin(-2);
        tm.setEasting(400000);
        tm.setNorthing(100000);
        
        double tempLat = 52+39.0/60+27.2531/3600;
        double tempLon = 1+43.0/60+4.5177/3600;
        
        // print initial state
        System.out.println("Latitude = "+tempLat);
        System.out.println("Longitude = "+tempLon);
        
        // convert forward
        Point tempPoint = new Point(tempLon, tempLat);
        tm.projectForward(tempPoint);
        System.out.println("Northing = "+tempPoint.getY());
        System.out.println("Easting = "+tempPoint.getX());
        
        // convert back
        tempPoint = new Point(651409.903, 313177.270);
        tm.projectBackward(tempPoint);
        System.out.println("Latitude = "+tempPoint.getY());
        System.out.println("Longitude = "+tempPoint.getX());
        
        // alternative routines
        // print initial state
        System.out.println("\n\n");
        System.out.println("Latitude = "+tempLat);
        System.out.println("Longitude = "+tempLon);
        
        // convert forward
        tempPoint = new Point(tempLon, tempLat);
        tm.altTransformForward(tempPoint);
        System.out.println("Northing = "+tempPoint.getY());
        System.out.println("Easting = "+tempPoint.getX());
        
        // convert back
        tempPoint = new Point(651409.903, 313177.270);
        tm.altTransformBackward(tempPoint);
        System.out.println("Latitude = "+tempPoint.getY());
        System.out.println("Longitude = "+tempPoint.getX());
        
    }
    
    
    /** perform the conversion */
    public void altTransformForward(Point inPoint){
        // lat lon conversion to radians
        double lat0 = Math.toRadians(myLatOragin);
        double inlat = Math.toRadians(inPoint.getY());
        double dlat = inlat-lat0;
        double alat = inlat+lat0;
        double sinlat = Math.sin(inlat);
        double sin2lat = sinlat*sinlat;
        double coslat = Math.cos(inlat);
        double tanlat2 = Math.pow(Math.tan(inlat), 2);
        double tanlat4 = Math.pow(Math.tan(inlat), 4);
        double lon0 = Math.toRadians(myLonOragin);
        double inlon = Math.toRadians(inPoint.getX());
        double dlon = inlon-lon0;
        
        // Ellipsoid parameters
        double a = myEllipsoid.getMajorAxis();
        double b = myEllipsoid.getMinorAxis();
        double e2 = (a*a-b*b)/(a*a);
        
        // conversion parameters
        double n = (a-b)/(a+b);
        double v = a*myCentralScale*Math.pow((1-e2*sin2lat), -0.5);
        double p = a*myCentralScale*(1-e2)*Math.pow((1-e2*sin2lat),-1.5);
        double nu2 = v/p-1;
        double n2 = n*n;
        double n3 = n*n*n;
        double M = b*myCentralScale*((1+n+(5.0/4.0)*n2+(5.0/4.0)*n3)*(dlat)-(3*n+3*n2+(21.0/8.0)*n3)*Math.sin(dlat)*Math.cos(alat)+((15.0/8.0)*n2+(15.0/8.0)*n3)*Math.sin(2*(dlat))*Math.cos(2*alat)-(35.0/24.0)*n3*Math.sin(3*dlat)*Math.cos(3*alat));
        double No = myNorthing;
        
        double I = M+No;
        double II = (v/2)*sinlat*coslat;
        double III = (v/24)*sinlat*Math.pow(coslat, 3)*(5-tanlat2+9*nu2);
        double IIIA = (v/720)*sinlat*Math.pow(coslat,5)*(61.0-58.0*tanlat2+tanlat4);
        double IV = v*coslat;
        double V = v/6*Math.pow(coslat,3)*(v/p-tanlat2);
        double VI = v/120*Math.pow(coslat,5)*(5-18.0*tanlat2+tanlat4+14.0*nu2-58.0*tanlat2*nu2);
        double N = I+II*Math.pow(dlon,2)+III*Math.pow(dlon,4)+IIIA*Math.pow(dlon,6);
        double E = myEasting+IV*(dlon)+V*Math.pow(dlon,3)+VI*Math.pow(dlon,5);
        inPoint.setX(E);
        inPoint.setY(N);
    }
    
    /** perform the revers transform */
    public void altTransformBackward(Point inPoint){
        // Northing and easting
        double E = inPoint.getX();
        double N = inPoint.getY();
        
        // Ellipsoid parameters
        double a = myEllipsoid.getMajorAxis();
        double b = myEllipsoid.getMinorAxis();
        double e2 = (a*a-b*b)/(a*a);
        
        // initial values
        double lat0 = Math.toRadians(myLatOragin);
        double lon0 = Math.toRadians(myLonOragin);
        
        // conversion parameters
        double n = (a-b)/(a+b);
        double n2 = n*n;
        double n3 = n*n*n;
        
        // Compute initial lat value
        double ilat = lat0;
        double M = 0;
        double dlat = 0;
        double alat = 0;
        do{
            ilat = (N-myNorthing-M)/(a*myCentralScale)+ilat;
            dlat = (ilat-lat0);
            alat = (ilat+lat0);
            M = b*myCentralScale*((1+n+(5.0/4.0)*n2+(5.0/4.0)*n3)*(dlat)-(3*n+3*n2+(21.0/8.0)*n3)*Math.sin(dlat)*Math.cos(alat)+((15.0/8.0)*n2+(15.0/8.0)*n3)*Math.sin(2*(dlat))*Math.cos(2*alat)-(35.0/24.0)*n3*Math.sin(3*dlat)*Math.cos(3*alat));
        }
        while(N-myNorthing-M > .001);
        ilat = (N-myNorthing-M)/(a*myCentralScale)+ilat;
        
        // conversion parameters
        double sinlat = Math.sin(ilat);
        double sin2lat = sinlat*sinlat;
        double coslat = Math.cos(ilat);
        double tanlat = Math.tan(ilat);
        double tanlat2 = Math.pow(tanlat, 2);
        double tanlat4 = Math.pow(tanlat, 4);
        double tanlat6 = Math.pow(tanlat, 6);
        double seclat = 1/coslat;
        double v = a*myCentralScale*Math.pow((1-e2*sin2lat), -0.5);
        double v3 = Math.pow(v,3);
        double v5 = Math.pow(v,5);
        double v7 = Math.pow(v,7);
        double p = a*myCentralScale*(1-e2)*Math.pow((1-e2*sin2lat),-1.5);
        double nu2 = v/p-1;
        
        
        double VII = tanlat/(2*p*v);
        double VIII = (tanlat/(24*p*v3))*(5+3*tanlat2+nu2-9*(tanlat2*nu2));
        double IX = (tanlat/(720*p*v5))*(61+90*tanlat2+45*tanlat4);
        double X = (seclat/v);
        double XI = (seclat/(6*v3))*(v/p+2*tanlat2);
        double XII = (seclat/(120*v5))*(5+28*tanlat2+24*tanlat4);
        double XIIA = (seclat/(5040*v7))*(61+662*tanlat2+1320*tanlat4+720*tanlat6);
        double de = E-myEasting;
        double lat = ilat-VII*Math.pow(de,2)+VIII*Math.pow(de,4)-IX*Math.pow(de,6);
        double lon = lon0+X*de-XI*Math.pow(de,3)+XII*Math.pow(de,5)-XIIA*Math.pow(de,7);
        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);
        inPoint.setX(lon);
        inPoint.setY(lat);
    }
    
    /** return the name of the projection */
    public String getProjectionName(){ return "Transverse Mercator Projection";}
    
    
    private static final String ELLIPSOID_NAME = "Ellipsoid";
    private static final String ELLIPSOID_CLASS = "EllipsoidClass";
    private static final String LAT_ORAGIN = "LatitudeOfOragin";
    private static final String LON_ORAGIN = "LongitudeOfOragin";
    private static final String NORHTING = "Northing";
    private static final String EASTING = "Easting";
    private static final String CENTRAL_SCALE = "CentralScale";
    
    /** Return the configuration information for this projection  */
    public Node getNode() {
        Node tempRoot = new Node("Transverse_Mercator_Projection");
        tempRoot.addAttribute(ELLIPSOID_NAME, myEllipsoid.getName());
        tempRoot.addAttribute(ELLIPSOID_CLASS, myEllipsoid.getClass().getName());
        tempRoot.addAttribute(LAT_ORAGIN, ""+myLatOragin);
        tempRoot.addAttribute(LON_ORAGIN, ""+myLonOragin);
        tempRoot.addAttribute(NORHTING, ""+myNorthing);
        tempRoot.addAttribute(EASTING, ""+myEasting);
        tempRoot.addAttribute(CENTRAL_SCALE, ""+myCentralScale);
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
            tempName = CENTRAL_SCALE;
            tempString = inNode.getAttribute(tempName);
            if (tempString != null) myCentralScale = Double.parseDouble(tempString);
        }
        catch (Exception e){
            throw new Exception("Can not read the falue for "+tempName+" for Projection"+getProjectionName());
        }
    }
    
    /** clone this object*/
    public Object clone(){
        TransversMercatorProjection tempProjection = new TransversMercatorProjection();
        tempProjection.setEasting(getEasting());
        tempProjection.setNorthing(getNorthing());
        tempProjection.setEllipsoid(getEllipsoid());
        tempProjection.setLatOragin(getLatOragin());
        tempProjection.setLonOragin(getLonOragin());
        tempProjection.setCentralScale(getCentralScale());
        return tempProjection;
    }
    
    /** set the envelope of the map.  The projection may do some initialization bassed on this value  */
    public void setEnvelope(Envelope inEnvelope) throws Exception {
    }
}
