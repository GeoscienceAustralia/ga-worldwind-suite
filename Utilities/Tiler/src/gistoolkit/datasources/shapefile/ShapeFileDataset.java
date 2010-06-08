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

import gistoolkit.datasources.*;
/**
 * A dataset specific to the shape file needs
 */
public class ShapeFileDataset extends GISDataset {
    
    /**
     * ShapeFileDataset constructor comment.
     */
    public ShapeFileDataset() {
        super();
    }
    
    /**
     * ShapeFileDataset constructor comment.
     * @param inAttributeNames java.lang.String[]
     */
    public ShapeFileDataset(java.lang.String[] inAttributeNames) {
        super(inAttributeNames);
    }
    
    protected void setRecord(ShapeFileRecord tempRecord, int inIndex){
        myVectRecords.setElementAt(tempRecord, inIndex);
    }
    
    protected ShapeFileRecord[] getShapeFileRecords(){
        ShapeFileRecord[] tempRecords = new ShapeFileRecord[myVectRecords.size()];
        myVectRecords.copyInto(tempRecords);
        return tempRecords;
    }
    
    protected void remove(ShapeFileRecord tempRecord){
        myVectRecords.removeElement(tempRecord);
    }
}