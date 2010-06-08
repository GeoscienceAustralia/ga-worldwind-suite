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
import javax.swing.*;
import gistoolkit.display.*;
import gistoolkit.display.widgets.*;
import gistoolkit.display.shader.stroke.*;
import gistoolkit.display.shader.images.ImageSource;
/**
 * Allows editing of the Random shader.
 */
public class RandomShaderPanel extends ShaderPanel{
    
    /** Creates new RandomShaderPanel */
    public RandomShaderPanel() {
        initPanel();
    }
    
    /** Determines if the fill color should be used */
    private JCheckBox myCheckboxFillColor = new JCheckBox();
    /** Reference to the default fill color */
    private ColorButton myFillColorButton = new ColorButton();
    
    /** Determines if the line color should be used */
    private JCheckBox myCheckboxLineColor = new JCheckBox();
    /** Reference to the default line color */
    private ColorButton myLineColorButton = new ColorButton();
    
    /** Determines if the label color should be used */
    private JCheckBox myCheckboxLabelColor = new JCheckBox();
    /** Reference to the default label color */
    private ColorButton myLabelColorButton = new ColorButton();
    
    /**Alpha percentage for the shader.*/
    private JComboBox myComboAlpha = new JComboBox();
    private String ALPHA1 = "1.0";
    private String ALPHA2 = "0.75";
    private String ALPHA3 = "0.50";
    private String ALPHA4 = "0.25";
    private String ALPHA5 = "0.0";
    
    /** Reference to the Random Shader */
    private RandomShader myShader = null;
    
    /** The Random shader needs a column on which to operate */
    private JTextField myTextFieldColumn = new JTextField();
    
    /** Reference to the fontbutton to use when changing the font for the labels.*/
    private FontButton myFontButtonLabel = new FontButton();
    
    /**
     * Reference to the Line Width
     */
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
    
    /** initialize the user interface */
    private void initPanel(){
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.fill = GridBagConstraints.BOTH;
        
        // set the default Fill color
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Default Fill Color:"), c);
        c.gridx++;
        add(myCheckboxFillColor, c);
        c.gridx++;
        add(myFillColorButton, c);
        
        // set the default Fill color
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Default Line Color:"), c);
        c.gridx++;
        add(myCheckboxLineColor, c);
        c.gridx++;
        add(myLineColorButton, c);
        
        // set the default Fill color
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Default Label Color:"), c);
        c.gridx++;
        add(myCheckboxLabelColor, c);
        c.gridx++;
        add(myLabelColorButton, c);
        
        // the Label Font
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Label Font"), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(myFontButtonLabel, c);
        c.gridwidth = 1;
        
        // Alpha Percentage
        c.gridy++;
        c.gridx = 0;
        add(new JLabel("Alpha Percent"), c);
        c.gridx++;
        c.gridwidth = 2;
        myComboAlpha.removeAllItems();
        myComboAlpha.addItem(ALPHA1);
        myComboAlpha.addItem(ALPHA2);
        myComboAlpha.addItem(ALPHA3);
        myComboAlpha.addItem(ALPHA4);
        myComboAlpha.addItem(ALPHA5);
        add(myComboAlpha, c);
        c.gridwidth = 1;
        
        // Line Width
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Line Width"), c);
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
        add(myComboLineWidth, c);
        c.gridwidth = 1;
        
        // Line Style
        ImageSource tempImageSource = new ImageSource();
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Line Style"), c);
        c.gridx++;
        c.gridwidth = 2;
        for (int i=0; i<myIconList.length; i++){
            myComboLineStyle.addItem(tempImageSource.getIcon(myIconList[i]));
        }
        add(myComboLineStyle, c);
        c.gridwidth = 1;
        
        // the combo to select the column
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Column:"), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        add(myTextFieldColumn, c);
        c.gridwidth = 1;
        c.weightx = 0;
        
        // some space
        c.gridy++;
        c.weighty = 1;
        add(new JPanel(), c);
        
    }
    
    /** Set the Random shader to edit */
    public void setShader(Shader inShader){
        if (inShader instanceof RandomShader){
            myShader = (RandomShader) inShader;
            if (myShader.getDefaultFillColor() == null) myCheckboxFillColor.setSelected(false);
            else {
                myFillColorButton.setColor(myShader.getDefaultFillColor());
                myCheckboxFillColor.setSelected(true);
            }
            if (myShader.getDefaultLineColor() == null) myCheckboxLineColor.setSelected(false);
            else {
                myLineColorButton.setColor(myShader.getDefaultLineColor());
                myCheckboxLineColor.setSelected(true);
            }
            if (myShader.getDefaultLabelColor() == null) myCheckboxLabelColor.setSelected(false);
            else{
                myLabelColorButton.setColor(myShader.getDefaultLabelColor());
                myCheckboxLabelColor.setSelected(true);
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
            myFontButtonLabel.setFont(myShader.getDefaultFont());
            myTextFieldColumn.setText(myShader.getColumnName());
            
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
        }
    }
    
    /** Gets the edited Random shader.*/
    public Shader getShader(){
        if (myShader != null) {
            if (!myCheckboxFillColor.isSelected()) myShader.setDefaultFillColor(null);
            else myShader.setDefaultFillColor(myFillColorButton.getColor());
            if (!myCheckboxLineColor.isSelected()) myShader.setDefaultLineColor(null);
            else myShader.setDefaultLineColor(myLineColorButton.getColor());
            if (!myCheckboxLabelColor.isSelected()) myShader.setDefaultLabelColor(null);
            else myShader.setDefaultLabelColor(myLabelColorButton.getColor());
            try{
                myShader.setAlpha(Float.parseFloat(myComboAlpha.getSelectedItem().toString()));
            }
            catch (Exception e){
                System.out.println("MonoShaderDlg "+e);
            }
            myShader.setColumnName(myTextFieldColumn.getText());
            myShader.setDefaultFont(myFontButtonLabel.getFont());
            
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
                System.out.println("RandomShaderDlg "+e);
            }
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
    
    /** Main entry point for the TandomShaderPanel, testing only. */
    public static void main(String[] inArgs){
        GISToolkitDialog tempDialog = new GISToolkitDialog();
        tempDialog.getContentPane().setLayout(new BorderLayout());
        tempDialog.getContentPane().add(new RandomShaderPanel(), BorderLayout.CENTER);
        tempDialog.setModal(true);
        tempDialog.show();
        System.exit(0);
    }
}