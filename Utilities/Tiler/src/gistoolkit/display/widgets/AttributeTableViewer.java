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

package gistoolkit.display.widgets;

import java.util.Vector;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import gistoolkit.features.*;

/**
 * Allows all the records in the dataset to be viewed.
*/
public class AttributeTableViewer extends JPanel implements ListSelectionListener{
    /** Holds a reference to the records so they can be returned when the event is called. */
    private Record[] myRecords = null;
    
    /** Table to hold all of the attributes. */
    private JTable myTable = null;
    
    /** The table model. */
    private DefaultTableModel myTableModel =null;
    
    /** Creates new TableViewer */ 
    public AttributeTableViewer() {
        initPanel();
    }
    
    /** Holds the list of interested observers. */
    private Vector myAttributeTableViewerListeners = new Vector();
    /** Add tihs observer to the list of interested observers.  It will be notified when a row is selected. */
    public void addAttributeTableViewerListener(AttributeTableViewerListener inListener){if (inListener != null) myAttributeTableViewerListeners.addElement(inListener);}
    /** Remove this observer from the list of observers that will be notified when a row is selected. */
    public void removeAttributeTableViewerListener(AttributeTableViewerListener inListener){if (inListener != null) myAttributeTableViewerListeners.remove(inListener);}
    /** Fire Row Selected */
    private void fireRowSelected(int inIndex){
        if (myRecords != null){
            if (inIndex < myRecords.length){
                if ((inIndex < myRecords.length) && (inIndex >=0)){
                    for (int i=0; i<myAttributeTableViewerListeners.size(); i++){
                        AttributeTableViewerListener tempListener = (AttributeTableViewerListener) myAttributeTableViewerListeners.elementAt(i);
                        tempListener.recordSelected(myRecords[inIndex]);
                    }
                }
            }
        }
    }
    
    /** Set up the visual display for this panel. */
    private void initPanel(){
        // Set the table as the cneter component
        setLayout(new BorderLayout());
        myTable = new JTable();
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane tempScrollPane = new JScrollPane(myTable);
        add(tempScrollPane, BorderLayout.CENTER);
        myTable.getSelectionModel().addListSelectionListener(this);
    }
    
    /** Set the Record Set. */
    public void setRecords(Record[] inRecords){
        if ((inRecords != null) && (inRecords.length > 0)){
            // Save the records for reference when the fire is called.
            myRecords = inRecords;
            
            // Create the titles
            String[] tempNames = inRecords[0].getAttributeNames();
            myTableModel  = new DefaultTableModel(tempNames, 0);
            for (int i=0; i<inRecords.length; i++){
                myTableModel.addRow(inRecords[i].getAttributes());
            }
            myTable.setModel(myTableModel);
        }
    }
    
    /** notified when the value of the table changes.*/
    public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting()){
            fireRowSelected(myTable.getSelectedRow());
        }
    }
 
}
