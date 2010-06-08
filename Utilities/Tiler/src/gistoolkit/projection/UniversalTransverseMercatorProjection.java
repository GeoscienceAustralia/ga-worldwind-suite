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
import gistoolkit.projection.ellipsoid.*;
/**
 * A projection used for converting from a WGS84 to UTM in a particular zone.
 * The default zone is 14 which lands somewhere in the central USA.  Hay, I live here.
 */
public class UniversalTransverseMercatorProjection extends TransversMercatorProjection implements EditableProjection{
    /** Retrieve the UTM zone of interest. */
    public int getUTMZone(double inlat, double inlon) throws Exception{
        // there is a utm zone every 3 degreese starting with the international date line.
        // each zone is 6 degreese wide.
        if (Math.abs(inlon) > 180) throw new Exception("Latitude out of range lon="+inlon+" Must be between -180 and 180");
        return (int) ((inlon+180.0)/6.0+1.0);
    }

    /** Creates new UniversalTransverseMercatorProjection */
    public UniversalTransverseMercatorProjection() {
        setEllipsoid(new WGS84());
        myFollowMap = false;
        try{
            setZone(14);
        }
        catch (Exception e){
            // there is no way this is going to cause a problem.
            System.out.println(e);
        }
        setLatOragin(0); // latitude of oragin is the equator.
        setEasting(500000); // easting for the northern hemisphere is 500000
        setNorthing(0); // northing for all zones in the north is 0, in the south it is -10000 meters.
    }
    
    /** UTM zone of interest */
    private int myUTMZone = 14;
    public int getZone(){return myUTMZone;}
    public void setZone(int inZone)throws Exception{
        if ((inZone <= 0 )||(inZone > 60)) throw new Exception("Zone out of bounds, UTMZones go from 0 to 60");
        setLonOragin((inZone-1)*6-180+3); // the 3 sets the center in the middle of the zone.
        myUTMZone = inZone;
    }

    private static final String ZONE_TAG= "Zone";
    private static final String HEMISPHERE_TAG = "Hemisphere";
    private static final String HEMISPHERE_NORTH = "North";
    private static final String HEMISPHERE_SOUTH = "South";
    
    /** Return the configuration information for this projection  */
    public Node getNode() {
        Node tempRoot = new Node("Universal_Transverse_Mercator_Projection");
        tempRoot.addAttribute(ZONE_TAG, ""+getZone());
        if (getNorthing() >= 10000000) tempRoot.addAttribute(HEMISPHERE_TAG, HEMISPHERE_SOUTH);
        else tempRoot.addAttribute(HEMISPHERE_TAG, HEMISPHERE_NORTH);
        return tempRoot;
    }
    
    /** Setup this projection using the configuration information in the node  */
    public void setNode(Node inNode) throws Exception {
        if (inNode == null) return;
        
        // find the ellipsoid
        String tempName = "None";
        try{
            tempName = ZONE_TAG;
            String tempString = inNode.getAttribute(ZONE_TAG);
            setZone(Integer.parseInt(tempString));
            
            tempName = HEMISPHERE_TAG;
            tempString = inNode.getAttribute(HEMISPHERE_TAG);
            if (tempString != null){
                if (tempString.equals(HEMISPHERE_SOUTH)){
                    setNorthing(10000000);
                }
            }
        }
        catch (Exception e){
            throw new Exception("Can not read the falue for "+tempName+" for Projection"+getProjectionName());
        }
    }
    
    /** return the name of the projection */
    public String getProjectionName(){ return "UTM (Universal Transverse Mercator)";}
    
    /** Determine if the projection should follow the map */
    private boolean myFollowMap = true;
    /** Return true if the projection follows the map, false if it does not */
    public boolean getFollowMap(){return myFollowMap;}
    /** Set the projection to follow the center of the map */
    public void setFollowMap(boolean inFollowMap){myFollowMap = inFollowMap;}
    
    /** 
     * Set the Envelope of the current map.  They are not reverse projected.
     */
    public void setEnvelope(Envelope inEnvelope) throws Exception{
        
        if (myFollowMap){
            // find the point at the center of the map.
            double x = (inEnvelope.getMinX()+inEnvelope.getMaxX())/2;
            double y = (inEnvelope.getMinY()+inEnvelope.getMaxY())/2;

            // convert this point to lat long
            Point tempPoint = new Point(x,y);
            projectBackward(tempPoint);

            // set the zone based on this point
            setZone(getUTMZone(tempPoint.getX(), tempPoint.getY()));
        }
    }
    
    /** return the panel needed to edit this projection  */
    public ProjectionPanel getEditPanel() {
        UniversalTransverseMercatorProjectionPanel tempPanel = new UniversalTransverseMercatorProjectionPanel();
        tempPanel.setProjection(this);
        return tempPanel;
    }
    
    /** Display the type of projection */
    public String toString(){return getProjectionName();}
    
    /** clone this object*/
    public Object clone(){
        UniversalTransverseMercatorProjection tempProjection = new UniversalTransverseMercatorProjection();
        try{
            tempProjection.setZone(getZone());
        }
        catch (Exception e){
            // not sure what to do here.
            System.out.println(e);
            e.printStackTrace();
        }
        return tempProjection;
    }    
        
}
