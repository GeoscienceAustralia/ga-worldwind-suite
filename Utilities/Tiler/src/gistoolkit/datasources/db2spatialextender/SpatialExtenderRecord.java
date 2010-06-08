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

package gistoolkit.datasources.db2spatialextender;

import gistoolkit.features.*;

/**
 * Record to handle the problems with updating records in spatial extender.
 */
public class SpatialExtenderRecord extends Record {
    
    /**
     * The previous objects.
     */
    private Object[] myPreviousAttributes = null;
    /** Retrieve the previous attributes. */
    protected Object[] getPreviousAttributes(){return myPreviousAttributes;}
    /** Set the previous attributes.*/
    protected void setPreviousAttributes(Object[] inAttributes){myPreviousAttributes = inAttributes;}
    
    /**
     * The previous shape
     */
    private Shape myPreviousShape = null;
    /** Retrieve the previous Shape. */
    protected Shape getPreviousShape(){return myPreviousShape;}
    /** Set the previous Shape. */
    protected void setPreviousShape(Shape inShape){myPreviousShape = inShape;}
    
    /**
     * Creats a copy of this record;
     */
    public Object clone(){
        SpatialExtenderRecord tempRecord = new SpatialExtenderRecord();
        if (getShape() != null){
            tempRecord.setPreviousShape(getShape());
            tempRecord.setShape((Shape) getShape().clone());
        }
        if (getAttributes() != null){
            tempRecord.setPreviousAttributes(getAttributes());
            Object[] tempObject = new Object[getAttributes().length];
            for (int i=0; i<getAttributes().length; i++){
                tempObject[i] = getAttributes()[i];
            }
            tempRecord.setAttributes(tempObject);
        }
        tempRecord.setAttributeNames(getAttributeNames());
        tempRecord.setAttributeTypes(getAttributeTypes());
        return tempRecord;
    }
    

    /** Creates new SpatialExtenderRecord */
    public SpatialExtenderRecord() {
    }

}
