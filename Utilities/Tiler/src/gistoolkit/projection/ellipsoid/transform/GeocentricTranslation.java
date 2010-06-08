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

/**
 * The vast majority of the transforms are simple geocentric translations.  This class will accomodate those translations.
 * @author  bitterstorm
 */
public class GeocentricTranslation extends EllipsoidTransform {
    
    /** Holdst the Translation in the X direction */
    private double myXTranslation = 0;
    /** Set the translation in the X direction */
    public void setXTranslation(double inXTranslation){myXTranslation = inXTranslation;}
    /** Return the translation in the X direction */
    public double getXTranslation(){return myXTranslation;}
    /** Holdst the Translation in the Y direction */
    private double myYTranslation = 0;
    /** Set the translation in the Y direction */
    public void setYTranslation(double inYTranslation){myYTranslation = inYTranslation;}
    /** Return the translation in the Y direction */
    public double getYTranslation(){return myYTranslation;}
    /** Holdst the Translation in the Z direction */
    private double myZTranslation = 0;
    /** Set the translation in the Z direction */
    public void setZTranslation(double inZTranslation){myZTranslation = inZTranslation;}
    /** Return the translation in the Z direction */
    public double getZTranslation(){return myZTranslation;}

    private String myName="Geocentric Translation";
    /** Creates new GeocentricTranslation */
    public GeocentricTranslation() {
        setName(myName);
    }
    
    /** Creates new GeocentricTranslation with the given ellipsoids, and translation parameters.*/
    public GeocentricTranslation(Ellipsoid inFromEllipsoid, Ellipsoid inToEllipsoid, double inXTranslation, double inYTranslation, double inZTranslation) {
        super (inFromEllipsoid, inToEllipsoid);
        myXTranslation = inXTranslation;
        myYTranslation = inYTranslation;
        myZTranslation = inZTranslation;
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
        
        double nx = x + myXTranslation;
        double ny = y + myYTranslation;
        double nz = z + myZTranslation;
        
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
        
        double nx = x - myXTranslation;
        double ny = y - myYTranslation;
        double nz = z - myZTranslation;
        
        myCPoint.setX(nx);
        myCPoint.setY(ny);
        myCPoint.setZ(nz);
        
        toPolar(getFromEllipsoid(), myCPoint, inPoint);
    }
    
//    /** Test this transform */
//    public static void main(String[] inargs){
//        // Retrieve the ellipsoid
//        Ellipsoid tempFromEllipsoid = EllipsoidFactory.getEllipsoid("WGS 72");
//        Ellipsoid tempToEllipsoid = EllipsoidFactory.getEllipsoid("WGS 84");
        
        // convert arc seconds to radians
//        PositionVectorTransform myTransform = new PositionVectorTransform(tempFromEllipsoid, tempToEllipsoid, 0, 0, 4.5, 0, 0, dz, 0.219);
//        Point tempPoint = new Point(4, 55);
//        System.out.println("X = "+tempPoint.getX());
//        System.out.println("Y = "+tempPoint.getY());
//        myTransform.forward(tempPoint);
//        System.out.println("X = "+tempPoint.getX());
//        System.out.println("Y = "+tempPoint.getY());
//        myTransform.reverse(tempPoint);
//        System.out.println("X = "+tempPoint.getX());
//        System.out.println("Y = "+tempPoint.getY());
//    }
    
    /** Set the given parameter to the given value */
    public void setParameter(String inName, String inValue){
        try{
            if (inName != null){
                if (inName.equalsIgnoreCase("dx")){
                    setXTranslation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("dy")){
                    setYTranslation(Double.parseDouble(inValue));
                }
                if (inName.equalsIgnoreCase("dz")){
                    setZTranslation(Double.parseDouble(inValue));
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
        private JTextField myTextFieldYt = new JTextField();
        private JTextField myTextFieldZt = new JTextField();
        
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
            c.weightx = 1;
            c.gridx++;
            add(myTextFieldXt, c);
            c.weightx = 0;
            
            // Y Translation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Y Translation"), c);
            c.gridx++;
            add(myTextFieldYt, c);
            
            // Z Translation
            c.gridx = 0;
            c.gridy++;
            add(new JLabel("Z Translation"), c);
            c.gridx++;
            add(myTextFieldZt, c);            
        }
        
        private GeocentricTranslation myTransform = null;
        /** set the transform to be edited */
        public void setTransform(EllipsoidTransform inTransform){
            if (inTransform == null) return;
            if (!(inTransform instanceof GeocentricTranslation)) return;
            myTransform = (GeocentricTranslation) (inTransform);
            
            myTextFieldXt.setText(""+myTransform.getXTranslation());
            myTextFieldYt.setText(""+myTransform.getYTranslation());
            myTextFieldZt.setText(""+myTransform.getZTranslation());
        }
        
        /** retrieve the edited transform */
        public EllipsoidTransform getTransform() throws Exception {
            if (myTransform == null) myTransform = new GeocentricTranslation();
            String tempName = "";
            String tempValue = "";
            try{
                tempName = "X Translation";
                tempValue = myTextFieldXt.getText();
                myTransform.setXTranslation(Double.parseDouble(tempValue));
                tempName = "Y Translation";
                tempValue = myTextFieldYt.getText();
                myTransform.setYTranslation(Double.parseDouble(tempValue));
                tempName = "Z Translation";
                tempValue = myTextFieldZt.getText();
                myTransform.setZTranslation(Double.parseDouble(tempValue));
            }
            catch (Exception e){
                throw new Exception("Error "+e+" parsing parameter "+tempName+" Value = "+tempValue);
            }
            return myTransform;
        }
    }    

}
