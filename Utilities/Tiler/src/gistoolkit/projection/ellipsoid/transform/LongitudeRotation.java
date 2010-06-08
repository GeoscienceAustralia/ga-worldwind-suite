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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.*;

/**
 * One of the trivial transformations for ellipsoids, takes the longitude of one ellipsoid, and adds an offset to arrive at the second.
 */
public class LongitudeRotation extends EllipsoidTransform {

    /** Creates new LongitudeRotation */
    public LongitudeRotation() {
        setName("Longitude Rotation");
    }

    /** The constant to add to every longitude value */
    private double myLongitudeOffset = 0;
    /** Set the Longitude Offset */
    public void setLongitudeOffset(double inOffset){myLongitudeOffset = inOffset;}
    /** Retrieve the Longitude Offset */
    public double getLongitudeOffset(){return myLongitudeOffset;}
    
    /** Convert the coordinates of the given point from reference to the From ellipsoid to reference to the To ellipsoid. */
    protected void doForward(Point inPoint){
        inPoint.setX(inPoint.getX()+myLongitudeOffset);
    }
    
    /** Convert the coordinates of the given point from reference to the To ellipsoid to reference to the From ellipsoid. */
    protected void doReverse(Point inPoint){
        inPoint.setX(inPoint.getX()-myLongitudeOffset);
    }

    /** Set the given parameter to the given value */
    public void setParameter(String inName, String inValue){
        if (inName != null){
            if (inName.equalsIgnoreCase("Offset")){
                if (inValue != null){
                    try{
                        setLongitudeOffset(Double.parseDouble(inValue));
                    }
                    catch (Exception e){
                        System.out.println("Error Converting Parmaeter "+inName+" To a double value = "+inValue);
                    }
                }
            }
        }
    }

    /** Create an edit panel to allow the user to edit the parameters of this transform */
    public TransformEditPanel getEditPanel(){
        EditPanel tempEdit = new EditPanel();
        tempEdit.setTransform(this);
        return tempEdit;
    }
    
    class EditPanel extends TransformEditPanel{
        private JTextField myTextFieldRotation = new JTextField();
        
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
            add(new JLabel("Longitude Offset"), c);
            c.gridx++;
            c.weightx = 1;
            add(myTextFieldRotation, c);
        }
        
        private LongitudeRotation myTransform = null;
        /** set the transform to be edited */
        public void setTransform(EllipsoidTransform inTransform){
            if (inTransform == null) return;
            if (!(inTransform instanceof LongitudeRotation)) return;
            myTransform = (LongitudeRotation) (inTransform);
            
            myTextFieldRotation.setText(""+myTransform.getLongitudeOffset());
        }
        
        /** retrieve the edited transform */
        public EllipsoidTransform getTransform() throws Exception {
            if (myTransform == null) myTransform = new LongitudeRotation();
            String tempName = "";
            String tempValue = "";
            try{
                tempName = "Longitude Offset";
                tempValue = myTextFieldRotation.getText();
                myTransform.setLongitudeOffset(Double.parseDouble(tempValue));
            }
            catch (Exception e){
                throw new Exception("Error "+e+" parsing parameter "+tempName+" Value = "+tempValue);
            }
            return myTransform;
        }
    }
    
}
