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

import gistoolkit.features.*;
import gistoolkit.common.*;
/**
 * Represents and performs a projection.
 */
public interface Projection {
    /** Get the units of the projection. */
    public String getUnitOfMeasure();
    
    /** Project the data in the forward direction */
    public void projectForward(Point inPoint) throws Exception;
    
    /** Project the data in the reverse direction */
    public void projectBackward(Point inPoint) throws Exception;
    
    /** set the envelope of the map.  The projection may do some initialization bassed on this value */
    public void setEnvelope(Envelope inEnvelope) throws Exception;
    
    /** return the name of the projection */
    public String getProjectionName();

    /** Return the configuration information for this projection */
    public Node getNode();
    
    /** Setup this projection using the configuration information in the node */
    public void setNode(Node inNode) throws Exception;
    
    /** Clone the object */
    public Object clone();
}
