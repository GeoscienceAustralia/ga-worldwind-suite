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
import javax.swing.border.*;
import javax.swing.table.*;
import gistoolkit.display.*;
import gistoolkit.display.widgets.*;
import gistoolkit.display.shader.stroke.*;
import gistoolkit.display.shader.images.ImageSource;
/**
 * Allows editing of the range shader.
 */
public class RangeShaderPanel extends ShaderPanel implements ActionListener{
    
    /** Creates new RangeShaderPanel */
    public RangeShaderPanel() {
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
    private RangeShader myShader = null;
    
    /** The range shader needs a column on which to operate */
    private JTextField myTextFieldColumn = new JTextField();
    
    /** The names for the columns */
    String[] myColumnNames = {"Min Value","Max Value","Fill Color", "Line Color", "LabelColor"};
    
    /** The table for adding and removing rows */
    private JTable myTable = null;
    
    /** The table model for adding and removing rows */
    private MyTableModel myModel = null;
    
    /** The new button for adding rows to the table */
    private JButton myNewButton = new JButton("New");
    
    /** The delete button for deleting rows from the table */
    private JButton myDeleteButton = new JButton("Delete");
    
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
        add(myTextFieldColumn, c);
        c.gridwidth = 1;
        
        // The table to add the Ranges
        c.gridx = 0;
        c.gridy++;
        add(new JLabel("Ranges"), c);
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        Object[][] tempData = {
            {"0","50",new Color(Color.red.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
            ,{"50","100",new Color(Color.orange.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
            ,{"100","150",new Color(Color.yellow.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
            ,{"150","200",new Color(Color.green.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
            ,{"200","250",new Color(Color.blue.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
            ,{"250","300",new Color(162,13,200), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())}
        };
        myModel = new MyTableModel(tempData, myColumnNames);
        myTable = new JTable(myModel);
        myTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        setUpColorEditor(myTable);
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane tempScrollPane = new JScrollPane(myTable);
        add(tempScrollPane, c);
        
        // add the new button
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 0;
        JPanel tempButtonPanel = new JPanel(new GridLayout(1,2));
        add(tempButtonPanel, c);
        tempButtonPanel.add(myNewButton);
        tempButtonPanel.add(myDeleteButton);
        myNewButton.addActionListener(this);
        myDeleteButton.addActionListener(this);
    }
    
    //Set up the editor for the Color cells.
    private void setUpColorEditor(JTable table) {
        //First, set up the button that brings up the dialog.
        final JButton button = new JButton("") {
            public void setText(String s) {
                //Button never shows text -- only color.
            }
        };
        button.setBackground(Color.white);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0,0,0,0));
        //Now create an editor to encapsulate the button, and
        //set it up as the editor for all Color cells.
        final ColorEditor colorEditor = new ColorEditor(button);
        table.setDefaultEditor(Color.class, colorEditor);
        //Set up the dialog that the button brings up.
        final JColorChooser colorChooser = new JColorChooser();
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorEditor.currentColor = colorChooser.getColor();
            }
        };
        final JDialog dialog = JColorChooser.createDialog(button,"Pick a Color", true, colorChooser, okListener, null); //XXXDoublecheck this is OK
        
        //Here's the code that brings up the dialog.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setBackground(colorEditor.currentColor);
                colorChooser.setColor(colorEditor.currentColor);
                //Without the following line, the dialog comes up
                //in the middle of the screen.
                dialog.setLocationRelativeTo(button);
                dialog.show();
            }
        });
    }
    
    /** Handle the action events from the new and delete buttons */
    public void actionPerformed(ActionEvent inIE){
        if (inIE.getSource() == myNewButton){
            Object[] row = {"0","0", new Color(Color.red.getRGB()), new Color(Color.black.getRGB()), new Color(Color.black.getRGB())};
            myModel.addRow(row);
        }
        if (inIE.getSource() == myDeleteButton){
            myModel.removeRow(myTable.getSelectedRow());
        }
    }
    
    /** Set the Range shader to edit */
    public void setShader(Shader inShader){
        if (inShader instanceof RangeShader){
            myShader = (RangeShader) inShader;
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
            Object[][] tempData = new Object[myShader.getMinValues().length][5];
            double[] tempMinValues = myShader.getMinValues();
            double[] tempMaxValues = myShader.getMaxValues();
            Color[] tempFillColors = myShader.getFillColors();
            Color[] tempLineColors = myShader.getLineColors();
            Color[] tempLabelColors = myShader.getLabelColors();
            for (int i=0; i<tempData.length; i++){
                tempData[i][0] = ""+tempMinValues[i];
                tempData[i][1] = ""+tempMaxValues[i];
                tempData[i][2] = tempFillColors[i];
                tempData[i][3] = tempLineColors[i];
                tempData[i][4] = tempLabelColors[i];
            }
            myModel = new MyTableModel(tempData, myColumnNames);
            myTable.setModel(myModel);
            
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
    
    /** Gets the edited Range shader.*/
    public Shader getShader(){
        if (myShader != null) {
            myShader.removeAllEntries();
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
            
            for (int i=0; i<myModel.getRowCount(); i++){
                String tempString = (String) myModel.getValueAt(i,0);
                double tempMinDouble = 0;
                try{
                    tempMinDouble = Double.parseDouble(tempString);
                }
                catch(NumberFormatException e){}
                
                tempString = (String) myModel.getValueAt(i,1);
                double tempMaxDouble = 0;
                try{
                    tempMaxDouble = Double.parseDouble(tempString);
                }
                catch(NumberFormatException e){}
                
                Color tempFillColor = (Color) myModel.getValueAt(i,2);
                Color tempLineColor = (Color) myModel.getValueAt(i,3);
                Color tempLabelColor = (Color) myModel.getValueAt(i,4);
                myShader.addColor(tempMinDouble, tempMaxDouble, tempFillColor, tempLineColor, tempLabelColor);
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
        }
        return myShader;
    }
    /*
     * The editor button that brings up the dialog.
     * We extend DefaultCellEditor for convenience,
     * even though it mean we have to create a dummy
     * check box.  Another approach would be to copy
     * the implementation of TableCellEditor methods
     * from the source code for DefaultCellEditor.
     */
    class ColorEditor extends DefaultCellEditor {
        Color currentColor = null;
        public ColorEditor(JButton b) {
            super(new JCheckBox()); //Unfortunately, the constructor
            //expects a check box, combo box,
            //or text field.
            editorComponent = b;
            setClickCountToStart(1); //This is usually 1 or 2.
            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
        public Object getCellEditorValue() {
            return currentColor;
        }
        public Component getTableCellEditorComponent(JTable table,
        Object value,
        boolean isSelected,
        int row,
        int column) {
            ((JButton)editorComponent).setText(value.toString());
            currentColor = (Color)value;
            return editorComponent;
        }
    }
    
    class ColorRenderer extends JLabel
    implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        public ColorRenderer(boolean isBordered) {
            super();
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }
        public Component getTableCellRendererComponent(
        JTable table, Object color,
        boolean isSelected, boolean hasFocus,
        int row, int column) {
            setBackground((Color)color);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }
    
    class MyTableModel extends DefaultTableModel {
        public MyTableModel(Object[][] inData, String[] inColumnNames){
            super(inData, inColumnNames);
        }
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            Object tempObject = getValueAt(0, c);
            if (tempObject == null) return null;
            return tempObject.getClass();
        }
        
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 0) {
                return false;
            } else {
                return true;
            }
        }
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
    
    /** Main entry point for the RangeShaderPanel, testing only.*/
    public static void main(String[] inArgs){
        GISToolkitDialog tempDialog = new GISToolkitDialog();
        tempDialog.getContentPane().setLayout(new BorderLayout());
        tempDialog.getContentPane().add(new RangeShaderPanel(), BorderLayout.CENTER);
        tempDialog.setModal(true);
        tempDialog.show();
        System.exit(0);
    }
}