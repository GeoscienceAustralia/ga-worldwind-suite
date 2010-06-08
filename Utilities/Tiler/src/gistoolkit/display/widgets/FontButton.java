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
/**
 * A button to allow editing of Fonts.
 */
public class FontButton extends JButton implements ActionListener{
    private FontDlg myChooser;
    
    /** Creates new FontButton */
    public FontButton() {
        super("Test");
        initPanel();
    }
    
    /** initialize the user interface */
    private void initPanel(){
        addActionListener(this);
    }

     /**
     * Respond to events from the JButton to display the color cooser.
     */
    public void actionPerformed(ActionEvent inAE){
        if (myChooser == null) myChooser = new FontDlg();
        myChooser.setFont(getFont());
        myChooser.setModal(true);
        myChooser.setVisible(true);
        setFont(myChooser.getFont());
        getParent().validate();
        paint(getGraphics());
    }
}
