package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Basic implementation of a {@link ModelLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicModelLayer extends AbstractModelLayer implements ModelLayer
{
	private ModelProvider provider;

	private String url;
	private URL context;
	private String dataCacheName;

	public BasicModelLayer(AVList params)
	{
		super(new ArrayList<FastShape>());

		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		provider = (ModelProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		color = (Color) params.getValue(AVKeyMore.COLOR);
		lineWidth = (Double) params.getValue(AVKeyMore.LINE_WIDTH);
		pointSize = (Double) params.getValue(AVKeyMore.POINT_SIZE);

		Validate.notBlank(url, "Model data url not set");
		Validate.notBlank(dataCacheName, "Model data cache name not set");

		Validate.notNull(provider, "Model data provider is null");
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	public boolean isLoading()
	{
		return provider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		provider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		provider.removeLoadingListener(listener);
	}

	@Override
	protected void requestData()
	{
		provider.requestData(this);
	}
}
