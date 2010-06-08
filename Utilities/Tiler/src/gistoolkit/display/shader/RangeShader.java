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

import java.math.*;
import java.text.*;
import java.awt.*;
import java.awt.image.*;
import gistoolkit.common.*;
/**
 * Returns a color bassed on a value.  If you want a shape shaded based on the numerical
 * value of one of it's attributes in the following scheme.
 * < 20 is one color,
 * < 40 is another color,
 * < 60 is another color,
 * everything else is the default color.
 * then this shader should work.
 */
public class RangeShader extends SimpleShader implements EditableShader{
    /** Vector of min values */
    private double[] myMinValues = new double[0];
    /** Vector of max values */
    private double[] myMaxValues = new double[0];
    /** Vector of FillColors */
    private Color[] myFillColors = new Color[0];
    /** Vector of LineColors */
    private Color[] myLineColors = new Color[0];
    /** Vector of LabelColors */
    private Color[] myLabelColors = new Color[0];
    
    /** Creates new RangeShader */
    public RangeShader() {
    }
    
    /** Removes all entries from the shader */
    public void removeAllEntries(){
        myMinValues = new double[0];
        myMaxValues = new double[0];
        myFillColors = new Color[0];
        myLineColors = new Color[0];
        myLabelColors = new Color[0];
    }
    
    /** add another color to the list null colors will not be drawn */
    public void addColor(double inMinValue, double inMaxValue, Color inFillColor, Color inLineColor, Color inLabelColor){
        
        // create a new place for the color
        double[] tempMinValues = new double[myMinValues.length+1];
        double[] tempMaxValues = new double[myMaxValues.length+1];
        Color[] tempFillColors = new Color[myFillColors.length+1];
        Color[] tempLineColors = new Color[myLineColors.length+1];
        Color[] tempLabelColors = new Color[myLabelColors.length+1];
        
        // assign the new ones
        for (int i=0; i<myMinValues.length; i++){
            tempMinValues[i] = myMinValues[i];
            tempMaxValues[i] = myMaxValues[i];
            tempFillColors[i] = myFillColors[i];
            tempLineColors[i] = myLineColors[i];
            tempLabelColors[i] = myLabelColors[i];
        }
        tempMinValues[tempMinValues.length-1] = inMinValue;
        tempMaxValues[tempMaxValues.length-1] = inMaxValue;
        tempFillColors[tempFillColors.length-1] = inFillColor;
        tempLineColors[tempLineColors.length-1] = inLineColor;
        tempLabelColors[tempLabelColors.length-1] = inLabelColor;
        myMinValues = tempMinValues;
        myMaxValues = tempMaxValues;
        myFillColors = tempFillColors;
        myLineColors = tempLineColors;
        myLabelColors = tempLabelColors;
    }
    
    /** return the list of minimum values, and the list of doubles */
    public double[] getMinValues(){return (double[]) myMinValues.clone();}
    /** return the list of maximumvalues, and the list of doubles */
    public double[] getMaxValues(){return (double[]) myMaxValues.clone();}
    /** return the list of fill colors */
    public Color[] getFillColors(){return (Color[]) myFillColors.clone();}
    /** return the list of line colors */
    public Color[] getLineColors(){return (Color[]) myLineColors.clone();}
    /** return the list of Label colors */
    public Color[] getLabelColors(){return (Color[]) myLabelColors.clone();}
    
    /** The name of the column to shade by */
    private String myColumnName = null;
    /** Get the name of the column to shade by */
    public String getColumnName(){return myColumnName;}
    /** Set the name of the column to shade by */
    public void setColumnName(String inColumnName){myColumnName = inColumnName;}
    
    /**
     * Searches through the attributes for the name, and returns the appropriate colors.
     */
    private int getColor(Object[] inAttributes, String[] inNames){
        // return the default color if no column name is found
        if (myColumnName == null) return -1;
        if (inNames == null) return -1;
        
        // find the attribute index
        int tempIndex = -1;
        for (int i=0; i<inNames.length; i++){
            if (myColumnName.equalsIgnoreCase(inNames[i])) {
                tempIndex = i;
                break;
            }
        }
        
        // if the column was not found then return error.
        if (tempIndex == -1) return -1;
        
        // get the Value
        if (inAttributes[tempIndex] == null) return -1;
        double tempValue = 0;
        try{
            if (inAttributes[tempIndex] instanceof String)tempValue = Double.parseDouble((String) inAttributes[tempIndex]);
            else if (inAttributes[tempIndex] instanceof Integer)tempValue = ((Integer) inAttributes[tempIndex]).doubleValue();
            else if (inAttributes[tempIndex] instanceof Float)tempValue = ((Float) inAttributes[tempIndex]).doubleValue();
            else if (inAttributes[tempIndex] instanceof Double)tempValue = ((Double) inAttributes[tempIndex]).doubleValue();
            else if (inAttributes[tempIndex] instanceof BigInteger)tempValue = ((BigInteger) inAttributes[tempIndex]).doubleValue();
            else if (inAttributes[tempIndex] instanceof BigDecimal)tempValue = ((BigDecimal) inAttributes[tempIndex]).doubleValue();
            else return -1;
        }
        catch (NumberFormatException e){
            return -1;
        }
        
        // return the correct color
        for (int i = 0; i<myMinValues.length; i++){
            if ((tempValue >= myMinValues[i]) && (tempValue <= myMaxValues[i]))return i;
        }
        
        // Did not find the range, so send the default
        return -1;
    }
    
    /** return the panel needed to edit this shader  */
    public ShaderPanel getEditPanel() {
        RangeShaderPanel tempPanel = new RangeShaderPanel();
        tempPanel.setShader(this);
        return tempPanel;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for drawing of lines.
     */
    public Graphics getLineGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        int tempIndex = getColor(inAttributes, inNames);
        if (tempIndex != -1) {
            Graphics2D g2d = (Graphics2D) inGraphics;
            g2d.setColor(myLineColors[tempIndex]);
            g2d.setFont(getDefaultFont());
            g2d.setComposite(getDefaultAlphaComposite());
            g2d.setStroke(getStroke());
            return g2d;
        }
        return super.getLineGraphics(inGraphics,inAttributes, inNames);
    }
        
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    public Graphics getFillGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        int tempIndex = getColor(inAttributes, inNames);
        if (tempIndex != -1) {
            Graphics2D g2d = (Graphics2D) inGraphics;
            g2d.setColor(myFillColors[tempIndex]);
            g2d.setFont(getDefaultFont());
            g2d.setComposite(getDefaultAlphaComposite());
            g2d.setStroke(getStroke());
            return g2d;
        }
        return super.getFillGraphics(inGraphics,inAttributes, inNames);
    }    
        
    /**
     * Set up the graphics context for the drawing of labels.
     * Always return the graphics context sent in after modifying it for labeling of features.
     */
    public Graphics getLabelGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
         int tempIndex = getColor(inAttributes, inNames);
        if (tempIndex != -1) {
            Graphics2D g2d = (Graphics2D) inGraphics;
            g2d.setColor(myLabelColors[tempIndex]);
            g2d.setFont(getDefaultFont());
            g2d.setComposite(getDefaultAlphaComposite());
            g2d.setStroke(getStroke());
            return g2d;
        }
        return super.getLabelGraphics(inGraphics,inAttributes, inNames);
    }
        
    /** Generate a lagend from this shader.  The RangeShader creates an entry for every entry in the list. */
    public BufferedImage getLegend(){
        int tempSpace = 3; // space between the drawn square, and the beginning of the text.
        int height = 20; // minimum height of the legend entry
        int width = 20; // minimum width of the legend entry
        int ascent = 0; // ascent of the text
        int squareWidth = 16; // width and height of the drawn square.
        
        // create a new image to perform calculations on.
        BufferedImage tempImage = new BufferedImage(40,20,BufferedImage.TYPE_INT_ARGB);
        Graphics tempGraphics = tempImage.getGraphics();
        Graphics2D g2d = (Graphics2D) tempGraphics;
        FontMetrics tempMetrics = g2d.getFontMetrics();
        
        // calculate the height of the rectangle.
        String[] tempStrings = new String[myMinValues.length];
        NumberFormat nf = NumberFormat.getInstance();
        int[] tempHeights = new int[myMinValues.length];
        int tempMaxWidth = 0;
        for (int i=0; i<myMinValues.length; i++){
            
            // minimum number
            String tempMinString = "";
            int tempMin = (int) myMinValues[i];
            if ((double)tempMin == myMinValues[i]) tempMinString = nf.format(tempMin);
            else tempMinString = nf.format(myMinValues[i]);
            
            // maximum number
            String tempMaxString = "";
            int tempMax = (int) myMaxValues[i];
            if ((double) tempMax == myMaxValues[i]) tempMaxString = nf.format(tempMax);
            else tempMaxString = nf.format(myMaxValues[i]);
            
            // construct the strings
            if (tempMetrics.getHeight() < 20) tempHeights[i] = 20;
            else tempHeights[i] = tempMetrics.getHeight();
            height = height + tempSpace + tempHeights[i];
            if (myMaxValues[i] == Double.POSITIVE_INFINITY) tempStrings[i] = "> "+tempMinString;
            else if (myMinValues[i] == Double.NEGATIVE_INFINITY) tempStrings[i] = "< " + tempMaxString;
            else if (myMinValues[i] == myMaxValues[i]) tempStrings[i] = tempMaxString;
            else tempStrings[i] = ""+tempMinString+" To "+tempMaxString;
            int tempStringWidth = tempMetrics.stringWidth(tempStrings[i]);
            if (tempStringWidth > tempMaxWidth) tempMaxWidth = tempStringWidth;
        }
        width = width + tempSpace + tempMaxWidth;
        
        // change the width of the image to match the label.
        if (height < 20) height = 20;
        ascent = tempMetrics.getAscent();
        
        // create the image the size to draw on.
        tempImage = new BufferedImage(width+20+tempSpace, height, BufferedImage.TYPE_INT_ARGB);
        
        tempGraphics = tempImage.getGraphics();
        g2d = (Graphics2D) tempGraphics;
        g2d.setBackground(Color.white);
        g2d.clearRect(0,0,tempImage.getWidth(), tempImage.getHeight());
        
        // draw all the images
        int tempCurrentHeight = 0;
        for (int i=0; i<myMinValues.length; i++){
            
            // draw the label
            if (myLabelColors[i] == null) g2d.setColor(Color.black);
            else g2d.setColor(myLabelColors[i]);
            g2d.drawString(tempStrings[i],2+squareWidth+tempSpace,tempCurrentHeight+ascent+2);
            
            // draw the fill and line
            if (myFillColors[i] != null){
                g2d.setColor(myFillColors[i]);
                g2d.fillRect(2,tempCurrentHeight+tempHeights[i]/2-squareWidth/2,squareWidth, squareWidth);
            }
            if (myLineColors[i] != null){
                g2d.setColor(myLineColors[i]);
                g2d.drawRect(2,tempCurrentHeight+tempHeights[i]/2-squareWidth/2,squareWidth, squareWidth);
            }
            
            // retrieve the current height
            tempCurrentHeight = tempCurrentHeight + tempSpace + tempHeights[i];
        }
        return tempImage;
    }
    
    private static final String CONFIG_NODE = "ConfigNode";
    private static final String COLUMN_NAME = "ColumnName";
    private static final String MAX_VALUE = "MaxValue";
    private static final String MIN_VALUE = "MinValue";
    private static final String FILL_COLOR = "FillColor";
    private static final String LINE_COLOR = "LineColor";
    private static final String LABEL_COLOR = "LabelColor";

    /** Get the configuration information for this shader */
    public Node getNode(){
        Node tempRoot = super.getNode();
        tempRoot.setName("RangeShader");
        tempRoot.addAttribute(COLUMN_NAME, getColumnName());
        
        // add the individual entries
        for (int i=0; i<myMaxValues.length; i++){
            Node tempNode = new Node(CONFIG_NODE);
            tempNode.addAttribute(MAX_VALUE, ""+myMaxValues[i]);
            tempNode.addAttribute(MIN_VALUE, ""+myMinValues[i]);
            tempNode.addAttribute(FILL_COLOR, ""+myFillColors[i].getRGB());
            tempNode.addAttribute(LINE_COLOR, ""+myLineColors[i].getRGB());
            tempNode.addAttribute(LABEL_COLOR, ""+myLabelColors[i].getRGB());
            tempRoot.addChild(tempNode);
        }
        return tempRoot;
    }
    
    /** Set the configuration information for this shader */
    public void setNode(Node inNode) throws Exception{
        if (inNode == null) return;
        super.setNode(inNode);
        
        myColumnName = inNode.getAttribute(COLUMN_NAME);
        
        Node[] tempNodes = inNode.getChildren(CONFIG_NODE);
        myMaxValues = new double[tempNodes.length];
        myMinValues = new double[tempNodes.length];
        myFillColors = new Color[tempNodes.length];
        myLineColors = new Color[tempNodes.length];
        myLabelColors = new Color[tempNodes.length];
        for (int i=0; i<tempNodes.length; i++){
            String tempString = tempNodes[i].getAttribute(MAX_VALUE);
            myMaxValues[i] = Double.parseDouble(tempString);
            tempString = tempNodes[i].getAttribute(MIN_VALUE);
            myMinValues[i] = Double.parseDouble(tempString);
            tempString = tempNodes[i].getAttribute(FILL_COLOR);
            myFillColors[i] = new Color(Integer.parseInt(tempString));
            tempString = tempNodes[i].getAttribute(LINE_COLOR);
            myLineColors[i] = new Color(Integer.parseInt(tempString));
            tempString = tempNodes[i].getAttribute(LABEL_COLOR);
            myLabelColors[i] = new Color(Integer.parseInt(tempString));
        }
    }
}
