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

package gistoolkit.display;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.features.featureutils.*;
import gistoolkit.projection.*;
/**
 * The GISDisplay is the parent panel for displaying the contents of the data source.
 * It is populated with layers which contain all the information and are responsable for
 * drawing the contents of the datasets they recieve from the datasources.
 */
public class GISDisplay extends JPanel{
    /**
     * Image for doing double buffering.
     */
    private Image myBufferImage;
    
    /**
     * Image for drawing the map
     */
    private Image myMapImage;
    
    /**
     * Layer to draw on the map
     */
    private Layer[] myLayers = new Layer[0];
    
    /**
     * The current bounds of the map.
     */
    private Envelope myEnvelope = null;
    
    /**
     * Internal class to listen for component events.
     */
    private class ComponentHandler implements ComponentListener, KeyListener{
        public void componentShown(ComponentEvent e) {
            redraw();
        }
        public void componentResized(ComponentEvent e) {
            try{
                setEnvelope(myEnvelope);
            }
            catch (Exception ex){
            }
        }
        public void componentHidden(ComponentEvent e) {
        }
        public void componentMoved(ComponentEvent e) {
            redraw();
        }
        
        /** Keyboard Messages */
        public void keyReleased(java.awt.event.KeyEvent ke) {
        }
        
        public void keyPressed(java.awt.event.KeyEvent ke) {
            // handle the escape key
            if(ke.getKeyCode() == KeyEvent.VK_ESCAPE){
                if (myDrawModel != null){
                    myDrawModel.reset();
                }
            }
        }
        
        public void keyTyped(java.awt.event.KeyEvent ke) {
        }
    }
    private ComponentHandler myComponentHandler;
    
    /**
     * internal class to handle asynchronous drawing.
     */
    private class AsyncDraw extends Thread{
        public void run(){
            Component tmpRootCmp = SwingUtilities.getRoot((Component)getParent());
            if(tmpRootCmp != null) {
                tmpRootCmp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            try{
                drawLayers(getGraphics());
                drawBufferImage(getGraphics());
                paint(getGraphics());
            }
            catch (Exception e){
                System.out.println("Exception in Async Draw "+e);
                e.printStackTrace();
            }
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if(tmpRootCmp != null) {
                tmpRootCmp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            if (myRedraw){
                myRedraw = false;
                run();
            }
            myAsyncDraw = null;
        }
    }
    private AsyncDraw myAsyncDraw;
    
    /**
     * Some optimization to kill the multiple threads stacking up.
     */
    private boolean myIsDrawing = false;
    
    /**
     * Draw model for drawing the graphics image
     */
    private DrawModel myDrawModel = null;
    
    /**
     * Converter for converting from world coordinates to screen coordinates.
     */
    private Converter myConverter = null;
    
    /**
     * GISDisplay constructor comment.
     */
    public GISDisplay() {
        super();
        myComponentHandler = new ComponentHandler();
        addComponentListener(myComponentHandler);
        addKeyListener(myComponentHandler);
    }
    
    /**
     * Method to use to set the layers on the map.
     * Adds the new layer on top of the other layers.
     */
    public void addLayer(Layer inLayer) throws Exception{
        if (inLayer == null) return;
        if (myProjection != null) inLayer.setProjection(myProjection, true);
        Layer[] tempLayers = new Layer[myLayers.length + 1];
        
        for (int i = 0; i < tempLayers.length; i++) {
            if (i == myLayers.length)
                tempLayers[i] = inLayer;
            else
                tempLayers[i] = myLayers[i];
        }
        myLayers = tempLayers;
        if (myEnvelope == null) setEnvelope(null);
        
        // set this layer as the selected one.
        setSelectedLayer(inLayer);
        
        // redraw the display
        update(getGraphics());
    }
    
    private String myKey = "Key";
    private boolean myRedraw = false; // redraw.
    /**
     * Draws the layers asynchronously.
     */
    public void drawLayersAsync(){
        synchronized(myKey){
            if (myAsyncDraw == null){
                myAsyncDraw = new AsyncDraw();
                myAsyncDraw.start();
            }
            else{
                myRedraw = true;
            }
        }
        return;
    }

    /** Scale bar to use with the map. */
    private ScaleBar myScaleBar = null;
    /** Set the scale bar to use with the map, use null for no scalebar. */
    public void setScaleBar(ScaleBar inScaleBar){myScaleBar = inScaleBar;}
    /** Get the currently employed scale bar for this may, may return null if no scalebar is set. */
    public ScaleBar getScaleBar(){return myScaleBar;}
    
    /**
     * Draw the current layers on the background image.
     */
    private synchronized void drawLayers(Graphics inGraphics) throws Exception{
        // if the current Envelope are null, then read them from the layers
        if (myEnvelope == null){
            EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
            for (int i=0; i<myLayers.length; i++){
                tempEnvelopeBuffer.expandToInclude(myLayers[i].getEnvelope());
            }
            myEnvelope = tempEnvelopeBuffer.getEnvelope();
        }
        if (myEnvelope == null) return;
        
        // create the converter to convert from world coordinates to screen coordinates.
        Envelope tempScreenEnvelope =
        new Envelope(0, 0, getSize().width, getSize().height);
        Converter tempConverter = new Converter(tempScreenEnvelope, myEnvelope);
        
        // create the buffer image
        if (getSize().width > 0){
            myBufferImage = createImage(getSize().width, getSize().height);
        }
        
        if (myBufferImage == null)
            return;
        
        // create the MapImage
        if ((getSize().width > 0))
            myMapImage = createImage(getSize().width, getSize().height);
        
        if (myMapImage == null)
            return;
        
        // retrieve the graphics object from the map image
        Graphics tempGraphics;
        tempGraphics = myMapImage.getGraphics();
        tempGraphics.setColor(getBackground());
        tempGraphics.fillRect(0,0,getSize().width, getSize().height);
        
        try{
            
            // loop through the layers drawing each one.
            for (int i = 0; i < myLayers.length; i++) {
                Layer tempLayer = myLayers[i];
                tempLayer.drawLayer(tempGraphics, tempConverter);
            }
            
            // loop through the layers labeling each one.
            for (int i = 0; i < myLayers.length; i++) {
                Layer tempLayer = myLayers[i];
                tempLayer.labelLayer(tempGraphics, tempConverter);
            }
            
            // draw the scale bar
            if (myScaleBar != null){
                myScaleBar.drawScale(tempGraphics, tempConverter, getWidth(), getHeight());
            }
        }
        catch (Throwable t){
            System.out.println("Error in draw layers");
            System.out.println(t);
            t.printStackTrace(System.out);
        }
        repaint();
    }
    
    /**
     * Retrieves the image used for double buffering the display.
     */
    public Image getBufferImage(){
        return myBufferImage;
    }
    
    /**
     * set the converter, called from the drawLayers routine.
     */
    public Converter getConverter(){
        if (myConverter == null){
            if (myEnvelope != null){
                Envelope tempScreenEnvelope = new Envelope(0, 0, getSize().width, getSize().height);
                myConverter = new Converter(tempScreenEnvelope, myEnvelope);
            }
        }
        return myConverter;
    }
    
    /**
     * Retireves the Envelope of the current map.
     */
    public Envelope getEnvelope(){
        if (myEnvelope != null) return (Envelope) myEnvelope.clone();
        return null;
    }
    
    /**
     * Get the default bounds from all of the layers.  This will be the size of the map
     * when it is zoomed out to just the size needed to view all the items.
     */
    private Envelope getLayerEnvelope() throws Exception{
        
        // loop through all the layers getting the maximum and minimum bounds
        EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
        for (int i = 0; i < myLayers.length; i++) {
            tempEnvelopeBuffer.expandToInclude(myLayers[i].getEnvelope());            
        }
        return tempEnvelopeBuffer.getEnvelope();
    }
    
    /**
     * method to use to set the layers on the map
     */
    public Layer[] getLayers() {
        return myLayers;
    }
    
    /**
     * Retrieves the image used for double buffering the display.
     */
    public Image getMapImage(){
        return myMapImage;
    }
    
    /**
     * Creates the onscreen display components for this component.
     * Creation date: (4/13/2001 3:16:06 PM)
     */
    public void initPanel() {
    }
    
    /**
     * Draw the graphics on the screen.
     */
    public void paint(Graphics g) {
        //super.paint(g);
        if (isVisible()&&isShowing()&&isEnabled()){
            if (myBufferImage != null){
                g.drawImage(myBufferImage, 0, 0, this);
            }
        }
    }
    
    /** Causes the display to refresh the data from the layers. */
    public void redraw(){
        update(getGraphics());
    }
    
    /**
     * set the converter, called from the drawLayers routine.
     */
    private void setConverter(Converter inConverter){
        myConverter = inConverter;
    }
    
    /** Returns a reference to the currently selected draw model */
    public DrawModel getDrawModel(){
        return myDrawModel;
    }
    
    /**
     * Sets the draw model for this item.  If the draw model implements mouse listeners, then they are registered.
     */
    public void setDrawModel(DrawModel inDrawModel){
        // remove the old draw model
        if(myDrawModel != null){
            if (myDrawModel instanceof MouseListener){
                removeMouseListener((MouseListener)myDrawModel);
            }
            if (myDrawModel instanceof MouseMotionListener){
                removeMouseMotionListener((MouseMotionListener) myDrawModel);
            }
            if (myDrawModel instanceof KeyListener){
                removeKeyListener((KeyListener) myDrawModel);
            }
        }
        if (inDrawModel != null){
            inDrawModel.setGISDisplay(this);
            if (inDrawModel instanceof MouseListener){
                addMouseListener((MouseListener)inDrawModel);
            }
            if (inDrawModel instanceof MouseMotionListener){
                addMouseMotionListener((MouseMotionListener) inDrawModel);
            }
            if (inDrawModel instanceof KeyListener){
                addKeyListener((KeyListener) inDrawModel);
            }
        }
        
        // set the draw model even if it is null
        if (myDrawModel != null) myDrawModel.remove();
        myDrawModel = inDrawModel;
        requestFocus();
    }
    
    /**
     * Sets the Envelope of the map, causing a requery from the database.
     */
    public synchronized void setEnvelope(Envelope inEnvelope) throws Exception{
        if (inEnvelope == null){
            EnvelopeBuffer tempEnvelopeBuffer = new EnvelopeBuffer();
            for (int i=0; i<myLayers.length; i++){
                tempEnvelopeBuffer.expandToInclude(myLayers[i].getEnvelope());
            }
            inEnvelope = tempEnvelopeBuffer.getEnvelope();
        }
        
        if (myEnvelope != null){
            if ((getWidth() > 0) && (getHeight() > 0)){
                // correct the Envelope so they are the correct shape
                Envelope tempScreenEnvelope = new Envelope(0,0,getWidth(), getHeight());
                Converter tempConvert = new Converter(tempScreenEnvelope, inEnvelope);

                // calculate the new Envelope
                double tempTopX = tempConvert.toWorldX(-1);
                double tempTopY = tempConvert.toWorldY(-1);
                double tempBottomX = tempConvert.toWorldX(getWidth()+1);
                double tempBottomY = tempConvert.toWorldY(getHeight()+1);

                // set the new Envelope
                myEnvelope = new Envelope(tempTopX, tempTopY, tempBottomX, tempBottomY);
                myConverter = new Converter(tempScreenEnvelope, myEnvelope);
            }
        }
        fireEnvelopeChanged(inEnvelope);
        redraw();
    }
    

    /**
     * overloaded update method for drawing to the screen.
     * <p>
     * This is where the majority of the visual work is done.
     *
     **/
    public void update(Graphics g) {
        if (g == null) return;
        try{
            drawLayersAsync();
//            drawBufferImage(g);
//            paint(g);
        }
        catch (Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    /** Draw the map image on the buffered image. */
    private void drawBufferImage(Graphics g){
        if (myDrawModel != null){
            if (isVisible()&&isShowing()&&isEnabled()){
                myDrawModel.draw();
            }
        }
        else{
            if ((myBufferImage != null)&&(myMapImage != null)){
                //super.paint(myBufferImage.getGraphics());
                if (myMapImage != null){
                    myBufferImage.getGraphics().drawImage(myMapImage, 0,0,this);
                }
                paint(g);
                //                g.drawImage(myBufferImage, 0, 0, this);
            }
            else{
                super.paint(g);
            }
        }
    }
    
    /** The currently selected layer */
    private Layer mySelectedLayer = null;
    /** Set the curently selected layer */
    public void setSelectedLayer(int inIndex){
        if (inIndex < myLayers.length) mySelectedLayer = myLayers[inIndex];
    }
    /** Set the curently selected layer */
    public void setSelectedLayer(Layer inLayer){
        for (int i=0; i<myLayers.length; i++){if (myLayers[i] == inLayer){ setSelectedLayer(i); break;}}
    }
    /** get the currently selected layer */
    public Layer getSelectedLayer(){return mySelectedLayer;}
    
    /**
     * method to use to set the layers on the map
     */
    public synchronized void moveLayerDown(Layer inLayer) {
        if (inLayer == null) return;
        
        // swap this layer with the one below it.
        for (int i = 1; i < myLayers.length; i++) {
            if (myLayers[i] == inLayer){
                myLayers[i] = myLayers[i-1];
                myLayers[i-1] = inLayer;
            }
        }
        
        // redraw the display
        update(getGraphics());
    }
    
    /**
     * method to use to set the layers on the map
     */
    public synchronized void moveLayerUp(Layer inLayer) {
        if (inLayer == null) return;
        
        // swap this layer with the one above it.
        for (int i = 0; i < myLayers.length-1; i++) {
            if (myLayers[i] == inLayer){
                myLayers[i] = myLayers[i+1];
                myLayers[i+1] = inLayer;
                break;
            }
        }
        
        // redraw the display
        update(getGraphics());
    }
    
    /**
     * Draw the current layers on the given Graphics context.  The Envelope are the
     * screen Envelope, they are not the world Envelope, the world Envelope for the
     * currently displayed map will be used.
     */
    public synchronized void printLayers(Graphics inGraphics, Envelope inEnvelope) {
        
        // create the converter to convert from world coordinates to print coordinates.
        Converter tempConverter = new Converter(inEnvelope, myEnvelope);
        
        // draw the individual layers
        try{
            
            // retrieve the graphics object from the map image
            Graphics2D tempGraphics = (Graphics2D) inGraphics;
            
            // set a clipping region so we don't over draw
            int width =(int) inEnvelope.getWidth()-1;
            int height = (int) inEnvelope.getHeight()-1;
            tempGraphics.setClip(0, 0, width, height);
            
            // loop through the layers drawing each one.
            for (int i = 0; i < myLayers.length; i++) {
                Layer tempLayer = myLayers[i];
                tempLayer.drawLayer(tempGraphics, tempConverter);
            }
            // loop through the layers drawing each one.
            for (int i = 0; i < myLayers.length; i++) {
                Layer tempLayer = myLayers[i];
                tempLayer.labelLayer(tempGraphics, tempConverter);
            }
            
            // draw a border
            tempGraphics.drawRect(0,0, width,  height);
        }
        catch (Throwable t){
            System.out.println("Error in draw layers");
            System.out.println(t);
            t.printStackTrace(System.out);
        }
    }
    
    /**
     * method to use to set the layers on the map
     */
    public synchronized void removeLayer(Layer inLayer) {
        if (inLayer == null) return;
        
        // if this is the selected layer, then set the selected layer to nothing
        if (inLayer == mySelectedLayer) mySelectedLayer = null;
        
        // add all the layers except the one sent in.
        Vector tempLayerVect = new Vector(myLayers.length);
        for (int i = 0; i < myLayers.length; i++) {
            if (myLayers[i] != inLayer){
                tempLayerVect.addElement(myLayers[i]);
            }
        }
        myLayers = new Layer[tempLayerVect.size()];
        tempLayerVect.copyInto(myLayers);
        
        // redraw the display
        update(getGraphics());
    }
    
    /** listeners */
    private Vector myVectListeners = new Vector();
    /** Add a listener */
    public void addGISDisplayListener(GISDisplayListener inGISDisplayListener){
        myVectListeners.addElement(inGISDisplayListener);
    }
    /** Remove a listener */
    public void removeGISDisplayLisetner(GISDisplayListener inGISDisplayListener){
        myVectListeners.removeElement(inGISDisplayListener);
    }
    /** Fire an Envelope Changed event */
    private void fireEnvelopeChanged(Envelope inEnvelope){
        for (int i=0; i<myVectListeners.size(); i++){
            ((GISDisplayListener) myVectListeners.elementAt(i)).envelopeChanged(inEnvelope);
        }
    }
    
    /** Reference to the currently used projection */
    public Projection myProjection = null;
    /** Retrieve the currently used projection */
    public Projection getProjection(){return myProjection;}
    
    /** Set the projection to use when drawing the map */
    public synchronized void setProjection(Projection inProjection) throws Exception{
        
        // reset the Envelope
        gistoolkit.features.Point tempPointTop = new gistoolkit.features.Point(myEnvelope.getMinX(), myEnvelope.getMaxY());
        gistoolkit.features.Point tempPointBottom = new gistoolkit.features.Point(myEnvelope.getMaxX(), myEnvelope.getMinY());
        Envelope tempEnvelope = (Envelope) myEnvelope.clone();
        if (myProjection != null){
            tempEnvelope = ShapeProjector.projectBackward(myProjection, tempEnvelope);
        }
        if (inProjection != null){
            tempEnvelope = ShapeProjector.projectForward(inProjection, tempEnvelope);
        }
        
        // update the projection in all the layers.
        myProjection = inProjection;
        for (int i=0; i<myLayers.length; i++){
            myLayers[i].setProjection(inProjection, true);
        }
        
        setEnvelope(tempEnvelope);
    }
    
    /** Retrieve the legend from the Layers */
    public Image getLegend(){
        Vector tempVectImages = new Vector();
        int height = 0;
        int width = 0;
        for (int i=0; i<myLayers.length; i++){
            Image tempImage = myLayers[i].getLegend();
            if (tempImage != null) {
                tempVectImages.add(tempImage);
                
                /** find the Envelope of the image */
                height = height + tempImage.getHeight(this);
                width = width + tempImage.getWidth(this);
            }
        }
        
        if (tempVectImages.size() > 0){
            Image myImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) myImage.getGraphics();
            int currentHeight = 0;
            for (int i=0; i<tempVectImages.size(); i++){
                Image tempImage = (Image) tempVectImages.elementAt(i);
                g2d.drawImage(tempImage, 0, currentHeight, this);
                currentHeight = currentHeight + tempImage.getHeight(this);
            }
            return myImage;
        }
        return null;
    }
    
    private static final String TOP_X = "TopX";
    private static final String TOP_Y = "TopY";
    private static final String BOTTOM_X = "BottomX";
    private static final String BOTTOM_Y = "BottomY";
    private static final String PROJECTION_CLASS = "ProjectionClass";
    private static final String LAYER_CLASS = "LayerClass";
    private static final String NODE_PROJECTION = "Projection";
    private static final String NODE_LAYER = "Layer";
    private static final String SCALEBAR_CLASS = "ScaleBarClass";
    private static final String NODE_SCALEBAR = "ScaleBar";
    
    /** Get the configuration information from this display */
    public Node getNode(){
        Node tempRoot = new Node("GISDisplay");
        
        // save the information associated with the Envelope of the map
        tempRoot.addAttribute(TOP_X, ""+myEnvelope.getMinX());
        tempRoot.addAttribute(TOP_Y, ""+myEnvelope.getMaxY());
        tempRoot.addAttribute(BOTTOM_X, ""+myEnvelope.getMaxX());
        tempRoot.addAttribute(BOTTOM_Y, ""+myEnvelope.getMinY());
        
        // save the information associated with the projection
        if (myProjection != null){
            Node tempProjection = new Node(NODE_PROJECTION);
            tempRoot.addChild(tempProjection);
            tempProjection.addAttribute(PROJECTION_CLASS, myProjection.getClass().getName());
            tempProjection.addChild(myProjection.getNode());
        }
        
        // save the information associated with the ScaleBar
        if (myScaleBar != null){
            Node tempScaleBarNode = new Node(NODE_SCALEBAR);
            tempScaleBarNode.addAttribute(SCALEBAR_CLASS, myScaleBar.getClass().getName());
            tempScaleBarNode.addChild(myScaleBar.getNode());
            tempRoot.addChild(tempScaleBarNode);
        }
        
        // save the information associated with the layers.
        for (int i=0; i<myLayers.length; i++){
            Node tempLayerNode = new Node(NODE_LAYER);
            tempRoot.addChild(tempLayerNode);
            tempLayerNode.addAttribute(LAYER_CLASS, myLayers[i].getClass().getName());
            tempLayerNode.addChild(myLayers[i].getNode());
        }
        return tempRoot;
    }
    
    /** Set the configuration information for this display */
    public void setNode(Node inNode) throws Exception{
        if (inNode == null) return;
        
        // the Envelope of the display
        String tempString = inNode.getAttribute(TOP_X);
        double tempTopX = Double.parseDouble(tempString);
        tempString = inNode.getAttribute(TOP_Y);
        double tempTopY = Double.parseDouble(tempString);
        tempString = inNode.getAttribute(BOTTOM_X);
        double tempBottomX = Double.parseDouble(tempString);
        tempString = inNode.getAttribute(BOTTOM_Y);
        double tempBottomY = Double.parseDouble(tempString);
        Envelope tempEnvelope = new Envelope(tempTopX, tempTopY, tempBottomX, tempBottomY);
        myEnvelope = tempEnvelope;
        
        // the Projection
        Node tempProjectionNode = inNode.getChild(NODE_PROJECTION);
        Projection tempProjection = null;
        if (tempProjectionNode != null){
            tempString = tempProjectionNode.getAttribute(PROJECTION_CLASS);
            if (tempString != null){
                tempProjection = (Projection) Class.forName(tempString).newInstance();
                Node[] tempNodes = tempProjectionNode.getChildren();
                if (tempNodes.length > 0) tempProjection.setNode(tempNodes[0]);
            }
        }
        // set the projection
        if (tempProjection != null) setProjection(tempProjection);
        
        // set the scalebar.
        Node tempScaleBarNode = inNode.getChild(NODE_SCALEBAR);
        if (tempScaleBarNode != null){
            String tempScaleBarClass = (String) tempScaleBarNode.getAttribute(SCALEBAR_CLASS);
            if (tempScaleBarClass != null){
                try{
                    ScaleBar tempScaleBar = (ScaleBar) Class.forName(tempScaleBarClass).newInstance();
                    Node[] tempNodes = tempScaleBarNode.getChildren();
                    if (tempNodes.length > 0) tempScaleBar.setNode(tempNodes[0]);
                    setScaleBar(tempScaleBar);
                }
                catch (Exception e){
                    System.out.println("Error reading Scalebar Configuration "+e);
                }
            }
        }

        // the Layers
        myLayers = new Layer[0];
        Node[] tempLayers = inNode.getChildren(NODE_LAYER);
        for (int i=0; i<tempLayers.length; i++){
            // initialize the layers.
            tempString = tempLayers[i].getAttribute(LAYER_CLASS);
            if (tempString != null){
                Layer tempLayer = (Layer) Class.forName(tempString).newInstance();
                Node[] tempNodes = tempLayers[i].getChildren();
                if (tempNodes.length > 0) tempLayer.setNode(tempNodes[0]);
                addLayer(tempLayer);
            }
        }
        setEnvelope(tempEnvelope);
    }
}