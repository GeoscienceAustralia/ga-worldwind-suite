package au.gov.ga.worldwind.viewer.panels.layers;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Setupable;

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

	public Object getLoadedObject()
	{
		if (isLayer())
			return layer;
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
