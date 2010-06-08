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

package gistoolkit.projection.ellipsoid;

import java.util.Vector;

import java.io.*;
import gistoolkit.projection.Ellipsoid;
import gistoolkit.projection.ellipsoid.EPSGEllipsoid;
/**
 * Reads the Ellipsoid text file to create the requested Ellipsoid.
 */
public class EllipsoidFactory extends Object {
    
    /** Creates new EllipsoidFactory */
    public EllipsoidFactory() {
    }
    
    /** Retrieve the ellipsoid by name */
    public static Ellipsoid getEllipsoid(String inName){
        if (inName == null) return null;
        
        // Retrieve the list of ellipsoids
        Ellipsoid[] tempEllipsoids = getKnownEllipsoids();
        if (tempEllipsoids == null) return null;
        
        // find the one with this name
        for (int i=0; i<tempEllipsoids.length; i++){
            if (inName.equalsIgnoreCase(tempEllipsoids[i].getName())){
                return tempEllipsoids[i];
            }
        }
        
        // didn't find it.
        return null;
    }
    
    private static Ellipsoid[] myEllipsoids = null;
    /** Return the list of know ellipsoids */
    public static Ellipsoid[] getKnownEllipsoids(){
        if (myEllipsoids != null) return myEllipsoids;
        
        // read the list from the File
        String tempFilename = "Ellipsoid.txt";
        InputStream in = new EllipsoidFactory().getClass().getResourceAsStream(tempFilename);
        if (in == null) {
            System.out.println("Resource " + tempFilename + " can not be found");
            return new Ellipsoid[0];
        }
        else{
            Vector tempVectEllipsoids = new Vector();
            InputStreamReader inread = new InputStreamReader(in);
            BufferedReader bread = new BufferedReader(inread);
            try{
            String tempLine = bread.readLine();
            while (tempLine != null){
                // The first element in the string is the OGC Identifying Number.
                String[] tempElements = parse(tempLine, '|');
                EPSGEllipsoid tempEllipsoid = new EPSGEllipsoid();
                boolean tempAdd = true;
                if (tempElements.length > 7){
                    // First element is the code
                    tempEllipsoid.setCode(tempElements[0]);
                    
                    // second element is the name
                    tempEllipsoid.setName(tempElements[1]);
                    
                    // third element is the major axis
                    double a = 0;
                    try{
                        a = Double.parseDouble(tempElements[2]);
                        tempEllipsoid.setMajorAxis(a);
                    }
                    catch (NumberFormatException e){
                        System.out.println("NumberFormatException Parsing Major Axis ="+tempElements[4]+" in Ellipsoid "+tempElements[1]);
                        tempAdd = false;
                    }
                    
                    // Fourth element is the Axis Units code
                    tempEllipsoid.setUnitOfMeasure(tempElements[3]);
                    
                    // Fifth element is the Inverse flattening
                    tempEllipsoid.setMinorAxis(0);
                    try{
                        if ((tempElements[4] != null) && (tempElements[4].trim().length() > 0)){
                            double f = Double.parseDouble(tempElements[4]);
                            // calculate the semiMinorAxis
                            double b  = a-a*(1.0/f);
                            tempEllipsoid.setMinorAxis(b);
                        }
                    }
                    catch (NumberFormatException e){
                        System.out.println("NumberFormatException Parsing Inverse Flattening = "+tempElements[4]+" in Ellipsoid "+tempElements[1]);
                        tempAdd = false;
                    }
                    
                    // Sixth element is the SemiMinor Axis
                    try{
                        if ((tempElements[5] != null) && (tempElements[5].trim().length() > 0)){
                            double b = Double.parseDouble(tempElements[5]);
                            tempEllipsoid.setMinorAxis(b);
                        }
                    }
                    catch (NumberFormatException e){
                        System.out.println("NumberFormatException Parsing Minor Axis = "+tempElements[4]+" in Ellipsoid "+tempElements[1]);
                        tempAdd = false;
                    }
                    
                    if (tempEllipsoid.getMinorAxis() == 0) {
                        System.out.println("No Minor Axis found for Ellipsoid "+tempElements[2]);
                        tempAdd = false;
                    }
                    
                    // Seventh element is the remarks
                    tempEllipsoid.setRemarks(tempElements[6]);
                    
                    // Eight element is the Information Source
                    tempEllipsoid.setInformationSource(tempElements[7]);
                }
                if (tempAdd) tempVectEllipsoids.addElement(tempEllipsoid);
                tempLine = bread.readLine();
            }   
            }
            catch(IOException e){
                System.out.println("IOException reading Ellipsoid.txt");
                return new Ellipsoid[0];
            }
            myEllipsoids = new Ellipsoid[tempVectEllipsoids.size()];
            tempVectEllipsoids.copyInto(myEllipsoids);
        }
        return myEllipsoids;
    }
    
    /** Parse the line into it's Tokens */
    public static String[] parse(String inString, char inToken){
        if (inString == null) return new String[0];
        
        Vector tempVectStrings = new Vector();
        int tempIndex = inString.indexOf(inToken);
        int tempOffset = 0;
        while (tempIndex != -1){
            tempVectStrings.addElement(inString.substring(tempOffset, tempIndex));
            tempOffset = tempIndex + 1;
            if (tempOffset < inString.length()){
                tempIndex = inString.indexOf(inToken, tempOffset);
            }
            else{
                break;
            }
        }
        if (tempOffset < inString.length()) tempVectStrings.add(inString.substring(tempOffset));
        
        String[] tempStrings = new String[tempVectStrings.size()];
        tempVectStrings.copyInto(tempStrings);
        for (int i=0; i<tempStrings.length; i++){
            tempStrings[i] = tempStrings[i].trim();
            if (tempStrings[i].length() > 0){
                if (tempStrings[i].charAt(0) == '"'){
                    tempStrings[i] = tempStrings[i].substring(1);
                }
            }
            if (tempStrings[i].length() > 0){
                if (tempStrings[i].charAt(tempStrings[i].length()-1) == '"'){
                    tempStrings[i] = tempStrings[i].substring(0,tempStrings[i].length()-1);
                }
            }
        }
        return tempStrings;
    }
    
    public static void main(String[] inArgs){
        Ellipsoid[] tempEllipsoids = EllipsoidFactory.getKnownEllipsoids();
        System.out.println(tempEllipsoids.length + " Ellipsoids read");
        for (int i=0; i<tempEllipsoids.length; i++){
            System.out.println("Name="+tempEllipsoids[i].getName()+" Major="+tempEllipsoids[i].getMajorAxis()+" Minor="+tempEllipsoids[i].getMinorAxis());
        }
    }
}
