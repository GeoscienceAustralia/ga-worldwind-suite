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
import java.awt.image.*;
import gistoolkit.common.*;
/**
 * Shades features in bins of colors.
 * There are a series of bin colors and names such as Low(Yellow) Medium(Green) High(Red).
 * A row is assigned to these different bins based on the values of two columns.  The first column determines which
 * entry the row should be assigned to.
 *
 * Given the data
 * Name    Value
 * Snuggle  3
 * nap      6
 * snore    20
 * snooze   10
 *
 * We want to shade the snuggle and nap into entries of yellow, green or red like this
 * Values        MinYellow   Max Yellow   Min Green  Max Green  Min Red, Max Red
 * snuggle, nap  2           3            3          4          4        10
 * snore, snooze 5           7            7          12         12       40
 *
 * With an entry with values of snuggle, map, and the biven ranges along with bins of yellow green and red, this
 * shader could accomplish the shading required.
 *
 */
public class BinShader extends SimpleShader implements EditableShader{
    
    /** The name of the column which contains the values to shade by */
    private String myValueColumnName = null;
    /** Get the name of the column to shade by */
    public String getValueColumnName(){return myValueColumnName;}
    /** Set the name of the column to shade by */
    public void setValueColumnName(String inValueColumnName){myValueColumnName = inValueColumnName;}
    
    /** The name of the column to bin by */
    private String myBinColumnName = null;
    /** Get the name of the column to shade by */
    public String getBinColumnName(){return myBinColumnName;}
    /** Set the name of the column to shade by */
    public void setBinColumnName(String inBinColumnName){myBinColumnName = inBinColumnName;}
    
    /** List of bin names, This contains strings. */
    private ArrayList myBinNameList = new ArrayList();
    /** Returns all the Bin Names */
    public String[] getBinNames(){
        String[] tempNames = new String[myBinNameList.size()];
        myBinNameList.toArray(tempNames);
        return tempNames;
    }
    
    /** List of bin colors, this contains the colors for the bins. */
    private ArrayList myBinColorList = new ArrayList();
    /** Returns all the BinColors */
    public Color[] getBinColors(){
        Color[] tempColors = new Color[myBinColorList.size()];
        myBinColorList.toArray(tempColors);
        return tempColors;
    }
    
    /** This is the list of ranges for the various values. */
    private ArrayList myEntryList = new ArrayList();
    /** Returns all the entries in the shader. */
    public BinEntry[] getEntries(){
        BinEntry[] tempEntries = new BinEntry[myEntryList.size()];
        myEntryList.toArray(tempEntries);
        return tempEntries;
    }
    
    /** Creates new BinShader */
    public BinShader() {
    }
    
    /** remove all entries from the shader. */
    public void removeAllEntries(){
        myEntryList = new ArrayList();
    }
    
    /** Remove all bins. */
    public void removeAllBins(){
        myBinNameList = new ArrayList();
        myBinColorList = new ArrayList();
        removeAllEntries();
    }
    
    
    /** Add a bin to the list of available bins. */
    public synchronized void addBin(String inBinName, Color inBinColor){
        if (inBinName == null) return;
        Color tempBinColor = inBinColor;
        if (tempBinColor == null) {
            tempBinColor = Color.white;
        }
        
        // update the entries.
        for (int i=0; i<myEntryList.size(); i++){
            BinEntry tempEntry = (BinEntry) myEntryList.get(i);
            tempEntry.addBin(0.0, 0.0);
        }
        myBinNameList.add(inBinName);
        myBinColorList.add(inBinColor);
    }
    /** Looks through the list of bins and tries to find the one with this name.  If this one is found, then it will be deleted and a value of true will be returned. */
    public synchronized boolean removeBin(String inBinName){
        if (inBinName == null) return false;
        for (int i=0; i<myBinNameList.size(); i++){
            String tempName = (String) myBinNameList.get(i);
            if (tempName.equals(inBinName)){
                
                // remove this bin from all of the entries.
                for (int j=0; j<myEntryList.size(); j++){
                    BinEntry tempEntry = (BinEntry) myEntryList.get(i);
                    tempEntry.removeBin(i);
                }
                
                // remove the entries from the bin lists.
                myBinNameList.remove(i);
                myBinColorList.remove(i);
                return true;
            }
        }
        return false;
    }
    
    /** Adds an entry to the list.  Entries have matching criteria, and high and low values. */
    public synchronized void addEntry(String[] inValues, double[] inMinValues, double[] inMaxValues){
        if (inValues == null) return;
        BinEntry tempEntry = new BinEntry(inValues, inMinValues, inMaxValues);
        myEntryList.add(tempEntry);
    }
    
    /** Last index of the shade by column. */
    private int myLastBinIndex = 0;
    
    /** Return the entry where this row falls.  Will return null if no entry matches. */
    private BinEntry getEntry(String[] inNames, Object[] inAttributes){
        // check the current bin index to see if it is the one we need.
        boolean tempFound = false;
        if (inNames.length > myLastBinIndex){
            if (inNames[myLastBinIndex] != null){
                if (inNames[myLastBinIndex].equalsIgnoreCase(myBinColumnName)){
                    tempFound = true;
                }
            }
        }
        // if the bin column was not found, then we need to check for it here.
        if (!tempFound){
            for (int i=0; i<inNames.length; i++){
                if (inNames[i] != null){
                    if (inNames[i].equalsIgnoreCase(myBinColumnName)){
                        myLastBinIndex = i;
                        tempFound = true;
                    }
                }
            }
        }
        // if we did not find the bin column, then return no entry
        if (!tempFound) return null;
        
        // if the value of the bin column is null, then return null
        if (inAttributes[myLastBinIndex] == null) return null;
        
        // look through the entries to try to find the one this row fits within.
        String tempValue = inAttributes[myLastBinIndex].toString();
        
        for (int i=0; i<myEntryList.size(); i++){
            BinEntry tempEntry = (BinEntry) myEntryList.get(i);
            if (tempEntry.containsValue(tempValue)){
                return tempEntry;
            }
        }
        // did not find this  row in any of the entries, then return null
        return null;
    }
    
    /** The last accessed values index. */
    private int myLastValuesIndex = 0;
    
    /** Get the fill color for this row */
    private Color getColor(String[] inNames, Object[] inAttributes){
        // look for the entry
        BinEntry tempEntry = getEntry(inNames, inAttributes);
        if (tempEntry != null){
            // look for the bin in which this row falls
            // check the current values index to see if it is the one we need.
            boolean tempFound = false;
            if (inNames.length > myLastValuesIndex){
                if (inNames[myLastValuesIndex] != null){
                    if (inNames[myLastValuesIndex].equalsIgnoreCase(myBinColumnName)){
                        tempFound = true;
                    }
                }
            }
            // if the values column was not found, then we need to find it here.
            if (!tempFound){
                for (int i=0; i<inNames.length; i++){
                    if (inNames[i] != null){
                        if (inNames[i].equalsIgnoreCase(myValueColumnName)){
                            myLastValuesIndex = i;
                            tempFound = true;
                        }
                    }
                }
            }
            // if we did not find the values column, then return no entry
            if (!tempFound) return null;
            
            // if the value of the bin column is null, then return null
            if (inAttributes[myLastValuesIndex] == null) return null;
            
            // attempt to find value of the color column
            double tempValue = 0;
            Object tempObject = inAttributes[myLastValuesIndex];
            if (tempObject instanceof Double){
                tempValue = ((Double) tempObject).doubleValue();
            }
            else if (tempObject instanceof Float){
                tempValue = ((Float) tempObject).doubleValue();
            }
            else if (tempObject instanceof String){
                try{
                    tempValue = Double.parseDouble((String) tempObject);
                }
                catch(NumberFormatException e){
                    // not a number return null
                    return null;
                }
            }
            else{
                String tempString = tempObject.toString();
                try{
                    tempValue = Double.parseDouble(tempString);
                }
                catch(NumberFormatException e){
                    // not a number return null
                    return null;
                }
            }
            
            // we have a value, look through the values of this entry to find the correct bin.
            int tempBin = tempEntry.getBin(tempValue);
            if (tempBin != -1){
                return (Color) myBinColorList.get(tempBin);
            }
        }
        // did not find a match, return null;
        return null;
    }
    
    /** Generate a lagend from this shader.  The RangeShader creates an entry for every entry in the list. */
    public BufferedImage getLegend(){
        int tempLeading = 3; // space between the edge of the legend and the content.
        int tempSquareWidth = 16; // width and height of the drawn square.
        int tempSpace = 3; // space between the drawn square, and the beginning of the text.
        
        // create a new image to perform calculations on.
        BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
        Graphics tempGraphics = tempImage.getGraphics();
        Graphics2D g2d = (Graphics2D) tempGraphics;
        FontMetrics tempMetrics = g2d.getFontMetrics();
        
        // calculate the height of the rectangle.
        int tempMaxWidth = 0;
        int tempMaxHeight = tempLeading+tempLeading;
        for (int i=0; i<myBinNameList.size(); i++){
            if (i>0) tempMaxHeight = tempMaxHeight + tempSpace;
            tempMaxHeight = tempMaxHeight + tempSquareWidth;
            String tempString = (String) myBinNameList.get(i);
            int tempStringWidth = tempMetrics.stringWidth(tempString);
            if (tempMaxWidth < tempStringWidth) tempMaxWidth = tempStringWidth;
        }
            
        // create the image the size to draw on.
        int tempImageWidth = tempLeading + tempSquareWidth + tempSpace + tempMaxWidth + tempLeading;
        tempImage = new BufferedImage(tempImageWidth, tempMaxHeight, BufferedImage.TYPE_INT_ARGB);
        tempGraphics = tempImage.getGraphics();
        g2d = (Graphics2D) tempGraphics;
        g2d.setBackground(Color.white);
        g2d.clearRect(0,0,tempImage.getWidth(), tempImage.getHeight());
         
        // draw the actual text.
        int tempHeight = tempLeading;
        int tempFontHeight = tempMetrics.getHeight();
        for (int i=0; i<myBinNameList.size(); i++){
            // draw the square
            g2d.setColor((Color) myBinColorList.get(i));
            g2d.fillRect(tempLeading, tempHeight, tempSquareWidth, tempSquareWidth);
            g2d.setColor(Color.black);
            g2d.drawRect(tempLeading, tempHeight, tempSquareWidth, tempSquareWidth);
            
            // draw the text.
            int tempTextHeight = tempHeight + tempSquareWidth/2 + tempFontHeight/2;
            g2d.drawString((String) myBinNameList.get(i), tempLeading + tempSquareWidth + tempSpace, tempTextHeight);
            
            // add this height to the current height.
            tempHeight = tempHeight + tempSpace;
            tempHeight = tempHeight + tempSquareWidth;
        }
        return tempImage;
    }

    private static final String BIN_COLUMN_NAME = "BinColumnName";
    private static final String VALUE_COLUMN_NAME = "ValueColumnName";
    private static final String BIN = "Bin";
    private static final String BIN_NAME = "BinName";
    private static final String BIN_COLOR = "BinColor";
    private static final String BIN_NUMBER = "BinNumber";
    private static final String ENTRY = "Entry";
    private static final String ENTRY_NUMBER = "EntryNumber";
    private static final String ENTRY_VALUE_NUMBER = "EntryValueNumber";
    private static final String ENTRY_VALUE = "EntryValue";
    private static final String ENTRY_BIN = "EntryBin";
    private static final String ENTRY_BIN_NUMBER = "EntryBinNumber";
    private static final String MAX_VALUE = "MaxValue";
    private static final String MIN_VALUE = "MinValue";
    private static final String STROKE_TYPE = "StrokeType";
    private static final String STROKE_TYPE_BASIC = "Basic";
    private static final String STROKE_TYPE_RAILROAD = "RailRoad";
    private static final String TIE_WIDTH = "TieWidth";
    private static final String TWO_LINE = "TwoLine";

    /** Get the configuration information for this shader */
    public Node getNode(){
        Node tempRoot = super.getNode();
        tempRoot.setName("BinShader");
        tempRoot.addAttribute(BIN_COLUMN_NAME, getBinColumnName());
        tempRoot.addAttribute(VALUE_COLUMN_NAME, getValueColumnName());
        tempRoot.addAttribute(BIN_NUMBER, ""+myBinNameList.size());
        tempRoot.addAttribute(ENTRY_NUMBER, ""+myEntryList.size());
        
        // add the BINS
        for (int i=0; i<myBinNameList.size(); i++){
            Node tempNode = new Node(BIN + i);
            tempNode.addAttribute(BIN_NAME, (String) myBinNameList.get(i));
            tempNode.addAttribute(BIN_COLOR, ""+ ((Color) myBinColorList.get(i)).getRGB());
            tempRoot.addChild(tempNode);
        }
        
        // add the entries
        for (int i=0; i<myEntryList.size(); i++){
            BinEntry tempEntry = (BinEntry) myEntryList.get(i);
            Node tempNode = new Node(ENTRY + i);
            // add the values list
            String[] tempValues = tempEntry.getValues();
            tempNode.addAttribute(ENTRY_VALUE_NUMBER, ""+tempValues.length);
            for (int j=0; j<tempValues.length; j++){
                tempNode.addAttribute(ENTRY_VALUE+j, tempValues[j]);
            }
            // add the bin entries
            tempNode.addAttribute(ENTRY_BIN_NUMBER, ""+tempEntry.getBinCount());
            for (int j=0; j<tempEntry.getBinCount(); j++){
                Node tempEntryBinNode = new Node(ENTRY_BIN+j);
                tempEntryBinNode.addAttribute(MIN_VALUE, ""+tempEntry.getMin(j));
                tempEntryBinNode.addAttribute(MAX_VALUE, ""+tempEntry.getMax(j));
                tempNode.addChild(tempEntryBinNode);
            }
            tempRoot.addChild(tempNode);
        }        
        return tempRoot;
    }
    
    /** Set the configuration information for this shader */
    public void setNode(Node inNode) throws Exception{
        if (inNode == null) return;
        super.setNode(inNode);
        
        myBinColumnName = inNode.getAttribute(BIN_COLUMN_NAME);
        myValueColumnName = inNode.getAttribute(VALUE_COLUMN_NAME);
        
        // find the bins
        int tempBinNumber = Integer.parseInt(inNode.getAttribute(BIN_NUMBER));
        for (int i=0; i<tempBinNumber; i++){
            // get the names and colors of the bins
            Node tempNode = inNode.getChild(BIN+i);
            String tempBinName = tempNode.getAttribute(BIN_NAME);
            int tempBinColorNum = Integer.parseInt(tempNode.getAttribute(BIN_COLOR));
            Color tempBinColor = new Color(tempBinColorNum);
            addBin(tempBinName, tempBinColor);
        }
        
        // find the entries.
        int tempEntryNumber = Integer.parseInt(inNode.getAttribute(ENTRY_NUMBER));
        for (int i=0; i<tempEntryNumber; i++){
            Node tempNode = inNode.getChild(ENTRY+i);
            
            // find the values 
            int tempNumValues = Integer.parseInt(tempNode.getAttribute(ENTRY_VALUE_NUMBER));
            String[] tempValues = new String[tempNumValues];
            for (int j=0; j<tempNumValues; j++){
                tempValues[j] = tempNode.getAttribute(ENTRY_VALUE+j);
            }
            
            // find the Bins
            int tempNumBins = Integer.parseInt(tempNode.getAttribute(ENTRY_BIN_NUMBER));
            double[] tempMins = new double[tempNumBins];
            double[] tempMaxs = new double[tempNumBins];
            for (int j=0; j<tempNumBins; j++){
                Node tempEntryBinNode = tempNode.getChild(ENTRY_BIN+j);
                double tempMin = Double.parseDouble(tempEntryBinNode.getAttribute(MIN_VALUE));
                tempMins[j] = tempMin;
                double tempMax = Double.parseDouble(tempEntryBinNode.getAttribute(MAX_VALUE));
                tempMaxs[j] = tempMax;
            }            
            addEntry(tempValues, tempMins, tempMaxs);
        }
    }
    /**
     * Set up the graphics context for the filling o shapes.
     * Always return the graphics context sent in after modifying it for filling of polygons.
     */
    private int mydo = 0;
    public Graphics getFillGraphics(Graphics inGraphics,Object[] inAttributes,String[] inNames) {
        if (inGraphics == null) return inGraphics;
        if (inNames == null) return inGraphics;
        if (inAttributes == null) return inGraphics;
        
        Graphics2D g2d = (Graphics2D) inGraphics;
        // get the color for this row
        Color tempColor = getColor(inNames, inAttributes);
        if (tempColor == null){
            if (getDefaultFillColor() == null) return null;
            g2d.setColor(getDefaultFillColor());
        }
        else{
            g2d.setColor(tempColor);
            mydo++;
        }
        g2d.setStroke(getStroke());
        g2d.setComposite(getDefaultAlphaComposite());
        g2d.setFont(getDefaultFont());
        return g2d;
    }
    
    /** return the panel needed to edit this shader  */
    public ShaderPanel getEditPanel() {
        BinShaderPanel tempPanel = new BinShaderPanel();
        tempPanel.setShader(this);
        return tempPanel;
    }    
}
