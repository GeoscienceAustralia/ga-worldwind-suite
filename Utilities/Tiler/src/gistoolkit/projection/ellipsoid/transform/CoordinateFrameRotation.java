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
import gistoolkit.projection.*;
import gistoolkit.projection.ellipsoid.*;

/**
 * Although being common practice in particularly the European E&P industry Position Vector Transformation sign convention is not universally accepted.  A variation on this formula is also used, particularly in the USA E&P industry.  That formula is based on the same definition of translation and scale parameters, but a different definition of the rotation parameters.  The associated convention is known as the "Coordinate Frame Rotation" convention.
 * The formula is:
 * <pre>
 * (X’)         (  1      +Rz     -Ry)   (X)     (dX)
 * (Y’)  =  M * ( -Rz      1      +Rx) * (Y)  +  (dY)
 * (Z’)         ( +Ry   -Rx       1 )    (Z)     (dZ)
 * </pre>
 * and the parameters are defined as:
 * (dX, dY, dZ)   : Translation vector, to be added to the point's position vector in coordinate system 'A' in order to transform from system 'A' to system 'B'; also: the coordinates of the origin of system 'A' in the 'B' frame.
 * (Rx, Ry, Rz)   : Rotations to be applied to the coordinate frame.  The sign convention is such that a positive rotation of the frame about an axis is defined as a clockwise rotation of the coordinate frame when viewed from the origin of the Cartesian coordinate system in the positive direction of that axis, that is a positive rotation about the Z-axis only from system 'A' to system 'B' will result in a smaller longitude value for the point in system 'B'.
 * M                  : The scale factor to be applied to the position vector in coordinate system 'A' in order to obtain the correct scale of coordinate system 'B'. M = (1+S*10 6), whereby S is the scale correction expressed in parts per million.
 * In the absence of rotations the two formulas are identical; the difference is solely in the rotations. The name of the second method reflects this.
 * Note that the same rotation that is defined as positive in the first method is consequently negative in the second and vice versa.  It is therefore crucial that the convention underlying the definition of the rotation parameters is clearly understood and is communicated when exchanging datum transformation parameters, so that the parameters may be associated with the correct coordinate transformation method (algorithm).
 * @author  ithaqua
 */
public class CoordinateFrameRotation extends PositionVectorTransform {
    
    private String myName = "Coordinate Frame Rotation";
    /** Creates a new CoordinateFrameRotation */
    public CoordinateFrameRotation(){
        super();
        setName(myName);
    }
    
    /**
     * Creates new CoordinateFrameRotation.  The To and From ellipsoids provide the reference frame.  The STranslation and YTranslation are in the units of measure of the ellipsoids
     * The Rotation parameters must be epxressed in decimal degreese.  The scale difference is expressed in parts per million or ppm.
     */
    public CoordinateFrameRotation(Ellipsoid inFromEllipsoid, Ellipsoid inToEllipsoid, double inXTranslation, double inYTranslation, double inZTranslation, double inXRotation, double inYRotation, double inZRotation, double inScaleDifference){
        super(inFromEllipsoid, inToEllipsoid, inXTranslation, inYTranslation, inZTranslation, inXRotation, inYRotation, inZRotation, inScaleDifference);
        setName(myName);
    }
    
    /** Cartesian coordinate to hold values. */
    private CartesianPoint myCPoint = new CartesianPoint();
    /** Transform the coordinates in the forward direction */
    protected void doForward(Point inPoint){
        
        toCartesian(getFromEllipsoid(), inPoint, myCPoint);
        double x = myCPoint.getX();
        double y = myCPoint.getY();
        double z = myCPoint.getZ();
        
        double M = 1+myScale*1e-6;
        
        double nx = (1*x      +myZr*y  -myYr*z)*M+myXt;
        double ny = (-myZr*x  +1*y     +myXr*z)*M+myYt;
        double nz = (+myYr*x  -myXr*y  +1*z   )*M+myZt;
        
        myCPoint.setX(nx);
        myCPoint.setY(ny);
        myCPoint.setZ(nz);
        
        toPolar(getToEllipsoid(), myCPoint, inPoint);
    }
    
    /** Transform the coordinates in the reverse direction */
    protected void doReverse(Point inPoint){
        
        toCartesian(getToEllipsoid(), inPoint, myCPoint);
        double x = myCPoint.getX();
        double y = myCPoint.getY();
        double z = myCPoint.getZ();
        
        // remove the scale, and translation
        double M = 1+myScale*1e-6;
        
        double nx = (x-myXt)/M;
        double ny = (y-myYt)/M;
        double nz = (z-myZt)/M;
        
        // solve the resulting Matrix.
        double[][] tempT = {{1,      +myZr,   -myYr, nx},
        {-myZr,   +1,      +myXr, ny},
        {+myYr,  -myXr,   +1,  nz }};
        
        Matrix tempMatrix = new Matrix(3,4,tempT);
        tempMatrix = tempMatrix.gaussJord();
        
        myCPoint.setX(tempMatrix.getValue(0,3));
        myCPoint.setY(tempMatrix.getValue(1,3));
        myCPoint.setZ(tempMatrix.getValue(2,3));
        
        toPolar(getFromEllipsoid(), myCPoint, inPoint);
    }
    
    /** Test this transform */
    public static void main(String[] inargs){
        // Retrieve the ellipsoid
        Ellipsoid tempFromEllipsoid = EllipsoidFactory.getEllipsoid("WGS 72");
        Ellipsoid tempToEllipsoid = EllipsoidFactory.getEllipsoid("WGS 84");
        
        // convert arc seconds to radians
        double dz = -0.554/(60*60);
        CoordinateFrameRotation myTransform = new CoordinateFrameRotation(tempFromEllipsoid, tempToEllipsoid, 0, 0, 4.5, 0, 0, dz, 0.219);
        Point tempPoint = new Point(4, 55);
        System.out.println("X = "+tempPoint.getX());
        System.out.println("Y = "+tempPoint.getY());
        myTransform.forward(tempPoint);
        System.out.println("X = "+tempPoint.getX());
        System.out.println("Y = "+tempPoint.getY());
        myTransform.reverse(tempPoint);
        System.out.println("X = "+tempPoint.getX());
        System.out.println("Y = "+tempPoint.getY());
    }
}
