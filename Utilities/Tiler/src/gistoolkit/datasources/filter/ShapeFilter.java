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
 * Filter for filtering on the shape.
 */
public class ShapeFilter implements Filter{

    /** Returns all the shapes that contain this shape. */
    public static final int SHAPE_CONTAINS = 0;
    /** Returns all the shapes that intersect/overlap this shape. */
    public static final int SHAPE_INTERSECTS = 1;
    /** Returns all the shapes that are the same shape as this shape. */
    public static final int SHAPE_EQUALS = 2;

    /** The descriptive name for this filter. */
    private String myFilterName = "Filter";
    /** Set the name for this filter. */
    public void setFilterName(String inFilterName){myFilterName = inFilterName;}
    /** Get the name for this filter. */
    public String getFilterName(){return myFilterName;}

    /** The shape to compare against. */
    private Shape myShape = null;
    
    /**Return the shape to compare against. Creates a defensive copy, so use sparingly.*/
    public Shape getShape(){return (Shape) myShape.clone();}
    
    /** The comparison to use. */
    private int myComparison = 0;
    /** Returns the type of comparison to perform. */
    public int getComparison(){return myComparison;}
    
    /** Creates a new filter bassed on this shape for use with the configuration utility.*/
    public ShapeFilter() {
    }
    /** Creates a new filter bassed on this shape.*/
    public ShapeFilter(int inComparison, Shape inShape) {
        myComparison = inComparison;
        myShape = inShape;
    }

    public boolean contains(Record inRecord) {
        if (inRecord == null) return false;
        Shape tempShape = inRecord.getShape();
        if (tempShape == null) return false;
        
        if (myComparison == SHAPE_CONTAINS){
            if (tempShape.contains(myShape)) return true;
        }
        if (myComparison == SHAPE_INTERSECTS){
            if (tempShape.intersects(myShape)) return true;
        }
        if (myComparison == SHAPE_EQUALS){
            if (tempShape.equals(myShape)) return true;
        }
        return false;
    }
    
    /** The tostring for this filter will nust return the name. */
    public String toString(){return myFilterName;}
    
    /**
     * Get the configuration information for the filter.
     */
    public Node getNode() {
        return null;
    }
    
    /**
     * Set the configuration information in the filter.
     */
    public void setNode(Node inNode) throws Exception {
    }
    
}
