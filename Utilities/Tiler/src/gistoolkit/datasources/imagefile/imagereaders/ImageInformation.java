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

import java.awt.Image;

/**
 * Class to transfer information about images back and forth from the readers.
 */
public class ImageInformation {
    /** The image this reader has created. */
    private Image myImage = null;
    /** Get the buffered Image. */
    public Image getImage(){return myImage;}

    /** The width of the image. */
    private int myImageWidth = 0;
    /** Get the width of the image. */
    public int getImageWidth(){return myImageWidth;}
    
    /** The height of the image. */
    private int myImageHeight = 0;
    /** get the height of the image. */
    public int getImageHeight(){return myImageHeight;}
    
    /** Creates a new instance of ImageInformation */
    public ImageInformation() {
    }
        
    /** Create a new instance of the ImageInformation with the given information. */
    public ImageInformation(Image inImage, int inWidth, int inHeight){
        myImage = inImage;
        myImageWidth = inWidth;
        myImageHeight = inHeight;
    }
}
