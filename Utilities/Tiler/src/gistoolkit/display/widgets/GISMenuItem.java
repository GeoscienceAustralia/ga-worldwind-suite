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
 * Menu Item to hold a command so it can be executed from the menu.
 */
public class GISMenuItem extends JMenuItem implements ActionListener{

    /** Creates new GISMenuItem */
    public GISMenuItem(String inName, Command inCommand) {
        super(inName);
        myCommand = inCommand;
        addActionListener(this);
    }

    /** Command to execute when this Menu Item is selected */
    private Command myCommand = null;
    /** Set the command to be executed */
    public void setCommand(Command inCommand){myCommand = inCommand;}
    /** Retrieve the command to be executed */
    public Command getCommand(){return myCommand;}
    
    public void actionPerformed(java.awt.event.ActionEvent p1) {
        if (myCommand != null) myCommand.execute();
    }
    
}
