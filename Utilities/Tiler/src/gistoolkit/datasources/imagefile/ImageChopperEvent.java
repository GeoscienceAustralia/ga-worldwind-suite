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

package gistoolkit.datasources.imagefile;

/**
 * Class to hold messages sent from the image chopper to any listeners.
 */
public class ImageChopperEvent{
    
    public static final int READING_FILE = 0;
    public static final int WRITING_FILE = 1;

    private int myActivity = 0;
    private String myMessage = "";
    
    /** Creates a new instance of ImageChopperEvent */
    public ImageChopperEvent(int inActivity, String inMessage) {
        myActivity = inActivity;
        myMessage = inMessage;
    }
    
    /** Create a string representation of this message. */
    public String toString(){
        if (myActivity == READING_FILE){
            return ("[Reading] "+myMessage);
        }
        else{
            return ("[Writing] "+myMessage);
        }
    }
}
