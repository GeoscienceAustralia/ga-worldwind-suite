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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * Button for displaying the colors editable in a shader.
 */
public class ColorButton extends JButton implements ActionListener{
    /** cache the color cooser to allow quicker display. */
    private ColorDlg myChooser = null;
    
    /**
     * Create a new color button with the default text.
     */
    public ColorButton() {
        super();
        initPanel();
    }
    
    /** Set the color to display */
    public void setColor(Color inColor){setBackground(inColor);}
    /** Retrieve the displayed color */
    public Color getColor(){return getBackground();}
    
    /**
     * Create a new color button with the given text displayed.
     * @param text java.lang.String
     */
    public ColorButton(String text) {
        super(text);
        initPanel();
    }
    
    /**
     * Respond to events from the JButton to display the color cooser.
     */
    public void actionPerformed(ActionEvent inAE){
        if (myChooser == null) myChooser = new ColorDlg();
        myChooser.setModal(true);
        myChooser.setVisible(true);
        myChooser.setLocationRelativeTo(this);
        setBackground(myChooser.getColor());
        paint(getGraphics());
    }
    
    /**
     * Initialize the panel to listen to any events.
     */
    private void initPanel(){
        addActionListener(this);
    }
}