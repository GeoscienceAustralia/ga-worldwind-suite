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

package gistoolkit.projection;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.*;
import gistoolkit.features.*;


/**
 * Projects an image from one projection to another.
 *
 * This is done by resampling the image into the new projection.
 */
public class ImageProjector {

    /** Creates new ImageProjector */
    public ImageProjector() {
    }

    /** Project the image forward. */
    public static void projectForward(Projection inProjection, RasterShape inImage) throws Exception{
        // shortcut if this is a degenerate case.
        if (inProjection == null) return;
        if (inProjection instanceof NoProjection) return;
        if (inImage == null) return;
        if (inImage.getImage() == null) return;        
        BufferedImage tempINImage = inImage.getImage();
        int tempINWidth = tempINImage.getWidth();
        int tempINHeight = tempINImage.getHeight();

        // project the Envelope forward.
        Envelope tempINEnvelope = inImage.getEnvelope();
        Envelope tempOUTEnvelope = ShapeProjector.projectForward(inProjection, tempINEnvelope);
        if ((tempINWidth <=0) || (tempINHeight <=0)){
            inImage.setImage(tempOUTEnvelope, tempINImage);
            return;
        }
        
        // create the new image the larger than the old image.                
        int tempOUTWidth = (int) (tempINWidth*1.5);
        int tempOUTHeight = (int) (tempINHeight*1.5);
        BufferedImage tempOUTImage = new BufferedImage(tempOUTWidth, tempOUTHeight, BufferedImage.TYPE_INT_ARGB);        
                
        // fill the background with a transparent color.
        Graphics g = tempOUTImage.getGraphics();
        g.setColor(new Color(255,255,255,0));
        g.fillRect(0,0,tempOUTWidth, tempOUTHeight);        
        
        // loop through the projected Envelope sampling the image.
        double tempINWidthStep = tempINEnvelope.getWidth()/tempINWidth;
        double tempINHeightStep = tempINEnvelope.getHeight()/tempINHeight;
        double tempOUTWidthStep = tempOUTEnvelope.getWidth()/tempOUTWidth;
        double tempOUTHeightStep = tempOUTEnvelope.getHeight()/ tempOUTHeight;
        double tempOUTWidthHalfStep = tempOUTWidthStep/2;
        double tempOUTHeightHalfStep = tempOUTHeightStep/2;
        Point tempPoint = new Point(0.0,0.0);
        for (int i=0; i<tempOUTHeight; i++){
            double tempOUTY = (tempOUTEnvelope.getMaxY() - tempOUTHeightStep*i)-tempOUTHeightHalfStep;
            for (int j=0; j<tempOUTWidth; j++){
                // find the projected location of the current pixel
                double tempOUTX = (tempOUTEnvelope.getMinX() + tempOUTWidthStep*j) + tempOUTWidthHalfStep;
                tempPoint.setX(tempOUTX);
                tempPoint.setY(tempOUTY);
                
                // reverse project the location
                inProjection.projectBackward(tempPoint);
                
                // find this pixel
                if (EnvelopeOverlap(tempINEnvelope, tempPoint)){
                    int tempINX = (int) ((tempPoint.getX() - tempINEnvelope.getMinX())/tempINWidthStep);
                    int tempINY = tempINHeight - (int) ((tempPoint.getY() - tempINEnvelope.getMinY())/tempINHeightStep);
                    int tempColor = tempINImage.getRGB(tempINX, tempINY-1);
                    tempOUTImage.setRGB(j, i, tempColor);
                }
            }
        }
        
        // return the projected image.
        inImage.setImage(tempOUTEnvelope, tempOUTImage);
    }
    
    /** Project the image Backward. */
    public static void projectBackward(Projection inProjection, RasterShape inImage) throws Exception{
        // shortcut if this is a degenerate case.
        if (inProjection == null) return;
        if (inProjection instanceof NoProjection) return;
        if (inImage == null) return;
        if (inImage.getImage() == null) return;        
        BufferedImage tempINImage = inImage.getImage();
        int tempINWidth = tempINImage.getWidth();
        int tempINHeight = tempINImage.getHeight();

        // project the Envelope forward.
        Envelope tempINEnvelope = inImage.getEnvelope();
        Envelope tempOUTEnvelope = ShapeProjector.projectBackward(inProjection, tempINEnvelope);
        if ((tempINWidth <=0) || (tempINHeight <=0)){
            inImage.setImage(tempOUTEnvelope, tempINImage);
            return;
        }
        
        // create the new image the same size as the old image.                
        int tempOUTWidth = tempINWidth;
        int tempOUTHeight = tempINHeight;
        BufferedImage tempOUTImage = new BufferedImage(tempOUTWidth, tempOUTHeight, BufferedImage.TYPE_INT_ARGB);        
                
        // // fill the background with a transparent color.
        Graphics g = tempOUTImage.getGraphics();
        g.setColor(new Color(255,255,255,0));
        g.fillRect(0,0,tempOUTWidth, tempOUTHeight);        
        
        // loop through the projected Envelope sampling the image.
        double tempINWidthStep = tempINEnvelope.getWidth()/tempINWidth;
        double tempINHeightStep = tempINEnvelope.getHeight()/tempINHeight;
        double tempOUTWidthStep = tempOUTEnvelope.getWidth()/tempOUTWidth;
        double tempOUTHeightStep = tempOUTEnvelope.getHeight()/ tempOUTHeight;
        double tempOUTWidthHalfStep = tempOUTWidthStep/2;
        double tempOUTHeightHalfStep = tempOUTHeightStep/2;
        Point tempPoint = new Point(0.0,0.0);
        for (int i=0; i<tempOUTHeight; i++){
            double tempOUTY = (tempOUTEnvelope.getMaxY() - tempOUTHeightStep*i) - tempOUTHeightHalfStep;
            for (int j=0; j<tempOUTWidth; j++){
                // find the projected location of the current pixel
                double tempOUTX = (tempOUTEnvelope.getMinX() + tempOUTWidthStep*j) + tempOUTWidthHalfStep;
                tempPoint.setX(tempOUTX);
                tempPoint.setY(tempOUTY);
                
                // reverse project the location
                inProjection.projectForward(tempPoint);
                
                // find this pixel
                if (EnvelopeOverlap(tempINEnvelope, tempPoint)){
                    int tempINX = (int) ((tempPoint.getX() - tempINEnvelope.getMinX())/tempINWidthStep);
                    int tempINY = tempINHeight - (int) ((tempPoint.getY() - tempINEnvelope.getMinY())/tempINHeightStep);
                    int tempColor = tempINImage.getRGB(tempINX, tempINY-1);
                    tempOUTImage.setRGB(j, i, tempColor);
                }
            }
        }
        
        // return the projected image.
        inImage.setImage(tempOUTEnvelope, tempOUTImage);
    }
    /** ReProject the image. */
    public static void reProject(Projection inFromProjection, Projection inToProjection, RasterShape inImage) throws Exception{
        // shortcut if this is a degenerate case.
        if ((inToProjection == null) || (inToProjection instanceof NoProjection)) projectBackward(inFromProjection, inImage);
        if ((inFromProjection == null) || (inFromProjection instanceof NoProjection)) projectForward(inToProjection, inImage);
        
        if (inImage == null) return;
        if (inImage.getImage() == null) return;        
        BufferedImage tempINImage = inImage.getImage();
        int tempINWidth = tempINImage.getWidth();
        int tempINHeight = tempINImage.getHeight();

        // project the Envelope forward.
        Envelope tempINEnvelope = inImage.getEnvelope();
        Envelope tempOUTEnvelope = ShapeProjector.projectBackward(inFromProjection, tempINEnvelope);
        tempOUTEnvelope = ShapeProjector.projectForward(inToProjection, tempOUTEnvelope);
        if ((tempINWidth <=0) || (tempINHeight <=0)){
            inImage.setImage(tempOUTEnvelope, tempINImage);
            return;
        }
        
        // create the new image the same size as the old image.                
        int tempOUTWidth = tempINWidth;
        int tempOUTHeight = tempINHeight;
        BufferedImage tempOUTImage = new BufferedImage(tempOUTWidth, tempOUTHeight, BufferedImage.TYPE_INT_ARGB);        
                
        // // fill the background with a transparent color.
        Graphics g = tempOUTImage.getGraphics();
        g.setColor(new Color(255,255,255,0));
        g.fillRect(0,0,tempOUTWidth, tempOUTHeight);        
        
        // loop through the projected Envelope sampling the image.
        double tempINWidthStep = tempINEnvelope.getWidth()/tempINWidth;
        double tempINHeightStep = tempINEnvelope.getHeight()/tempINHeight;
        double tempOUTWidthStep = tempOUTEnvelope.getWidth()/tempOUTWidth;
        double tempOUTHeightStep = tempOUTEnvelope.getHeight()/ tempOUTHeight;
        double tempOUTWidthHalfStep = tempOUTWidthStep/2;
        double tempOUTHeightHalfStep = tempOUTHeightStep/2;
        Point tempPoint = new Point(0.0,0.0);
        for (int i=0; i<tempOUTHeight; i++){
            double tempOUTY = (tempOUTEnvelope.getMaxY() - tempOUTHeightStep*i) - tempOUTHeightHalfStep;
            for (int j=0; j<tempOUTWidth; j++){
                // find the projected location of the current pixel
                double tempOUTX = (tempOUTEnvelope.getMinX() + tempOUTWidthStep*j) + tempOUTWidthHalfStep;
                tempPoint.setX(tempOUTX);
                tempPoint.setY(tempOUTY);
                
                // reverse project the location
                inToProjection.projectBackward(tempPoint);
                inFromProjection.projectForward(tempPoint);
                
                // find this pixel
                if (EnvelopeOverlap(tempINEnvelope, tempPoint)){
                    int tempINX = (int) ((tempPoint.getX() - tempINEnvelope.getMinX())/tempINWidthStep);
                    int tempINY = tempINHeight - (int) ((tempPoint.getY() - tempINEnvelope.getMinY())/tempINHeightStep);
                    int tempColor = tempINImage.getRGB(tempINX, tempINY-1);
                    tempOUTImage.setRGB(j, i, tempColor);
                }
            }
        }
        
        // return the projected image.
        inImage.setImage(tempOUTEnvelope, tempOUTImage);
    }
    /**
     * Determines if the two rectangles overlap. Returns true if they do, and false if they do not.
     * @return boolean
     */
    public static boolean EnvelopeOverlap(Envelope inEnvelope, Point inPoint) {
        if (inEnvelope == null) return false;
        if (inPoint == null) return false;
        
        // check the for courners
        if (inEnvelope.getMinX() > inPoint.getX()) return false;
        if (inEnvelope.getMaxY() < inPoint.getY()) return false;
        
        // eliminate rectangles which do not overlap.
        if (inEnvelope.getMaxX() < inPoint.getX()) return false;
        if (inEnvelope.getMinY() > inPoint.getY()) return false;
        return true;
    }
    
}
