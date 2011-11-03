/*
 * LoaderFactory.java
 *
 * Created on February 27, 2008, 10:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.nasa.worldwind.formats.models.loader;

import gov.nasa.worldwind.formats.models.ModelLoadException;
import gov.nasa.worldwind.formats.models.geometry.Model;


/**
 *
 * @author Brian Wood
 */
public class LoaderFactory {
    private static final int FILETYPE_UNKNOWN =    -1;
    private static final int FILETYPE_3DS  =	    1;
    private static final int FILETYPE_OBJ  =	    2;
    private static final int FILETYPE_DAE = 		3;
    
    public static Model load(String source) throws ModelLoadException {
        iLoader loader = getLoader(source);
        if (loader == null)
            return null;
        
        return loader.load(source);
    }
    
    private static iLoader getLoader(String path) {
        switch(determineFiletype(path)) {
            case FILETYPE_3DS:
                return new MaxLoader();

            case FILETYPE_OBJ:
                return new WaveFrontLoader();
                
            case FILETYPE_DAE:
            	return new ArdorColladaLoader();
            	//TODO: Smoother switch between ardor and JAXB loaders
            	//return new ColladaLoader();
                
            default:
                return null;
        }
    }
    
    /**
     * Parses the file suffix to determine what file format the model is in.
     *
     * @param path File path info
     * @returns int Constant indicating file type
     */
    private static int determineFiletype(String path) {
	int type = FILETYPE_UNKNOWN;
	String tokens[] = path.split("\\.");	
	
	if(tokens[tokens.length - 1].equals("3ds"))
	    type = FILETYPE_3DS;
	else if(tokens[tokens.length - 1].equals("obj"))
	    type = FILETYPE_OBJ;
	else if(tokens[tokens.length - 1].equals("dae"))
	    type = FILETYPE_DAE;
	    
	return type;
    }
}
