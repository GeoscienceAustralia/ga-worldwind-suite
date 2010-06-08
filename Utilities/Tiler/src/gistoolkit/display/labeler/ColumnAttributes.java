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

package gistoolkit.display.labeler;

/**
 *
 * Class to contain the attributes of a column for labeling purposes.
 */
public class ColumnAttributes {
    
    /** Available Formats. */
    public static final int FORMAT_NONE = 0;
    public static final int FORMAT_NUMBER = 1;
    public static final int FORMAT_CURRENCY = 2;
    

    /** The number of the column to label by. */
    private int myColumnNum = 0;
    /** Set the number of the column to label. */
    public void setColumnNum(int inNum){myColumnNum = inNum;}
    /** Get the number of the column to label.  */
    public int getColumnNum(){return myColumnNum;}
    
    /** The format to apply to the column. */
    private int myColumnFormat = 0;
    /** Set the format to apply to the column. */
    public void setColumnFormat(int inColumnFormat){myColumnFormat = inColumnFormat;}
    /** Get the format to apply to the column.*/
    public int getColumnFormat(){return myColumnFormat;}
    
    /** The string to append before the column. */
    private String myColumnPreString = "";
    /** Set the string to append before the column. */
    public void setColumnPreString(String inString){ myColumnPreString = inString;}
    /** Get the string to append before the column. */
    public String getColumnPreString(){return myColumnPreString;}
    
    /** The string to append after the column. */
    private String myColumnPostString = "";
    /** Set the string to append after the column. */
    public void setColumnPostString(String inString){ myColumnPostString = inString;}
    /** Get the string to append after the column. */
    public String getColumnPostString(){return myColumnPostString;}

    /** Creates a new instance of ColumnAttributes */
    public ColumnAttributes(int inColumnNum, int inColumnFormat, String inColumnPreString, String inColumnPostString){
        myColumnNum = inColumnNum;
        myColumnFormat = inColumnFormat;
        myColumnPreString = inColumnPreString;
        myColumnPostString = inColumnPostString;
    }    
}
