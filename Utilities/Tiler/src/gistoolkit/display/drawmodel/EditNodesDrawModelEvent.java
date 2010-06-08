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

package gistoolkit.display.drawmodel;

import gistoolkit.features.*;
/**
 * Holds Information about the events taking place within the Edit Nodes Draw Model.
 */
public class EditNodesDrawModelEvent {

    /** A point was added to the shape. */
    public static final int POINT_ADDED = 1;
    /** A point was removed from the shape. */
    public static final int POINT_REMOVED = 2;
    /** A point was moved within the shape. */
    public static final int POINT_MOVED = 3;
    
    /** What happened to the shape. */
    private int myType = 0;
    /** Determine what type of event happened. */
    public int getType(){return myType;}
    
    /** The point that was affected. */
    private Point myPoint = null;
    /** Retrieve the point that was modified. */
    public Point getPoint(){return myPoint;}
    
    /** The shape that was modified. */
    private Shape myShape = null;
    /** Retrieve the shape that was modified. */
    public Shape getShape(){return myShape;}
    
    /** Creates new EditNodesDrawModelEvent with the given type and shape.*/
    public EditNodesDrawModelEvent(int inType, Shape inShape) {
        myType = inType;
        myShape = inShape;
    }

    /** Creates new EditNodesDrawModelEvent with the specified type, Shape and the affected point.*/
    public EditNodesDrawModelEvent(int inType, Shape inShape, Point inPoint) {
        myType = inType;
        myShape = inShape;
        myPoint = inPoint;
    }
}
