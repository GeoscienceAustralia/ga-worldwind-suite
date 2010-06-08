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
import gistoolkit.features.AttributeType;

/**
 * Class to provide date filtering on attributes in an SQL oriented data source.
 */
public class SQLStringAttributeFilter extends StringAttributeFilter implements SQLFilter {
    
    /** Creates new SQLDateAttributeFilter for use with the configuration utility only.*/
    public SQLStringAttributeFilter() {
    }
    
    /** Yah, I know I need a different place for the toSQL functions, than SimpleDBDataSource, I'm working on it.  */
    public String getFilterSQL(SQLConverter inConverter) {
        int tempComparison = getComparison();
        String tempComp = "=";
        if (tempComparison == ATTRIBUTE_EQUALS){
            tempComp = "=";
        }
        if (tempComparison == ATTRIBUTE_GREATER){
            tempComp = ">";
        }
        if (tempComparison == ATTRIBUTE_LESS){
            tempComp = "<";
        }
        String tempAttributeValue = (String) getAttributeValue();
        if (tempAttributeValue == null) return null;
        if (tempAttributeValue.length() == 0) return null;
        return getAttributeName()+tempComp+inConverter.toSQLString(tempAttributeValue, new AttributeType(AttributeType.STRING));
    }    
}
