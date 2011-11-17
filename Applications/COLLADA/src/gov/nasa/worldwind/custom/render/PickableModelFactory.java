/*
 * Model3DFactory.java
 *
 * Created on February 27, 2008, 10:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.nasa.worldwind.custom.render;

import java.util.HashMap;

import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

/**
 * 
 * @author Brian Wood
 * @author Greg Rodgers
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PickableModelFactory
{
	private static HashMap<Object, Model> modelCache = new HashMap<Object, Model>();

	public static Model createModel(String source) throws ModelLoadException
	{
		Model model = modelCache.get(source);
		if (model == null)
		{
			model = PickableLoaderFactory.load(source);
			modelCache.put(source, model);
		}

		if (model == null)
			throw new ModelLoadException();

		return model;
	}
}
