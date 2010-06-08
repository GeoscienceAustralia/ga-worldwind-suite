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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import gistoolkit.display.*;
import gistoolkit.display.widgets.*;
import gistoolkit.display.shader.stroke.*;
import gistoolkit.display.shader.images.ImageSource;
/**
 * Allows editing of the range shader.
 */
public class BinShaderPanel extends ShaderPanel implements ActionListener{
    
    /** Creates new RangeShaderPanel */
    public BinShaderPanel() {
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
    
    /** Reference to the Range Shader */
    private BinShader myShader = null;
    
    /** The bin shader needs a column to determine which entry the row uses. */
    private JTextField myTextFieldBinColumn = new JTextField();
    
    /** The bin shader needs a column to determine the value to use to find the color. */
    private JTextField myTextFieldValueColumn = new JTextField();
    
    /** The panel for holding the information about the grid. */
    private JPanel myGridPanel = new JPanel();
    
    /** The new button for adding Columns (new Bins) to the table */
    private JButton myNewBinButton = new JButton("New Bin");
    
    /** The delete button for deleting Columns (Bins) from the table*/
    private JButton myDeleteBinButton = new JButton("Delete Bin");
    
    /** The new button for adding Rows (new Entries) to the table */
    private JButton myNewEntryButton = new JButton("New Entry");
    
    /** The delete button for deleting Rows (Entries) from the table*/
    private JButton myDeleteEntryButton = new JButton("Delete Entry");
    
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
    
    
    /** List for holding the label buttons. */
    private ArrayList myBinTitleButtonList = new ArrayList();
    /** List for holding the color buttons. */
    private ArrayList myBinColorButtonList = new ArrayList();
    /** List for holding the Value buttons. */
    private ArrayList myValueButtonList = new ArrayList();
    /** List for holding the lists of values. */
    private ArrayList myListMinTextFieldList = new ArrayList();
    /** List for holding the lists of values. */
    private ArrayList myListMaxTextFieldList = new ArrayList();
    
    /** Clear the contents of the array. */
    public void clearGrid(){
        myGridPanel.removeAll();
        myBinTitleButtonList = new ArrayList();
        myBinColorButtonList = new ArrayList();
        myValueButtonList = new ArrayList();
        myListMinTextFieldList = new ArrayList();
        myListMaxTextFieldList = new ArrayList();
    }
    
    /** Return a reference to this for the internal class. */
    private BinShaderPanel getThis(){return this;}
    
    /** Internal class for handling messages from the buttons. */
    private class MyEventHandler implements ActionListener{
        JColorChooser myColorChooser = new JColorChooser();
        
        public void actionPerformed(java.awt.event.ActionEvent inAE) {
            // check for bin titles.
            for (int i=0; i<myBinTitleButtonList.size(); i++){
                if (inAE.getSource() == myBinTitleButtonList.get(i)){
                    // set the title of the button.
                    String tempTitle = JOptionPane.showInputDialog(getThis(), "Bin Name ?", "Bin Name", JOptionPane.QUESTION_MESSAGE);
                    if (tempTitle != null){
                        ((JButton) myBinTitleButtonList.get(i)).setText(tempTitle);
                    }
                    return;
                }
            }
            // check for the values.
            for (int i=0; i<myValueButtonList.size(); i++){
                if (inAE.getSource() == myValueButtonList.get(i)){
                    // set the title of the button.
                    String tempTitle = JOptionPane.showInputDialog(getThis(), "Values Comma separated.", "Values", JOptionPane.QUESTION_MESSAGE);
                    if (tempTitle != null){
                        ((JButton) myValueButtonList.get(i)).setText(tempTitle);
                    }
                    return;
                }
            }
        }
    }
    private MyEventHandler myEventHandler = new MyEventHandler();
    
    
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
        
        // the combo to select the bin column
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Bin Column:"), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(myTextFieldBinColumn, c);
        c.gridwidth = 1;
        
        // the combo to select the value column
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Value Column:"), c);
        c.gridx++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(myTextFieldValueColumn, c);
        c.gridwidth = 1;
        
        // The table to add the Ranges
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Bins"), c);
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        JScrollPane tempScrollPane = new JScrollPane(myGridPanel);
        add(tempScrollPane, c);
        
        // the bins.
        String[] tempBinNames = {"Early Warning","Critical","Failure"};
        Color[] tempBinColors = {Color.green, Color.yellow, Color.red};
        
        // entries
        String[] tempEntryValues0 = {"Hull","Shields","Diflectors"};
        double[] tempEntryMinValues0 = {20,40,80};
        double[] tempEntryMaxValues0 = {40,80,2000};
        String[] tempEntryValues1 = {"Reactor"};
        double[] tempEntryMinValues1 = {2,8,16};
        double[] tempEntryMaxValues1 = {8,16,32};
        String[] tempEntryValues2 = {"Impulse","ion"};
        double[] tempEntryMinValues2 = {200,400,800};
        double[] tempEntryMaxValues2 = {400,800,2000};
        BinEntry[] tempEntries = new BinEntry[3];
        tempEntries[0] = new BinEntry(tempEntryValues0, tempEntryMinValues0, tempEntryMaxValues0);
        tempEntries[1] = new BinEntry(tempEntryValues1, tempEntryMinValues1, tempEntryMaxValues1);
        tempEntries[2] = new BinEntry(tempEntryValues2, tempEntryMinValues2, tempEntryMaxValues2);
        
        addBins(tempBinNames, tempBinColors, tempEntries);
        
        // add the new button
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        JPanel tempButtonPanel = new JPanel(new GridLayout(1,4));
        add(tempButtonPanel, c);
        tempButtonPanel.add(myNewBinButton);
        tempButtonPanel.add(myDeleteBinButton);
        tempButtonPanel.add(myNewEntryButton);
        tempButtonPanel.add(myDeleteEntryButton);
        myNewBinButton.addActionListener(this);
        myDeleteBinButton.addActionListener(this);
        myNewEntryButton.addActionListener(this);
        myDeleteEntryButton.addActionListener(this);
    }
    
    public void addBins(String[] inBinNames, Color[] inBinColors, BinEntry[] inEntries){
        clearGrid();
        myGridPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.BOTH;
        
        // add the bin names
        gc.gridx = 2;
        gc.gridy = 0;
        for (int i=0; i<inBinNames.length; i++){
            JButton tempButton = new JButton(inBinNames[i]);
            myBinTitleButtonList.add(tempButton);
            tempButton.addActionListener(myEventHandler);
            myGridPanel.add(tempButton, gc);
            gc.gridx++;
        }
        
        // add the bin colors
        gc.gridx = 2;
        gc.gridy++;
        for (int i=0; i<inBinColors.length; i++){
            ColorButton tempButton = new ColorButton(" .. ");
            tempButton.setBackground(inBinColors[i]);
            myBinColorButtonList.add(tempButton);
            tempButton.addActionListener(myEventHandler);
            myGridPanel.add(tempButton, gc);
            gc.gridx++;
        }
        
        // add the entry.
        for (int i=0; i<inEntries.length; i++){
            ArrayList tempMinList = new ArrayList();
            ArrayList tempMaxList = new ArrayList();
            BinEntry tempEntry = inEntries[i];
            gc.gridx = 0;
            gc.gridy++;
            gc.gridheight = 2;
            String tempLabel = "";
            String[] tempEntryValues = tempEntry.getValues();
            for (int j=0; j<tempEntryValues.length; j++){
                if (j>0) tempLabel = tempLabel + ",";
                tempLabel = tempLabel + tempEntryValues[j];
            }
            JButton tempButton = new JButton(tempLabel);
            tempButton.addActionListener(myEventHandler);
            myValueButtonList.add(tempButton);
            myGridPanel.add(tempButton, gc);
            gc.gridheight = 1;
            int ybase = gc.gridy;
            gc.gridx++;
            myGridPanel.add(new JLabel("Min:"), gc);
            gc.gridy++;
            myGridPanel.add(new JLabel("Max:"), gc);
            for (int j=0; j<tempEntry.getBinCount(); j++){
                gc.gridx++;
                gc.gridy = ybase;
                JTextField tempTextField = new JTextField(""+tempEntry.getMin(j));
                tempMinList.add(tempTextField);
                myGridPanel.add(tempTextField, gc);
                gc.gridy++;
                tempTextField = new JTextField(""+tempEntry.getMax(j));
                tempMaxList.add(tempTextField);
                myGridPanel.add(tempTextField, gc);
            }
            myListMinTextFieldList.add(tempMinList);
            myListMaxTextFieldList.add(tempMaxList);
        }
    }
    /** Add a single bin to the list (new column) */
    public void addBin(String inBinName, Color inBinColor){
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.BOTH;
        
        // add the bin names
        gc.gridx = 2 + myBinTitleButtonList.size();
        gc.gridy = 0;
        JButton tempJButton = new JButton(inBinName);
        myBinTitleButtonList.add(tempJButton);
        tempJButton.addActionListener(myEventHandler);
        myGridPanel.add(tempJButton, gc);
        
        // add the bin colors
        gc.gridy++;
        ColorButton tempCButton = new ColorButton(" .. ");
        tempCButton.setBackground(inBinColor);
        myBinColorButtonList.add(tempCButton);
        tempCButton.addActionListener(myEventHandler);
        myGridPanel.add(tempCButton, gc);
        
        for (int i=0; i<myListMaxTextFieldList.size(); i++){
            gc.gridy++;
            ArrayList tempMinList = (ArrayList) myListMinTextFieldList.get(i);
            JTextField tempTextField = new JTextField("0.0");
            tempMinList.add(tempTextField);
            myGridPanel.add(tempTextField, gc);
            gc.gridy++;
            ArrayList tempMaxList = (ArrayList) myListMaxTextFieldList.get(i);
            tempTextField = new JTextField("0.0");
            tempMaxList.add(tempTextField);
            myGridPanel.add(tempTextField, gc);
        }
        myGridPanel.validate();
    }
    
    /** Add a single entry to the list (new Row) */
    public void addEntry(String inBinName){
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2,2,2,2);
        gc.fill = GridBagConstraints.BOTH;
        
        // add the Value Button
        gc.gridx = 0;
        gc.gridheight = 2;
        gc.gridy = myValueButtonList.size() * 2 + 2;
        JButton tempJButton = new JButton("Blank");
        myValueButtonList.add(tempJButton);
        tempJButton.addActionListener(myEventHandler);
        myGridPanel.add(tempJButton, gc);
        
        // add the low and high values
        gc.gridheight = 1;
        gc.gridx++;
        int ybase = gc.gridy;
        myGridPanel.add(new JLabel("Min:"), gc);
        gc.gridy++;
        myGridPanel.add(new JLabel("Max:"), gc);
        ArrayList tempMinList = new ArrayList();
        ArrayList tempMaxList = new ArrayList();
        for (int i=0; i<myBinColorButtonList.size(); i++){
            gc.gridx++;
            gc.gridy = ybase;
            JTextField tempTextField = new JTextField("0.0");
            tempMinList.add(tempTextField);
            myGridPanel.add(tempTextField, gc);
            gc.gridy++;
            tempTextField = new JTextField("0.0");
            tempMaxList.add(tempTextField);
            myGridPanel.add(tempTextField, gc);
        }
        myListMinTextFieldList.add(tempMinList);
        myListMaxTextFieldList.add(tempMaxList);
        myGridPanel.validate();
    }
    
    /** Handle the action events from the new and delete buttons */
    public void actionPerformed(ActionEvent inAE){
        if (inAE.getSource() == myNewBinButton){
            addBin("Name",Color.white);
        }
        if (inAE.getSource() == myNewEntryButton){
            addEntry("Name");
        }
    }
    
    /** Set the Range shader to edit */
    public void setShader(Shader inShader){
        if (inShader instanceof BinShader){
            myShader = (BinShader) inShader;
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
            myTextFieldBinColumn.setText(myShader.getBinColumnName());
            myTextFieldValueColumn.setText(myShader.getValueColumnName());
            
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
            addBins(myShader.getBinNames(), myShader.getBinColors(), myShader.getEntries());
        }
    }
    
    /** Gets the edited Range shader.*/
    public Shader getShader(){
        if (myShader != null) {
            myShader.removeAllBins();
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
                System.out.println("BinShaderDlg "+e);
            }
            myShader.setBinColumnName(myTextFieldBinColumn.getText());
            myShader.setValueColumnName(myTextFieldValueColumn.getText());
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
                System.out.println("BinShaderDlg "+e);
            }
            
            // set the bins in the shader.
            myShader.removeAllBins();
            for (int i=0; i<myBinTitleButtonList.size(); i++){
                myShader.addBin(((JButton) myBinTitleButtonList.get(i)).getText(), ((ColorButton) myBinColorButtonList.get(i)).getColor());
            }
            // set the entries in the shader
            for (int i=0; i<myValueButtonList.size(); i++){
                // the list of valid values
                String tempValue = ((JButton) myValueButtonList.get(i)).getText();
                StringTokenizer st = new StringTokenizer(tempValue);
                ArrayList tempValuesList = new ArrayList();
                while (st.hasMoreElements()){
                    tempValuesList.add(st.nextToken(",").trim());
                }
                String[] tempValues = new String[tempValuesList.size()];
                tempValuesList.toArray(tempValues);
                
                // the minimum and maximum values.
                ArrayList tempMinList = (ArrayList) myListMinTextFieldList.get(i);
                ArrayList tempMaxList = (ArrayList) myListMaxTextFieldList.get(i);
                double[] tempMins = new double[tempMinList.size()];
                double[] tempMaxs = new double[tempMaxList.size()];
                for (int j=0; j<tempMinList.size(); j++){
                    double tempMin = 0.0;
                    try{
                        JTextField tempTextField = (JTextField) tempMinList.get(j);
                        tempMin = Double.parseDouble(tempTextField.getText());
                    }
                    catch (Exception e){}
                    double tempMax = 0.0;
                    try{
                        JTextField tempTextField = (JTextField) tempMaxList.get(j);
                        tempMax = Double.parseDouble(tempTextField.getText());
                    }
                    catch (Exception e){}
                    tempMins[j] = tempMin;
                    tempMaxs[j] = tempMax;
                }
                
                // add the entry
                myShader.addEntry(tempValues, tempMins, tempMaxs);
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
    
    /** Main entry function for testing of this panel. */
    public static void main(String[] inArgs){
        GISToolkitDialog tempDialog = new GISToolkitDialog();
        tempDialog.getContentPane().setLayout(new BorderLayout());
        tempDialog.getContentPane().add(new BinShaderPanel(), BorderLayout.CENTER);
        tempDialog.setModal(true);
        tempDialog.show();
        System.exit(0);
    }
}