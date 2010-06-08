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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.*;

import gistoolkit.features.Point;
import gistoolkit.projection.Ellipsoid;
import gistoolkit.projection.ellipsoid.EllipsoidFactory; // testing only
/**
 * Coppied from EPSG Transformation Methods.
 * <p>
 * Transformation of coordinates from one geographic coordinate system into another (also known as a "datum transformation") is usually carried out as an implicit concatenation of three transformations:
 * [geographical to geocentric >> geocentric to geocentric >> geocentric to geographic]
 * The middle part of the concatenated transformation, from geocentric to geocentric, is usually described as a simplified 7-parameter Helmert transformation, expressed in matrix form with 7 parameters, in what is known as the "Bursa-Wolf" formula:
 * <pre>
 *   (X’)         (  1     -Rz    +Ry)     (X)    (dX)
 *   (Y’)  =  M * ( +Rz     1     -Rx)  *  (Y)  + (dY)
 *   (Z’)         ( -Ry   +Rx      1 )     (Z)    (dZ)
 * </pre>
 * The parameters are commonly referred to defining the datum transformation "from Datum 'A' to Datum 'B'", whereby (X, Y, Z) are the geocentric coordinates of the point on Datum ‘A’ and (X’, Y’, Z’) are the geocentric coordinates of the point on Datum ‘B’.  However, that does not define the parameters uniquely; neither is the definition of the parameters implied in the formula, as is often believed.  However, the following definition, which is consistent witth the "Position Vector Transformation" convention, is common E&P survey practice:
 * (dX, dY, dZ)   :Translation vector, to be added to the point's position vector in coordinate system 'A' in order to transform from system 'A' to system 'B'; also: the coordinates of the origin of system 'A' in the 'B' frame.
 * (Rx, Ry, Rz)   :Rotations to be applied to the point's vector.  The sign convention is such that a positive rotation about an axis is defined as a clockwise rotation of the position vector when viewed from the origin of the Cartesian coordinate system in the positive direction of that axis. E.g. a positive rotation about the Z-axis only from system 'A' to system 'B' will result in a larger longitude value for the point in system 'B'.
 * M                  :	The scale correction to be made to the position vector in coordinate system 'A' in order to obtain the correct scale of coordinate system 'B'. M = (1+S*10 6), whereby S is the scale correction expressed in parts per million.
 * </p>
 * @author  ithaqua
 */
public class PositionVectorTransform extends EllipsoidTransform {
    
    /** The translation to apply to the X coordinated */
    protected double myXt = 0;
    /** Set the translation to apply to the X coordinate */
    public void setXTranslation(double inXTranslation){myXt = inXTranslation;}
    /** Retrieve the translation to apply to the X coordinate */
    public double getXTranslation(){return myXt;}
    /** The rotation to apply to the X coordinated */
    protected double myXr = 0;
    /** Set the rotation to apply to the X coordinate */
    public void setXRotation(double inXRotation){myXr = inXRotation;}
    /** Retrieve the rotation to apply to the X coordinate */
    public double getXRotation(){return myXr;}
    
    /** The translation to apply to the Y coordinated */
    protected double myYt = 0;
    /** Set the translation to apply to the Y coordinate */
    public void setYTranslation(double inYTranslation){myYt = inYTranslation;}
    /** Retrieve the translation to apply to the Y coordinate */
    public double getYTranslation(){return myYt;}
    /** The rotation to apply to the Y coordinated */
    protected double myYr = 0;
    /** Set the rotation to apply to the Y coordinate */
    public void setYRotation(double inYRotation){myYr = inYRotation;}
    /** Retrieve the rotation to apply to the Y coordinate */
    public double getYRotation(){return myYr;}
    
    /** The translation to apply to the Z coordinated */
    protected double myZt = 0;
    /** Set the translation to apply to the Z coordinate */
    public void setZTranslation(double inZTranslation){myZt = inZTranslation;}
    /** Retrieve the translation to apply to the Z coordinate */
    public double getZTranslation(){return myZt;}
    /** The rotation to apply to the Z coordinated */
    protected double myZr = 0;
    /** Set the rotation to apply to the Z coordinate */
    public void setZRotation(double inZRotation){myZr = inZRotation;}
    /** Retrieve the rotation to apply to the Z coordinate */
    public double getZRotation(){return myZr;}
    
    /** The scale value to apply to the entire transform in parts per million, or 1e-6 */
    protected double myScale;
    /** Set the scale value to apply to the entire transform in parts per million (ppm) or 1e-6*/
    public void setScale(double inScale){myScale = inScale;}
    /** Get the scale value to apply to the entire transform in parts per million (ppm) or 1e-6*/
    public double getScale(){return myScale;}
    
    private String myName = "Position Vector Transform";
    
    /** Creates a new PositionVectorTransform */
    public PositionVectorTransform(){
        super();
        setName(myName);
    }
    
    /**
     * Creates new PositionVectorTransform.  The To and From ellipsoids provide the reference frame.  The STranslation and YTranslation are in the units of measure of the ellipsoids
     * The Rotation parameters must be epxressed in decimal degreese.  The scale difference is expressed in parts per million or ppm.
     */
    public PositionVectorTransform(Ellipsoid inFromEllipsoid,Ellipsoid inToEllipsoid,double inXTranslation,double inYTranslation,double inZTranslation,double inXRotation,double inYRotation,double inZRotation,double inScaleDifference){
        super(inFromEllipsoid, inToEllipsoid);
        myXt = inXTranslation;
        myYt = inYTranslation;
        myZt = inZTranslation;
        myXr = Math.toRadians(inXRotation);
        myYr = Math.toRadians(inYRotation);
        myZr = Math.toRadians(inZRotation);
        myScale = inScaleDifference;
        setName(myName);
    }
    
    /** Cartesian coordinate to hold values. */
    private CartesianPoint myCPoint = new CartesianPoint();
    
    /** Transform the coordinates in the forward direction */
    protected void doForward(Point inPoint){
//        System.out.println("FromEllipsoid = "+getFromEllipsoid());
//        System.out.println("ToEllipsoid = "+getToEllipsoid());
//        System.out.println("X="+inPoint.getX());
//        System.out.println("Y="+inPoint.getY());
//        System.out.println("xt="+myXt);
//        System.out.println("xr="+myXr);
//        System.out.println("yt="+myYt);
//        System.out.println("yr="+myYr);
//        System.out.println("zt="+myZt);
//        System.out.println("zr="+myZr);
//        System.out.println("Scale="+myScale);
        
        toCartesian(getFromEllipsoid(), inPoint, myCPoint);
        double x = myCPoint.getX();
        double y = myCPoint.getY();
        double z = myCPoint.getZ();
        
        double M = 1+myScale*1e-6;
        
        double nx = (1*x      -myZr*y  +myYr*z)*M+myXt;
        double ny = (myZr*x   +1*y     -myXr*z)*M+myYt;
        double nz = (-myYr*x  +myXr*y  +1*z   )*M+myZt;
        
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
        double[][] tempT = {{1,      -myZr,   +myYr, nx},
        {myZr,   +1,      -myXr, ny},
        {-myYr,  +myXr,   +1,  nz }};
        
        Matrix tempMatrix = new Matrix(3,4,tempT);
        tempMatrix = tempMatrix.gaussJord();
        
        myCPoint.setX(tempMatrix.getValue(0,3));
        myCPoint.setY(tempMatrix.getValue(1,3));
        myCPoint.setZ(tempMatrix.getValue(2,3));
        
        toPolar(getFromEllipsoid(), myCPoint, inPoint);
    }
    
    /** Set the given parameter to the given value */
    public void setParameter(String inName, String inValue){
        try{
            if (inName != null){
                if (inName.equalsIgnoreCase("dx")){
                    setXTranslation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("rx")){
                    setXRotation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("dy")){
                    setYTranslation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("ry")){
                    setYRotation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("dz")){
                    setZTranslation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("rz")){
                    setZRotation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("scale")){
                    setScale(Double.parseDouble(inValue));
                }
            }
        }
        catch (Exception e){
            System.out.println("Error Converting Parmaeter "+inName+" To a double value = "+inValue);
        }
    }
    
    /** Create an edit panel to allow the user to edit this transform */
    public TransformEditPanel getEditPanel(){
        EditPanel tempEdit = new EditPanel();
        tempEdit.setTransform(this);
        return tempEdit;
    }
    
    class EditPanel extends TransformEditPanel{
        private JTextField myTextFieldXt = new JTextField();
        private JTextField myTextFieldXr = new JTextField();
        private JTextField myTextFieldYt = new JTextField();
        private JTextField myTextFieldYr = new JTextField();
        private JTextField myTextFieldZt = new JTextField();
        private JTextField myTextFieldZr = new JTextField();
        private JTextField myTextFieldScale = new JTextField();
        
        public EditPanel(){
            initPanel();
        }
        
        /** Initialize the edit panel */
        private void initPanel(){
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2,2,2,2);
            c.weightx = 0;
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            
            // X Translation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("X Translation"), c);
            c.gridx++;
            c.weightx = 1;
            add(myTextFieldXt, c);
            c.weightx = 0;
            // X Rotation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("X Rotation (deg)"), c);
            c.gridx++;
            add(myTextFieldXr, c);
            
            // Y Translation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Y Translation"), c);
            c.gridx++;
            add(myTextFieldYt, c);
            // Y Rotation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Y Rotation (deg)"), c);
            c.gridx++;
            add(myTextFieldYr, c);
            
            // Z Translation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Z Translation"), c);
            c.gridx++;
            add(myTextFieldZt, c);
            // Z Rotation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Z Rotation (deg)"), c);
            c.gridx++;
            add(myTextFieldZr, c);
            
            // Scale
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Scale (ppm)"), c);
            c.gridx++;
            add(myTextFieldScale, c);
        }
        
        private PositionVectorTransform myTransform = null;
        /** set the transform to be edited */
        public void setTransform(EllipsoidTransform inTransform){
            if (inTransform == null) return;
            if (!(inTransform instanceof PositionVectorTransform)) return;
            myTransform = (PositionVectorTransform) (inTransform);
            
            myTextFieldXt.setText(""+myTransform.getXTranslation());
            myTextFieldXr.setText(""+myTransform.getXRotation());
            myTextFieldYt.setText(""+myTransform.getYTranslation());
            myTextFieldYr.setText(""+myTransform.getYRotation());
            myTextFieldZt.setText(""+myTransform.getZTranslation());
            myTextFieldZr.setText(""+myTransform.getZRotation());
            myTextFieldScale.setText(""+myTransform.getScale());
        }
        
        /** retrieve the edited transform */
        public EllipsoidTransform getTransform() throws Exception {
            if (myTransform == null) myTransform = new PositionVectorTransform();
            String tempName = "";
            String tempValue = "";
            try{
                tempName = "X Translation";
                tempValue = myTextFieldXt.getText();
                myTransform.setXTranslation(Double.parseDouble(tempValue));
                tempName = "X Rotation";
                tempValue = myTextFieldXr.getText();
                myTransform.setXRotation(Double.parseDouble(tempValue));
                tempName = "Y Translation";
                tempValue = myTextFieldYt.getText();
                myTransform.setYTranslation(Double.parseDouble(tempValue));
                tempName = "Y Rotation";
                tempValue = myTextFieldYr.getText();
                myTransform.setYRotation(Double.parseDouble(tempValue));
                tempName = "Z Translation";
                tempValue = myTextFieldZt.getText();
                myTransform.setZTranslation(Double.parseDouble(tempValue));
                tempName = "Z Rotation";
                tempValue = myTextFieldZr.getText();
                myTransform.setZRotation(Double.parseDouble(tempValue));
                tempName = "Scale";
                tempValue = myTextFieldScale.getText();
                myTransform.setScale(Double.parseDouble(tempValue));
            }
            catch (Exception e){
                throw new Exception("Error "+e+" parsing parameter "+tempName+" Value = "+tempValue);
            }
            return myTransform;
        }
    }
    
    /** Test this transform */
    public static void main(String[] inargs){
        // Retrieve the ellipsoid
        Ellipsoid tempFromEllipsoid = EllipsoidFactory.getEllipsoid("WGS 72");
        Ellipsoid tempToEllipsoid = EllipsoidFactory.getEllipsoid("WGS 84");
        
        // convert arc seconds to radians
        double rz = 0.554/(60*60);
        PositionVectorTransform myTransform = new PositionVectorTransform(tempFromEllipsoid, tempToEllipsoid, 0, 0, 4.5, 0, 0, rz, 0.219);
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
