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
import java.util.*;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import gistoolkit.features.*;
import gistoolkit.display.*;

/**
 * Draw model to handle the creation of new shapes.
 */
public class NewShapeDrawModel extends SimpleDrawModel{
    
    /** Constant for drawing a Polygon */
    public static final int POLYGON = 0;
    /** Constant for drawing a Line */
    public static final int LINE = 1;
    /** Constant for drawing a Point */
    public static final int POINT = 2;
    
    /** What type of shape are we currently drawing */
    private int myShapeType = POLYGON;
    /** Set the type of shape this draw model is to create */
    public void setShapeType(int inShapeType){myShapeType = inShapeType; draw();}
    
    /** Array List to hold the posative points */
    private ArrayList myPosativeList = new ArrayList();
    
    /** Array List to hold the negative points */
    private ArrayList myNegativeList = new ArrayList();
    
    /** My Point List for keeping the points in the order in which they were added */
    private ArrayList myPointList = new ArrayList();
    
    /** Creates new NewShapeDrawModel */
    public NewShapeDrawModel() {
    }
    
    /** Command to notify when editing is done */
    private Command myCommand = null;
    
    /** Creates new NewShapeDrawModel with the given command*/
    public NewShapeDrawModel(Command inCommand) {
        myCommand = inCommand;
    }
    
    /** Last point added, used for draging */
    private Point myLastPoint = null;
    
    /** add a new point to the shape, listens to the gis editor.*/
    public void mousePressed(MouseEvent e){
        if (e.getClickCount() > 1) {
            if (myCommand != null) myCommand.executeDraw(this);
        }
        else{
            // some setup needs to have happened by this time.
            if (myGISDisplay == null) return;
            myGISDisplay.requestFocus();
            Converter tempConverter = myGISDisplay.getConverter();
            if (tempConverter == null) return;
            
            // create the point
            Point tempPoint = new Point(tempConverter.toWorldX(e.getX()), tempConverter.toWorldY(e.getY()));
            
            // if this is a point then add it.
            if (myShapeType == POINT){
                addPointToPoint(e, tempPoint);
            }
            if (myShapeType == LINE){
                addPointToLine(e, tempPoint);
            }
            if (myShapeType == POLYGON){
                addPointToPolygon(e, tempPoint);
            }
            draw();
        }
    }
    
    /** Drag the point */
    public void mouseDragged(MouseEvent e){
        if (myGISDisplay == null) return;
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter == null) return;
        if (myLastPoint != null){
            Point snappedPoint = null;
            if(e.getModifiers() == (MouseEvent.BUTTON1_MASK+MouseEvent.CTRL_MASK)) {
                snappedPoint = myGISDisplay.getSelectedLayer().getDataset().getSnappedPoint(e.getX(),e.getY(),tempConverter);
            }
            if (myLastPoint != null){
                if(snappedPoint != null) {
                    myLastPoint.setX(snappedPoint.getX());
                    myLastPoint.setY(snappedPoint.getY());
                }else{
                    myLastPoint.setX(tempConverter.toWorldX(e.getX()));
                    myLastPoint.setY(tempConverter.toWorldY(e.getY()));
                }
            }
            draw();
            if(snappedPoint != null) {
                drawSnappedPoint(snappedPoint);
            }
        }
    }
    
    /** Called when the point is added */
    private void addPoint(Point inPoint){
        myLastPoint = inPoint;
        myPointList.add(inPoint);
    }
    
    /** When the user clicks the mouse, add the point to the point or multipoint */
    private void addPointToPoint(MouseEvent inEvent, Point inPoint){
        if (myPosativeList.isEmpty()){
            myPosativeList.add(inPoint);
            addPoint(inPoint);
        }
        else{
            // if the controll key is not held down, then add the point and make it a multipoint
            if (!inEvent.isControlDown()){
                myPosativeList.add(inPoint);
                addPoint(inPoint);
            }
            // if the control key is held down, then modify the last point
            else{
                myPosativeList.remove(myPosativeList.size()-1);
                myPosativeList.add(inPoint);
                myPointList.remove(myPointList.size()-1);
                addPoint(inPoint);
            }
        }
    }
    
    /** When the user clicks the mouse, add the new point to the line or multi line */
    private void addPointToLine(MouseEvent inEvent, Point inPoint){
        if (inEvent.isControlDown()){
            if (myPosativeList.size() > 0){
                myPosativeList.remove(myPosativeList.size()-1);
                myPosativeList.add(inPoint);
                myPointList.remove(myPointList.size()-1);
                addPoint(inPoint);
            }
        }
        else{
            myPosativeList.add(inPoint);
            addPoint(inPoint);
        }
    }
    /** When the user clicks the mouse, add the new point to the polygon or multi polygon */
    private void addPointToPolygon(MouseEvent inEvent, Point inPoint){
        // if the controll is down, then modify the last point
        if (inEvent.isControlDown()){
            if (myLastPoint != null){
                if (myPosativeList.remove(myLastPoint)){
                    myPosativeList.add(inPoint);                
                }
                else if (myNegativeList.remove(myLastPoint)){
                    myNegativeList.add(inPoint);
                }
                myPointList.remove(myPointList.size()-1);                
                addPoint(inPoint);
            }
        }
        // if the shift key is down, then add this point to the hole
        else if (inEvent.isShiftDown()){
            myNegativeList.add(inPoint);
            addPoint(inPoint);
        }
        // if no modification keys are depressed, then add this point to the polygon.
        else{
            myPosativeList.add(inPoint);
            addPoint(inPoint);
        }
    }
     /**
     * The function which is called to draw the point on the screen.
     */
    private void drawSnappedPoint(Point inPoint) {
        // draw the shape on the buffer
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
    
    /** Draw the shape on the screen */
    public void draw(){
        if (myShapeType == POINT){
            drawPoint();
        }
        if (myShapeType == LINE){
            drawLine();
        }
        if (myShapeType == POLYGON){
            drawPolygon();
        }
    }
    
    /** Draw the Point */
    private void drawPoint(){
        // Draw the rest of the map on the buffer
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;
        if (myGISDisplay.getMapImage() == null) return;
        myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter == null) return;
        
        // draw the Points on the buffer
        if (myPosativeList.size() > 0){
            Graphics g = myGISDisplay.getBufferImage().getGraphics();
            for (int i=0; i<myPosativeList.size(); i++){
                Point tempPoint = (Point) myPosativeList.get(i);
                int x = tempConverter.toScreenX(tempPoint.getX());
                int y = tempConverter.toScreenY(tempPoint.getY());
                g.setColor(Color.black);
                g.fillOval(x-3, y-3, 8,8);
                g.setColor(Color.red);
                g.fillOval(x-3, y-3, 6,6);
            }
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
    }
    
    /** Draw the Line */
    private void drawLine(){
        // Draw the rest of the map on the buffer
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;
        if (myGISDisplay.getMapImage() == null) return;
        myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter == null) return;
        
        // draw the Points and lines on the buffer
        if (myPosativeList.size() > 0){
            Graphics g = myGISDisplay.getBufferImage().getGraphics();
            int tempLastX = 0;
            int tempLastY = 0;
            for (int i=0; i<myPosativeList.size(); i++){
                Point tempPoint = (Point) myPosativeList.get(i);
                int x = tempConverter.toScreenX(tempPoint.getX());
                int y = tempConverter.toScreenY(tempPoint.getY());
                g.setColor(Color.black);
                g.fillOval(x-3, y-3, 8,8);
                g.setColor(Color.red);
                g.fillOval(x-3, y-3, 6,6);
                if (i>0){
                    g.setColor(Color.red);
                    g.drawLine(x,y,tempLastX, tempLastY);
                }
                tempLastX = x;
                tempLastY = y;
            }
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
    }
    
    /** Draw the Polygon */
    private void drawPolygon(){
        // Draw the rest of the map on the buffer
        if (myGISDisplay == null) return;
        if (myGISDisplay.getBufferImage() == null) return;
        if (myGISDisplay.getMapImage() == null) return;
        myGISDisplay.getBufferImage().getGraphics().drawImage(myGISDisplay.getMapImage(), 0, 0, myGISDisplay);
        Converter tempConverter = myGISDisplay.getConverter();
        if (tempConverter == null) return;
        
        // draw the Polygon on the buffer
        if (myPosativeList.size() > 0){
            Graphics g = myGISDisplay.getBufferImage().getGraphics();
            int tempLastX = 0;
            int tempLastY = 0;
            for (int i=0; i<myPosativeList.size()+1; i++){
                Point tempPoint = null;
                if (i < myPosativeList.size()) tempPoint = (Point) myPosativeList.get(i);
                else tempPoint = (Point) myPosativeList.get(0);
                int x = tempConverter.toScreenX(tempPoint.getX());
                int y = tempConverter.toScreenY(tempPoint.getY());
                g.setColor(Color.black);
                g.fillOval(x-3, y-3, 8,8);
                g.setColor(Color.red);
                g.fillOval(x-3, y-3, 6,6);
                if (i>0){
                    g.setColor(Color.red);
                    g.drawLine(x,y,tempLastX, tempLastY);
                }
                tempLastX = x;
                tempLastY = y;
            }
        }
        
        // draw the Hole on the buffer
        if (myNegativeList.size() > 0){
            Graphics g = myGISDisplay.getBufferImage().getGraphics();
            int tempLastX = 0;
            int tempLastY = 0;
            for (int i=0; i<myNegativeList.size()+1; i++){
                Point tempPoint = null;
                if (i < myNegativeList.size()) tempPoint = (Point) myNegativeList.get(i);
                else tempPoint = (Point) myNegativeList.get(0);
                int x = tempConverter.toScreenX(tempPoint.getX());
                int y = tempConverter.toScreenY(tempPoint.getY());
                g.setColor(Color.black);
                g.fillOval(x-3, y-3, 8,8);
                g.setColor(Color.cyan);
                g.fillOval(x-3, y-3, 6,6);
                if (i>0){
                    g.setColor(Color.cyan);
                    g.drawLine(x,y,tempLastX, tempLastY);
                }
                tempLastX = x;
                tempLastY = y;
            }
        }
        
        // Draw the buffer on the display
        myGISDisplay.getGraphics().drawImage(myGISDisplay.getBufferImage(), 0, 0, myGISDisplay);
    }
    
    /** Retrieve the shape from the draw model */
    public Shape getShape(){
        if (myShapeType == POINT) return getShapePoint();
        if (myShapeType == LINE) return getShapeLine();
        if (myShapeType == POLYGON) return getShapePolygon();
        return null;
    }
    /** Return the point from the arrays */
    public Shape getShapePoint(){
        if (myPosativeList.size() == 0) return null;
        if (myPosativeList.size() == 1) return (Point) myPosativeList.get(0);
        Point[] tempPoints = new Point[myPosativeList.size()];
        myPosativeList.toArray(tempPoints);
        MultiPoint tempMultiPoint = new MultiPoint(tempPoints);
        return tempMultiPoint;
    }
    /** Return the line from the arrays */
    public Shape getShapeLine(){
        if (myPosativeList.size() == 0) return null;
        if (myPosativeList.size() == 1) return null;
        Point[] tempPoints = new Point[myPosativeList.size()];
        myPosativeList.toArray(tempPoints);
        LineString tempLineString = new LineString(tempPoints);
        return tempLineString;
    }
    /** Return the polygon from the arrrays */
    public Shape getShapePolygon(){
        if (myPosativeList.size() == 0) return null;
        if (myPosativeList.size() == 1) return null;

        // The posative part of the polygon
        Point[] tempPoints = new Point[myPosativeList.size()];
        myPosativeList.toArray(tempPoints);
        LinearRing tempPosativeRing = new LinearRing(tempPoints);
        
        // the Negative part of the polygon
        LinearRing[] tempHoles = null;
        if (myNegativeList.size() > 2){
            Point[] tempHolePoints = new Point[myNegativeList.size()];
            myNegativeList.toArray(tempHolePoints);
            LinearRing tempNegativeRing = new LinearRing(tempHolePoints);
            tempHoles = new LinearRing[1];
            tempHoles[0] = tempNegativeRing;
        }
        
        // create the polygon and return.
        Polygon tempPolygon = new Polygon(tempPosativeRing, tempHoles);
        return tempPolygon;
    }
    
    /** Called to finish drawing */
    public void done(){
    }
    
    /** Called when a key is pressed */
    public void keyPressed(java.awt.event.KeyEvent inKeyEvent){
        if ((inKeyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (inKeyEvent.getKeyCode() == KeyEvent.VK_DELETE)){
            if (myPointList.size() > 0){
                Point tempPoint = (Point) myPointList.get(myPointList.size()-1);
                myPosativeList.remove(tempPoint);
                myNegativeList.remove(tempPoint);
                myPointList.remove(tempPoint);
                if (myPointList.size() > 0){
                    myLastPoint = (Point) myPointList.get(myPointList.size()-1);
                }
                else{
                    myLastPoint = null;
                }
                draw();
                
            }
        }
        if ((inKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) || (inKeyEvent.getKeyCode() == KeyEvent.VK_ACCEPT)){
            myCommand.executeDraw(this);
        }
    }
    
    /** Reset the draw model to the initial state */
    public void reset(){
        myNegativeList.clear();
        myPointList.clear();
        myPosativeList.clear();
        myLastPoint = null;
        draw();
    }
}
