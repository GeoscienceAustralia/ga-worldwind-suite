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

import gistoolkit.common.*;
import gistoolkit.features.*;
import gistoolkit.display.Style;
import gistoolkit.projection.Projection;
import gistoolkit.datasources.filter.Filter;

/**  The DataSource is responsible for handling inserts, updates, deletes, selects,
 *  and projection of data.
 * <p>
 * There are ReadOnly data sources that do not need to handle inserts updates and deletes.
 * In this case, the data modification methods should do nothing, and the isUpdateable() 
 * method should return false.
 * </p>
 * <p>
 * The DataSource is responsible for projecting the data from the native storage
 * projection, to the projection of the rest of the software. The GISToolkit software assumes
 * all features are in the same projection before operating on them, however as 
 * some DataSources can project much more efficiently that the GISToolkit Projection 
 * Engine can, the  DataSources are responsible for handling these projections.  All data
 * coming out or going into a datasource is assumed to be in the projection of the toolkit,
 * not the native projection of the data.  The two may be the same, and in that case, the
 * datasource may not need to project.
 * </p>
 */
public interface DataSource {

    /**
     * Returns the bounding rectangle of all the shapes in the Data Source.
     * <p>
     * This is used to set the initial context of the view if the datasource alone is selected.
     *  In the case that the bounding rectangle of the DataSource can not be found, a value of null 
     *  should be returned.  In this case, the display context must be set either by another data source,
     *  or by some other piece of information before the datasource is called.
     * </p>
     */
    public Envelope getEnvelope() throws Exception;
    
    /**
     * Sets an identifier string for the datasource.
     */
    public void setName(String inName);
    
    /**
     * Returns the identifier string for the datasource.
     */
    public String getName();
    
    /**
     * Inserts the given record into the datasource.
     * <p>
     * This record may or may not have come from this datasource.  In the case that it has not 
     * come from this datasource, a best attempt is made to insert it into the underlying collection 
     * of records.  There are many restrictions that may cause a datasource to not accept a given 
     * record.  In this case, the insert should throw an exception with an explanation of why this is 
     * happening.  Attributes should be matched by name where available, and datatypes converted 
     * from the record to the datasource where possible.  Extranious of the record should be ignored, 
     * and reasonable defaults should be provided where the record does not contain attributes 
     * present in the data source.
     * </p>
     */
    public void insert(Record inRecord)throws Exception;

    /**
     * Update the data source with the changed record.
     * <p>
     * In this case, the record should have come from this DataSource.  If it has not, then an 
     * exception should be raised indicating this problem.  In addition, the DataSource is 
     * responsible for determining which record in the data source this Record should be 
     * updateing. The Record sent in is almost assuredly a clone() of a record retrieved from 
     * this datasource.
     * </p>
     */
    public void update(Record inRecord) throws Exception;
    
    /**
     * Delete this record from the database.
     * <p>
     * The record sent in should have been selected from this DataSource, if it was not, 
     * then an exception should be thrown that indicates that condition.  This record is 
     * almost assuredly a clone() of the record retrieved from the DataSource, and it is 
     * the responsibility of the DataSource to determine which record within its datastore 
     * is to be updated with the values present in this record.
     * </p>
     */
    public void delete(Record inRecord) throws Exception;

    /**
     * Commit all changes since the last commit.
     * <p>
     * The DataSource interface implements a database type of useage system.  
     * This method is used in that contect to make changes to the DataSource permanent.  
     * The idea is that calling insert, update, or delete will begin a transaction.  All calls to 
     * these methods will take effect within the data source, but may be reversed by the rollback() 
     * method.  Calling the commit() method will make the changes permanent.
     * </p>
     */
    public void commit() throws Exception;
            
    /**
     * Rollback any changes to this datasource since the last commit.
     * The DataSource interface implements a database type of useage system.  
     * This method is used in that contect to remove changes to the DataSource.  
     * The idea is that calling insert, update, or delete will begin a transaction.  All calls to 
     * these methods will take effect within the data source, but may be reversed by the rollback() 
     * method.  Calling the commit() method will make the changes permanent.
     */
    public void rollback() throws Exception;

    /**
     * Reads all the objects from the data source.
     */
    public GISDataset readDataset() throws Exception;
    
    /**
     * Reads only the objects from the data source that intersect these extents.
     * <p>
     * The envelope sent in is in the projected coordinate system. The datasource is
     * responsible for projecting the envelope sent in to the coordinate system of the data,
     *  and projecting the data features from their native coordinate system to the coordinate
     *  system of the rest of the system.
     * </p>
     */
    public GISDataset readDataset(Envelope inEnvelope) throws Exception;
        
    /**
     * Determines if this datasource is updateable.
     * <p>
     * In the cases where the data source is ReadOnly, this method should always return false.
     * </p>
     */
    public boolean isUpdateable();
    
    /**
     * Adds a datasource listener to this datasource.
     */
    public void addDataSourceListener(DataSourceListener inDataSourceListener);
    
    /**
     * Removes the datasource lisener from this datasource.
     */
    public void removeDataSourceListener(DataSourceListener inDataSourceListener);
    
    /**
     * Sets the projection to which this datasource is required to project its contents.
     * <p>
     * The CacheProjected flag indicates to the Data source that the to projection will not 
     * be changing often, and it is OK to project once and cache it. Setting this flag to false 
     * indicates to the DataSource that the toProjection will be changing often.
     * </p>
     */
    public void setToProjection(Projection inProjection, boolean inCacheProjected) throws Exception;
    
    /**
     * Gets the projection to which this datasource is required to project its contents.
     */
    public Projection getToProjection();

    /**
     * Sets the projection to use to convert from the storage media, source projection.
     * <p>
     * This is only a suggestion to the DataSource. In the case where the DataSource 
     * can indipendently determine which projection the underlying data is in, it is expected 
     * the DataSource will use that parameter, and not use the one sent in with this method.
     * In cases where it cannot though, this method may be used to indicated to the DataSource
     *  which projection to use for the underlying data.
     * </p>
     * <p>It is expected that this projection will be run in reverse, to reverse project alread
     * projected data, and that it will not change often so it is OK for this to be done just 
     * once.
     * </p>
     */
    public void setFromProjection(Projection inProjection) throws Exception;
    
    /**
     * Gets the projection to use to convert from the storage media, source projection. 
     * It is expected that this projection will be run in reverse, to reverse project already projected data,
     * and that it will not change often so it is OK for this to be done just once.
     */
    public Projection getFromProjection();
    
    /** Get the configuration information for this data source */
    public Node getNode();
    
    /** Set the configuration information for this data source */
    public void setNode(Node inNode) throws Exception;
    
    /** 
     * Adds a filter to this DataSource.
     * <p>
     * The dataset should only return the subset of the data from the filter that match
     *  both the attributes and the shape criteria that the filter defines.
     * </p>
     */
    public void setFilter(Filter inFilter);
    
    /** Get the filter to use with this datasource. */
    public Filter getFilter();
    
    /** Get the style to use with this datasource. 
     * <p>
     * The datasource may define a default style to use for displaying it.  
     * This is just a suggestion to the renderer.  As this method provides upward dependencies,
     * It should be removed, however it is usefull.
     * </p>
     * */
    public Style getStyle();    
}