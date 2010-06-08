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

package gistoolkit.display.shader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import gistoolkit.display.*;
import gistoolkit.display.shader.stroke.*;
import gistoolkit.display.shader.images.ImageSource;
import gistoolkit.display.widgets.*;

/**
 * Dialog for editing the attributes of a mono shader.
 */
public class MonoShaderPanel extends ShaderPanel {
    
    /** Creates new MonoShaderPanel */
    public MonoShaderPanel() {
        initPanel();
    }
    
    /**Reference to the shader to be edited.*/
    private MonoShader myShader = null;
    
    /**Allows editing the name of the shader*/
    private JTextField myTextFieldName = new JTextField();
    
    /**Alpha percentage for the shader.*/
    private JComboBox myComboAlpha = new JComboBox();
    private String ALPHA1 = "1.0";
    private String ALPHA2 = "0.75";
    private String ALPHA3 = "0.50";
    private String ALPHA4 = "0.25";
    private String ALPHA5 = "0.0";
    
    /**Reference to the check box for indicating if lines should be used.*/
    private JCheckBox myCheckboxLine = new JCheckBox();
    
    /**Reference to the Line Width*/
    private JComboBox myComboLineWidth = new JComboBox();
    private String LINEWIDTH1 = "1.0";
    private String LINEWIDTH2 = "2.0";
    private String LINEWIDTH3 = "3.0";
    private String LINEWIDTH4 = "4.0";
    private String LINEWIDTH5 = "5.0";
    private String LINEWIDTH6 = "6.0";
    private String LINEWIDTH7 = "7.0";
    private String LINEWIDTH8 = "8.0";
    private String LINEWIDTH9 = "9.0";
    private String LINEWIDTH10 = "10.0";
    
    /** Constants for type of stroke. */
    private String BASIC_STROKE = "BasicStroke";
    private String RR_STROKE = "RailRoad Stroke";
    
    /**Reference to the Line Style.*/
    private JComboBox myComboLineStyle = new JComboBox();
    private String[] myIconList = {
        "line.gif",
        "dash5x5.gif",
        "dash10x10.gif",
        "dash15x5.gif",
        "dash15x5x5x5.gif",
        "dash15x5x5x5x5x5.gif",
        "rr1.gif",
        "rr2.gif"
    };
    private String[] myStrokeTypeList = {
        BASIC_STROKE,
        BASIC_STROKE,
        BASIC_STROKE,
        BASIC_STROKE,
        BASIC_STROKE,
        BASIC_STROKE,
        RR_STROKE,
        RR_STROKE
    };
    private float[][] myDashArray = {
        {},
        {5,5},
        {10, 10},
        {15, 5},
        {15, 5, 5, 5},
        {15, 5, 5, 5, 5, 5},
        {},
        {}
    };
    
    /**Reference to the Line color shader button*/
    private ColorButton myColorButtonLine = new ColorButton();
    
    /**Reference to the check box for indicating if this should be used.*/
    private JCheckBox myCheckboxFill = new JCheckBox();
    
    /**Reference to the check box for indicating if this should be used.*/
    private JCheckBox myCheckboxPattern = new JCheckBox();
    
    /** Buffered Image to use for storing the pattern. */
    private java.awt.image.BufferedImage myFillPattern = null;
    /** Location of the FillPatternImage. */
    private String myFillPatternFileName = null;
    
    /**Reference to the fill color shader button.*/
    private ColorButton myColorButtonFill = new ColorButton();
    
    /**Reference to the Highlight color shader button.*/
    private ColorButton myColorButtonHighlight = new ColorButton();
    
    /** Button for browsing for new images. */
    private JButton myButtonFillStyle = new JButton("Find Image");
    
    /**Reference to the check box for indicating if labels should be used.*/
    private JCheckBox myCheckboxLabel = new JCheckBox();
    /**Reference to the Label color shader button.*/
    private ColorButton myColorButtonLabel = new ColorButton();
    
    /**Reference to the fontbutton to use when changing the font for the labels.*/
    private FontButton myFontButtonLabel = new FontButton();
    private MonoShaderPanel getThis(){return this;}
    
    /** Class for handling events from the buttons, the Find Image button in particular.*/
    private class MonoShaderPanelEventHandler implements ActionListener{
        FindImageDialog myFindImageDialog = null;
        public void actionPerformed(java.awt.event.ActionEvent inAE) {
            if (inAE.getSource() == myButtonFillStyle){
                // show a new dialog to look for the fill style.
                if (myFindImageDialog == null){
                    myFindImageDialog = new FindImageDialog();
                    myFindImageDialog.setModal(true);
                }
                if (myFillPattern != null) myFindImageDialog.setImage(myFillPattern);
                if (myFillPatternFileName != null) myFindImageDialog.setImageName(myFillPatternFileName);
                myFindImageDialog.setVisible(true);
                if (myFindImageDialog.isOK()){
                    try{
                        myFillPattern = myFindImageDialog.getImage();
                        myFillPatternFileName = myFindImageDialog.getImageName();
                    }
                    catch (Exception e){
                        JOptionPane.showMessageDialog(getThis(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    private MonoShaderPanelEventHandler myEventHandler = new MonoShaderPanelEventHandler();
        
    /**
     * Initialize the panel
     */
    private void initPanel() {
        // retrieve the panel and populate.
        JPanel tempPanel = this;
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.BOTH;
        
        // the name
        c.gridx = 0;
        c.gridy = 0;
        tempPanel.add(new JLabel("Name "), c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx++;
        tempPanel.add(myTextFieldName, c);
        c.gridwidth = 1;
        
        // the Line color
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        tempPanel.add(new JLabel("Line Color"), c);
        c.gridx++;
        tempPanel.add(myCheckboxLine, c);
        c.gridx++;
        tempPanel.add(myColorButtonLine, c);
        
        // Line Width
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Line Width"), c);
        c.gridx++;
        c.gridwidth = 2;
        myComboLineWidth.addItem(LINEWIDTH1);
        myComboLineWidth.addItem(LINEWIDTH2);
        myComboLineWidth.addItem(LINEWIDTH3);
        myComboLineWidth.addItem(LINEWIDTH4);
        myComboLineWidth.addItem(LINEWIDTH5);
        myComboLineWidth.addItem(LINEWIDTH6);
        myComboLineWidth.addItem(LINEWIDTH7);
        myComboLineWidth.addItem(LINEWIDTH8);
        myComboLineWidth.addItem(LINEWIDTH9);
        myComboLineWidth.addItem(LINEWIDTH10);
        tempPanel.add(myComboLineWidth, c);
        c.gridwidth = 1;
        
        // Line Style
        ImageSource tempImageSource = new ImageSource();
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Line Style"), c);
        c.gridx++;
        c.gridwidth = 2;
        for (int i=0; i<myIconList.length; i++){
            myComboLineStyle.addItem(tempImageSource.getIcon(myIconList[i]));
        }
        tempPanel.add(myComboLineStyle, c);
        c.gridwidth = 1;
        
        // Fill Color
        c.gridy++;
        c.gridx = 0;
        tempPanel.add(new JLabel("Fill Color"), c);
        c.gridx++;
        tempPanel.add(myCheckboxFill, c);
        c.gridx++;
        tempPanel.add(myColorButtonFill, c);
        
        // Fill Pattern
        c.gridy++;
        c.gridx = 0;
        tempPanel.add(new JLabel("Fill Pattern"), c);
        c.gridx++;
        tempPanel.add(myCheckboxPattern, c);
        c.gridx++;
        tempPanel.add(myButtonFillStyle, c);
        myButtonFillStyle.addActionListener(myEventHandler);
        
        // Highlight Color
        c.gridy++;
        c.gridx = 0;
        tempPanel.add(new JLabel("Highlight Color"), c);
        c.gridx++;
        tempPanel.add(myColorButtonHighlight, c);
        
        // Alpha Percentage
        c.gridy++;
        c.gridx = 0;
        tempPanel.add(new JLabel("Alpha Percent"), c);
        c.gridx++;
        c.gridwidth = 2;
        myComboAlpha.removeAllItems();
        myComboAlpha.addItem(ALPHA1);
        myComboAlpha.addItem(ALPHA2);
        myComboAlpha.addItem(ALPHA3);
        myComboAlpha.addItem(ALPHA4);
        myComboAlpha.addItem(ALPHA5);
        tempPanel.add(myComboAlpha, c);
        c.gridwidth = 1;
        
        // the Label Color
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Label Color"), c);
        c.gridx++;
        tempPanel.add(myCheckboxLabel, c);
        c.gridx++;
        tempPanel.add(myColorButtonLabel, c);
        
        // the Label Font
        c.gridx = 0;
        c.gridy++;
        tempPanel.add(new JLabel("Label Font"), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        tempPanel.add(myFontButtonLabel, c);
        c.gridwidth = 1;
        
        // add some space
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        c.weightx = 1;
        tempPanel.add(new JPanel(), c);
    }
    
    /**
     * Set the shader to be edited.
     */
    public void setShader(Shader inShader){
        if (inShader instanceof MonoShader){
            myShader = (MonoShader) inShader;
            myTextFieldName.setText(myShader.getName());
            myColorButtonFill.setBackground(myShader.getFillColor());
            myColorButtonLine.setBackground(myShader.getLineColor());
            myColorButtonLabel.setBackground(myShader.getLabelColor());
            myFontButtonLabel.setFont(myShader.getLabelFont());
            myColorButtonHighlight.setBackground(myShader.getHighlightColor());
            
            if (myShader.getLineColor() == null) myCheckboxLine.setSelected(false);
            else myCheckboxLine.setSelected(true);
            if (myShader.getLabelColor() == null) myCheckboxLabel.setSelected(false);
            else myCheckboxLabel.setSelected(true);
            if (myShader.getFillColor() == null) myCheckboxFill.setSelected(false);
            else myCheckboxFill.setSelected(true);
            if (myShader.getFillPattern() != null){
                myFillPattern = myShader.getFillPattern();
            }
            if (myShader.getFillPatternFileName() != null){
                myFillPatternFileName = myShader.getFillPatternFileName();
            }
            float tempAlphaF = myShader.getAlpha();
            if (Float.parseFloat(ALPHA1) == tempAlphaF){
                myComboAlpha.setSelectedItem(ALPHA1);
            }
            if (Float.parseFloat(ALPHA2) == tempAlphaF){
                myComboAlpha.setSelectedItem(ALPHA2);
            }
            if (Float.parseFloat(ALPHA3) == tempAlphaF){
                myComboAlpha.setSelectedItem(ALPHA3);
            }
            if (Float.parseFloat(ALPHA4) == tempAlphaF){
                myComboAlpha.setSelectedItem(ALPHA4);
            }
            if (Float.parseFloat(ALPHA5) == tempAlphaF){
                myComboAlpha.setSelectedItem(ALPHA5);
            }
            
            Stroke tempStroke = myShader.getStroke();
            myComboLineWidth.setSelectedIndex(0);
            myComboLineStyle.setSelectedIndex(0);
            if (tempStroke instanceof BasicStroke){
                float tempLineWidthF = ((BasicStroke) tempStroke).getLineWidth();
                setmyComboLineWidth(tempLineWidthF);
                float[] tempDashArray = ((BasicStroke) tempStroke).getDashArray();
                if (tempDashArray != null){
                    for(int i=0; i<myDashArray.length; i++){
                        float[] tempMyDashArray = myDashArray[i];
                        if (tempMyDashArray.length == tempDashArray.length){
                            boolean tempFound = true;
                            for (int j=0; j<tempMyDashArray.length; j++){
                                if (tempMyDashArray[j] != tempDashArray[j]){
                                    tempFound = false;
                                    break;
                                }
                            }
                            if (tempFound){
                                myComboLineStyle.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                }
            }
            else if (tempStroke instanceof RailRoadStroke){
                float tempLineWidthF = ((RailRoadStroke) tempStroke).getLineWidth();
                boolean tempTwoLine = ((RailRoadStroke) tempStroke).getTwoLine();
                String myGif = "rr1.gif";
                if (tempTwoLine) myGif = "rr2.gif";
                setmyComboLineWidth(tempLineWidthF);
                for (int i=0; i<myIconList.length; i++){
                    if (myIconList[i].equals(myGif)){
                        myComboLineStyle.setSelectedIndex(i);
                    }
                }
            }
        }
    }

    /** retrieve the edited shader from the dialog */
    public Shader getShader(){
        if (myShader == null) return null;
        myShader.setName(myTextFieldName.getText());
        myShader.setFillColor(myColorButtonFill.getBackground());
        if (myFillPatternFileName != null) try{myShader.setFillPatternFileName(myFillPatternFileName);}catch(Exception e){System.out.println(e); e.printStackTrace();}
        else if (myFillPattern != null) myShader.setFillPattern(myFillPattern);
        myShader.setLineColor(myColorButtonLine.getBackground());
        myShader.setHighlightColor(myColorButtonHighlight.getBackground());
        if (!myCheckboxLine.isSelected()) myShader.setLineColor(null);
        if (!myCheckboxFill.isSelected()) myShader.setFillColor(null);
        if (!myCheckboxPattern.isSelected()) myShader.setFillPattern(null);
        
        myShader.setLabelColor(myColorButtonLabel.getBackground());
        if (!myCheckboxLabel.isSelected()) myShader.setLabelColor(null);
        myShader.setLabelFont(myFontButtonLabel.getFont());
        
        try{
            myShader.setAlpha(Float.parseFloat(myComboAlpha.getSelectedItem().toString()));
        }
        catch (Exception e){
            System.out.println("MonoShaderDlg "+e);
        }
        try{
            // is this a Basic Stroke, or a Different stroke.
            int tempIndex = myComboLineStyle.getSelectedIndex();
            if (myStrokeTypeList[tempIndex] == BASIC_STROKE){
                float[] tempDashArray = myDashArray[myComboLineStyle.getSelectedIndex()];
                if (tempDashArray.length == 0){
                    myShader.setStroke(new BasicStroke(Float.parseFloat(myComboLineWidth.getSelectedItem().toString()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ));
                }
                else{
                    myShader.setStroke(new BasicStroke(Float.parseFloat(myComboLineWidth.getSelectedItem().toString()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, tempDashArray, (float) 0 ));
                }
            }
            if (myStrokeTypeList[tempIndex] == RR_STROKE){                    
                int tempLineWidth = (int) Float.parseFloat(myComboLineWidth.getSelectedItem().toString());
                int tempTieWidth = tempLineWidth*2;
                if (myIconList[tempIndex].equals("rr2.gif")){
                    tempTieWidth = tempLineWidth*3+tempLineWidth*2;
                    myShader.setStroke(new RailRoadStroke((float)tempLineWidth, (float)tempTieWidth, true));
                }
                else{
                    if ((tempLineWidth % 2) != 0) tempTieWidth = tempTieWidth+1;
                    myShader.setStroke(new RailRoadStroke((float)tempLineWidth, (float)tempTieWidth, false));
                }
            }
        }
        catch (Exception e){
            System.out.println("MonoShaderDlg "+e);
        }
        
        return myShader;
        
    }

    /** Private function to set the combo line width given the float. */
    private void setmyComboLineWidth(float inLineWidth){
        float tempLineWidthF = inLineWidth;
        if (Float.parseFloat(LINEWIDTH1) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH1);
        }
        if (Float.parseFloat(LINEWIDTH2) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH2);
        }
        if (Float.parseFloat(LINEWIDTH3) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH3);
        }
        if (Float.parseFloat(LINEWIDTH4) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH4);
        }
        if (Float.parseFloat(LINEWIDTH5) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH5);
        }
        if (Float.parseFloat(LINEWIDTH6) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH6);
        }
        if (Float.parseFloat(LINEWIDTH7) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH7);
        }
        if (Float.parseFloat(LINEWIDTH8) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH8);
        }
        if (Float.parseFloat(LINEWIDTH9) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH9);
        }
        if (Float.parseFloat(LINEWIDTH10) == tempLineWidthF){
            myComboLineWidth.setSelectedItem(LINEWIDTH10);
        }
    }
    public static void main(String[] inArgs){
        GISToolkitDialog tempDialog = new GISToolkitDialog();
        tempDialog.getContentPane().setLayout(new BorderLayout());
        tempDialog.getContentPane().add(new MonoShaderPanel(), BorderLayout.CENTER);
        tempDialog.setModal(true);
        tempDialog.show();
        System.exit(0);
    }
}
