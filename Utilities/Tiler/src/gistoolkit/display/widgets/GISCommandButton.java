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

import java.awt.event.*;
import javax.swing.*;
import gistoolkit.display.*;
/**
 * Button for executing a GIS Command.
 */
public class GISCommandButton extends JButton implements ActionListener, GISButton{
    
    // command to execute when this button is pressed.
    private Command myCommand;
    
    /**
     * GISButton constructor comment.
     */
    public GISCommandButton() {
        super();
        addActionListener(this);
    }
    
    /**
     * GISButton constructor comment.
     * @param text java.lang.String
     */
    public GISCommandButton(String text) {
        super(text);
        addActionListener(this);
    }
    
    /**
     * GISButton constructor comment.
     * @param text java.lang.String
     */
    public GISCommandButton(String text, Command inCommand) {
        super(text);
        myCommand = inCommand;
        addActionListener(this);
    }
    
    /**
     * GISButton constructor comment.
     * @param text java.lang.String
     * @param icon javax.swing.Icon
     */
    public GISCommandButton(String text, Icon icon) {
        super(text, icon);
        addActionListener(this);
    }
    
    /**
     * GISButton constructor comment.
     * @param icon javax.swing.Icon
     */
    public GISCommandButton(Icon icon) {
        super(icon);
        addActionListener(this);
    }
    public void actionPerformed(ActionEvent inAE){
        if (myCommand != null) myCommand.execute();
    }
    
    /**
     * gets the command to execute when the button is pressed.
     */
    public Command getCommand(){
        return myCommand;
    }
    
    /**
     * Set the command to execute when the button is pressed.
     */
    public void setCommand(Command inCommand){
        myCommand = inCommand;
    }
}