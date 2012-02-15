package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Setupable;

/**
 * Container class that is used when loading a layer or elevation model from a
 * layer definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LoadedLayer implements Setupable
{
	private Layer layer;
	private ElevationModel elevationModel;
	private final AVList params;
	private URL legendURL;
	private URL queryURL;

	public LoadedLayer(Object o, AVList params)
	{
		this.params = params;

		if (o == null)
		{
			throw new NullPointerException("Null layer loaded");
		}
		else if (o instanceof Layer)
		{
			layer = (Layer) o;
		}
		else if (o instanceof ElevationModel)
		{
			elevationModel = (ElevationModel) o;
		}
		else
		{
			throw new IllegalArgumentException("Unknown layer type loaded: " + o);
		}
	}

	/**
	 * @return Does this represent a layer?
	 */
	public boolean isLayer()
	{
		return layer != null;
	}

	/**
	 * @return The loaded layer
	 */
	public Layer getLayer()
	{
		return layer;
	}

	/**
	 * @return Does this represent an elevation model?
	 */
	public boolean isElevationModel()
	{
		return elevationModel != null;
	}

	/**
	 * @return The loaded elevation model
	 */
	public ElevationModel getElevationModel()
	{
		return elevationModel;
	}

	/**
	 * @return The loaded layer or elevation model
	 */
	public Object getLoadedObject()
	{
		return isLayer() ? layer : elevationModel;
	}

	/**
	 * @return The URL pointing at the legend
	 */
	public URL getLegendURL()
	{
		return legendURL;
	}

	/**
	 * Set the legend URL
	 * 
	 * @param legendURL
	 */
	public void setLegendURL(URL legendURL)
	{
		this.legendURL = legendURL;
	}

	/**
	 * @return The URL to used for querying points in the loaded layer
	 */
	public URL getQueryURL()
	{
		return queryURL;
	}

	/**
	 * Set the query URL
	 * 
	 * @param queryURL
	 */
	public void setQueryURL(URL queryURL)
	{
		this.queryURL = queryURL;
	}

	/**
	 * @return The attribute-value list parameters read when loading the layer
	 *         definition
	 */
	public AVList getParams()
	{
		return params;
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		if (isLayer() && getLayer() instanceof Setupable)
		{
			((Setupable) getLayer()).setup(wwd);
		}
		else if (isElevationModel() && getElevationModel() instanceof Setupable)
		{
			((Setupable) getElevationModel()).setup(wwd);
		}
	}
}
