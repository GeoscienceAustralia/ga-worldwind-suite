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

package gistoolkit.features;

/**
 * This class keeps a record of the type of object that the column represents.
 * There are a set of types that are known to the toolkit, these have names like
 * STRING, INTEGER, FLOAT, TIMESTAMP, and BOOLEAN.
 *
 * Additional types can be added by giving them a new type name.  An editor should be provided for these types as well.
 */
public class AttributeType {
    /** The type used for string data, or character data, mapps to Char, Varchar, etc. */
    public static final String STRING="String";
    
    /** The type used for integer data, or number data not including a decimal point.  The length is applied to this value. if -1 is given for the length, then the length is ignored.*/
    public static final String INTEGER="Integer";
    
    /** The type used for floating point data.  These are represented as doubles, and floats.  The length is applied to the portion before the decimal point, and the aux length is applied to the portion after. If -1 is given for any one of the lengths, then the lengths are ignored.*/
    public static final String FLOAT="Float";
    
    /** The type used for date and time information. */
    public static final String TIMESTAMP = "Timestamp";
    
    /** The type used for boolean, or true/false data.*/
    public static final String BOOLEAN = "Boolean";
    
    /** The type used for Types that are unknown to this toolkit */
    public static final String UNKNOWN = "Unknown";
    
    /** Creates new AttributeType with the given name and no lengths.*/
    public AttributeType(String inName) {
        myTypeName = inName;
    }

    /** Creates new AttributeType with the given name and length.*/
    public AttributeType(String inName, int inLength) {
        myTypeName = inName;
        myLength = inLength;
    }
    
    /** Creates new AttributeType with the given name and lengths.*/
    public AttributeType(String inName, int inLength, int inAuxLength) {
        myTypeName = inName;
        myLength = inLength;
        myAuxLength = inAuxLength;
    }

    /** The type of the column. */
    private String myTypeName = null;
    /** Return the type of this attribute. May be one of the known types, or a type provided by the datasource.*/
    public String getType(){return myTypeName;}
    
    /** The length of the column */
    private int myLength = -1;
    /** This is the maximum length of the attribute.  Many data storage techniques enforce a maximum length on attrubyte types.*/
    public int getLength(){return myLength;}
    
    /** An additional Length.  This length is used for the decimal length in some data bases that require a length before the decimal, and one after. If a -1 is returned, it should be ignored.*/
    private int myAuxLength = -1;
    /** This is an additional nength.  There are occasionally two lengths required to represent an attribute. If a -1 is returned, it should be ignored.*/
    public int getAuxLength(){return myAuxLength;}

}
