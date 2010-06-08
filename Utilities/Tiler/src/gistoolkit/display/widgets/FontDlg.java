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
 * Dialog to allow the selecting of fonts.
 */
public class FontDlg extends GISToolkitDialog implements ItemListener{
    private Font[] myFontSelection = null;
    private JComboBox myChoiceFont = new JComboBox();
    private JComboBox myChoiceStyle = new JComboBox();
    private JComboBox myChoiceSize = new JComboBox();
    private int[] myStyles = {Font.PLAIN, Font.BOLD, Font.ITALIC};
    private String[] myStyleStrings = {"Plain","Bold","Italic"};
    private int[] mySizes = {7,8,9,10,12,14,16,18,20,25,30,40,48,56,72};
    private JLabel myDisplayLabel = new JLabel("Font");
    
    /** Creates new FontDlg */
    public FontDlg() {
        super();
        initPanel();
    }
    
    /** initialize the User Interface elements of the dialog */
    private void initPanel(){
        setTitle("Select Font");
        JPanel tempPanel = (JPanel) getContentPane();
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        
        // choice of font
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        tempPanel.add(new JLabel("Font Face"), c);
        c.gridx++;
        tempPanel.add(myChoiceFont, c);
        myFontSelection = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        for (int i=0; i<myFontSelection.length; i++){
            myChoiceFont.addItem(myFontSelection[i].getFontName());
        }
        myChoiceFont.addItemListener(this);
        
        // choice of style
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Font Style"), c);
        c.gridx++;
        tempPanel.add(myChoiceStyle, c);
        for (int i=0; i<myStyles.length; i++){
            myChoiceStyle.addItem(myStyleStrings[i]);
        }
        myChoiceStyle.addItemListener(this);
        
        // The size of the font
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Font Size"), c);
        c.gridx++;
        tempPanel.add(myChoiceSize, c);
        myChoiceSize.setEditable(true);
        for (int i=0; i<mySizes.length; i++){
            myChoiceSize.addItem(""+mySizes[i]);
        }
        myChoiceSize.addItemListener(this);
        
        // feedback
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        myDisplayLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        tempPanel.add(myDisplayLabel, c);
        
        setSize(500,400);
        resetSize();
        
    }
    
    public void itemStateChanged(ItemEvent inIE){
        try{
            Font tempFont = myFontSelection[myChoiceFont.getSelectedIndex()];
            float tempSize = (float) mySizes[myChoiceSize.getSelectedIndex()];
            tempFont = tempFont.deriveFont(myStyles[myChoiceStyle.getSelectedIndex()], tempSize);
            myDisplayLabel.setText(tempFont.getFontName());
            myDisplayLabel.setFont(tempFont);
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        pack();
    }
    
    /** set the font */
    public void setFont(Font inFont){
        // set the name
        myDisplayLabel.setText(inFont.getFontName());
        for (int i=0; i<myFontSelection.length; i++){
            if (myFontSelection[i].getFontName().equalsIgnoreCase(inFont.getFontName())){
                myChoiceFont.setSelectedIndex(i);
                break;
            }
        }
        
        // set the style
        for (int i=0; i<myStyles.length; i++){
            if (myStyles[i] == inFont.getStyle()){
                myChoiceStyle.setSelectedIndex(i);
                break;
            }
        }
        
        // set the size
        for (int i=0; i<mySizes.length; i++){
            if (mySizes[i] == inFont.getSize()){
                myChoiceSize.setSelectedIndex(i);
                break;
            }
        }
    }
    
    /** Retrieve the currently selected font from the dialog */
    public Font getFont(){
        return myDisplayLabel.getFont();
    }
}
