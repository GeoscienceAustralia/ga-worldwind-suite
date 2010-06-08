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

package gistoolkit.datasources.memory;

import gistoolkit.features.Shape;
import gistoolkit.features.Record;

/**
 * I need some way to determine if the record has been updated.  This is one such way.
 * @author  ithaqua
 * @version 
 */
public class MemoryRecord extends Record{

    /** Creates new MemoryRecord */
    public MemoryRecord() {
    }
    
    /** Pointer to the orrigional shape */
    protected Shape myOrrigionalShape = null;

    /** Reference to the orrigional attributes of the shape */
    protected Object[] myOrrigionalAttributes = null;
    
    /** Creates a new MemoryRecord from the given record */
    public MemoryRecord(Record inRecord){
        myOrrigionalShape = (Shape) inRecord.getShape().clone();
        setShape(inRecord.getShape());
        setOrrigionalAttributes(inRecord.getAttributes());
        setAttributeNames(inRecord.getAttributeNames());
        setAttributeTypes(inRecord.getAttributeTypes());
        setAttributes(inRecord.getAttributes());
    }
    
    /** Need a clone method to allow editing of this record */
    public Object clone(){
        MemoryRecord tempNewRecord = new MemoryRecord();
        tempNewRecord.setShape(getShape());
        tempNewRecord.myOrrigionalShape = myOrrigionalShape;
        tempNewRecord.setAttributeNames(getAttributeNames());
        tempNewRecord.setAttributeTypes(getAttributeTypes());
        tempNewRecord.setAttributes(getAttributes());
        tempNewRecord.myOrrigionalAttributes = myOrrigionalAttributes;
        return tempNewRecord;
    }
    /** Set the attributes */
    protected void setOrrigionalAttributes(Object[] inAttributes){
        if (inAttributes == null) myOrrigionalAttributes = null;
        myOrrigionalAttributes = new Object[inAttributes.length];
        for (int i=0; i<inAttributes.length; i++){
            myOrrigionalAttributes[i] = inAttributes[i];
        }
    }
}
