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
import java.awt.image.BufferedImage;
import gistoolkit.common.*;

/**
 * A shader that will always return the same color regardless of what values are sent in.
 */
public class RandomShader extends SimpleShader implements EditableShader{
    /**
     * Random number generater for the states.
     */
    private Random myRandom = new Random(10);
    
    /** The name of the column to shade by */
    private String myColumnName = null;
    /** Get the name of the column to shade by */
    public String getColumnName(){return myColumnName;}
    /** Set the name of the column to shade by */
    public void setColumnName(String inColumnName){myColumnName = inColumnName;}
    
    /** Hashmap to hold the list of already available colors. These are the colors that have been used this to render this image.*/
    private Hashtable myHashTableColors = new Hashtable();
    /** Hashmap to hold the list of predefined available colors. This list can be set by the user to provide defaults for some(or all) of the colors.*/
    private Hashtable myHashTableAuxillaryColors = new Hashtable();
    
    /** Set the values and the associated colors, null colors will be assigned a random color.*/
    public void setColors(String[] inValues, Color[] inColors){
        int tempIndex = 0;
        myHashTableColors = new Hashtable();
        if (inValues != null){
            for (int i=0; i<inValues.length; i++){
                Color tempColor = null;
                if (inColors != null) tempColor=inColors[i];
                String tempValue = inValues[i];
                if (tempValue != null){
                    if (tempColor == null){
                        if (tempIndex < myGoodColors.length){
                            tempColor = myGoodColors[tempIndex];
                            tempIndex++;
                        }
                        else{
                            tempColor = new Color(myRandom.nextInt());
                        }
                    }
                    myHashTableColors.put(tempValue, tempColor);
                }
            }
        }
    }
    /** Set the values and the associated colors, null colors will be assigned a random color.*/
    public void setAuxilliaryColors(String[] inValues, Color[] inColors){
        int tempIndex = 0;
        myHashTableAuxillaryColors = new Hashtable();
        if (inValues != null){
            for (int i=0; i<inValues.length; i++){
                Color tempColor = null;
                if (inColors != null) tempColor=inColors[i];
                String tempValue = inValues[i];
                if (tempValue != null){
                    if (tempColor == null){
                        if (tempIndex < myGoodColors.length){
                            tempColor = myGoodColors[tempIndex];
                            tempIndex++;
                        }
                        else{
                            tempColor = new Color(myRandom.nextInt());
                        }
                    }
                    myHashTableAuxillaryColors.put(tempValue, tempColor);
                }
            }
        }
    }
    public String[] getValues(){
        String[] tempValues = new String[myHashTableColors.size()];
        Enumeration e = myHashTableColors.keys();
        int i=0;
        while (e.hasMoreElements()){
            tempValues[i] = (String) e.nextElement();
            i++;
        }
        return tempValues;
    }
    public Color[] getColors(){
        Color[] tempColors = new Color[myHashTableColors.size()];
        Enumeration e = myHashTableColors.elements();
        int i=0;
        while (e.hasMoreElements()){
            tempColors[i] = (Color) e.nextElement();
            i++;
        }
        return tempColors;
    }
    
    /** List of top colors to use. */
    private Color[] myGoodColors = {
        new Color(0xFF3118), new Color(0xFF9c4a), new Color(0xFFFF42), new Color(0xDEF763), new Color(0x52b552), new Color(0x31ada5), new Color(0x31b5d6), new Color(0x3152a5), new Color(0x9c2aa4), new Color(0xffff00), new Color(0x00ffff), new Color(0xff00ff)
    };
    
    /**
     * Creates a new Random Shader with a Red Highlight Color, and a black label and line color.
     */
    public RandomShader() {
        super();
        setDefaultLabelColor(Color.black);
        setDefaultHighlightColor(Color.red);
    }
    
    /**
     * Creates a new Random Shader with this default line color.
     */
    public RandomShader(Color inColor) {
        super();
        setDefaultLineColor(inColor);
    }
    
    /**
     * Creates a new Random Shader with this default line color, and highlight color.
     */
    public RandomShader(Color inLineColor, Color inHighlightColor) {
        super();
        setDefaultLineColor(inLineColor);
        setDefaultHighlightColor(inHighlightColor);
    }

    private static String COLUMN_NAME = "ColumnName";
    private static String NODE_NAME = "RandomShader";
    public Node getNode(){
        Node tempNode = super.getNode();
        tempNode.setName(NODE_NAME);
        if (myColumnName != null) tempNode.addAttribute(COLUMN_NAME, getColumnName());
        return tempNode;
    }
    /** Set the configuration information into this shader. */
    public void setNode(Node inNode)throws Exception{
        super.setNode(inNode);
        if (inNode != null){
            String tempString = inNode.getAttribute(COLUMN_NAME);
            if (tempString != null){
                setColumnName(tempString);
            }
        }
    }
    /**
     * Read the properties for the initialization of the rendere from the properties sent in.
     */
    public void load(Properties inProperties) throws Exception {
    }
    
    /** Generate a legend from this shader. Currently just returns null. */
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
        int tempNumEntries = myHashTableColors.size();
        String[] tempStrings = new String[tempNumEntries];
        Color[] tempColors = new Color[tempNumEntries];
        int[] tempHeights = new int[tempNumEntries];
        int tempMaxWidth = 0;
        Enumeration e = myHashTableColors.keys();
        int i=0;
        while (e.hasMoreElements()){
            
            // construct the string
            Object tempKey = e.nextElement();
            String tempName = "none";
            if (tempKey != null) tempName = tempKey.toString();
            tempStrings[i] = tempName;
            tempColors[i] = (Color) myHashTableColors.get(tempKey);
            
            // calculate the height
            if (tempMetrics.getHeight() < 20) tempHeights[i] = 20;
            else tempHeights[i] = tempMetrics.getHeight();
            height = height + tempSpace + tempHeights[i];
            
            // calculate the width
            int tempStringWidth = tempMetrics.stringWidth(tempName);
            if (tempStringWidth > tempMaxWidth) tempMaxWidth = tempStringWidth;
            
            // increment the counter
            i++;
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
        for (i=0; i<tempNumEntries; i++){
            
            // draw the label
            g2d.setColor(getDefaultLabelColor());
            g2d.drawString(tempStrings[i],2+squareWidth+tempSpace,tempCurrentHeight+ascent+2);
            
            // draw the fill and line
            g2d.setColor(tempColors[i]);
            g2d.fillRect(2,tempCurrentHeight+tempHeights[i]/2-squareWidth/2,squareWidth, squareWidth);
            
            g2d.setColor(getDefaultLineColor());
            g2d.drawRect(2,tempCurrentHeight+tempHeights[i]/2-squareWidth/2,squareWidth, squareWidth);
            
            // retrieve the current height
            tempCurrentHeight = tempCurrentHeight + tempSpace + tempHeights[i];
        }
        return tempImage;
    }
    
    /**
     * Set up the graphics context for the drawing of lines.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    public Graphics getFillGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        
        Color tempColor = getColor(inAttributes, inNames);
        if (tempColor != null) {
            Graphics2D g2d = (Graphics2D) inGraphics;
            g2d.setColor(tempColor);
            g2d.setFont(getDefaultFont());
            g2d.setComposite(getDefaultAlphaComposite());
            g2d.setStroke(getStroke());
            return g2d;
        }
        Graphics tempGraphics = super.getFillGraphics(inGraphics,inAttributes, inNames);
        if (tempGraphics != null) tempGraphics.setColor(new Color(myRandom.nextInt()));
        return tempGraphics;
    }
    
    /**
     * Searches through the attributes for the name, and returns the appropriate colors.
     */
    private static int myPreviousIndex = -1;
    private Color getColor(Object[] inAttributes, String[] inNames){
        // return the default color if no column name is found
        if (myColumnName == null) return null;
        if (inNames == null) return null;
        
        // Look for the
        int tempIndex = -1;
        if (myPreviousIndex != -1){
            if (myPreviousIndex < inNames.length){
                if (myColumnName == inNames[myPreviousIndex]){
                    tempIndex = myPreviousIndex;
                }
                else if (myColumnName.equalsIgnoreCase(inNames[myPreviousIndex])){
                    tempIndex = myPreviousIndex;
                }
                else{
                    tempIndex = getIndex(inNames, myColumnName);
                }
            }
            else {
                tempIndex = getIndex(inNames, myColumnName);
            }
        }
        else {
            tempIndex = getIndex(inNames, myColumnName);
        }
        
        // get the color for this attribute
        Color tempColor = null;
        Object tempKey = null;
        if (tempIndex == -1) tempColor = getDefaultFillColor();
        else{
            tempKey = inAttributes[tempIndex];
            if (tempKey != null){
                tempColor = (Color) myHashTableColors.get(tempKey);
                
                // get a new color for this key
                if (tempColor == null){
                    tempColor = (Color) myHashTableAuxillaryColors.get(tempKey);
                    if (tempColor == null){
                        int tempSize = myHashTableColors.size();
                        if (tempSize < myGoodColors.length){
                            tempColor = myGoodColors[tempSize];
                        }
                        else{
                            tempColor = new Color(myRandom.nextInt());
                        }
                    }
                    
                    // add the new color to the hash map
                    if (tempColor != null){
                        myHashTableColors.put(tempKey, tempColor);
                    }
                }
            }
            else tempColor = getDefaultFillColor();
        }
        return tempColor;
    }
    
    /** Return the value for the given name. */
    private int getIndex(String[] inNames, String inKey){
        for (int i=0; i<inNames.length; i++){
            if (inNames[i] == inKey) return i;
            if (inNames[i].equals(inKey)) return i;
        }
        return -1;
    }
    
    /** return the panel needed to edit this shader  */
    public ShaderPanel getEditPanel() {
        RandomShaderPanel tempPanel = new RandomShaderPanel();
        tempPanel.setShader(this);
        return tempPanel;
    }
    
}