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

package gistoolkit.display.labeler;

import java.util.TreeSet;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.*;
import gistoolkit.common.*;
import gistoolkit.display.*;
import gistoolkit.features.*;
/**
 * Simple Labeler to be extended by more advanced renderers.
 * @author  ithaqua
 */
public abstract class SimpleLabeler implements Labeler{
    /** A name for this labeler, returns "Simple Labeler". */
    public abstract String getLabelerName();
    
    /** The name of the column to label by */
    private int myLabelColumn = 0;
    /** Set the number of the column to label by.*/
    public void setLabelColumn(int inLabelColumn){myLabelColumn = inLabelColumn;}
    /** Retrive the label by column.*/
    public int getLabelColumn(){return myLabelColumn;}
    
    /** The distance from the point in pixels to draw the label. */
    private int myLabelOffset = 0;
    /** Set the distance from the label point that the label will be placed. */
    public void setLabelOffset(int inOffset){myLabelOffset = inOffset;}
    /** Get the distance from the label point that the label will be placed. */
    public int getLabelOffset(){return myLabelOffset;}
    
    /** Class to handle overlaps. */
    private OverlapManager myOverlapManager = new OverlapManager();
    
    /** The orientation of the label. */
    public static final int CENTER = 0;
    public static final int NORTH = 1;
    public static final int EAST = 2;
    public static final int SOUTH = 3;
    public static final int WEST = 4;
    
    /** The orientation of the labels from their anchor. */
    private int myLabelOrientation = CENTER;
    /** Set the orientation of the labels relative to their anchor. */
    public void setLabelOrientation(int inOrientation){myLabelOrientation = inOrientation;}
    /** Get the orientation of the labels relative to their anchor. */
    public int getLabelOrientation(){return myLabelOrientation;}
    
    /** Indicates that duplicates are allowed.*/
    private boolean myAllowDuplicates = false;
    /**Indicates that duplicates are allowed.*/
    public void setAllowDuplicates(boolean inDuplicates){myAllowDuplicates = inDuplicates;}
    /**Returns true if duplicates are allowed, false otherwise.*/
    public boolean getAllowDuplicates(){return myAllowDuplicates;}
    /** Hash table to hold the list of already written labels. */
    private TreeSet myDuplicates = new TreeSet();
    
    /** Checks if this is a duplicate value. Always returns true if duplicates are allowed.*/
    public boolean isDuplicate(Object inValue){
        // if duplicates are allowed, then return false
        if (myAllowDuplicates) return false;
        
        // look for this value in the hash table
        if (myDuplicates.contains(inValue)) return true;
        
        // if it is not a duplicate add it.
        myDuplicates.add(inValue);
        return false;
    }
    
    /** Removes this entry from the list of duplicates. */
    public void removeDuplicate(Object inValue){
        myDuplicates.remove(inValue);
    }
    
    /** Indicates that overlaping labels should be removed. */
    private boolean myAllowOverlaps = false;
    /** Set the allow overlaps flag.  If set to true, then overlaping labels are allowed.  If set to false, then overlaping labels are removed. */
    public void setAllowOverlaps(boolean inOverlaps){myAllowOverlaps = inOverlaps;}
    /** Get the allow overlaps flag.  If set to true, then overlaping labels are allowed.  If set to false, then overlaping labels are removed. */
    public boolean getAllowOverlaps(){return myAllowOverlaps;}
    
    /** Return the OverlapManager for this labeler. */
    public OverlapManager getOverlapManager(){
        if (myAllowOverlaps == true) return null;
        return myOverlapManager;}
    
    /** Checks if this is an overlaping value. Always returns false if overlaps are allowed.*/
    public boolean isOverLaps(int inLocX, int inLocY, int inWidth, int inHeight){
        if (myAllowOverlaps) return false;
        return myOverlapManager.isOverLaps(inLocX, inLocY, inWidth, inHeight);
    }
    /** Checks if this is an overlaping value. Always returns false if overlaps are allowed.*/
    public boolean isOverLaps(java.awt.Shape inShape){
        if (myAllowOverlaps) return false;
        return myOverlapManager.isOverLaps(inShape);
    }
    
    
    /** Convenience method for subclasses, given the anchor position, calculate the label position. */
    public java.awt.Point getLabelPosition(int inX, int inY, int inWidth, int inHeight){
        int tempOrientation = getLabelOrientation();
        switch(tempOrientation){
            case CENTER:
                return new java.awt.Point(inX, inY-myLabelOffset);
            case NORTH:
                return new java.awt.Point(inX, inY-myLabelOffset-(inHeight/2));
            case EAST:
                return new java.awt.Point(inX+inWidth/2+myLabelOffset, inY);
            case SOUTH:
                return new java.awt.Point(inX, inY+myLabelOffset+(inHeight/2));
            case WEST:
                return new java.awt.Point(inX-inWidth/2-myLabelOffset, inY);
        }
        return new java.awt.Point(inX, inY-myLabelOffset);
    }
    
    /** overwrite any alpha shading already applied */
    private AlphaComposite myAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
    
    /** Creates new BasicLabeler */
    public SimpleLabeler() {
    }
    
    /**
     * Draw the label for the record on the graphics context
     */
    public boolean drawLabel(Record inRecord,Graphics inGraphics,Converter inConverter,Shader inShader) {
        if (myLabelColumn < 0) return false;
        if (inRecord == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        if (inShader == null) return false;
        if (inRecord.getShape() == null) return false;
        if (inRecord.getAttributeNames() == null) return false;
        if (inRecord.getAttributes() == null) return false;
        if (inShader == null) return false;
        Graphics tempGraphics = inShader.getLabelGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics == null) return false;
        
        // draw the label.
        return drawLabel(inRecord, tempGraphics, inConverter);
    }
    
    /**
     * Draw the record after checking for not null.
     */
    protected abstract boolean drawLabel(Record inRecord, Graphics inGraphics, Converter inConverter);
    
    /**
     * Highlight the Label when the shape is highlighted.
     */
    public boolean drawLabelHighlight(Record inRecord,Graphics inGraphics,Converter inConverter,Shader inShader) {
        if (myLabelColumn < 0) return false;
        if (inRecord == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        if (inShader == null) return false;
        if (inRecord.getShape() == null) return false;
        if (inRecord.getAttributeNames() == null) return false;
        if (inRecord.getAttributes() == null) return false;
        if (inShader == null) return false;
        Graphics tempGraphics = inShader.getLabelHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics == null) return false;
        
        // draw the label.
        beginLabel();
        myDuplicates.clear();
        myOverlapManager.clear();
        boolean tempValue = drawLabel(inRecord, tempGraphics, inConverter);
        myOverlapManager.clear();
        myDuplicates.clear();
        endLabel();
        return tempValue;
    }
    
    /** Return the name of the labeler. */
    public String toString(){return getLabelerName();}
    
    
    private static final String LABEL_COLUMN = "LabelColumn";
    private static final String LABEL_OFFSET = "LabelOffset";
    private static final String LABEL_ORIENTATION = "LabelOrientation";
    private static final String LABEL_ALLOW_DUPLICATES = "LabelAllowDuplicates";
    private static final String LABEL_ALLOW_OVERLAPS = "LabelAllowOverlaps";
    
    /** get the configuration information for this labeler  */
    public Node getNode() {
        Node tempRoot = new Node("SimpleLabeler");
        tempRoot.addAttribute(LABEL_COLUMN, ""+getLabelColumn());
        tempRoot.addAttribute(LABEL_OFFSET, ""+getLabelOffset());
        tempRoot.addAttribute(LABEL_ORIENTATION, ""+getLabelOrientation());
        tempRoot.addAttribute(LABEL_ALLOW_DUPLICATES, ""+getAllowDuplicates());
        tempRoot.addAttribute(LABEL_ALLOW_OVERLAPS, ""+getAllowOverlaps());
        return tempRoot;
    }
    
    /** Set the configuration information for this labeler  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        String tempString = inNode.getAttribute(LABEL_COLUMN);
        if (tempString != null){
            try{
                setLabelColumn(Integer.parseInt(tempString));
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing value for " +LABEL_COLUMN+" Value = "+tempString);
            }
        }
        tempString = inNode.getAttribute(LABEL_OFFSET);
        if (tempString != null){
            try{
                setLabelOffset(Integer.parseInt(tempString));
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing value for " +LABEL_OFFSET+" Value = "+tempString);
            }
        }
        tempString = inNode.getAttribute(LABEL_ORIENTATION);
        if (tempString != null){
            try{
                setLabelOrientation(Integer.parseInt(tempString));
            }
            catch (NumberFormatException e){
                System.out.println("NumberFormatException parsing value for " +LABEL_ORIENTATION+" Value = "+tempString);
            }
        }
        tempString = inNode.getAttribute(LABEL_ALLOW_DUPLICATES);
        if (tempString != null){
            if (tempString.equalsIgnoreCase("TRUE")){
                setAllowDuplicates(true);
            }
            else{
                setAllowDuplicates(false);
            }
        }
        tempString = inNode.getAttribute(LABEL_ALLOW_OVERLAPS);
        if (tempString != null){
            if (tempString.equalsIgnoreCase("TRUE")){
                setAllowOverlaps(true);
            }
            else{
                setAllowOverlaps(false);
            }
        }
    }
    
    
    /**
     * Called before the layer is initially labeled to allow the labeler to prepare for labeling.
     */
    public void beginLabel(){
        myDuplicates.clear();
        myOverlapManager.clear();
    }
    
    /**
     * Called after the layer has completed labeling.
     */
    public void endLabel() {
        myDuplicates.clear();
        myOverlapManager.clear();
    }
    
    /**
     * Draw the string even if it contains carrage returns.
     */
    public static void drawString(String inString, Graphics2D inGraphics, float inX, float inY, float inWidth, float inHeight){
        String[] tempStrings = getStrings(inString);
        // if there is only one, then return it
        FontMetrics fm = inGraphics.getFontMetrics();
        float tempHeight = -inHeight/2;
        
        float tempStartX = inX - inWidth/2;
        
        // add the rest.
        for (int i=0; i<tempStrings.length; i++){
            Rectangle2D r2d = fm.getStringBounds(tempStrings[i],inGraphics);
            float tempOffset = (float) (inWidth - r2d.getWidth())/2;
            
            inGraphics.drawString(tempStrings[i], tempStartX+tempOffset, (inY+tempHeight+(float)r2d.getHeight()));
            tempHeight = tempHeight + (float) r2d.getHeight();
        }
    }
    
    /**
     *  Method to get the length and width of the string even if it contains carage returns.
     */
    public static Rectangle2D getBounds(String inString, Graphics2D inGraphics){
        double tempWidth = 0;
        double tempHeight = 0;
        
        FontMetrics fm = inGraphics.getFontMetrics();
        String[] tempStrings = getStrings(inString);
        // if there is only one, then return it
        if (tempStrings.length == 1){
            return fm.getStringBounds(tempStrings[0],inGraphics);
        }
        
        // add the rest.
        for (int i=0; i<tempStrings.length; i++){
            Rectangle2D r2d = fm.getStringBounds(tempStrings[i],inGraphics);
            if (tempWidth < r2d.getWidth()) tempWidth = r2d.getWidth();
            tempHeight = tempHeight + r2d.getHeight();
        }
        
        Rectangle r2d = new Rectangle();
        r2d.setRect(0,0, tempWidth, tempHeight);
        return r2d;
        
    }
    
    /**
     * Get the individual Strings.
     */
    public static String[] getStrings(String inString){
        int tempIndex = inString.indexOf("\n");
        if (tempIndex == -1) {
            String[] tempStrings = {inString};
            return tempStrings;
        }
        
        int tempStartIndex = 0;
        ArrayList tempList = new ArrayList();
        
        // break the strings up.
        while (tempIndex >=0){
            String tempString = inString.substring(tempStartIndex, tempIndex);
            tempList.add(tempString);
            tempStartIndex = tempIndex+1;
            tempIndex = inString.indexOf("\n", tempIndex+1);
        }
        // save the last one
        tempList.add(inString.substring(tempStartIndex));
        
        
        // save the strings as an array
        String[] tempStrings = new String[tempList.size()];
        tempList.toArray(tempStrings);
        return tempStrings;
    }    
}

