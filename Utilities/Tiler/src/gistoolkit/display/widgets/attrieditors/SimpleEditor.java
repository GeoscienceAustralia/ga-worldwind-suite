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

package gistoolkit.display.widgets.attrieditors;
import javax.swing.*;
import gistoolkit.display.widgets.AttributeEditor;
/**
 * Class to allow editing of data within a cell or grid.
 * This one just displays the text representation of the object, but does not edit it.
 */
public class SimpleEditor extends JLabel implements AttributeEditor{

    /** Creates new SimpleEditor */
    public SimpleEditor() {
    }

    /** Store the attribute so it can be returned as it was given */
    private Object myAttribute = null;
    
    /** Set the attribute to be edited */
    public void setAttribute(Object inAttribute){
        myAttribute = inAttribute;
        setText(""+myAttribute);
    }
    
    /** Return the edited (or in this case not) attribute */
    public Object getAttribute(){
        return myAttribute;
    }
}
