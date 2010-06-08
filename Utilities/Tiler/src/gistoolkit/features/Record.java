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
 * Represents a single record from the dataset. 
 * Records can only contain one shape feature per record.
 * <p>
 * Contains both the shape and any attribute data associated with the shape.
 * </p>
 */
public class Record {
    /**
     * Returns the attribute type of the attribute.
     */
    private AttributeType[] myAttributeTypes = null;
    
    /**
     * Returns the attribute names associated with the attributes.
     */
    private String[] myAttributeNames = null;
    
    /**
     * The attribute data associated with this shape.
     */
    private Object[] myAttributes = null;
    
    /**
     * The Shape.
     */
    private Shape myShape = null;
    
    /**
     * Record constructor comment.
     */
    public Record() {
        super();
    }
    
    /**
     * Creats a copy of this record;
     */
    public Object clone(){
        Record tempRecord = new Record();
        if (myShape != null){
            tempRecord.setShape((Shape) myShape.clone());
        }
        if (myAttributes != null){
            Object[] tempObject = new Object[myAttributes.length];
            for (int i=0; i<myAttributes.length; i++){
                tempObject[i] = myAttributes[i];
            }
            tempRecord.setAttributes(tempObject);
        }
        tempRecord.setAttributeNames(myAttributeNames);
        tempRecord.setAttributeTypes(myAttributeTypes);
        return tempRecord;
    }
    
    /**
     * Returns the AttributeTypes of the columns in this record.
     */
    public AttributeType[] getAttributeTypes(){
        if (myAttributeTypes == null) return new AttributeType[0];
        return myAttributeTypes;
    }
    
    /**
     * Returns the attribute names associated with the attributes.
     */
    public String[] getAttributeNames(){
        if (myAttributeNames == null) return new String[0];
        return myAttributeNames;
    }
    
    /**
     * Returns the attribute data associated with this shape.
     */
    public Object[] getAttributes(){
        if (myAttributes == null) return new Object[0];
        return myAttributes;
    }
    
    /**
     * Returns the Shape associated with this object
     */
    public Shape getShape(){
        return myShape;
    }
    
    /**
     * Sets the AttributeTypes associated with the attributes.
     */
    public void setAttributeTypes(AttributeType[] inAttributeTypes){
        myAttributeTypes = inAttributeTypes;
    }
    
    /**
     * Sets the attribute names associated with the attributes.
     */
    public void setAttributeNames(String[] inAttributeNames){
        myAttributeNames = inAttributeNames;
    }
    
    /**
     * Set the attribute data associated with this shape
     */
    public void setAttributes(Object[] inAttributes){
        myAttributes = inAttributes;
    }
    
    /**
     * Sets the shape associated with this object
     */
    public void setShape(Shape inShape){
        myShape = inShape;
    }
}