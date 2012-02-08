package au.gov.ga.worldwind.common.layers.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.common.util.FastShape;

/**
 * Simple implementation of {@link ModelLayer} that renders a list of
 * {@link FastShape}s provided in the class constructor.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocalModelLayer extends AbstractModelLayer implements ModelLayer
{
	public LocalModelLayer(List<FastShape> shapes)
	{
		super();

		for (FastShape shape : shapes)
		{
			addShape(shape);
		}
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
	protected void requestData()
	{
	}
}
