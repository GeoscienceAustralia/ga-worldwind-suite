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

/**
 * Interface for use when developing a draw model to manipulate the map.
 */
public interface DrawModel {
    /**
     * The function which is called to draw the image on the map.
     */
    public void draw();
    
    /** Called when the DrawModel is added to the display to allow it to do initialization*/
    public void setGISDisplay(GISDisplay inGISDisplay);

    /**
     * Function called to indicate that this draw model will be removed.
     */
    public void remove();
    
    /** Called when the DrawModel should quit doing what it is doing and reset to the initial state */
    public void reset();
}