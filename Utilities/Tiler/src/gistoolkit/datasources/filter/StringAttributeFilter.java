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
 * Class for filtering on attributes that are strings.
 */
public class StringAttributeFilter extends AttributeFilter{

    /** The attribute value to use in comparisons. */
    private String myAttributeValue = null;
    /** Get the attribute value to use in comparisons. */
    public Object getAttributeValue(){return myAttributeValue;}
    
    /** Creates new StringAttributeFilter, for use with the configuration utilities. */
    public StringAttributeFilter() {
    }
    /** Creates new StringAttributeFilter */
    public StringAttributeFilter(String inAttributeName, int inComparison, String inValue) {
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
        String tempString = tempAttribute.toString();
        
        int tempComparison = getComparison();
        if (tempComparison == ATTRIBUTE_EQUALS){
            if (myAttributeValue.equals(tempString)) return true;
        }
        if (tempComparison == ATTRIBUTE_GREATER){
            if (tempString.compareTo(myAttributeValue) > 0)return true;
        }
        if (tempComparison == ATTRIBUTE_LESS){
            if (tempString.compareTo(myAttributeValue) < 0)return true;
        }
        return false;
    }
    /** Get the configuration information for this filter  */
    public Node getNode() {
        Node tempRoot = super.getNode();
        tempRoot.setName("StringAttributeFilter");
        tempRoot.addAttribute(ATTRIBUTE_VALUE, (String) getAttributeValue());
        return tempRoot;
    }
    /** Set the configuration information for this data source  */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            super.setNode(inNode);
            myAttributeValue = inNode.getAttribute(ATTRIBUTE_VALUE);
        }
    }
    
    /** Set the attribute value.  */
    public void setValue(String inValue) {
        myAttributeValue = inValue;
    }
    
}
