package au.gov.ga.worldwind.common.layers.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import au.gov.ga.worldwind.common.util.FastShape;

public class LocalModelLayer extends AbstractModelLayer implements ModelLayer
{
	public LocalModelLayer(List<FastShape> shapes)
	{
		super(shapes);
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
