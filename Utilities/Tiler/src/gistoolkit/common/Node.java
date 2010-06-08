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

package gistoolkit.common;
import java.util.*;

/**
 * Keeps track of a hirearchy of information.
 * @author  ithaqua
 * @version 
 */
public class Node extends Object {

    /** Creates new Node */
    public Node() {
    }
    
    /** Creates new Node */
    public Node(String inName) {
        myName = inName;
    }

    /** Each node must have a name */
    private String myName;
    /** Set the name of this node */
    public void setName(String inName){myName = inName;}
    /** Retrieve the name of this node */
    public String getName(){return myName;}
    
    /** Some Nodes have Values */
    private String myValue;
    /** Set the value of this node */
    public void setValue(String inValue) {myValue = inValue;}
    /** Get the value of this node */
    public String getValue(){return myValue;}
    
    
    // The sub nodes of this node.
    private Vector myChildVect = new Vector();
    /** Add a child node to this node. */
    public void addChild(Node inNode){ if (inNode == null) return; myChildVect.addElement(inNode);}
    /** return the first child with the given name*/
    public Node getChild(String inName){
        if (inName == null) return null;
        for (int i=0; i<myChildVect.size(); i++){
            String tempName = ((Node) myChildVect.elementAt(i)).getName();
            if (inName.equalsIgnoreCase(tempName)) return (Node) myChildVect.elementAt(i);
        }
        return null;
    }
    /** return the list of child nodes */
    public Node[] getChildren(){
        Node[] tempNodes = new Node[myChildVect.size()];
        myChildVect.copyInto(tempNodes);
        return tempNodes;
    }
    /** return the children with the given name*/
    public Node[] getChildren(String inName){
        if (inName == null) return null;
        Vector tempVect = new Vector();
        for (int i=0; i<myChildVect.size(); i++){
            String tempName = ((Node) myChildVect.elementAt(i)).getName();
            if (inName.equalsIgnoreCase(tempName)) tempVect.addElement(myChildVect.elementAt(i));
        }
        Node[] tempNodes = new Node[tempVect.size()];
        tempVect.copyInto(tempNodes);
        return tempNodes;
    }
    
    /** attributes names of this node */
    private Vector myAttributeNames = new Vector();
    /** attribute values of this node */
    private Vector myAttributeValues = new Vector();
    /** Add an attribute to this node */
    public void addAttribute(String inAttributeName, String inAttributeValue){
        myAttributeNames.addElement(inAttributeName);
        myAttributeValues.addElement(inAttributeValue);
    }
    
    /** Retrieve the value of this attribute */
    public String getAttribute(String inAttributeName){
        if (inAttributeName == null) return null;
        String tempString;
        for (int i=0; i<myAttributeNames.size(); i++){
            tempString = (String) myAttributeNames.elementAt(i);
            if (tempString.equalsIgnoreCase(inAttributeName)) return (String) myAttributeValues.elementAt(i);
        }
        return null;
    }
    
    /** Retrieve the list of attribute names */
    public String[] getAttributeNames(){
        String[] tempStrings = new String[myAttributeNames.size()];
        myAttributeNames.copyInto(tempStrings);
        return tempStrings;
    }
    
    public String toString(){
        return myName + "=" + myValue + " "+myAttributeValues.size()+" Attributes "+ myChildVect.size()+" Children";
    }
}
