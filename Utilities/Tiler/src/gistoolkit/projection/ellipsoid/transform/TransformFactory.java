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

package gistoolkit.projection.ellipsoid.transform;

import java.io.*;
import gistoolkit.projection.*;
import gistoolkit.projection.ellipsoid.*;

/**
 * Class to contain the list of all known transforms, and to find prepopulated transforms for a given set of ellipsoids.
 */
public class TransformFactory extends Object {
    
    /** Creates new TransformFactory */
    public TransformFactory() {
    }
    
    /** List the known Transforms */
    private EllipsoidTransform[] myTransforms = {
        new NoTransform(),
        new LongitudeRotation(),
        new LatitudeTranslation(),
        new GeocentricTranslation(),
        new PositionVectorTransform(),
        new CoordinateFrameRotation()
    };
    
    /** Return the list of know transforms */
    public EllipsoidTransform[] getKnownTransforms(){return myTransforms;}
    
    /** Find the ellipsoid transform and parameters for converting between the given ellipsoids */
    public EllipsoidTransform getTransform(Ellipsoid inFromEllipsoid, Ellipsoid inToEllipsoid){
        if ((inFromEllipsoid == null) || (inToEllipsoid == null)) return null;
        
        // read the list from the File
        String tempFilename = "Transform.txt";
        InputStream in = new TransformFactory().getClass().getResourceAsStream(tempFilename);
        if (in == null) {
            System.out.println("Resource " + tempFilename + " can not be found");
            return null;
        }
        else{
            InputStreamReader inread = new InputStreamReader(in);
            BufferedReader bread = new BufferedReader(inread);
            try{
                
                String[] tempForwardElements = null; // a forward conversion was found
                String[] tempReverseElements = null; // a reverse conversion was found
                String tempLine = bread.readLine();
                while (tempLine != null){
                    tempLine = tempLine.trim();
                    // lines starting with # are comments.
                    if (!tempLine.startsWith("#")){
                        // The first element in the string is the Identifying Name.
                        String[] tempElements = EllipsoidFactory.parse(tempLine, '|');
                        if (tempElements[0].equalsIgnoreCase(inFromEllipsoid.getName()) && (tempElements[1].equalsIgnoreCase(inToEllipsoid.getName()))){
                            tempForwardElements = tempElements;
                            break;
                        }
                        if (tempElements[0].equalsIgnoreCase(inToEllipsoid.getName()) && (tempElements[1].equalsIgnoreCase(inFromEllipsoid.getName()))){
                            if (tempElements[3].equalsIgnoreCase("YES")){ // the reversable parameter
                                if (tempReverseElements == null) // use the first reverse found, but search for a forward first.
                                    tempReverseElements = tempElements;
                            }
                        }
                    }
                    tempLine = bread.readLine();
                }
                
                // if a forward trans form was found, then create it
                if (tempForwardElements != null) return createTransform(tempForwardElements);
                if (tempReverseElements != null) {
                    EllipsoidTransform tempTransform = createTransform(tempReverseElements);;
                    if (tempTransform != null){
                        tempTransform.setIsReversed(true);
                        return tempTransform;
                    }
                }
            }
            catch(IOException e){
                System.out.println("IOException reading Ellipsoid.txt");
            }
        }
        if (inFromEllipsoid.getName().equalsIgnoreCase(inToEllipsoid.getName())) return new NoTransform();
        return null;
    }
    
    private EllipsoidTransform createTransform(String[] inElements){
        
        // The first elements is the name of the From Ellipse to create
        Ellipsoid tempFromEllipsoid = EllipsoidFactory.getEllipsoid(inElements[0]);
        if (tempFromEllipsoid == null) return null;
        
        // The second element is the name of the To Ellipsoid
        Ellipsoid tempToEllipsoid = EllipsoidFactory.getEllipsoid(inElements[1]);
        if (tempToEllipsoid == null) return null;
        
        // The third element indicates if this transform is reverseable
        
        // The fourth element is the name of the transform.
        EllipsoidTransform tempTransform = null;
        if (inElements[2].equalsIgnoreCase("NoConversion")) tempTransform = new NoTransform();
        if (inElements[2].equalsIgnoreCase("PositionVectorTransform")) tempTransform = new PositionVectorTransform();
        if (inElements[2].equalsIgnoreCase("CoordinateFrameRotation")) tempTransform = new CoordinateFrameRotation();
        if (inElements[2].equalsIgnoreCase("LongitudeRotation")) tempTransform = new LongitudeRotation();
        if (tempTransform == null) return null;
        
        // the rest of the elements are the parameters for the transform
        tempTransform.setToEllipsoid(tempToEllipsoid);
        tempTransform.setFromEllipsoid(tempFromEllipsoid);
        
        for (int i=4; i<inElements.length; i++){
            String tempString = inElements[i];
            int tempIndex = tempString.indexOf('=');
            if (tempIndex == -1){
                tempTransform.setParameter(tempString, null);
            }
            else{
                String tempName = tempString.substring(0,tempIndex);
                String tempValue = null;
                if (tempIndex+1 < tempString.length()){
                    tempValue = tempString.substring(tempIndex+1);
                }
                tempTransform.setParameter(tempName, tempValue);
            }
        }
        return tempTransform;
    }
}
