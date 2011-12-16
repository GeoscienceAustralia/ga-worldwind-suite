package au.gov.ga.worldwind.common.layers.model;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.util.FastShape;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

public class LocalModelLayer extends AbstractLayer implements ModelLayer
{
	private FastShape shape;
	
	public LocalModelLayer(FastShape shape)
	{
		this.shape = shape;
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return null;
	}

	@Override
	public String getDataCacheName()
	{
		return null;
	}

	@Override
	public boolean isLoading()
	{
		return false;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
	}

	@Override
	public void setup(WorldWindow wwd)
	{
	}

	@Override
	public Sector getSector()
	{
		if(shape == null)
		{
			return null;
		}
		return shape.getSector();
	}

	@Override
	public FastShape getShape()
	{
		return shape;
	}

	@Override
	public void setShape(FastShape shape)
	{
		this.shape = shape;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if(!isEnabled() || shape == null)
		{
			return;
		}
		shape.render(dc);
	}
}
