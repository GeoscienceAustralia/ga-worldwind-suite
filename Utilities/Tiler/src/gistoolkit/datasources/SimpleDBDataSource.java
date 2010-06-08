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
import gistoolkit.datasources.filter.*;
/**
 * Contains many convenience methods for building a data source based on a database.
 */
public abstract class SimpleDBDataSource extends SimpleDataSource{
    
    /** Creates new SimpleDBDataSource */
    public SimpleDBDataSource() {
    }

    /** The filter SQL generated when this filter is set. */
    private String myFilterSQL = null;
    /** The currently active filter. */
    private Filter mySQLFilter = null;
    /** Returns the SQL for the filters. */
    public String getFilterSQL(){
        if (myFilterSQL == null) return "";
        return myFilterSQL;
    }
    
    /** Retrieve the filter from the data source. */
    public Filter getFilter(){
        if (mySQLFilter == null){
            return super.getFilter();
        }
        else{
            return mySQLFilter;
        }
    }
                
    /** Set the filters into this data source. */
    public void setFilter(Filter inFilter){
        if (inFilter instanceof SQLFilter){
            myFilterSQL = ((SQLFilter) inFilter).getFilterSQL(getSQLConverter());
            mySQLFilter = (SQLFilter) inFilter;
            clearCache();
        }
        else{
            super.setFilter(inFilter);
            mySQLFilter = null;
        }

        if (inFilter == null){
            myFilterSQL = null;
            clearCache();
        }
    }
    
    /** Returns the converter for this Database. */
    public SQLConverter getSQLConverter(){return new SimpleSQLConverter();}

}
