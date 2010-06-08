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

package gistoolkit.display.renderer;

import java.io.*;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.*;
import gistoolkit.display.renderer.images.ImageSource;
/**
 * Created to render Points as an image.
 */
public class PointImageRenderer extends PointRenderer{
    /** The name for this renderer, always returns "Point Image Renderer" */
    public String getRendererName(){ return "Point Image Renderer";}

    /** File name of the image to use. */
    private String myImageFileName = null;
    /** Returns the file name of the image. */
    public String getImageFileName(){return myImageFileName;}
    /** Set the image file name. */
    public void setImage(String inImageFileName) throws Exception{
        File tempFile = new File(inImageFileName);
        Image tempImage = null;
        if (!tempFile.exists()){
            // check for it in the images folder
            ImageSource tempImageSource = new ImageSource();
            tempImage = tempImageSource.getImage(inImageFileName);
            if (tempImage == null){
                throw new Exception ("File "+inImageFileName+" was not found.");
            }
            myImageFileName = inImageFileName;
        }
        else{
            tempImage = Toolkit.getDefaultToolkit().createImage(tempFile.getAbsolutePath());
            myImageFileName = tempFile.getAbsolutePath();
        }
        if (tempImage == null) throw new Exception("Error reading image "+tempFile.getAbsolutePath());
        MediaTracker mt = new MediaTracker(new Panel());
        mt.addImage(tempImage, 1);
        mt.waitForAll();
        setImage(tempImage);
    }
    /** The image to use to render the point. */
    private java.awt.Image myImage = null;
    /** Set the iamge. */
    public void setImage(Image inImage){
        myImage = inImage;
        myWidth = inImage.getWidth(myObserver);
        myHeight = inImage.getHeight(myObserver);
        if (myWidth > 0)myHalfWidth = myWidth/2;
        else myHalfWidth = 0;
        if (myHeight > 0) myHalfHeight = myHeight/2;
        else myHalfHeight = 0;
    }
    /** Get the image. */
    public Image getImage(){return myImage;}
    
    private int myWidth = 0;
    private int myHeight = 0;
    private int myHalfWidth = 0;
    private int myHalfHeight = 0;
    
    /** Internal class to listen to the image as it is loaded. */
    private class MyImageObserver implements java.awt.image.ImageObserver{
        
        public boolean imageUpdate(java.awt.Image image, int param, int param2, int param3, int param4, int param5) {
            if (myImage == image){
                myWidth = image.getWidth(myObserver);
                myHeight = image.getHeight(myObserver);
                if (myWidth > 0)myHalfWidth = myWidth/2;
                else myHalfWidth = 0;
                if (myHeight > 0) myHalfHeight = myHeight/2;
                else myHalfHeight = 0;
            }
            if ((myWidth != 0) && (myHeight != 0)){
                return false;
            }
            return true;
        }
    }
    private MyImageObserver myObserver = new MyImageObserver();
    
    /**
     * MonoShader constructor comment.
     */
    public PointImageRenderer() {
        super();
    }
    
    /**
     * Draws the line by drawing lines between the points.
     */
    public boolean drawShape(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if ((inRecord.getShape() instanceof Point)){
        
            Point tempPoint = (Point) inRecord.getShape();

            // Draw the point.
            Graphics tempGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
            if (tempGraphics != null){
                if (myImage != null){
                    drawImage(tempGraphics, tempPoint, inConverter);
                }
                else{
                    drawPoint(tempGraphics, tempPoint, inConverter);
                }
            }
            return true;
        }
        if ((inRecord.getShape() instanceof MultiPoint)){
        
            Point[] tempPoints = inRecord.getShape().getPoints();

            for (int i=0; i<tempPoints.length; i++){
                // Draw the point.
                Graphics tempGraphics = inShader.getLineGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
                if (tempGraphics != null){
                    if (myImage != null){
                        drawImage(tempGraphics, tempPoints[i], inConverter);
                    }
                    else{
                        drawPoint(tempGraphics, tempPoints[i], inConverter);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Draws the Line in the highlight color.
     */
    public boolean drawShapeHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader) {
        if (inRecord == null) return false;
        if (!(inRecord.getShape() instanceof Point))
            return false;
        
        Point tempPoint = (Point) inRecord.getShape();
        
        // Draw the lines.
        Graphics tempGraphics = inShader.getLineHighlightGraphics(inGraphics, inRecord.getAttributes(), inRecord.getAttributeNames());
        if (tempGraphics != null){
            if (myImage != null){
                drawImageHighlight(tempGraphics, tempPoint, inConverter);
            }
            else{
                drawPoint(tempGraphics, tempPoint, inConverter);
            }
        }
        return true;
    }
            
    /**
     * Draw the image.
     */
    protected boolean drawImage(Graphics inGraphics, Point inPoint, Converter inConverter){
        if (inPoint == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        int x1 = inConverter.toScreenX(inPoint.getX());
        int y1 = inConverter.toScreenY(inPoint.getY());
        inGraphics.drawImage(myImage, x1-myHalfWidth, y1-myHalfHeight, myWidth, myHeight, myObserver);
        return true;
    }
    
    /**
     * Draw the image.
     */
    protected boolean drawImageHighlight(Graphics inGraphics, Point inPoint, Converter inConverter){
        if (inPoint == null) return false;
        if (inGraphics == null) return false;
        if (inConverter == null) return false;
        
        int x1 = inConverter.toScreenX(inPoint.getX());
        int y1 = inConverter.toScreenY(inPoint.getY());
        drawImage(inGraphics, inPoint, inConverter);
        inGraphics.drawLine(x1-(myHalfWidth+1), y1-(myHalfHeight+1), x1+(myHalfWidth+1), y1-(myHalfHeight+1));
        inGraphics.drawLine(x1+(myHalfWidth+1), y1-(myHalfHeight+1), x1+(myHalfWidth+1), y1+(myHalfHeight+1));
        inGraphics.drawLine(x1-(myHalfWidth+1), y1-(myHalfHeight+1), x1-(myHalfWidth+1), y1+(myHalfHeight+1));
        inGraphics.drawLine(x1-(myHalfWidth+1), y1+(myHalfHeight+1), x1+(myHalfWidth+1), y1+(myHalfHeight+1));
        return true;
    }
    
    
    private static final String POINT_IMAGE_RENDERER_NODE = "PointImageRenderer";
    private static final String IMAGE_FILE_NAME = "ImageFileName";
    /** Get the configuration information for this renderer  */
    public Node getNode() {
        Node tempNode = super.getNode();
        if (tempNode == null) tempNode = new Node(POINT_IMAGE_RENDERER_NODE);
        tempNode.setName(POINT_IMAGE_RENDERER_NODE);
        tempNode.addAttribute(IMAGE_FILE_NAME, myImageFileName);
        return tempNode;
    }
    
    /** Set the configuration information for this renderer  */
    public void setNode(Node inNode) throws Exception {
        if (inNode != null){
            super.setNode(inNode);
            String tempImageFileName = inNode.getAttribute(IMAGE_FILE_NAME);
            if (tempImageFileName != null){
                setImage(tempImageFileName);
            }
        }
    }
    
    /** For display in lists and such. */
    public String toString(){
        return "Point Image Renderer";
    }
}