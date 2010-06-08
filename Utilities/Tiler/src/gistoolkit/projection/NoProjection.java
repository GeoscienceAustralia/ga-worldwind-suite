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

import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.projection.Ellipsoid;
import gistoolkit.projection.ellipsoid.*;
/**
 * Does no projection, just displays the data in the projection it currently has.
 */
public class NoProjection implements Projection, EditableProjection, EllipsoidProjection{

    /** Creates new NoProjection */
    public NoProjection() {
    }
    
    /** No Projections can have Ellipsoids to help define the data */
    private Ellipsoid myEllipsoid = new WGS84();
    /** Return the Ellipsoid for this projection */
    public Ellipsoid getEllipsoid(){return myEllipsoid;}
    /** Set the Ellipsoid for this projection */
    public void setEllipsoid(Ellipsoid inEllipsoid){myEllipsoid = inEllipsoid;}

    /** Returns the units of measure for the projection. */
    public String getUnitOfMeasure(){
        if (myEllipsoid == null) return "Map Units";
        return myEllipsoid.getUnitOfMeasure();
    }
    
    /** do not perform any projection, just return the same point */
    public void projectForward(Point inPoint) throws Exception{}
    
    /** do not perform any projection, just return the same point */
    public void projectBackward(Point inPoint) throws Exception{}
    
    /** return the name of the projection */
    public String getProjectionName(){ return "No Projection";}

    /** Display the type of projection */
    public String toString(){return getProjectionName();}
    
    /** set the Envelope of the map.  The projection may do some initialization bassed on this value  */
    public void setEnvelope(Envelope inEnvelope) throws Exception {
    }
    
    /** Return the configuration information for this projection  */
    public Node getNode() {
        Node tempRoot = new Node("Projection");
        tempRoot.addAttribute("ProjectionName",getProjectionName());
        return tempRoot;
    }
    
    /** Setup this projection using the configuration information in the node  */
    public void setNode(Node inNode) throws Exception {
    }
    
    /** return the panel needed to edit this projection  */
    public ProjectionPanel getEditPanel() {
        NoProjectionPanel tempEditPanel = new NoProjectionPanel();
        tempEditPanel.setProjection(this);
        return tempEditPanel;
    }    

    /** clone this object*/
    public Object clone(){
        NoProjection tempProjection = new NoProjection();
        tempProjection.setEllipsoid(getEllipsoid());
        return tempProjection;
    }    
}
