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

package gistoolkit.datasources.filter;
import gistoolkit.datasources.SQLConverter;

/**
 * Class to contain SQLFilters.
 */
public class SQLJoin extends Join implements SQLFilter{

    /** Creates new SQLExpression, only for use with set node. */
    public SQLJoin() {
    }

    /** Create a new SQLExpression with these two filters. */
    public SQLJoin(SQLFilter inFilter1, int inComparison, SQLFilter inFilter2){
        super(inFilter1, inComparison, inFilter2);
    }
    
    /** The SQL functions have been moved to database specific SQLConverters. */
    public String getFilterSQL(SQLConverter inSQLConverter) {
        String tempComparison = " AND ";
        if (getComparison() == OR) tempComparison = " OR "; 
        String tempFilter1SQL = ((SQLFilter)getFilter1()).getFilterSQL(inSQLConverter);
        String tempFilter2SQL = ((SQLFilter)getFilter2()).getFilterSQL(inSQLConverter);
        if ((tempFilter1SQL == null) || (tempFilter1SQL.length() == 0)){
            if ((tempFilter2SQL == null) || (tempFilter2SQL.length() == 0)){
                return "";
            }
            System.out.println("SQLJoin, filter2sql="+tempFilter2SQL);
            return tempFilter2SQL;
        }
        else if ((tempFilter2SQL == null) || (tempFilter2SQL.length() == 0)){
            System.out.println("SQLJoin, filter1sql="+tempFilter1SQL);            
            return tempFilter1SQL;
        }
        return "("+tempFilter1SQL+tempComparison+tempFilter2SQL+")";
    }
    
}
