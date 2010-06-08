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

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;
import gistoolkit.features.*;
/**
 * Object for handling printing call backs from the printing subsystem.
 */
public class PrintMap implements Printable{
    /** Save a reference to the GISDisplay */
    private GISDisplay myGISDisplay = null;
    
    /** Generate the image only once, much faster this way, though, uses more memory */
    private Image myImage = null;

    /** Creates new PrintMap */
    public PrintMap(GISDisplay inDisplay) {
        super();
        myGISDisplay = inDisplay;
    }
    
    /**
     * Send this graphic to the printer.
     */
    public void print(){
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
            } catch (Exception pe) {
                JOptionPane.showMessageDialog(myGISDisplay, pe.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Prints the map on the given graphics context with the given page format.
     */
    public int print (Graphics g, PageFormat pageFormat, int page) {
        //--- Validate the page number, we only print the first page
        if (page == 0) {
            if (myImage == null){
                // Generate the image
                int width = (int) pageFormat.getImageableWidth()-1;
                int height = (int) pageFormat.getImageableHeight()-1;
                myImage = myGISDisplay.createImage(width, height);
                Graphics2D g2d = (Graphics2D) myImage.getGraphics();
                g2d.setBackground(Color.white);
                g2d.clearRect(0,0,width, height);
                myGISDisplay.printLayers(myImage.getGraphics(), new Envelope(0,0, width, height));
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());
            g2d.drawImage(myImage, 0,0, myGISDisplay);
            return (PAGE_EXISTS);
        }
        else
            return (NO_SUCH_PAGE);
    }
    

}
