/*
 * Model3DFactory.java
 *
 * Created on February 27, 2008, 10:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.nasa.worldwind.formats.models;


import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.formats.models.loader.LoaderFactory;

import java.util.HashMap;

/**
 *
 * @author Brian Wood
 * modifications made by Greg Rodgers
 */
public class ModelFactory {
    
    private static HashMap<Object, Model> modelCache = new HashMap<Object, Model>();
        
    public static Model createModel(String source) throws ModelLoadException {
        Model model = modelCache.get(source);
        if (model == null) {
            model = LoaderFactory.load(source);
            modelCache.put(source, model);
        }
        
        if (model == null)
            throw new ModelLoadException();
        
        return model;
    }
}
