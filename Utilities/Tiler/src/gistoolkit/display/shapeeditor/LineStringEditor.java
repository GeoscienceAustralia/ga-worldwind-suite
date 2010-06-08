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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import gistoolkit.features.*;

/**
 * Provides the user interface for editing LineStrings.
 */
public class LineStringEditor extends ShapeEditor implements ActionListener, ListSelectionListener, TableModelListener{
    
    /** Button for adding Points. */
    private JButton myButtonAdd;
    
    /** Button for deleting Points. */
    private JButton myButtonDelete;
    
    /** The column names for the table */
    private String[] myColumnNames = {"X", "Y"};
    
    /** A table to display the X and Y coordinates, hope it is fast enough. */
    private JTable myTable = null;
    
    /** A default table model to hold the data for the table. */
    private DefaultTableModel myTableModel = null;
    
    /** Determines when to listen to the table and when not to.*/
    private boolean myListen = true;
    
    /** Creates new LineStringEditor. */
    public LineStringEditor() {
        initPanel();
    }
    
    /** Set up the user interface elements for this panel */
    public void initPanel(){
        setLayout(new BorderLayout());
        
        // The central theme is the JTable
        myTableModel = new DefaultTableModel(myColumnNames, 0);
        myTableModel.addTableModelListener(this);        
        myTable = new JTable(myTableModel);
        myTable.setRowSelectionAllowed(true);
        myTable.getSelectionModel().addListSelectionListener(this);
        
        // add a scroll panel for the table
        JScrollPane tempScrollPane = new JScrollPane(myTable);
        add(tempScrollPane, BorderLayout.CENTER);
        
        // add the add and delete buttons.
        JPanel tempButtonPanel = new JPanel(new GridLayout(1,2,2,2));
        tempButtonPanel.add(myButtonAdd = new JButton("Add"));
        tempButtonPanel.add(myButtonDelete = new JButton("Delete"));
        add(tempButtonPanel, BorderLayout.SOUTH);
        
        // set this is an action listener for the buttons
        myButtonAdd.addActionListener(this);
        myButtonDelete.addActionListener(this);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent inAE) {
        if (inAE.getSource() == myButtonAdd){
            int index = myTable.getSelectedRow();
            addPoint(index, new Point(0,0));
        }
        if (inAE.getSource() == myButtonDelete){
            int index = myTable.getSelectedRow();
            removePoint(index);
        }
    }
    
    /** Get the number of points. */
    public int getNumPoints(){return myTableModel.getRowCount();}
    
    /** Add the point at the given location, if a -1 is sent in, the point will be added at the end.*/
    public void addPoint(int index, Point inPoint){
        if (inPoint == null) return;
        
        // Modify the shape
        LineString tempLineString = (LineString) getShape();
        
        // if the index is -1, then set it to the size of the shape
        if (index == -1) index = tempLineString.getPoints().length;
        tempLineString.add(index, inPoint.getX(), inPoint.getY());
        
        // modify the table.
        String[] tempPoints = {""+inPoint.getX(), ""+inPoint.getY()};
        myListen = false;
        myTableModel.insertRow(index, tempPoints);
        myListen = true;
        
        // I like a certain of quasi persistent selection
        if (myTableModel.getRowCount() > index){
            myTable.setRowSelectionInterval(index, index);
        }
        
        // notify the observers that the point was added
        firePointAdded(inPoint);
    }
    
    /** Add the point to the display. */
    public void addPoint(Point inPoint){
        if (inPoint == null) return;
        
        // Modify the shape
        LineString tempLineString = (LineString) getShape();
        tempLineString.add(tempLineString.getNumPoints(), inPoint.getX(), inPoint.getY());
        
        // modify the table.
        String[] tempPoints = {""+inPoint.getX(), ""+inPoint.getY()};
        myListen = false;
        myTableModel.addRow(tempPoints);
        myListen = true;
        
        // I like a certain of quasi persistent selection
        int index = myTableModel.getRowCount()-1;
        myTable.setRowSelectionInterval(index, index);

        // notify the observers that the point was added
        firePointAdded(inPoint);
    }
    
    /** Set the LineString to be edited into the panel. */
    public void setShape(Shape inShape){
        super.setShape(inShape);
        if (inShape instanceof LineString){
            myListen = false;
            
            // remove all the points from the table
            myTable.setVisible(false);
            int tempLength = myTableModel.getRowCount();
            for (int i=0; i<tempLength; i++){
                myTableModel.removeRow(0);
            }
            myTable.setVisible(true);
            
            // loop through the points adding them to the display.
            LineString tempLineString = (LineString) inShape;
            double[] tempXPoints = tempLineString.getXCoordinates();
            double[] tempYPoints = tempLineString.getYCoordinates();            
            for (int i=0; i<tempXPoints.length; i++){
                // modify the table.
                String[] tempData = {""+tempXPoints[i], ""+tempYPoints[i]};
                myTableModel.addRow(tempData);
            }
            myListen = true;
            fireShapeUpdated(inShape);
        }
    }
    
    
    /** Called to notify the editor that the point was deleted. */
    public void removePoint(int inIndex) {
        // remove the point from the multi point
        LineString tempLineString = (LineString) getShape();
        int tempNumPoints = tempLineString.getNumPoints();
        if (tempNumPoints > 0){
            if ((inIndex < 0) || (inIndex >= tempNumPoints)) inIndex = tempNumPoints-1;
            Point tempPoint = tempLineString.getPoint(inIndex);
            tempLineString.remove(inIndex);
            
            // remove the point from the table
            myListen = false;
            myTableModel.removeRow(inIndex);
            myListen = true;
            
            // I like a certain of quasi persistent selection
            if (myTableModel.getRowCount() > inIndex){
                myTable.setRowSelectionInterval(inIndex, inIndex);
            }
            
            // notify the listeners
            firePointRemoved(tempPoint);
        }
    }
    
    /** Called to notify the editor that the point was deleted. */
    public void removePoint(Point inPoint) {
        // find the point
        LineString tempLineString = (LineString) getShape();
        double[] tempXs = tempLineString.getXCoordinates();
        double[] tempYs = tempLineString.getYCoordinates();
        for (int i=0; i<tempXs.length; i++){
            if (inPoint.equals(tempXs[i], tempYs[i])){
                // remove the point from the multi point
                tempLineString.remove(i);
                
                // remove the point from the table
                myListen = false;
                myTableModel.removeRow(i);
                myListen = true;
                
                // I like a certain of quasi persistent selection
                if (myTableModel.getRowCount() > i){
                    myTable.setRowSelectionInterval(i, i);
                }
                
                // notify the listeners
                firePointRemoved(inPoint);
                break;
            }
        }
    }
    
    /** Called to notify the editor that the Point was moved.  */
    public void movePoint(int inIndex, Point inPoint) {
        if (inIndex != -1){
            // updatethe table
            myTableModel.setValueAt(""+inPoint.getX(), inIndex, 0);
            myTableModel.setValueAt(""+inPoint.getY(), inIndex, 1);
            
            // notify the listeners
            fireShapeUpdated(inPoint);
        }
    }
        
    private Point getPoint(int inIndex){
        LineString tempLineString = (LineString) getShape();
        return tempLineString.getPoint(inIndex);
    }
    
    /** Called to notify that the shape has radically changed, usually causes a reload.  */
    public void shapeUpdated(Shape inShape) {
        // update the values in the rows
        if (inShape instanceof LineString){
            LineString tempLineString = (LineString) inShape;
            if (tempLineString.getNumPoints() > 0) {
                myTableModel = new DefaultTableModel(myColumnNames, 0);
                myTable.setModel(myTableModel);
            }
            else{
                int tempDifference = myTableModel.getRowCount() - tempLineString.getNumPoints();
                myListen = false;
                if (tempDifference > 0){
                    for (int i=0; i<tempDifference; i++){
                        myTableModel.removeRow(0);
                    }
                }
                if (tempDifference < 0){
                    for (int i=0; i<Math.abs(tempDifference); i++){
                        String[] tempString = {"0","0"};
                        myTableModel.addRow(tempString);
                    }
                }
                myListen = true;
            }
            
            // update the table
            double[] tempXs = tempLineString.getXCoordinates();
            double[] tempYs = tempLineString.getYCoordinates();
            for (int i=0; i<tempXs.length; i++){
                myTableModel.setValueAt(""+tempXs[i], i, 0);
                myTableModel.setValueAt(""+tempYs[i], i, 0);
            }
        }
        super.setShape(inShape);
    }
        
    /** Called to notify that the point has been selected, helps keep this editor in synch with outside events.  */
    public void selectPoint(int inIndex) {
        // scroll the display to visible.
        if (inIndex != -1){
            LineString tempLineString = (LineString) getShape();
            Point tempPoint = tempLineString.getPoint(inIndex);
            myTable.setRowSelectionInterval(inIndex, 1);
            myTable.scrollRectToVisible(myTable.getCellRect(inIndex, 0, true));
            firePointSelected(tempPoint);
        }
    }
       
    /** notified when the value of the table changes.*/
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
        if ((myListen) && (!listSelectionEvent.getValueIsAdjusting())){
            int index = myTable.getSelectedRow();
            Point tempPoint = getPoint(index);
            if (tempPoint != null) firePointSelected(tempPoint);
        }
    }
    
    /** Called when changes to the tables happen, used to update the points. */
    public void tableChanged(javax.swing.event.TableModelEvent tableModelEvent) {
        if (getShape() != null){
            LineString tempLineString = (LineString) getShape();
            
            int tempfirst = tableModelEvent.getFirstRow();
            int tempLast = tableModelEvent.getLastRow();
            for (int i=tempfirst; i<=tempLast; i++){
                try{
                    double x = Double.parseDouble((String) myTableModel.getValueAt(i,0));
                    double y = Double.parseDouble((String) myTableModel.getValueAt(i,1));
                    tempLineString.setPoint(i, x, y);
                }
                catch(NumberFormatException e){
                }
            }
            
            fireShapeUpdated(getShape());
        }
    }    
    
    /** Testing only */
    public static void main(String[] inArgs){
        JFrame tempDialog = new JFrame("Testing MultiPoint Editor");
        LineStringEditor tempEditor = new LineStringEditor();
        Point[] tempPoints = new Point[5];
        tempPoints[0] = new Point(12345,54321);
        tempPoints[1] = new Point(1234.5,5432.1);
        tempPoints[2] = new Point(123.45,543.21);
        tempPoints[3] = new Point(12.345,54.321);
        tempPoints[4] = new Point(1.2345,5.4321);
        
        tempEditor.setShape(new LineString(tempPoints));
        tempDialog.setContentPane(tempEditor);
        tempDialog.pack();
        tempDialog.show();
    }    
}
