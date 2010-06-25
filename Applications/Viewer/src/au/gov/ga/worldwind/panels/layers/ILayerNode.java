package au.gov.ga.worldwind.panels.layers;

import java.net.URL;

public interface ILayerNode extends INode
{
	public URL getLayerURL();

	public URL getLegendURL();

	public void setLegendURL(URL legendURL);

	public URL getQueryURL();

	public void setQueryURL(URL queryURL);

	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public double getOpacity();

	public void setOpacity(double opacity);

	public boolean hasError();

	public Exception getError();

	public void setError(Exception error);

	public boolean isLayerLoading();

	public void setLayerLoading(boolean layerLoading);

	public Long getExpiryTime();

	public void setExpiryTime(Long expiryTime);
}
