package au.gov.ga.worldwind.panels.layers;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

public class LoadedLayer
{
	private Layer layer;
	private ElevationModel elevationModel;
	private URL legendURL;
	private URL queryURL;

	public LoadedLayer(Object o)
	{
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

	public boolean isLayer()
	{
		return layer != null;
	}

	public Layer getLayer()
	{
		return layer;
	}

	public boolean isElevationModel()
	{
		return elevationModel != null;
	}

	public ElevationModel getElevationModel()
	{
		return elevationModel;
	}

	public URL getLegendURL()
	{
		return legendURL;
	}

	public void setLegendURL(URL legendURL)
	{
		this.legendURL = legendURL;
	}

	public URL getQueryURL()
	{
		return queryURL;
	}

	public void setQueryURL(URL queryURL)
	{
		this.queryURL = queryURL;
	}
}
