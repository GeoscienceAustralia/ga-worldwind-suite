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

import java.util.*;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import javax.swing.*;
/**
 * Class to display an image to the client.
 */
public class ImagePanel extends JPanel implements ComponentListener, Printable{
    /** The image to display */
    private Image myImage = null;
    
    /** The double buffer image */
    private Image myDoubleBufferImage = null;
    
    /** Image for use with printing */
    private Image myPrintImage = null;
    
    /** Creates new ImagePanel */
    public ImagePanel() {
        super();
        addComponentListener(this);
        setBackground(Color.gray);
    }
    
    /** set the image to display. */
    public void setImage(Image inImage){
        myImage = inImage;
        myDoubleBufferImage = createImage(getWidth(), getHeight());
        update(getGraphics());
    }
    
    /** Draw the image on the screen */
    public void paint(Graphics inGraphics){
        inGraphics.drawImage(myDoubleBufferImage, 0, 0, this);
    }
    
    /** overload the draw method */
    public void update(Graphics inGraphics){
        if (myImage != null){
            if (myDoubleBufferImage == null) myDoubleBufferImage = createImage(getWidth(), getHeight());
            if (myDoubleBufferImage != null){
                Graphics tempGraphics = myDoubleBufferImage.getGraphics();
                tempGraphics.drawImage(myImage, 0, 0, this);
                repaint();
            }
        }
    }

    /** Prevents overrunning the processor on show machines with redraws */
    private long myTime = 0;
    
    /** 
     * Called when the image is updated.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
        if ((flags & ALLBITS) != 0) {
            update(getGraphics());
        }
        else {
            long tempTime = new Date().getTime();
            if ((tempTime - myTime) > 500) {
                update(getGraphics());
                myTime = new Date().getTime();;
            }
        }
        return true;
    }
 
    /**
     * Send this graphic to the printer.
     */
    public void print(){
        // save my image
        if (myImage == null) return;
        myPrintImage = createImage(myImage.getWidth(this), myImage.getHeight(this));
        if (myPrintImage == null) return;
        
        // copy the current image to the print image.
        Graphics g = myPrintImage.getGraphics();
        g.drawImage(myImage,0,0,this);
        //--- Create a printerJob object
        PrinterJob printJob = PrinterJob.getPrinterJob ();
        
        //--- Set the printable class to this one since we
        //--- are implementing the Printable interface
        printJob.setPrintable (this);
        
        //--- Show a print dialog to the user. If the user
        //--- clicks the print button, then print, otherwise
        //--- cancel the print job
        if (printJob.printDialog()) {
            try {
                
                printJob.print();
            } catch (Exception PrintException) {
                PrintException.printStackTrace();
            }
        }
    }
    
    /**
     * Draws the contents of the image panel to the given graphics context with the given page format.
     */
    public int print(Graphics inGraphics, PageFormat inPageFormat,int inPage) throws java.awt.print.PrinterException {
        //--- Validate the page number, we only print the first page
        if (inPage == 0) {
            Graphics2D g2d = (Graphics2D) inGraphics;
            g2d.translate (inPageFormat.getImageableX (), inPageFormat.getImageableY ());
            g2d.drawImage(myPrintImage, 0, 0, this);
            return (PAGE_EXISTS);
        }
        else
            return (NO_SUCH_PAGE);
    }    
    
    public void componentShown(java.awt.event.ComponentEvent p1) {
    }
    
    public void componentResized(java.awt.event.ComponentEvent p1) {
        if ((getWidth() > 0) && (getHeight() > 0)){
            myDoubleBufferImage = createImage(getWidth(), getHeight());
            update(getGraphics());
        }
    }
    
    public void componentHidden(java.awt.event.ComponentEvent p1) {
    }
    
    public void componentMoved(java.awt.event.ComponentEvent p1) {
    }    
}
