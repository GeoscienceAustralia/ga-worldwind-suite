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

package gistoolkit.datasources.filter;

import gistoolkit.features.*;
import gistoolkit.common.*;

/**
 * Base class for filtering Attributes.
 */
public abstract class AttributeFilter implements Filter{
    /** Returns all the records where this attribute is equal to this value. */
    public static final int ATTRIBUTE_EQUALS = 3;
    /** Returns all the records where this attribute is greater than this value. */
    public static final int ATTRIBUTE_GREATER = 4;
    /** Returns all the records where this attribute is less than this value. */
    public static final int ATTRIBUTE_LESS = 5;
    
    /** The descriptive name for this filter. */
    private String myFilterName = "Filter";
    /** Set the name for this filter. */
    public void setFilterName(String inFilterName){myFilterName = inFilterName;}
    /** Get the name for this filter. */
    public String getFilterName(){return myFilterName;}
    
    /** The comparison to use. */
    private int myComparison = 0;
    /** Sets the comparison to use. */
    protected void setComparison(int inComparison){myComparison = inComparison;}
    /** Returns the type of comparison to perform. */
    public int getComparison(){return myComparison;}

    /** The attribute name to use in comparisons. */
    private String myAttributeName = null;
    /** Set the attribute name to use in comparisons. */
    protected void setAttributeName(String inAttributeName){myAttributeName = inAttributeName;}
    /** Get the attribute name to use in comparisons. */
    public String getAttributeName(){return myAttributeName;}
    
    /** Return the attribute value. */
    public abstract Object getAttributeValue();
    
    /** Set the attribute value. */
    public abstract void setValue(String inValue);
    
    /** The index of the last attribute of this name. */
    private int myLastIndex = -1;
    
    /** Finds the attribute in the attribute array, returns -1 if it is not found. */
    protected int findAttribute(Record inRecord){
        String[] tempAttributeNames = inRecord.getAttributeNames();
        if (tempAttributeNames == null) return-1;
        
        // check the last one.
        if (myLastIndex > -1)
            if (tempAttributeNames.length > myLastIndex)
                if (tempAttributeNames[myLastIndex] != null)
                    if (myAttributeName.equalsIgnoreCase(tempAttributeNames[myLastIndex]))
                        return myLastIndex;
                    
        // Now check them all.
        for (int i=0; i<tempAttributeNames.length; i++){
            if (tempAttributeNames[i] != null){
                if (myAttributeName.equalsIgnoreCase(tempAttributeNames[i])){
                    myLastIndex = i;
                    return i;
                }
            }
        }
        return -1;        
    }
    
    /** The tostring for this filter will nust return the name. */
    public String toString(){return myFilterName;}
    
    /** Construct the filter name */
    public String createFilterName(){
        StringBuffer sb = new StringBuffer();
        sb.append(myAttributeName);
        if (myComparison == ATTRIBUTE_EQUALS) sb.append(" = ");
        if (myComparison == ATTRIBUTE_GREATER) sb.append(" > ");
        if (myComparison == ATTRIBUTE_LESS) sb.append(" < ");
        sb.append(" ");
        sb.append(getAttributeValue());
        return sb.toString();
    }
    
    /** String to ensure that the data source name tag is constant */
    private static final String FILTER_NAME = "FilterName";
    private static final String ATTRIBUTE_NAME = "AttributeName";
    private static final String COMPARISON_TAG = "Comparison";
    protected static final String ATTRIBUTE_VALUE = "AttributeValue";
    
    /** Get the configuration information for this filter  */
    public Node getNode() {
        Node tempRoot = new Node("SimpleAttributeFilter");
        tempRoot.addAttribute(FILTER_NAME, getFilterName());
        tempRoot.addAttribute(ATTRIBUTE_NAME, getAttributeName());
        tempRoot.addAttribute(COMPARISON_TAG, ""+getComparison());
        return tempRoot;
    }
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            setFilterName(inNode.getAttribute(FILTER_NAME));
            setAttributeName(inNode.getAttribute(ATTRIBUTE_NAME));
            String tempString = inNode.getAttribute(COMPARISON_TAG);
            try{
                int tempInt = Integer.parseInt(tempString);
                setComparison(tempInt);
            }
            catch (NumberFormatException e){}
        }
    }
}
