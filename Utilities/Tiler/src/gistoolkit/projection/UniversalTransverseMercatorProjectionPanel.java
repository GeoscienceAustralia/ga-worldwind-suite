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
import java.awt.event.*;
import javax.swing.*;
/**
 * Panel for editing the Universal Transverse Mercator Projection.
 */
public class UniversalTransverseMercatorProjectionPanel extends ProjectionPanel implements ActionListener{

    /** Creates new UniversalTransverseMercatorProjectionPanel */
    public UniversalTransverseMercatorProjectionPanel() {
        initPanel();
    }
    
    /** hold a reference to the Projection so it may be retrieved */
    private UniversalTransverseMercatorProjection myProjection = null;
    
    /** a choice box of the UTM Zone to use */
    private JComboBox myComboZones = new JComboBox();
    
    /** a choice box of the North/South Zone to use */
    private JComboBox myComboNS = new JComboBox();

    /** A selection of wheather to follow the map or not */
    private JCheckBox myCheckBoxfollowMap = new JCheckBox("Zone Follows Map Center");
    
    /** Initialize the GUI controls for this projection */
    private void initPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.fill = GridBagConstraints.BOTH;
        
        // the checkbox
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        //add(myCheckBoxfollowMap, c);
        myCheckBoxfollowMap.addActionListener(this);
        
        // the zone combo box
        c.gridx = 0;
        c.gridy = 1;
        add(myComboZones, c);
        
        // populate the zones
        for (int i=0; i<60; i++){
            myComboZones.addItem("Zone"+i);
        }
        
        // the North/South combo box
        c.gridx = 0;
        c.gridy++;
        add(myComboNS, c);        
        myComboNS.addItem("North");
        myComboNS.addItem("South");
        
        // panel to push all the controls to the top
        c.gridx = 0;
        c.gridy++;
        c.weighty = 1;
        add(new JPanel(), c);
    }
    
    /** Respond to the actions from the checkbox */
    public void actionPerformed(ActionEvent inAE){
        if (myCheckBoxfollowMap.isSelected()){
            myComboZones.setEnabled(false);
        }
        else myComboZones.setEnabled(true);
    }
    
    /** Set the Projection within the panel */
    public void setProjection(Projection inProjection){
        if (inProjection instanceof UniversalTransverseMercatorProjection){
            myProjection = (UniversalTransverseMercatorProjection) inProjection;
            if (myProjection != null){
                myComboZones.setSelectedIndex(myProjection.getZone());
                myCheckBoxfollowMap.setSelected(myProjection.getFollowMap());
                if (myProjection.getNorthing() > 0) myComboNS.setSelectedIndex(1);
                else myComboNS.setSelectedIndex(0);
            }
        }
    }
    
    /** Retrieve the edited projection from the panel */
    public Projection getProjection(){
        if (myProjection == null) return null;
        myProjection.setFollowMap(myCheckBoxfollowMap.isSelected());
        try{
            myProjection.setZone(myComboZones.getSelectedIndex());
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        int tempNS = myComboNS.getSelectedIndex();
        if (tempNS == 0) myProjection.setNorthing(0);
        if (tempNS == 1) myProjection.setNorthing(10000000);
        return myProjection;
    }
}
