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

import java.awt.*;
import javax.swing.*;
import gistoolkit.features.*;
import gistoolkit.display.*;
import gistoolkit.display.widgets.attrieditors.*;

/**
 * Panel for editing the values of the attributes of the dataset sent in.
 */
public class AttributePanel extends JPanel {
    
    // Text Area for displaying the text information.
    private JTextArea myTextArea = new JTextArea();
    
    /** Creates new EditCommandAttributePanel */
    public AttributePanel() {
    }
    
    /** Keep a pointer to the record sent in so it can be returned after it has been edited */
    public Record myRecord = null;
    
    /** Array for the editors to be set to use them appropriately */
    public AttributeEditor[] myEditors = null;
    
    /** Set the Record whose attributes are to be edited */
    public void setRecord(Record inRecord, Layer inLayer){
        // remove all previous elements from the panel
        removeAll();
        
        // create a Panel for Adding controls to
        JPanel tempPanel = new JPanel();
                        
        // Validate the data
        myRecord = inRecord;
        if (inRecord == null) return;
        Object[] tempAttributes = inRecord.getAttributes();
        AttributeType[] tempAttributeTypes = inRecord.getAttributeTypes();
        String[] tempNames = inRecord.getAttributeNames();
        if (tempAttributes == null) return;
        if (tempNames == null) return;
        myEditors = new AttributeEditor[tempNames.length];
        
        // Populate the panel with the new stuff
        tempPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        
        for (int i=0; i<tempNames.length; i++){
            
            // add the label
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            JLabel tempLabel = new JLabel(tempNames[i]);
            tempPanel.add(tempLabel, c);

            // add the editor
            c.gridx = 1;
            c.weightx = 1;
            AttributeEditor tempEditor = getAttributeEditor(tempAttributeTypes[i], tempAttributes[i]);
            if (tempEditor instanceof Component){
                tempPanel.add( (Component) tempEditor, c);
            }
            myEditors[i] = tempEditor;
        }
        
        // add a panel at the bottom to push everything to the top.
        c.gridy++;
        c.weighty = 1;
        tempPanel.add(new JPanel(), c);

        // create a scroll pane for scrolling the above panel
        JScrollPane tempScrollPane = new JScrollPane(tempPanel);
        
        // Add the scroll
        setLayout(new BorderLayout());
        add(tempScrollPane, BorderLayout.CENTER);
    }
    
    /** Retrieve the edited record from the panel */
    public Record getRecord(){
        
        // update the record with the data in the editors
        if (myEditors != null){
            Object[] tempAttributes = myRecord.getAttributes();
            for (int i=0; i<myEditors.length; i++){
                tempAttributes[i] = myEditors[i].getAttribute();
            }
        }
        return myRecord;
    }
    
    /** Retrieve an appropriate attribute editor for this type */
    public AttributeEditor getAttributeEditor(AttributeType inType, Object inObject){
        AttributeEditor tempAttributeEditor = null;
        try{
            if(inType == null) {
                tempAttributeEditor = new SimpleStringEditor();
            }else   // boolean
            if (inType.getType() == AttributeType.BOOLEAN){
                tempAttributeEditor = new SimpleBooleanEditor();
            }else   // strings
            if (inType.getType() == AttributeType.STRING){
                tempAttributeEditor = new SimpleStringEditor();
            }else   // double
            if (inType.getType() == AttributeType.FLOAT){
                tempAttributeEditor = new SimpleDoubleEditor();
            }else   // integers
            if (inType.getType() == AttributeType.INTEGER){
                tempAttributeEditor = new SimpleIntegerEditor();
            }else   // short
            if (inType.getType() == AttributeType.TIMESTAMP){
                tempAttributeEditor = new SimpleDateEditor();
            }else {
                System.out.println("AttributePanel: UnknownType"+inType.getType());
                tempAttributeEditor = new SimpleEditor();
            }
            tempAttributeEditor.setAttribute(inObject);
        }
        catch (Exception e){
            System.out.println("AttributePanel: Exception");
            e.printStackTrace();
        }
        return tempAttributeEditor;
    }

    /** Retrieve an appropriate attribute editor for this object */
    public AttributeEditor getAttributeEditor(Object inObject){
        AttributeEditor tempAttributeEditor = null;
        try{
            if(inObject == null) {
                tempAttributeEditor = new SimpleStringEditor();
            }else   // boolean
            if (inObject instanceof Boolean){
                tempAttributeEditor = new SimpleBooleanEditor();
            }else   // strings
            if (inObject instanceof String){
                tempAttributeEditor = new SimpleStringEditor();
            }else   // double
            if (inObject instanceof Double){
                tempAttributeEditor = new SimpleDoubleEditor();
            }else   // float
            if (inObject instanceof Float){
                tempAttributeEditor = new SimpleFloatEditor();
            }else   // integers
            if (inObject instanceof Integer){
                tempAttributeEditor = new SimpleIntegerEditor();
            }else   // short
            if (inObject instanceof Short){
                tempAttributeEditor = new SimpleShortEditor();
            }else   // date
            if (inObject instanceof java.util.Date){
                tempAttributeEditor = new SimpleDateEditor();
            }else {
                System.out.println("AttributePanel: "+inObject.getClass());
                tempAttributeEditor = new SimpleEditor();
            }
            tempAttributeEditor.setAttribute(inObject);
        }
        catch (Exception e){
            System.out.println("Exception Should never happen");
            e.printStackTrace();
        }
        return tempAttributeEditor;
    }
}
