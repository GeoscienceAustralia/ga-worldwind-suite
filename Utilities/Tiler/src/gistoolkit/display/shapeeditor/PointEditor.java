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
import java.text.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.*;
import gistoolkit.features.*;

/**
 * Provides the user interface that allows a point to be edited.
 */
public class PointEditor extends ShapeEditor implements ActionListener{
    
    /** A TextField for entering the Value of X. */
    private JFormattedTextField myXField = null;
    /** A TextField for entering the Value of Y. */
    private JFormattedTextField myYField = null;
    
    /** Creates new PointEditor */
    public PointEditor() {
        initPanel();
    }
    
    /** Initialize this panel  */
    private void initPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        
        // The X coordinate
        c.gridx = 0;
        c.gridy++;
        JLabel tempLabel = new JLabel("X=");
        add(tempLabel, c);
        myXField = new JFormattedTextField(new DecimalFormat("#.##########"));
        myXField.addActionListener(this);
        c.gridx++;
        c.weightx = 1;
        add(myXField, c);
        
        // The Y coordinate
        c.gridx++;
        c.weightx = 0;
        tempLabel = new JLabel("Y=");
        add(tempLabel, c);
        myYField = new JFormattedTextField(new DecimalFormat("#.##########"));
        myYField.addActionListener(this);
        c.gridx++;
        c.weightx = 1;
        add(myYField, c);
        
        // Something to take up space
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.weighty = 1;
        add(new JPanel(), c);
    }
    
    /** Set the Point into the panel */
    public void setShape(Shape inShape){
        super.setShape(inShape);
        if (inShape instanceof Point){
            Point tempPoint = (Point) inShape;
            myXField.setText(""+tempPoint.getX());
            myYField.setText(""+tempPoint.getY());
        }
    }
    
    /** Get the Point from the panel. */
    public Shape getShape(){
        if (super.getShape() instanceof Point){
            Point tempPoint = (Point) super.getShape();
            tempPoint.setX(Double.parseDouble(myXField.getText()));
            tempPoint.setY(Double.parseDouble(myYField.getText()));
        }
        return super.getShape();
    }
    
    /** Testing only */
    public static void main(String[] inArgs){
        JFrame tempDialog = new JFrame("Testing Point Editor");
        PointEditor tempEditor = new PointEditor();
        tempEditor.initPanel();
        tempEditor.setShape(new Point(20502.122, 0.1234));
        tempDialog.setContentPane(tempEditor);
        tempDialog.show();
    }
    
    /** Called to notify the editor that the point was deleted. */
    public void removePoint(Point inPoint) {
        // Do nothing here, if a point is removed, the shape will be deleted.
    }
    
    /** Called to notify the editor that the moint was moved.  */
    public void movePoint(int inIndex, Point inPoint) {
        // update the point coordinates.
        if (inPoint == getShape()){
            myXField.setText(""+inPoint.getX());
            myYField.setText(""+inPoint.getY());
            fireShapeUpdated(inPoint);
        }
    }
    
    /** Called to notify the editor that the point was added. */
    public void addPoint(Point inPoint) {
        // can't add more than one point to a point.
    }
    
    /** Called to notify that the point has been deselected, helps to keep this editor in synch with outside events.  */
    public void deselectedPoint(Point inPoint) {
        // there is only one point in question
        if (inPoint == getShape()){
            firePointDeselected(inPoint);
        }
    }
    
    /** Called to notify that the point has been selected, helps keep this editor in synch with outside events.  */
    public void selectPoint(int inIndex) {
        // the only point is always selected.
        if (inIndex == 0){
            firePointSelected((Point) getShape());
        }
    }
    
    private void updatePoint() {
        if (getShape() != null){
            Point tempPoint = (Point) getShape();
            try{
                double x = Double.parseDouble(myXField.getText());
                double y = Double.parseDouble(myYField.getText());
                tempPoint.setX(x);
                tempPoint.setY(y);
            }
            catch(NumberFormatException e){
            }
            
            fireShapeUpdated(getShape());
        }
    }
    
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        updatePoint();
    }
    
}
