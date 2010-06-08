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

package gistoolkit.display.widgets;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import gistoolkit.display.*;
/**
 * Button which will display a menu under and indented from the current button when it is selected.
 */
public class GISMenuButton extends JPanel implements ActionListener{
    // command to execute when this button is pressed.
    private Command myCommand;
    
    /**
     * Root button to display.
     */
    private AbstractButton myButton;
    
    /**
     * The grid bag constraints for adding items to the list.
     */
    private GridBagConstraints myGridBagConstraints;
    
    /**
     * Vector to hold the buttons for display.
     */
    private Vector myButtons = new Vector();
    
    /**
     * GISMenuButton constructor comment.
     */
    public GISMenuButton() {
        super();
    }
    
    /**
     * Create a button with this text title and this command.
     */
    public GISMenuButton(Command inCommand, AbstractButton inButton) {
        myButton = inButton;
        myCommand = inCommand;
        myButton.addActionListener(this);
    }

    /** Set the tooltip text */
    public void setToolTipText(String inToolTip){
        super.setToolTipText(inToolTip);
        if (myButton != null) myButton.setToolTipText(inToolTip);
    }
    
    /** Set the Icon */
    public void setIcon(Icon inIcon){
        if (myButton != null) myButton.setIcon(inIcon);
    }
    /** Set the Selected Icon */
    public void setSelectedIcon(Icon inIcon){
        if (myButton != null) myButton.setSelectedIcon(inIcon);
    }
    /** Set the icon to be displayed when the mouse is over the button */
    public void setRolloverIcon(Icon inIcon){
        if (myButton != null) myButton.setRolloverIcon(inIcon);
    }
    
    /** Add a mouse listener to the set */
    public void addMouseListener(MouseListener inML){
        if (myButton != null) myButton.addMouseListener(inML);
    }
    
    /** Remove the mouse listener from the set */
    public void removeMouseListener(MouseListener inML){
        if (myButton != null) myButton.removeMouseListener(inML);
    }
    
    /** Execute the command should it exist */
    public void actionPerformed(ActionEvent inAE){
        if (myCommand != null) myCommand.execute();
    }
    
    /**
     * Add a button to this menu.
     */
    public void addButton(AbstractButton inButton){
        if (!myButtons.contains(inButton)){
            myButtons.addElement(inButton);
            
            // add the button to the panel if it has been initialized.
            // if not, then it will be added on initPanel()
            if (myGridBagConstraints != null){
                myGridBagConstraints.gridy++;
                add(inButton, myGridBagConstraints);
            }
            else{
                inButton.setVisible(false);
            }
            dovalidate();
        }
    }
    
    /**
     * Validate this button.
     */
    public void dovalidate(){
        if (getParent() != null){
            getParent().validate();
        }
        else{
            super.validate();
        }
        
    }
    
    /**
     * gets the command to execute when the button is pressed.
     */
    public Command getCommand(){
        return myCommand;
    }
    
    /**
     * performs a click on the active Button
     */
    public void doClick() {
        myButton.doClick();
    }
    
    /**
     * Initialize this menu button, adding the panels.
     */
    public void initPanel(){
        // set the layout to a grid bag layout.
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        
        // Add the button
        if (myButton != null){
            add(myButton, c);
        }
        
        // add any additional buttons.
        c.insets = new Insets(2,2,2,2);
        for (int i=0; i<myButtons.size(); i++){
            c.gridy++;
            add((AbstractButton) myButtons.elementAt(i), c);
        }
        
        // Save the constraints for later
        myGridBagConstraints = c;
        
    }
    
    /**
     * Show the cascading buttons.
     */
    public void setButtonsVisible(boolean inVisible){
        for (int i=0; i<myButtons.size(); i++){
            ((AbstractButton) myButtons.elementAt(i)).setVisible(inVisible);
        }
        dovalidate();
    }
    
    /**
     * Set the command to execute when the button is pressed.
     */
    public void setCommand(Command inCommand){
        myCommand = inCommand;
    }
}
