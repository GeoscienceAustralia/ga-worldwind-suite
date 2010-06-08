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
 * Handles the inputing of the basic information associated with a simple projection.
 */
public class SimpleProjectionPanel extends ProjectionPanel {

    /** Reference to the TextField for the Latitude of oragin */
    protected JTextField myTextFieldLatitude0 = new JTextField("0");
    /** Return the Orrigional Latitude in degreese*/
    public double getOrrigionalLatitude(){return Double.parseDouble(myTextFieldLatitude0.getText());}
    /** set the Orrigional Latitude in degreese */
    public void setOrrigionalLatitude(double inLat){myTextFieldLatitude0.setText(""+inLat);}
    /** reference to the TextField for the Longitude of the oragin */
    protected JTextField myTextFieldLongitude0 = new JTextField("0");
    /** Return the Orrigional Longitude */
    public double getOrrigionalLongitude(){return Double.parseDouble(myTextFieldLongitude0.getText());}
    /** Set the Orrigional longitude */
    public void setOrrigionalLongitude(double inLong){myTextFieldLongitude0.setText(""+inLong);}
    /** Reference to the false easting of the projection */
    protected JTextField myTextFieldEasting0 = new JTextField("0");
    /** return the false easting of the projection */
    public double getFalseEasting(){return Double.parseDouble(myTextFieldEasting0.getText());}
    /** set the false easting of the projection */
    public void setFalseEasting(double inEasting){myTextFieldEasting0.setText(""+inEasting);}
    /** Reference to the false northing of the projection */
    protected JTextField myTextFieldNorthing0 = new JTextField("0");
    /** return the false northing of the projection */
    public double getFalseNorthing(){return Double.parseDouble(myTextFieldNorthing0.getText());}
    /** Set the false norghing of the projection */
    public void setFalseNorthing(double inNorthing){myTextFieldNorthing0.setText(""+inNorthing);}
    /** Choice box for selecting the Elipse to use */
    private JComboBox myComboElipses = new JComboBox();
    /** Reference to the known ellipsoids */
    private Ellipsoid[] myEllipsoids = new Ellipsoid[0];
    
    /** Creates new SimpleProjectionPanel */
    public SimpleProjectionPanel() {
        initPanel();
    }
    
    /** Set up the user interface elements for this panel */
    private void initPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        // The ellipsoid of choice
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Ellipsoid for base data"), c);
        // load up the ellipsoid choice
        myEllipsoids = EllipsoidFactory.getKnownEllipsoids();
        for (int i=0; i<myEllipsoids.length; i++){
            myComboElipses.addItem(myEllipsoids[i]);
        }
        c.gridy++;
        add(myComboElipses, c);
        
        // Latitude of oragin
        c.gridy++;
        add(new JLabel("Latitude of oragin"), c);
        c.gridy++;
        add(myTextFieldLatitude0, c);
        
        // Longitude of oragin
        c.gridy++;
        add(new JLabel("Longitude of oragin"), c);
        c.gridy++;
        add(myTextFieldLongitude0, c);
        
        // The false northing
        c.gridy++;
        add(new JLabel("False Northing of Map"), c);
        c.gridy++;
        add(myTextFieldNorthing0, c);
        
        // The false easting
        c.gridy++;
        add(new JLabel("False Easting of Map"), c);
        c.gridy++;
        add(myTextFieldEasting0, c);
        
        // the panel for display of further parameters
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel tempPanel = new JPanel(new BorderLayout());
        myDisplayPanel = new JPanel();
        tempPanel.add(myDisplayPanel, BorderLayout.NORTH);
        add(tempPanel, c);
        
    }
    
    /** Panel for displaying additional parameters */
    private JPanel myDisplayPanel = null;
    
    /** return the panel for display of additional parameters */
    protected JPanel getDisplayPanel(){
        return myDisplayPanel;
    }

    /** Currently editing projection */
    private SimpleProjection myProjection = null;
    
    /** set the projection to be edited */
    public void setProjection(Projection inProjection){
        if (inProjection instanceof SimpleProjection){
            myProjection = (SimpleProjection) inProjection;
            for (int i=0; i<myComboElipses.getItemCount(); i++){
                Ellipsoid tempElipse = (Ellipsoid) myComboElipses.getItemAt(i);
                if (tempElipse != null){
                    Ellipsoid tempProjecitonEllipsoid = myProjection.getEllipsoid();
                    if (tempProjecitonEllipsoid != null){
                        if (tempElipse.getName().equalsIgnoreCase(tempProjecitonEllipsoid.getName())){
                            myComboElipses.setSelectedIndex(i);
                        }
                    }
                }
            }
            
            myTextFieldEasting0.setText(""+myProjection.getEasting());
            myTextFieldNorthing0.setText(""+myProjection.getNorthing());
            myTextFieldLatitude0.setText(""+myProjection.getLatOragin());
            myTextFieldLongitude0.setText(""+myProjection.getLonOragin());
        }
    }
    
    /** Return the edited projection back to the caller*/
    public Projection getProjection(){
        if (myProjection != null){
            myProjection.setEllipsoid((Ellipsoid) myComboElipses.getSelectedItem());
            myProjection.setEasting(getFalseEasting());
            myProjection.setNorthing(getFalseNorthing());
            myProjection.setLatOragin(getOrrigionalLatitude());
            myProjection.setLonOragin(getOrrigionalLongitude());
        }
        return myProjection;
    }
}
