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

package gistoolkit.display.shapeeditor;

import java.util.Vector;
import javax.swing.JPanel;
import gistoolkit.features.Shape;
import gistoolkit.features.Point;

/**
 * There is the need to type in particular coordinates when editing a shape.  This panel will provide the user interface to allow that.
 */
public abstract class ShapeEditor extends JPanel{

    /** Creates new ShapeEditor */
    public ShapeEditor() {
    }

    /** The shape to be edited */
    protected Shape myShape = null;
    
    /** Set the shape into the panel */
    public void setShape(Shape inShape){myShape = inShape;}
    
    /** retrieve the shape from the panel */
    public Shape getShape(){return myShape;}
    
    /** Called to notify the editor that the point was added.*/
    public abstract void addPoint(Point inPoint);
    
    /** Called to notify the editor that the point was deleted.*/
    public abstract void removePoint(Point inPoint);
    
    /** Called to notify the editor that the moint was moved. */
    public abstract void movePoint(int inIndex, Point inPoint);
    
    /** Called to notify that the point has been selected, helps keep this editor in synch with outside events. */
    public abstract void selectPoint(int inIndex);
    
    /** Called to notify that the shape has radically changed, usually causes a reload.  */
    public void shapeUpdated(Shape inShape) {
        setShape(inShape);
    }
           
    /** Vector to hold the listeners for this shape editor. */
    private Vector myListeners = new Vector();
    /** Add a new listener to this Shape Editor. */
    public void addShapeEditorListener(ShapeEditorListener inListener){
        if (inListener != null) myListeners.add(inListener);
    }
    /** Remove the listener from this ShapeEditor. */
    public void removeShapeEditorListener(ShapeEditorListener inListener){
        myListeners.remove(inListener);
    }
    
    /** Publish that a point was added. */
    protected void firePointAdded(Point inPoint){
        for (int i=0; i<myListeners.size(); i++){
            try{
                ShapeEditorListener tempListener = (ShapeEditorListener) myListeners.elementAt(i);
                tempListener.pointAdded(inPoint);
            }
            catch (Throwable t){
                System.out.println(t);
                t.printStackTrace();
            }
        }
    }

    /** Publish that a point was removed. */
    protected void firePointRemoved(Point inPoint){
        for (int i=0; i<myListeners.size(); i++){
            try{
                ShapeEditorListener tempListener = (ShapeEditorListener) myListeners.elementAt(i);
                tempListener.pointRemoved(inPoint);
            }
            catch (Throwable t){
                System.out.println(t);
                t.printStackTrace();
            }
        }
    }
    
    /** Publish that a point was selected. */
    protected void firePointSelected(Point inPoint){
        for (int i=0; i<myListeners.size(); i++){
            try{
                ShapeEditorListener tempListener = (ShapeEditorListener) myListeners.elementAt(i);
                tempListener.pointSelected(inPoint);
            }
            catch (Throwable t){
                System.out.println(t);
                t.printStackTrace();
            }
        }
    }

    /** Publish that a point was Deselected. */
    protected void firePointDeselected(Point inPoint){
        for (int i=0; i<myListeners.size(); i++){
            try{
                ShapeEditorListener tempListener = (ShapeEditorListener) myListeners.elementAt(i);
                tempListener.pointDeselected(inPoint);
            }
            catch (Throwable t){
                System.out.println(t);
                t.printStackTrace();
            }
        }
    }
    
    /** Publish that the Shape was updated. */
    protected void fireShapeUpdated(Shape inShape){
        for (int i=0; i<myListeners.size(); i++){
            try{
                ShapeEditorListener tempListener = (ShapeEditorListener) myListeners.elementAt(i);
                tempListener.shapeUpdated(inShape);
            }
            catch (Throwable t){
                System.out.println(t);
                t.printStackTrace();
            }
        }
    }
}
