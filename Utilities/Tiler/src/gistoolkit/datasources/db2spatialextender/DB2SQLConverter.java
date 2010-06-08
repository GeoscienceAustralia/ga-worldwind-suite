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
package gistoolkit.datasources.db2spatialextender;

import java.util.*;
import gistoolkit.features.*;
import gistoolkit.datasources.SimpleSQLConverter;

/**
 * Class to convert java types and AttributeTypes to SQL for a particular database.
 */
public class DB2SQLConverter extends SimpleSQLConverter{
    /** Convert a java date into the JDBC date representaion for this database. */
    public String toSQLDate(java.util.Date inDate, AttributeType inAttributeType){
        // Create the String representation of the date.
        Calendar c = Calendar.getInstance();
        c.setTime(inDate);
        int Year = c.get(Calendar.YEAR);
        int Month = c.get(Calendar.MONTH)+1;
        int Day = c.get(Calendar.DAY_OF_MONTH);
        int Hour = c.get(Calendar.HOUR_OF_DAY);
        int Min = c.get(Calendar.MINUTE);
        int Sec = c.get(Calendar.SECOND);
        return "TIMESTAMP('"+Year+to2(Month)+to2(Day)+to2(Hour)+to2(Min)+to2(Sec)+"')";
    }
    /** Convert an integer to a two digit string. */
    private String to2(int inInt){
        String tempString = ""+inInt;
        if (tempString.length() == 0) return "00";
        if (tempString.length() == 1) return "0"+tempString;
        if (tempString.length() == 2) return tempString;
        return "99";
    }
}