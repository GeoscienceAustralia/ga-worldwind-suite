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
import gistoolkit.features.Record;

/**
 * Class to handle multiple filters.
 */
public class Join implements Filter{
    /** Takes the logical AND of the two filters. */
    public static final int AND = 0;
    /** Takes the logical OR of the two filters. */
    public static final int OR = 1;

    /** The descriptive name for this filter. */
    private String myFilterName = "Join";
    /** Set the name for this filter. */
    public void setFilterName(String inFilterName){myFilterName = inFilterName;}
    /** Get the name for this filter. */
    public String getFilterName(){return myFilterName;}

    /** The first filter to compare. */
    private Filter myFilter1 = null;
    /** Return the first filter. */
    public Filter getFilter1() {return myFilter1;}

    /** The comparison to perform for the two filters. */
    private int myComparison = 0;
    /** Return the comparison to perform on the two filters. */
    public int getComparison(){return myComparison;}
    
    /** The Second filter to compare. */
    private Filter myFilter2 = null;
    /** Return the second filter. */
    public Filter getFilter2() {return myFilter2;}

    /** Creates new Expression for use with the set node method only.*/
    public Join() {
    }

    /** Creates new Expression */
    public Join(Filter inFilter1, int inComparison, Filter inFilter2) {
        myFilter1 = inFilter1;
        myComparison = inComparison;
        myFilter2 = inFilter2;
    }
    
    /**
     * Determines if this record should or should not be returned as part of the resulting dataset.
     *
     * <p> Returns True if the record should be included, and returns False if it should not. <p>
     */
    public boolean contains(Record inRecord) {
        if (myComparison == AND){
            if (myFilter1.contains(inRecord) && (myFilter2.contains(inRecord))) return true;
        }
        else{
            if (myFilter1.contains(inRecord) || (myFilter2.contains(inRecord))) return true;
        }
        return false;
    }
    
    
    private static String FILTER_NAME = "FilterName";
    private static String COMPARISON = "Comparison";
    private static String FILTER1_NODE = "FILTER1";
    private static String FILTER2_NODE = "FILTER2";
    private static String FILTER_CLASS = "FilterClass";
    /**
     * Get the configuration information for the filter.
     */
    public Node getNode() {
        Node tempRoot = new Node("Join");
        tempRoot.addAttribute(FILTER_NAME, getFilterName());
        tempRoot.addAttribute(COMPARISON, ""+myComparison);
        
        // add  the first filter
        Node tempNode = new Node(FILTER1_NODE);
        tempNode.addAttribute(FILTER_CLASS, myFilter1.getClass().getName());
        tempNode.addChild(myFilter1.getNode());
        tempRoot.addChild(tempNode);
        
        // add  the second filter
        tempNode = new Node(FILTER2_NODE);
        tempNode.addAttribute(FILTER_CLASS, myFilter2.getClass().getName());
        tempNode.addChild(myFilter2.getNode());
        tempRoot.addChild(tempNode);
        
        // return the configuration information
        return tempRoot;
    }
    
    /**
     * Set the configuration information in the filter.
     */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            setFilterName(inNode.getAttribute(FILTER_NAME));
            try{
                myComparison = Integer.parseInt(inNode.getAttribute(COMPARISON));
            }
            catch(NumberFormatException e){
                throw new Exception("Comparison for filter "+getFilterName()+" is "+inNode.getAttribute(COMPARISON)+" which is not an integer");
            }
            
            // configure the first filter
            Node tempNode = inNode.getChild(FILTER1_NODE);
            if (tempNode == null) throw new Exception ("Join "+getFilterName()+" must have two filters, "+FILTER1_NODE+" is missing");
            else{
                String tempClassName = tempNode.getAttribute(FILTER_CLASS);
                if (tempClassName == null) throw new Exception("Join "+getFilterName()+" must have a class for filter 1");
                myFilter1 = (Filter) Class.forName(tempClassName).newInstance();
                Node[] tempFilterNodes = tempNode.getChildren();
                if (tempFilterNodes.length > 0){
                    myFilter1.setNode(tempFilterNodes[0]);
                }
            }
                
            // configure the second filter
            tempNode = inNode.getChild(FILTER2_NODE);
            if (tempNode == null) throw new Exception ("Join "+getFilterName()+" must have two filters, "+FILTER2_NODE+" is missing");
            else{
                String tempClassName = tempNode.getAttribute(FILTER_CLASS);
                if (tempClassName == null) throw new Exception("Join "+getFilterName()+" must have a class for filter 2");
                myFilter2 = (Filter) Class.forName(tempClassName).newInstance();
                Node[] tempFilterNodes = tempNode.getChildren();
                if (tempFilterNodes.length > 0){
                    myFilter2.setNode(tempFilterNodes[0]);
                }
            }
        }
    }    
}
