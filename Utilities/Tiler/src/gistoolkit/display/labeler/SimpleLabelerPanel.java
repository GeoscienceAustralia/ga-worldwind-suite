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

package gistoolkit.display.labeler;

import java.awt.*;
import javax.swing.*;
import gistoolkit.display.*;
/**
 * Panel for editing a SimpleLabeler
 */
public class SimpleLabelerPanel extends LabelerPanel {

    /** The text field to use to select the record to label by */
    private JComboBox myComboColumns = new JComboBox();
    
    /** The combo box with the available anchors. */
    private JComboBox myComboOrientation = new JComboBox();
    
    /** The text field to type in an offset. */
    private JTextField myTextFieldOffset = new JTextField();
    
    /** The check box to indicate if duplicates are allowed. */
    private JCheckBox myCheckBoxDuplicates = new JCheckBox("Duplicates");
    
    /** The check box to indicate if overlaps are allowed. */
    private JCheckBox myCheckBoxOverlaps = new JCheckBox("Overlaps");
    
    /** Creates new SimpleLabelerPanel */
    public SimpleLabelerPanel() {
        initPanel();
    }
    
    /** Reference to the SimpleLabeler to edit.*/
    private SimpleLabeler myLabeler = null;
    
    /** Set the SimpleLabeler to be edited */
    public void setLabeler(Labeler inLabeler){
        if (inLabeler instanceof SimpleLabeler){
            myLabeler = (SimpleLabeler) inLabeler;
            if (myComboColumns.getItemCount() > myLabeler.getLabelColumn()){
                myComboColumns.setSelectedIndex(myLabeler.getLabelColumn());
            }
            if (myComboOrientation.getItemCount() > myLabeler.getLabelOrientation()){
                myComboOrientation.setSelectedIndex(myLabeler.getLabelOrientation());
            }
            if (myLabeler.getAllowDuplicates()){
                myCheckBoxDuplicates.setSelected(true);
            }
            else{
                myCheckBoxDuplicates.setSelected(false);
            }
            if (myLabeler.getAllowOverlaps()){
                myCheckBoxOverlaps.setSelected(true);
            }
            else{
                myCheckBoxOverlaps.setSelected(false);
            }
            myTextFieldOffset.setText(""+myLabeler.getLabelOffset());
        }
    }
    
    /** Retrieve the edited labeler */
    public Labeler getLabeler(){
        // label column
        myLabeler.setLabelColumn(myComboColumns.getSelectedIndex());
        
        // label orientation
        myLabeler.setLabelOrientation(myComboOrientation.getSelectedIndex());
        
        // set the offset from the anchor position.
        int tempOffset = 0;
        try{
            tempOffset = Integer.parseInt(myTextFieldOffset.getText());
        }
        catch (NumberFormatException e){
        }
        myLabeler.setLabelOffset(tempOffset);
        
        // allow duplicates
        if (myCheckBoxDuplicates.isSelected()){
            myLabeler.setAllowDuplicates(true);
        }
        else myLabeler.setAllowDuplicates(false);
        
        // allow overlaps
        if (myCheckBoxOverlaps.isSelected()){
            myLabeler.setAllowOverlaps(true);
        }
        else myLabeler.setAllowOverlaps(false);
        
        // return the configured labeler
        return myLabeler;
    }
    
    /** Set up the user interface elements for this panel. */
    private void initPanel(){
        // populate the entries in the orientation combo
        myComboOrientation.addItem("Center");
        myComboOrientation.addItem("North");
        myComboOrientation.addItem("East");
        myComboOrientation.addItem("South");
        myComboOrientation.addItem("West");
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        // the label to tell the user which column
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Label Column"), c);
        
        // The text field
        c.gridy++;
        add(myComboColumns, c);
        
        // the label to tell the user which to select an anchor.
        c.gridy++;
        add(new JLabel("Anchor"), c);
        c.gridy++;
        add(myComboOrientation, c);
        
        // Offset
        c.gridy++;
        add(new JLabel("Offset in pixels"), c);
        c.gridy++;
        add(myTextFieldOffset, c);
        
        // The checkbox for duplicates
        c.gridy++;
        add(myCheckBoxDuplicates, c);
        
        // The checkbox for overlaps
        c.gridy++;
        add(myCheckBoxOverlaps, c);

        // Fill up the rest of the space
        c.gridy++;
        c.weighty = 1;
        add(new JPanel(), c);
    }
    
    /** Set the columns */
    public void setColumns(String[] inColumns){
        myComboColumns.removeAllItems();
        if (inColumns != null){
            for (int i=0; i<inColumns.length; i++){
                myComboColumns.addItem(inColumns[i]);
            }
        }
    }
}
