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

package gistoolkit.display.shader;

/**
 * Class to hold the information for a particular entry in the bin shader.
 */
public class BinEntry {
    
    /** The values that are shaded with this range. */
    private String[] myValuesList = new String[0];
    /** Return the values for this entry. */
    public String[] getValues(){return myValuesList;}
    /** The minimum values for this entry. */
    private double[] myMinList = new double[0];
    /** The maximum values for this entry. */
    private double[] myMaxList = new double[0];
    
    /** Creates new BinEntry */
    public BinEntry() {
    }
    /** Creates new BinEntry with the given values*/
    public BinEntry(String[] inValues, double[] inMinValues, double[] inMaxValues) {
        myValuesList = inValues;
        myMinList = inMinValues;
        myMaxList = inMaxValues;
    }
    
    /** Add a bin to this bin entry. */
    public void addBin(double inMin, double inMax){
        double[] tempCurMaxDouble = myMaxList;
        double[] tempCurMinDouble = myMinList;
        double[] tempMaxDouble = new double[myMaxList.length+1];
        double[] tempMinDouble = new double[myMinList.length+1];
        for (int j=0; j<myMinList.length; j++){
            tempMaxDouble[j] = tempCurMaxDouble[j];
            tempMinDouble[j] = tempCurMinDouble[j];
        }
        tempMaxDouble[tempCurMaxDouble.length] = 0.0;
        tempMinDouble[tempCurMinDouble.length] = 0.0;
        myMaxList = tempMaxDouble;
        myMinList = tempMinDouble;
    }
    
    /** Remove the entry from this bin entry. */
    public void removeBin(int inIndex){
        // if the index is out of range, do nothing. could throw index out of range exception?
        if (inIndex >= myMaxList.length) return;
        if (inIndex < 0) return;
        double[] tempCurMaxDouble = myMaxList;
        double[] tempCurMinDouble = myMinList;
        double[] tempMaxDouble = new double[myMaxList.length-1];
        double[] tempMinDouble = new double[myMinList.length-1];
        
        for (int k=0; k<myMaxList.length; k++){
            if (k < inIndex){
                tempMaxDouble[k] = tempCurMaxDouble[k];
                tempMinDouble[k] = tempCurMinDouble[k];
            }
            if (k > inIndex){
                tempMaxDouble[k-1] = tempCurMaxDouble[k];
                tempMinDouble[k-1] = tempCurMinDouble[k];
            }
        }
        myMaxList = tempMaxDouble;
        myMinList = tempMinDouble;
    }
    
    /** Determines if this entry contains this value. */
    public boolean containsValue(String inValue){
        if (inValue == null) return false;
        if (myValuesList == null) return false;
        if (myValuesList.length == 0) return true;
        for (int i=0; i<myValuesList.length; i++){
            if (myValuesList[i] != null){
                if (myValuesList[i].equals(inValue)){
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Get the number of bins. */
    public int getBinCount(){ return myMaxList.length; }
    /** Get the min at the given index. */
    public double getMin(int inIndex){return myMinList[inIndex];}
    /** Get the max at the given index. */
    public double getMax(int inIndex){return myMaxList[inIndex];}
    
    
    /** Based on this value, find the bin where this row resides, will return -1 if no bin exists. */
    public int getBin(double inValue){
        for (int i=0; i<myMaxList.length; i++){
            if ((inValue <= myMaxList[i]) && (inValue >= myMinList[i])){
                return i;
            }
        }
        return -1;
    } 
}
