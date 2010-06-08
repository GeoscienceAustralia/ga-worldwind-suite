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

package gistoolkit.projection.ellipsoid.transform;

import gistoolkit.features.*;
import gistoolkit.projection.Ellipsoid;

/**
 * Super class for all EllipsoidConversion routines.
 */
public abstract class EllipsoidTransform extends Object {

    /** Creates new EllipsoidConversion */
    public EllipsoidTransform() {
    }
    
    /** Create new Ellipsoid from the given ellipsoids */
    public EllipsoidTransform(Ellipsoid inFromEllipsoid,Ellipsoid inToEllipsoid){
        setFromEllipsoid(inFromEllipsoid);
        setToEllipsoid(inToEllipsoid);
    }
    
    /** the name to display to the user */
    private String myName = "No Name";
    /** Set the name of the transform */
    protected void setName(String inName){myName = inName;}
    /** Get the name of the transform */
    public String getName(){return myName;}
    /** To appear in a combo box.*/
    public String toString(){
        if (myIsReversed) return "Reverse "+getName();
        else return getName();
    }
    
    /** The Ellipsoid to convert from */
    private Ellipsoid myFromEllipsoid = null;
    /** Set the Ellipsoid to convert from */
    public void setFromEllipsoid(Ellipsoid inEllipsoid){myFromEllipsoid = inEllipsoid;}
    /** Retrieve the Ellipsoid to convert from */
    public Ellipsoid getFromEllipsoid(){return myFromEllipsoid;}
    
    /** The Ellipsoid to convert to */
    private Ellipsoid myToEllipsoid = null;
    /** Set the Ellipsoid to convert to */
    public void setToEllipsoid(Ellipsoid inEllipsoid){myToEllipsoid = inEllipsoid;}
    /** Retrieve the Ellipsoid to convert to */
    public Ellipsoid getToEllipsoid(){return myToEllipsoid;}

    /** Convert the coordinates of the given point from reference to the From ellipsoid to reference to the To ellipsoid. */
    public final void forward(Point inPoint){
        if (myIsReversed) doReverse(inPoint);
        else doForward(inPoint);
    }
    
    /** Convert the coordinates of the given point from reference to the To ellipsoid to reference to the From ellipsoid. */
    public final void reverse(Point inPoint){
        if (myIsReversed) doForward(inPoint);
        else doReverse(inPoint);
    }

    /** Convert the coordinates of the given point from reference to the From ellipsoid to reference to the To ellipsoid. */
    protected abstract void doForward(Point inPoint);
    
    /** Convert the coordinates of the given point from reference to the To ellipsoid to reference to the From ellipsoid. */
    protected abstract void doReverse(Point inPoint);

    /** Set the given parameter to the given value */
    public abstract void setParameter(String inName, String inValue);
    
    /** Get the edit panel for this transform, if there is not one, then just return null (the default)*/
    public TransformEditPanel getEditPanel(){return null;}
    
    /** Reverse the Transform */
    private boolean myIsReversed = false;
    /** Set the Reversed flag */
    public void setIsReversed(boolean inIsReversed){myIsReversed = inIsReversed;}
    /** Get the Reversed flag */
    public boolean getIsReversed(){return myIsReversed;}
    
    /**
     * Convert the given point from polar coordinates on the given from ellipsoid to cartesizn coordinates.
     */
    public static void toCartesian (Ellipsoid inEllipsoid, Point inPoint, CartesianPoint inCartesianPoint){
        if ((inPoint == null) || (inCartesianPoint == null) || (inEllipsoid == null)) return;
        double phi = Math.toRadians(inPoint.getY());
        double lam = Math.toRadians(inPoint.getX());
        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        
        // first convert the coordinates to cartesian
        double a = inEllipsoid.getMajorAxis();
        double b = inEllipsoid.getMinorAxis();
        double f = (a-b)/a;
        double v = a/Math.sqrt(1-(2*f-f*f)*sinphi*sinphi);
        double x = v*cosphi*Math.cos(lam);
        double y = v*cosphi*Math.sin(lam);
        double one_f = 1-f;
        double z = v*one_f*one_f*sinphi;
        
        inCartesianPoint.setX(x);
        inCartesianPoint.setY(y);
        inCartesianPoint.setZ(z);
    }
    
    /**
     * Convert the given point from it's Cartesian representation to a point on the given TO ellipsoid
     */
    public static void toPolar(Ellipsoid inEllipsoid, CartesianPoint inCartesianPoint, Point inPoint){
        // convert the parameters back to the latitude and longitude values.
        // This is an itterative solution
        double x =inCartesianPoint.getX();
        double y =inCartesianPoint.getY();
        double z =inCartesianPoint.getZ();
        double a = inEllipsoid.getMajorAxis();
        double b = inEllipsoid.getMinorAxis();
        double f = (a-b)/a;
        double lam = Math.atan(y/x);
        double phi0 = 0;
        double phi = Math.toRadians(inPoint.getY());
        for (int i=0; i<10; i++){
            double sinphi = Math.sin(phi);
            double v = a/Math.sqrt(1-(2*f-f*f)*sinphi*sinphi);
            phi0 = Math.atan( (z+(2*f-f*f)*v*sinphi)/(Math.sqrt(x*x+y*y)));
            if (Math.abs(phi-phi0) < 1e-7) break;
        }
        phi = phi0;
        inPoint.setX(Math.toDegrees(lam));
        inPoint.setY(Math.toDegrees(phi));
    }    
}
