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

import gistoolkit.display.renderer.*;
import java.util.Vector;
import java.awt.Graphics;
import java.awt.event.*;
import gistoolkit.display.*;
import gistoolkit.features.*;
/**
 * A model used for highlighting features on the map as the pointer is moved over it.
 * Creation date: (4/18/2001 3:20:16 PM)
 */
public class EditNodesDrawModel	extends SimpleDrawModel{
    
    /** Modes in which this draw model may operate.*/
    public static int MOVE = 0;
    public static int ADD = 1;
    public static int DELETE = 2;
    public static int MOVE_SHAPE = 3;
    
    /** The current mode of this draw model. */
    private int myMode = MOVE;
    
    /** The last selected object.  It is used to optimize the screen redraw. */
    private Record myRecord = null;
    
    /** Shader to provide the theme for shading the highlighted shapes.*/
    private Shader myShader = null;
    
    /** Point to use when moving.  Used in the drag routine.*/
    private Point myStartPoint = null;
    
    /** Index of the point to move. */
    private int myMoveIndex = -1;
    
    /** Layer from which the record came so it may be updated with the done() method.*/
    private Layer myLayer = null;
    
    /** EditNodesDrawModelListeners that have registered interest in these events. */
    private Vector myVectListeners = new Vector();
    /** Add a listener to this draw model to recieve events when they happen. */
    public void addEditNodesDrawModelListener(EditNodesDrawModelListener inListener){myVectListeners.addElement(inListener);}
    /** Remove this listsner. */
    public void removeEditNodesDrawModelListener(EditNodesDrawModelListener inListener){myVectListeners.remove(inListener);}
    /** Fire an event when a point is added. */
    protected void firePointAdded(Point inPoint){
        if (myVectListeners.size() == 0) return;
        fireEvent(new EditNodesDrawModelEvent(EditNodesDrawModelEvent.POINT_ADDED, myRecord.getShape(), inPoint));
    }
    /** Fire an event when a point is moved. */
    protected void firePointMoved(Point inPoint){
        if (myVectListeners.size() == 0) return;
        fireEvent(new EditNodesDrawModelEvent(EditNodesDrawModelEvent.POINT_MOVED, myRecord.getShape(), inPoint));
    }
    /** Fire an event when a point is deleted. */
    protected void firePointRemoved(Point inPoint){
        if (myVectListeners.size() == 0) return;
        fireEvent(new EditNodesDrawModelEvent(EditNodesDrawModelEvent.POINT_REMOVED, myRecord.getShape(), inPoint));
    }

    /** notify the listeners of an event */
    protected void fireEvent(EditNodesDrawModelEvent inEvent){
        for (int i=0; i<myVectListeners.size(); i++){
            ((EditNodesDrawModelListener) myVectListeners.elementAt(i)).shapeUpdated(inEvent);
        }
    }
    
    /** Create a new EditNodesDrawModel.  This constructor does not attach to a command. */
    public EditNodesDrawModel() {
        super();
    }
    
    /** Create a new EditNodesDrawModel, with a connection to the command.*/
    public EditNodesDrawModel(Command inCommand) {
        super();
        myCommand = inCommand;
    }
    
    /**
     * Ends the editing of the polygon, and updates the record.
     */
    public void done() throws Exception{
        if (myLayer != null){
            myLayer.update(myRecord);
            myLayer = null;
            myRecord = null;
            myStartPoint = null;
            myMoveIndex = -1;
            if (myCommand != null) myCommand.executeDraw(this);
            myGISDisplay.update(myGISDisplay.getGraphics());
            draw();
        }
    }
    
    /**
     * The function which is called to draw the image on the map.
     */
    public void draw() {
        // draw the map on the buffer
        if ((myGISDisplay.getBufferImage() != null) && (myGISDisplay.getMapImage() != null)){
            myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        }
        
        // draw the shape on the buffer
        if (myRecord != null){
            if (myRecord.getShape() != null){
                FeatureRenderer tempRenderer = new FeatureRenderer();
                
                // Draw the shape
                tempRenderer.drawShapeHighlight(myRecord, myGISDisplay.getBufferImage().getGraphics(), myGISDisplay.getConverter(), myShader);
                tempRenderer.drawShapePoints(myRecord, myGISDisplay.getBufferImage().getGraphics(), myGISDisplay.getConverter(), myShader);
            }
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
    }
    
    /**
     * The function which is called to draw the point on the screen.
     */
    public void drawPoint(Point inPoint) {
        
        // draw the shape on the buffer
        if (myRecord != null){
            if (myRecord.getShape() != null){
                Converter c = myGISDisplay.getConverter();
                int x = c.toScreenX(inPoint.getX());
                int y = c.toScreenY(inPoint.getY());
                if ((x > 0) && (y > 0) && (x < myGISDisplay.getWidth()) && (y < myGISDisplay.getHeight())){
                    Graphics g = myGISDisplay.getBufferImage().getGraphics();
                    g.setColor(java.awt.Color.cyan);
                    g.fillOval(x-5, y-5, 10,10);
                    g.setColor(java.awt.Color.black);
                    g.drawOval(x-5, y-5, 10,10);
                    
                    // Draw the buffer on the display
                    myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
                }
            }
        }
    }
    
    /** Returns the layer associated with the record.*/
    public gistoolkit.display.Layer getLayer() {
        return myLayer;
    }
    
    /** Get the state of the model, Add = add nodes, Move = move nodes, Delete = delete nodes.*/
    public int getMode(){
        return myMode;
    }
    
    /** Returns the last selected record.  If there is not record selected, then it returns null.*/
    public Record getRecord(){
        return myRecord;
    }
    
    /** Determines if the draw model has a selection to operate on. */
    public boolean isSelected() {
        if (myRecord != null) return true;
        return false;
    }
    
    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     */
    public void mouseDragged(MouseEvent e) {
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter != null){
            double tempClickX = tempConverter.toWorldX(e.getX());
            double tempClickY = tempConverter.toWorldY(e.getY());
            
            if (myMode == MOVE_SHAPE){
                if (myStartPoint != null){
                    if (myRecord != null){
                        Shape tempShape = myRecord.getShape();
                        if (tempShape != null){
                            tempShape.translate(tempClickX-myStartPoint.getX(), tempClickY-myStartPoint.getY());
                            firePointMoved(null);
                            myStartPoint = new Point(tempClickX, tempClickY);
                        }
                    }
                }
            }
            Point snappedPoint = null;
            if ((myMode == MOVE) || (myMode == ADD)){
                Shape tempShape = myRecord.getShape();
                if(e.getModifiers() == (MouseEvent.BUTTON1_MASK+MouseEvent.CTRL_MASK)) {
                    snappedPoint = myGISDisplay.getSelectedLayer().getDataset().getSnappedPoint(tempClickX,tempClickY,tempConverter);
                }
                if (myMoveIndex >-1){
                    if(snappedPoint != null) {
                        tempShape.setPoint(myMoveIndex, snappedPoint.getX(), snappedPoint.getY());
                    }else{
                        tempShape.setPoint(myMoveIndex, tempClickX, tempClickY);
                    }
                    firePointMoved(myStartPoint);
                }
            }
            draw();
            if(snappedPoint != null) {
                drawPoint(snappedPoint);
            }
        }
    }
    
    /**
     * Called when the user presses the mouse button.
     */
    public void mousePressed(MouseEvent e) {
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter != null){
            double tempClickX = tempConverter.toWorldX(e.getX());
            double tempClickY = tempConverter.toWorldY(e.getY());
            if (myRecord == null) return;
            Shape tempShape = myRecord.getShape();
            if (tempShape != null){
                
                // if this type is in ADD mode, then add the point.
                if (myMode == ADD) {
                    
                    myMoveIndex = tempShape.add(tempClickX, tempClickY);
                    firePointAdded(tempShape.getPoint(myMoveIndex));
                }
                if (myMode == MOVE) {
                    myMoveIndex = tempShape.getClosestIndex(tempClickX, tempClickY);
                    if(myMoveIndex > -1){
                        tempShape.setPoint(myMoveIndex, tempClickX, tempClickY);
                        firePointMoved(tempShape.getPoint(myMoveIndex));
                    }
                }
                if (myMode == MOVE_SHAPE) {
                    myStartPoint = new Point(tempClickX, tempClickY);
                }
                if (myMode == DELETE) {
                    
                    // delete the node closest to this click location.
                    int tempIndex = tempShape.getClosestIndex(tempClickX, tempClickY);
                    Point tempPoint = tempShape.getPoint(tempIndex);
                    if (tempShape.remove(tempIndex)){
                        firePointRemoved(tempPoint);
                    }
                }
                draw();
            }
        }
    }
    
    /**
     * Called when the user releases the mouse butotn.
     */
    public void mouseReleased(MouseEvent e){
        myStartPoint = null;
    }
    
    /**
     * Sets the layer that goes along with the record.
     * Creation date: (4/27/2001 4:08:05 PM)
     * @param newLayer gistoolkit.display.Layer
     */
    public void setLayer(gistoolkit.display.Layer newLayer) {
        myLayer = newLayer;
    }
    
    /**
     * Set the state of the model, Add = add nodes, Move = move nodes, Delete = delete nodes.
     */
    public void setMode(int inState){
        myMode = inState;
        if (inState == DELETE){
            myStartPoint = null;
        }
    }
    
    /**
     * Returns the last selected record.  If there is not record selected, then it returns null.
     */
    public void setRecord(Record inRecord ){
        myRecord = inRecord;
    }
    
    /**
     * Set the shader used for shading the highlighted shapes.
     */
    public void setShader(Shader inShader){
        myShader = inShader;
    }
}