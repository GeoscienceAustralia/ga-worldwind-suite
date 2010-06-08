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

import gistoolkit.features.*;
/**
 * Used to notify a datasource listener that something has occured within a data source.
 */
public class DataSourceEvent extends Object {
    
    /**
     * Record of interest to this event. May not always apply.
     */
    private Record myRecord;

    /** Creates new DataSourceEvent */
    public DataSourceEvent(Record inRecord) {
        myRecord = inRecord;
    }

}
