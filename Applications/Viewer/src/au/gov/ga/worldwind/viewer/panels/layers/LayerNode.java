package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Loader;
import au.gov.ga.worldwind.viewer.panels.dataset.ILayerDefinition;

public class LayerNode extends AbstractNode implements ILayerNode
{
	private URL layerURL;
	private URL legendURL;
	private URL queryURL;
	private boolean enabled;
	private double opacity;
	private Exception error = null;
	private boolean layerLoading = false;
	private boolean layerDataLoading = false;
	private Long expiryTime;
	private Loader loader;

	public LayerNode(String name, URL infoURL, URL iconURL, boolean expanded, URL layerURL,
			boolean enabled, double opacity, Long expiryTime)
	{
		super(name, infoURL, iconURL, expanded);
		setLayerURL(layerURL);
		setEnabled(enabled);
		setOpacity(opacity);
		setExpiryTime(expiryTime);
	}

	@Override
	public URL getLayerURL()
	{
		return layerURL;
	}

	public void setLayerURL(URL layerURL)
	{
		this.layerURL = layerURL;
	}

	@Override
	public URL getLegendURL()
	{
		return legendURL;
	}

	@Override
	public void setLegendURL(URL legendURL)
	{
		this.legendURL = legendURL;
	}

	@Override
	public URL getQueryURL()
	{
		return queryURL;
	}

	@Override
	public void setQueryURL(URL queryURL)
	{
		this.queryURL = queryURL;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		if (enabled && getOpacity() <= 0.2)
			setOpacity(1d);
	}

	public static LayerNode createFromLayerDefinition(ILayerDefinition definition)
	{
		return new LayerNode(definition.getName(), definition.getInfoURL(),
				definition.getIconURL(), true, definition.getLayerURL(), definition.isEnabled(),
				1.0, null);
	}

	@Override
	public boolean isLoading()
	{
		return super.isLoading() || isLayerLoading() || isLayerDataLoading()
				|| (loader != null && loader.isLoading());
	}

	@Override
	public boolean isLayerLoading()
	{
		return layerLoading;
	}

	@Override
	public void setLayerLoading(boolean layerLoading)
	{
		this.layerLoading = layerLoading;
	}

	@Override
	public boolean isLayerDataLoading()
	{
		return layerDataLoading;
	}

	@Override
	public void setLayerDataLoading(boolean layerDataLoading)
	{
		this.layerDataLoading = layerDataLoading;
	}

	@Override
	public void setLoader(Loader loader)
	{
		this.loader = loader;
	}

	@Override
	public boolean hasError()
	{
		return error != null;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public void setError(Exception error)
	{
		this.error = error;
	}

	@Override
	public double getOpacity()
	{
		return opacity;
	}

	@Override
	public void setOpacity(double opacity)
	{
		this.opacity = opacity;
	}

	@Override
	public Long getExpiryTime()
	{
		return expiryTime;
	}

	@Override
	public void setExpiryTime(Long expiryTime)
	{
		this.expiryTime = expiryTime;
	}
}