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
import javax.swing.*;
/**
 * Class to allow the editing of colors.
 */
public class ColorDlg extends GISToolkitDialog {
    
    /**
     * Reference to the color chooser.
     */
    private JColorChooser myColorChooser = new JColorChooser();
    
    /**
     * ColorDlg constructor comment.
     */
    public ColorDlg() {
        super();
        initPanel();
    }
    
    /**
     * ColorDlg constructor comment.
     */
    public ColorDlg(Color inColor) {
        super();
        initPanel();
        myColorChooser.setColor(inColor);
    }
    
    /**
     * Get the color from the dialog.
     */
    public Color getColor(){
        return myColorChooser.getColor();
    }
    
    /**
     * Create the GUI Widgets for this panel.
     */
    private void initPanel(){
        setTitle("Select Color");
        JPanel tempPanel = (JPanel) getContentPane();
        
        tempPanel.setLayout(new BorderLayout());
        tempPanel.add(myColorChooser, BorderLayout.CENTER);
        setSize(500,400);
        resetSize();
    }
    
    /**
     * Set the color in the dialog.
     */
    public void setColor(Color inColor){
        myColorChooser.setColor(inColor);
    }
}