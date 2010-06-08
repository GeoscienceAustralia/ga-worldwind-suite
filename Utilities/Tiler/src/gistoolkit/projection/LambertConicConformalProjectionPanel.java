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

import java.awt.*;
import javax.swing.*;
/**
 * Allows the user to edit the attributes of a projection.
 * @author  ithaqua
 */
public class LambertConicConformalProjectionPanel extends SimpleProjectionPanel{

    /** allows the user to input the first of two latitudes of zero distortion */
    private JTextField myTextFieldLatitude1 = new JTextField();
    
    /** allows the user to imput the second of two latitudes of zero distortion */
    private JTextField myTextFieldLatitude2 = new JTextField();
    
    /** allows the user to input the scale factor */
    private JTextField myTextFieldScaleFactor = new JTextField();
    
    /** Creates new LambertConnicConformalProjectionPanel */
    public LambertConicConformalProjectionPanel() {
        initPanel();
    }

    /** Setup the user interface elements of this panel */
    private void initPanel(){
        JPanel tempPanel = getDisplayPanel();
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        // the First latitude
        c.gridx = 0;
        c.gridy = 0;
        tempPanel.add(new JLabel("First Latitude"), c);
        c.gridy++;
        tempPanel.add(myTextFieldLatitude1, c);
        
        // the Second latitude
        c.gridy++;
        tempPanel.add(new JLabel("Second Latitude"), c);
        c.gridy++;
        tempPanel.add(myTextFieldLatitude2, c);
        
        // the Scale factor
        c.gridy++;
        tempPanel.add(new JLabel("Scale Factor"), c);
        c.gridy++;
        tempPanel.add(myTextFieldScaleFactor, c);
    }
    
    /** Reference to the currently editing projection */
    private LambertConicConformalProjection myLCCProjection = null;
    
    /** Set the projection to be edited */
    public void setProjection(Projection inProjection){
        if (inProjection instanceof LambertConicConformalProjection){
            super.setProjection(inProjection);
            myLCCProjection = (LambertConicConformalProjection) inProjection;
            myTextFieldLatitude1.setText(""+myLCCProjection.getLatitude1());
            myTextFieldLatitude2.setText(""+myLCCProjection.getLatitude2());
            myTextFieldScaleFactor.setText(""+myLCCProjection.getScaleFactor());
        }
    }
    
    /** Retrieve the edited projeciton */
    public Projection getProjection(){
        if (myLCCProjection != null){
            super.getProjection();
            myLCCProjection.setLatitude1(Double.parseDouble(myTextFieldLatitude1.getText()));
            myLCCProjection.setLatitude2(Double.parseDouble(myTextFieldLatitude2.getText()));
            myLCCProjection.setScaleFactor(Double.parseDouble(myTextFieldScaleFactor.getText()));
        }
        return myLCCProjection;
    }
    
    
}
