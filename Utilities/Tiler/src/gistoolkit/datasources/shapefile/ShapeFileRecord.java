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
package gistoolkit.datasources.shapefile;

import gistoolkit.features.*;
/**
 * A record type to keep track of the index in the shape file for updates.
 */
public class ShapeFileRecord extends Record {
    /**
     * The index into the shape file of the record.
     */
    private int myIndex;
    /**
     * ShapeFileRecord constructor comment.
     */
    public ShapeFileRecord() {
        super();
    }
    
    /**
     * Create a Shape File Record from an existing Record.
     * The index is the index of the record within the shape file.
     */
    public ShapeFileRecord(int inIndex, Record inRecord) {
        super();
        setIndex(inIndex);
        setAttributeNames(inRecord.getAttributeNames());
        setAttributeTypes(inRecord.getAttributeTypes());
        setAttributes(inRecord.getAttributes());
        setShape(inRecord.getShape());
    }
    
    
    /**
     * Create a new Shape file record with the given index, shape attributes attributenames, and attributeTypes.
     */
    public ShapeFileRecord(int inindex, Shape inShape, Object[] inAttributes, String[] inAttributeNames, AttributeType[] inAttributeTypes) {
        super();
        setAttributeNames(inAttributeNames);
        setAttributeTypes(inAttributeTypes);
        setAttributes(inAttributes);
        setShape(inShape);
        myIndex = inindex;
    }
    
    /**
     * Creats a copy of this record;
     */
    public Object clone(){
        ShapeFileRecord tempRecord = new ShapeFileRecord();
        if (getShape() != null){
            tempRecord.setShape((Shape) getShape().clone());
        }
        if (getAttributes() != null){
            Object[] tempObject = new Object[getAttributes().length];
            for (int i=0; i<getAttributes().length; i++){
                tempObject[i] = getAttributes()[i];
            }
            tempRecord.setAttributes(tempObject);
        }
        tempRecord.setAttributeNames(getAttributeNames());
        tempRecord.setAttributeTypes(getAttributeTypes());
        tempRecord.setIndex(myIndex);
        return tempRecord;
    }
    /**
     * Returns the index into the shape file.
     * Creation date: (4/25/2001 4:35:01 PM)
     * @return int
     */
    public int getIndex() {
        return myIndex;
    }
    /**
     * Sets the index into the shape file.
     * Creation date: (4/25/2001 4:35:01 PM)
     * @param newIndex int
     */
    public void setIndex(int newIndex) {
        myIndex = newIndex;
    }
}