/*
 * iLoader.java
 *
 * Created on February 27, 2008, 10:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.nasa.worldwind.formats.models.loader;

import gov.nasa.worldwind.formats.models.ModelLoadException;
import gov.nasa.worldwind.formats.models.geometry.Model;


/**
 *
 * @author RodgersGB
 */
public interface iLoader {
    public Model load(String path) throws ModelLoadException;
}
