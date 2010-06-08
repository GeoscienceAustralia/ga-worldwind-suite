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
 * Just add an offset to the Y value.
 */
public class LatitudeTranslation extends EllipsoidTransform {

    /** Creates new LatitudeTranslation */
    public LatitudeTranslation() {
        setName("Latitude Translation");
    }
    
    /** The constant to add to every Latitude value */
    private double myLatitudeOffset = 0;
    /** Set the Latitude Offset */
    public void setLatitudeOffset(double inOffset){myLatitudeOffset = inOffset;}
    /** Retrieve the Latitude Offset */
    public double getLatitudeOffset(){return myLatitudeOffset;}
    
    /** Convert the coordinates of the given point from reference to the From ellipsoid to reference to the To ellipsoid. */
    protected void doForward(Point inPoint){
        inPoint.setY(inPoint.getY()+myLatitudeOffset);
    }
    
    /** Convert the coordinates of the given point from reference to the To ellipsoid to reference to the From ellipsoid. */
    protected void doReverse(Point inPoint){
        inPoint.setY(inPoint.getY()-myLatitudeOffset);
    }

    /** Set the given parameter to the given value */
    public void setParameter(String inName, String inValue){
        if (inName != null){
            if (inName.equalsIgnoreCase("Offset")){
                if (inValue != null){
                    try{
                        setLatitudeOffset(Double.parseDouble(inValue));
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
            add(new JLabel("Latitude Offset"), c);
            c.gridx++;
            c.weightx = 1;
            add(myTextFieldRotation, c);
        }
        
        private LatitudeTranslation myTransform = null;
        /** set the transform to be edited */
        public void setTransform(EllipsoidTransform inTransform){
            if (inTransform == null) return;
            if (!(inTransform instanceof LatitudeTranslation)) return;
            myTransform = (LatitudeTranslation) (inTransform);
            
            myTextFieldRotation.setText(""+myTransform.getLatitudeOffset());
        }
        
        /** retrieve the edited transform */
        public EllipsoidTransform getTransform() throws Exception {
            if (myTransform == null) myTransform = new LatitudeTranslation();
            String tempName = "";
            String tempValue = "";
            try{
                tempName = "Latitude Offset";
                tempValue = myTextFieldRotation.getText();
                myTransform.setLatitudeOffset(Double.parseDouble(tempValue));
            }
            catch (Exception e){
                throw new Exception("Error "+e+" parsing parameter "+tempName+" Value = "+tempValue);
            }
            return myTransform;
        }
    }

}
