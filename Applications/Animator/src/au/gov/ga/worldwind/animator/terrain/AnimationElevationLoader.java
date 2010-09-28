package au.gov.ga.worldwind.animator.terrain;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.globes.ElevationModel;

import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.terrain.ElevationModelFactory;
import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * A factory class that can load an {@link ElevationModel} from a provided {@link URL}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationElevationLoader
{
	
	/**
	 * The factory to use for loading elevation models.
	 * <p/>
	 * Defaults to an instance of {@link ElevationModelFactory}. Can be overridden
	 * with the {@link #setElevationFactory(Factory)} method
	 */
	private static Factory elevationFactory;

	public static Factory getElevationFactory()
	{
		if (elevationFactory == null)
		{
			elevationFactory = (Factory) WorldWind.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
		}
		return elevationFactory;
	}
	
	public static void setElevationFactory(Factory factory)
	{
		if (factory != null)
		{
			elevationFactory = factory;
		}
	}
	
	/**
	 * Load an elevation model from a definition file at the provided location.
	 */
	public static ElevationModel loadElevationModel(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		Element element = XMLUtil.getElementFromSource(url);
		
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, url);
		
		ElevationModel result = (ElevationModel)getElevationFactory().createFromConfigSource(element, params);
		if (result == null)
		{
			throw new IllegalArgumentException("Unable to load elevation model from location " + url);
		}
		
		result.setValue(AVKeyMore.CONTEXT_URL, url);
		
		return result;
	}
}
