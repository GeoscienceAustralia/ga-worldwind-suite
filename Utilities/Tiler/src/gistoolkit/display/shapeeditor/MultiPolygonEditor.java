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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import gistoolkit.features.*;

/**
 * Provides the user interface for editing MultiPolygons.
 */
public class MultiPolygonEditor extends ShapeEditor implements ActionListener{
    
    /** Tabbed Pane to display the collection of line string editors. */
    private JTabbedPane myTabbedPane = null;
    
    /** Vector for handling the editors. */
    private Vector myVectEditors = new Vector();
    
    /** Button for adding Points. */
    private JButton myButtonAdd;
    
    /** Button for deleting Points. */
    private JButton myButtonDelete;
    
    /** Creates new MultiPolygonEditor */
    public MultiPolygonEditor() {
        initPanel();
    }
    
    private class ShapeListener implements ShapeEditorListener{
        
        /** Called when a point is removed.  */
        public void pointRemoved(Point inPoint) {
            firePointRemoved(inPoint);
        }
        
        /** Called when a point is added to the shape.  */
        public void pointAdded(Point inPoint) {
            firePointAdded(inPoint);
        }
        
        /** Called when a point is deselected.  */
        public void pointDeselected(Point inPoint) {
            firePointDeselected(inPoint);
        }
        
        /** Called when any update happens to the shape.  */
        public void shapeUpdated(Shape inShape) {
            fireShapeUpdated(inShape);
        }
        
        /** Called when a point is selected.  */
        public void pointSelected(Point inPoint) {
            firePointSelected(inPoint);
        }
    }
    ShapeListener myShapeListener = new ShapeListener();
    
    /** Initialize the user interface for this panel. */
    private void initPanel(){
        setLayout(new BorderLayout());
        
        // The central theme is the JTabbedPane
        myTabbedPane = new JTabbedPane();
        add(myTabbedPane, BorderLayout.CENTER);
        
        // add the add and delete buttons.
        JPanel tempButtonPanel = new JPanel(new GridLayout(1,2,2,2));
        tempButtonPanel.add(myButtonAdd = new JButton("New"));
        tempButtonPanel.add(myButtonDelete = new JButton("Expunge"));
        add(tempButtonPanel, BorderLayout.SOUTH);
        
        // set this is an action listener for the buttons
        myButtonAdd.addActionListener(this);
        myButtonDelete.addActionListener(this);
        
    }
    
    /** Set the shape into the dialog. */
    public void setShape(Shape inShape){
        if (inShape instanceof MultiPolygon){
            super.setShape(inShape);
            myTabbedPane.removeAll();
            myVectEditors.clear();
            
            // retrieve the Polygons
            Polygon[] tempPolygons = ((MultiPolygon) inShape).getPolygons();
            
            // add the panels to the tabbed panel
            for (int i=0; i<tempPolygons.length; i++){
                PolygonEditor tempEditor = new PolygonEditor();
                tempEditor.setShape(tempPolygons[i]);
                tempEditor.addShapeEditorListener(myShapeListener);
                myVectEditors.add(tempEditor);
                myTabbedPane.addTab("Polygon "+i, tempEditor);
            }
        }
    }
    
    /** Called to notify the editor that the moint was moved.  */
    public void movePoint(int inIndex, Point inPoint) {
        int tempIndex = inIndex;
        for (int i=0; i<myVectEditors.size(); i++){
            PolygonEditor tempEditor = (PolygonEditor) myVectEditors.elementAt(i);
            if (tempIndex > tempEditor.getNumPoints()){
                tempIndex = tempIndex - tempEditor.getNumPoints();
            }
            else{
                tempEditor.movePoint(tempIndex, inPoint);
                myTabbedPane.setSelectedComponent(tempEditor);
                break;
            }
        }
    }
    
    /** Called to notify that the point has been selected, helps keep this editor in synch with outside events.  */
    public void selectPoint(int inIndex) {
        int tempIndex = inIndex;
        for (int i=0; i<myVectEditors.size(); i++){
            PolygonEditor tempEditor = (PolygonEditor) myVectEditors.elementAt(i);
            if (tempIndex > tempEditor.getNumPoints()){
                tempIndex = tempIndex - tempEditor.getNumPoints();
            }
            else{
                tempEditor.selectPoint(tempIndex);
                myTabbedPane.setSelectedComponent(tempEditor);
                break;
            }
        }
    }
    
    /** Called to notify the editor that the point was added. */
    public void addPoint(Point inPoint) {
    }
    
    /** Called to notify the editor that the point was deleted. */
    public void removePoint(Point inPoint) {
    }
    
    public void actionPerformed(java.awt.event.ActionEvent inAE) {
        if (inAE.getSource() == myButtonAdd){
            int index = myTabbedPane.getSelectedIndex();
            PolygonEditor tempEditor = new PolygonEditor();
            Point[] tempPoints = new Point[4];
            tempPoints[0] = new Point(0,0);
            tempPoints[1] = new Point(1,1);
            tempPoints[2] = new Point(3,1);
            tempPoints[3] = tempPoints[0];
            LinearRing tempRing = new LinearRing(tempPoints);
            Polygon tempPolygon = new Polygon(tempRing);
            MultiPolygon tempMultiPolygon = (MultiPolygon) getShape();
            tempMultiPolygon.addPolygon(index, tempPolygon);
            tempEditor.setShape(tempPolygon);
            tempEditor.addShapeEditorListener(myShapeListener);
            if (myTabbedPane.getTabCount() == 0){
                myTabbedPane.addTab("Polygon "+myVectEditors.size(), tempEditor);
                myVectEditors.add(tempEditor);
            }
            else{
                myTabbedPane.insertTab("Polygon "+myVectEditors.size(), null, tempEditor, null, index);
                myVectEditors.insertElementAt(tempEditor, index);
            }
            fireShapeUpdated(getShape());
        }
        if (inAE.getSource() == myButtonDelete){
            if (myTabbedPane.getTabCount() > 0){
                int index = myTabbedPane.getSelectedIndex();
                myTabbedPane.removeTabAt(index);
                myVectEditors.remove(index);
                MultiPolygon tempMultiPolygon = (MultiPolygon) getShape();
                tempMultiPolygon.removePolygon(index);
                fireShapeUpdated(getShape());
            }
        }
    }
    
    /** Testing only */
    public static void main(String[] inArgs){
        JFrame tempDialog = new JFrame("Testing MultiPolygon Editor");
        MultiPolygonEditor tempEditor = new MultiPolygonEditor();
        Polygon[] tempPolygons = new Polygon[2];
        
        // line 1
        Point[] tempPoints = new Point[5];
        tempPoints[0] = new Point(12345,54321);
        tempPoints[1] = new Point(1234.5,5432.1);
        tempPoints[2] = new Point(123.45,543.21);
        tempPoints[3] = new Point(12.345,54.321);
        tempPoints[4] = new Point(1.2345,5.4321);
        tempPolygons[0] = new Polygon(new LinearRing(tempPoints));
        
        // line 2
        tempPoints = new Point[5];
        tempPoints[0] = new Point(12345,54321);
        tempPoints[1] = new Point(1234.5,5432.1);
        tempPoints[2] = new Point(123.45,543.21);
        tempPoints[3] = new Point(12.345,54.321);
        tempPoints[4] = new Point(1.2345,5.4321);
        LinearRing tempHole = new LinearRing(tempPoints);
        
        // line 3
        tempPoints = new Point[5];
        tempPoints[0] = new Point(12345,54321);
        tempPoints[1] = new Point(1234.5,5432.1);
        tempPoints[2] = new Point(123.45,543.21);
        tempPoints[3] = new Point(12.345,54.321);
        tempPoints[4] = new Point(1.2345,5.4321);
        tempPolygons[1] = new Polygon(new LinearRing(tempPoints));
        tempPolygons[1].addHole(0, tempHole);
        
        tempEditor.setShape(new MultiPolygon(tempPolygons));
        tempDialog.setContentPane(tempEditor);
        tempDialog.pack();
        tempDialog.show();
    }
}
