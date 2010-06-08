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

import gistoolkit.features.Point;
/**
 * Projects to the Albers Equal Area Projection.
 * Having virtually no data to go on with this projection, I just copied the data from the Proj4 C++ code base
 * with a great deal of alteration to move it to java.  I know little or nothing about this projection.
 */
public class AlbersEqualAreaProjection extends SimpleProjection implements EditableProjection {
    
    /** Creates new AlbersEqualArea */
    public AlbersEqualAreaProjection() {
        setLatOragin(23.0);
        setLonOragin(-96.0);
        setLatitude1(29.5);
        setLatitude2(45.5);
        setEasting(0);
        setNorthing(0);
        
    }
    
    /** Return the name of this projeciton */
    public String getProjectionName() {return "Albers Equal Area";}
    
    /**
     * The two parallel AlbersEqualArea projection provides two parallels of zero distortion.
     * This is the first of those latitudes where the distortion is zero.
     */
    private double myLat1 = 0;
    /** return the first latitude where the distortion is zero */
    public double getLatitude1(){return myLat1;}
    /** Set the first latitude where the distortion is zero */
    public void setLatitude1(double inLat1){myLat1 = inLat1;}
    
    /**
     * The two AlbersEqualArea projection provides two parallels of zero distortion.
     * This is the first of those latitudes where the distortion is zero.
     */
    private double myLat2 = 0;
    /** return the first latitude where the distortion is zero */
    public double getLatitude2(){return myLat2;}
    /** Set the first latitude where the distortion is zero */
    public void setLatitude2(double inLat2){myLat2 = inLat2;}
    
    
    private double n = 0;
    private double n2 = 0; // n+n
    private double ec = 0;
    private double c = 0;
    private double dd = 0;
    private double rho0 = 0;
    private double rho;
    private double lam0; // longitude of oragin in radians
    private double phi0; // latitude of oragin in radians
    private double phi1; // first standard parallel in radians
    private double phi2; // second standard parallel in radians
    private boolean ellipse; // true if using an elipse
    private double e = 0; // the eccentricity of the elipsoid
    private double one_es = 0; // one minus the eccentricity squared
    private double es = 0; // the eccentricity of the elipsoid squared.
    
    /** setup the projection */
    protected void doSetup() throws Exception{
        double cosphi, sinphi;
        boolean secant;
        lam0 = Math.toRadians(getLonOragin());
        phi0 = Math.toRadians(getLatOragin());
        phi1 = Math.toRadians(getLatitude1());
        phi2 = Math.toRadians(getLatitude2());
        es = getESquared();
        e = getEccentricity();
        
        n = Math.sin(phi1);
        sinphi = n;
        cosphi = Math.cos(phi1);
        secant = Math.abs(phi1 - phi2) >= 1e-10;
        one_es = 1.0-es;
        ellipse = es > 0.0;
        if( ellipse ) {
            double ml1, m1;
            m1 = pj_msfn(sinphi, cosphi, es);
            ml1 = pj_qsfn(sinphi, e, one_es);
            if (secant) { /* secant cone */
                double ml2, m2;
                
                sinphi = Math.sin(phi2);
                cosphi = Math.cos(phi2);
                m2 = pj_msfn(sinphi, cosphi, es);
                ml2 = pj_qsfn(sinphi, e, one_es);
                n = (m1 * m1 - m2 * m2) / (ml2 - ml1);
            }
            ec = 1. - .5 * one_es * Math.log((1. - e) / (1. + e)) / e;
            c = m1 * m1 + n * ml1;
            dd = 1.0 / n;
            rho0 = dd * Math.sqrt(c - n * pj_qsfn(Math.sin(phi0), e, one_es));
        }
        else {
            if (secant) n = 0.5 * (n + Math.sin(phi2));
            n2 = n + n;
            c = cosphi * cosphi + n2 * sinphi;
            dd = 1.0 / n;
            rho0 = dd * Math.sqrt(c - n2 * Math.sin(phi0));
        }
    }
    
    /** Project the data in the forward direction  */
    public void projectForward(Point inPoint) throws Exception {
        setup();
        double phi = Math.toRadians(inPoint.y);//-phi0; // latitude
        double lam = Math.toRadians(inPoint.x)-lam0; // longitude
        if (ellipse){
            rho = c-n * pj_qsfn(Math.sin(phi), e, one_es);
        }
        else rho = c-n2 * Math.sin(phi);
        rho = dd * Math.sqrt(rho);
        lam = lam * n;
        double x = rho * Math.sin( lam );
        double y = rho0 - rho * Math.cos(lam);
        double a = getEllipsoid().getMajorAxis();
        inPoint.setX(getEasting() + a*x);
        inPoint.setY(getNorthing() + a*y);
    }
    /** Project the data in the reverse direction  */
    public void projectBackward(Point inPoint) throws Exception {
        setup();
        double x = inPoint.getX();
        double y = inPoint.getY();
        double TOL7 = 1.e-7;
        double phi = 0;
        double lam = 0;
        double a = getEllipsoid().getMajorAxis();

        double ra = 1/a; 
	x = (x - getEasting()) * ra; /* descale and de-offset */
	y = (y - getNorthing()) * ra;
        
        if( (rho = hypot(x, y = rho0 - y)) != 0.0 ) {
            if (n < 0.) {
                rho = -rho;
                x = -x;
                y = -y;
            }
            phi =  rho / dd;
            if (ellipse) {
                phi = (c - phi * phi) / n;
                if (Math.abs(ec - Math.abs(phi)) > TOL7) {
                    phi = phi1_(phi, e, one_es);
                }
                else phi = phi < 0. ? -HALFPI : HALFPI;
            }
            else if (Math.abs(phi = (c - phi * phi) / n2) <= 1.) phi = Math.asin(phi);
            else phi = phi < 0. ? -HALFPI : HALFPI;
            lam = Math.atan2(x, y) / n;
        }
        else {
            lam = 0.;
            phi = n > 0. ? HALFPI : - HALFPI;
        }
        inPoint.setX(Math.toDegrees(lam0+lam));
        inPoint.setY(Math.toDegrees(phi0+phi));
    }
    
    /** Calculate the itterative result for phi1 */
    private double phi1_(double qs, double Te, double Tone_es) {
        int N_ITER = 15;
        double EPSILON = 1e-7;
        double TOL = 1e-10;
        double Phi, sinpi, cospi, con, com, dphi;
        
        Phi = Math.asin (.5 * qs);
        if (Te < EPSILON) return( Phi );
        int i = N_ITER;
        do {
            sinpi = Math.sin (Phi);
            cospi = Math.cos (Phi);
            con = Te * sinpi;
            com = 1. - con * con;
            dphi = .5 * com * com / cospi * (qs / Tone_es - sinpi / com + .5 / Te * Math.log ((1. - con) /
            (1. + con)));
            Phi += dphi;
        } while ((Math.abs(dphi) > TOL) && (--i > 0));
        return Phi;
    }

    /** Get the panel used to edit this projection */
    public ProjectionPanel getEditPanel(){
        AlbersEqualAreaProjectionPanel lccp = new AlbersEqualAreaProjectionPanel();
        lccp.setProjection(this);
        return lccp;
    }    
    
    private static final String LATITUDE_ONE = "Latitude1";
    private static final String LATITUDE_TWO = "Latitude2";
    /** Return the configuration information for this projection */
    public Node getNode(){
        Node tempRoot = super.getNode();
        tempRoot.addAttribute(LATITUDE_ONE, ""+myLat1);
        tempRoot.addAttribute(LATITUDE_TWO, ""+myLat2);
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
        }
        catch (Exception e){
            throw new Exception("Can not read value for "+tempName+" for "+getProjectionName());
        }
    }
    
    /** clone this projection*/
    public Object clone(){
        AlbersEqualAreaProjection tempProjection = new AlbersEqualAreaProjection();
        tempProjection.setEasting(getEasting());
        tempProjection.setNorthing(getNorthing());
        tempProjection.setEllipsoid(getEllipsoid());
        tempProjection.setLatOragin(getLatOragin());
        tempProjection.setLonOragin(getLonOragin());
        tempProjection.setLatitude1(getLatitude1());
        tempProjection.setLatitude2(getLatitude2());
        return tempProjection;
    }
}
