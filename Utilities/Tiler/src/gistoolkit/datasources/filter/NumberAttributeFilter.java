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

import gistoolkit.common.*;
import gistoolkit.features.*;

/**
 * Class for filtering on attributes that are Numbers.
 */
public class NumberAttributeFilter extends AttributeFilter{

    /** The attribute value to use in comparisons. */
    private double myAttributeValue = 0.0;
    /** Get the attribute value to use in comparisons. */
    public Object getAttributeValue(){return new Double(myAttributeValue);}
    
    /** Creates new NumberAttributeFilter, for use with the configuration utilities only. */
    public NumberAttributeFilter() {
    }
    /** Creates new NumberAttributeFilter */
    public NumberAttributeFilter(String inAttributeName, int inComparison, double inValue) {
        setAttributeName(inAttributeName);
        setComparison(inComparison);
        myAttributeValue = inValue;
    }
    /** Determiens if this record should be included in the subsequent set. */
    public boolean contains(Record inRecord) {
        if (inRecord == null) return false;
        int tempAttributeNum = findAttribute(inRecord);
        if (tempAttributeNum == -1) return false;
        
        Object[] tempAttributes = inRecord.getAttributes();
        if (tempAttributes == null) return false;
        Object tempAttribute = tempAttributes[tempAttributeNum];
        if (tempAttribute == null) return false;
        if (tempAttribute instanceof Number){
            double tempDouble = ((Number) tempAttribute).doubleValue();
            
            // this code is duplicated below darn it all.
            int tempComparison = getComparison();
            if (tempComparison == ATTRIBUTE_EQUALS){
                if (myAttributeValue==tempDouble) return true;
            }
            if (tempComparison == ATTRIBUTE_GREATER){
                if (tempDouble > myAttributeValue) return true;
            }
            if (tempComparison == ATTRIBUTE_LESS){
                if (tempDouble < myAttributeValue) return true;
            }
        }
        else if (tempAttribute instanceof String){
            try{
                double tempDouble = Double.parseDouble((String)tempAttribute);
                
                // this code is duplicated from above darn it all.
                int tempComparison = getComparison();
                if (tempComparison == ATTRIBUTE_EQUALS){
                    if (myAttributeValue==tempDouble) return true;
                }
                if (tempComparison == ATTRIBUTE_GREATER){
                    if (tempDouble > myAttributeValue) return true;
                }
                if (tempComparison == ATTRIBUTE_LESS){
                    if (tempDouble < myAttributeValue) return true;
                }
            }
            catch (NumberFormatException e){
                // returning false below anyway
            }
        }
        return false;
    }
    /** Get the configuration information for this filter  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("NumberAttributeFilter");
        tempRoot.addAttribute(ATTRIBUTE_VALUE, ""+myAttributeValue);
        return tempRoot;
    }
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            super.setNode(inNode);
            try{
                myAttributeValue = Double.parseDouble(inNode.getAttribute(ATTRIBUTE_VALUE));
            }
            catch (NumberFormatException e){
                throw new Exception ("Error double value for filter "+getFilterName()+" is "+inNode.getAttribute(ATTRIBUTE_VALUE)+" which is not a number");
            }
        }
    }
    
    /** Set the attribute value.  */
    public void setValue(String inValue) {
        try{
            myAttributeValue = Double.parseDouble(inValue);
        }
        catch (NumberFormatException e){}
    }
}
