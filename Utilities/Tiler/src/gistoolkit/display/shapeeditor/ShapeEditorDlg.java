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

import java.util.*;
import java.awt.Frame;
import javax.swing.JDialog;
import gistoolkit.features.*;

/**
 * Dialog for editing the points of a shape.
 */
public class ShapeEditorDlg extends JDialog{

    /** The list of shape editorlisteners interested in events from this shape editor.*/
    private Vector myVectListeners = new Vector();
    /** Add a shape editor listener to receive events from the editor panels.*/
    public void addShapeEditorListener(ShapeEditorListener inListener){myVectListeners.add(inListener);}
    /** Remove a shape editor listener from the list of listeners to receive events from the editor panels. */
    public void removeShapeEditorListener(ShapeEditorListener inListener){
        myVectListeners.remove(inListener);
        if (myShapeEditor != null){
            myShapeEditor.removeShapeEditorListener(inListener);
        }
    }
    
    /** Holds a reference to the current shape editor. */
    private ShapeEditor myShapeEditor = null;
        
    /** Creates new ShapeEditorDlg */
    public ShapeEditorDlg(Frame inFrame, boolean inModal) {
        super(inFrame, "Edit Shape Points", inModal);
    }
    
    /** Set the shape into the dialog. */
    public void setShape(Shape inShape){
        ShapeEditor tempShapeEditor = null;
        if (inShape instanceof Point){
            tempShapeEditor = new PointEditor();
            tempShapeEditor.setShape(inShape);
        }
        if (inShape instanceof MultiPoint){
            tempShapeEditor = new MultiPointEditor();
            tempShapeEditor.setShape(inShape);
        }
        if (inShape instanceof LineString){
            tempShapeEditor = new LineStringEditor();
            tempShapeEditor.setShape(inShape);
        }
        if (inShape instanceof MultiLineString){
            tempShapeEditor = new MultiLineStringEditor();
            tempShapeEditor.setShape(inShape);
        }
        if (inShape instanceof Polygon){
            tempShapeEditor = new PolygonEditor();
            tempShapeEditor.setShape(inShape);
        }
        if (inShape instanceof MultiPolygon){
            tempShapeEditor = new MultiPolygonEditor();
            tempShapeEditor.setShape(inShape);
        }

        myShapeEditor = tempShapeEditor;
        
        if (tempShapeEditor != null){
            for (int i=0; i<myVectListeners.size(); i++){
                tempShapeEditor.addShapeEditorListener((ShapeEditorListener) myVectListeners.elementAt(i));
            }
        
            // add this to the central dialog
            setContentPane(tempShapeEditor);
            
            // set the size of the dialog
            pack();
        }
    }
    
    /** Notify the editor that the shape has been updated. */
    public void shapeUpdated(Shape inShape){
        if (myShapeEditor != null){
            myShapeEditor.shapeUpdated(inShape);
        }
    }
}
