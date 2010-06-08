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

package gistoolkit.datasources.imagefile.imagereaders;

import java.io.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 * Class to protect from Instantiation exceptions if javax.imageio is not available..
 */
public class ImageIOImageReader implements ImageReaderInterface{
    
    /** The width of the image. */
    private int myWidth = -1;
    public int getWidth(){return myWidth;}
    
    /** The height of the image*/
    private int myHeight = -1;
    public int getHeight(){return myHeight;}
    
    /** The Rendered Image in case it should be needed. */
    private BufferedImage myImage = null;
    public BufferedImage getImage(){return myImage;}
    
    /** Create an empty reader. */
    public ImageIOImageReader() throws Exception{
    }
   
    /** Creates a new instance of JAIImageReader */
    public ImageIOImageReader(String inFile) throws Exception{
        // Read from a file
        File file = new File(inFile);
        BufferedImage image = ImageIO.read(file);
        myWidth = image.getWidth();
        myHeight = image.getHeight();
        myImage = image;
    }
    
    /** Use the javax.imageio api to read the image. */
    public ImageInformation readImage(String inFile){
        try{
            BufferedImage image = ImageIO.read(new File(inFile));            
            int tempWidth = image.getWidth();
            int tempHeight = image.getHeight();
            
            if ((tempWidth > 0) && (tempHeight > 0)){
                myImage = image;
                myWidth = tempWidth;
                myHeight = tempHeight;
                ImageInformation tempImageInformation = new ImageInformation(image, myImage.getWidth(), myImage.getHeight());
                return tempImageInformation;
            }
            
        }
        catch (Exception e){
        }
        return null;
    }
    
}
