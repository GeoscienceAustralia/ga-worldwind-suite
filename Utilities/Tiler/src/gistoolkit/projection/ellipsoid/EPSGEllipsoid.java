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

package gistoolkit.projection.ellipsoid;

/**
 * Place to store the additional imformation provided by EPSG.
 * @author  ithaqua
 */
public class EPSGEllipsoid extends SimpleEllipsoid {

    /** Creates new EPSGEllipsoid */
    public EPSGEllipsoid() {
    }
    
    /** The EPSG Number or Code */
    private String myCode = "";
    /** Return the EPSG code for this ellipsoid */
    public String getCode(){return myCode;}
    /** Set the EPSG code for this ellipsoid */
    public void setCode(String inCode){myCode = inCode;}
    
    /** Additional Remarks about the ellipsoid */
    private String myRemarks = "";
    /** Return the EPSG Remarks for this ellipsoid */
    public String getRemarks(){return myRemarks;}
    /** Set the remarks for this ellipsoid */
    public void setRemarks(String inRemarks){myRemarks = inRemarks;}
    
    /** The information source for this Ellipsoid */
    private String myInformationSource = "";
    /** Return the InformationSource for this Ellipsoid */
    public String getInformationSource(){return myInformationSource;}
    /** Set the InformationSource for this Ellipsoid*/
    public void setInformationSource(String inInformationSource){myInformationSource = inInformationSource;}    
}
