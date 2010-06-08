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

package gistoolkit.datasources;

/**
 * Rastor data sources tend to need to know what the pixel resolution of the resulting image should be.
 * They use this information to determine what resolution of image to generate, and where to retrieve the information from among other things.
 */
public interface RasterDatasource extends DataSource{

    /** 
     * Set the width of the image to retrieve.
     * The data source can use this information to generate the appropriate rastor shapes.
     * Several shapes may be generated or just one that cover this area.  This is just a 
     * suggestion.
     */
    public void setImageWidth(int inWidth);
    
    /** 
     * Set the height of the image to retrieve.
     * The data source can use this information to generate the appropriate rastor shapes.
     * Several shapes may be generated or just one that cover this area.  This is just a 
     * suggestion.
     */
    public void setImageHeight(int inHeight);
    
}

