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
import gistoolkit.projection.ellipsoid.*;

/**
 * Allows the user to select the type of Ellipsoid for a no projection projection.
 */
public class NoProjectionPanel extends ProjectionPanel {
    
    /** Allows the us er to select the Ellipsoid to describe this projection */
    private JComboBox myChoiceEllipsoid = new JComboBox();
    
    /** Creates new NoProjectionPanel */
    public NoProjectionPanel() {
        initPanel();
    }
    
    /** Initialize the User Interface Components of this panel */
    private void initPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        
        // Label
        c.gridx = 0;
        c.gridy = 0;
        add (new JLabel("Ellipsoid"), c);
        c.gridx = 0;
        c.gridy++;
        add (myChoiceEllipsoid, c);
        
        // Space Panel
        c.gridy++;
        c.weighty = 1;
        add (new JPanel(), c);
        
        // Populate the combo box
        Ellipsoid[] tempEllipsoids = EllipsoidFactory.getKnownEllipsoids();
        for (int i=0; i<tempEllipsoids.length; i++){
            myChoiceEllipsoid.addItem(tempEllipsoids[i]);
        }
    }
    
    private NoProjection myProjection = null;
    /** Set the projection to be edited */
    public void setProjection(Projection inProjection){
        if (inProjection == null) return;
        if (inProjection instanceof NoProjection){
            myProjection = (NoProjection) inProjection;
            if (myProjection.getEllipsoid() != null){
                String tempEllipsoidName = myProjection.getEllipsoid().getName();
                for (int i=0; i<myChoiceEllipsoid.getItemCount(); i++){
                    String tempName = ((Ellipsoid) myChoiceEllipsoid.getItemAt(i)).getName();
                    if (tempEllipsoidName.equalsIgnoreCase(tempName)){
                        myChoiceEllipsoid.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
    
    /** Retrieve the edited projection */
    public Projection getProjection(){
        if (myProjection == null){
            myProjection = new NoProjection();
        }
        Ellipsoid tempEllipsoid = (Ellipsoid) myChoiceEllipsoid.getSelectedItem();
        myProjection.setEllipsoid(tempEllipsoid);
        return myProjection;
    }
}
