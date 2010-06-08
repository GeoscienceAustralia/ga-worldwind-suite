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

import java.awt.Graphics;
import gistoolkit.common.*;
import gistoolkit.features.*;

/**
 * Labelers are responsible for labeling the features of a particular layer.
 * They are part of the style of the layer, and can be replaced.
 */
public interface Labeler{
    /**
     * Get a name for this labeler.
     */
    public String getLabelerName();
    
    /**
     * Called before the layer is initially labeled to allow the labeler to prepare for labeling.
     */
    public void beginLabel();
    
    /**
     * Called after the layer has completed labeling.
     */
    public void endLabel();

    /**
     * Draw the label for the record on the graphics context
     */
    public boolean drawLabel(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader);
    
    /**
     * Highlight the Label when the shape is highlighted.
     */
    public boolean drawLabelHighlight(Record inRecord, Graphics inGraphics, Converter inConverter, Shader inShader);
    
    /** get the configuration information for this labeler */
    public Node getNode();
    
    /** Set the configuration information for this labeler */
    public void setNode(Node inNode) throws Exception;
    
}
