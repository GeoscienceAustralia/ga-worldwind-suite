/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2003, Ithaqua Enterprises Inc.
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
 * Class to provide comparisons of numbers within an SQL String.
 */
public class SQLNumberAttributeFilter extends NumberAttributeFilter implements SQLFilter{
    
    /** Creates a new instance of SQLNumberAttributeFilter */
    public SQLNumberAttributeFilter() {
    }

	/**Constructor for SQLNumberAttributeFilter.*/
	public SQLNumberAttributeFilter(String inAttributeName,	int inComparison, double inValue) {
		super(inAttributeName, inComparison, inValue);
	}

    /** Returns the actual SQL String to return to the database. */
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
		Double tempAttributeValue = (Double) getAttributeValue();
		if (tempAttributeValue == null) return null;
        return getAttributeName()+tempComp+inConverter.toSQLDecimal(tempAttributeValue, new AttributeType(AttributeType.FLOAT));
	}        
}
