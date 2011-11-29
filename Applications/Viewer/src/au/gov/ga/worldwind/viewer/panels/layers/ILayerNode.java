package au.gov.ga.worldwind.viewer.panels.layers;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Loader;

public interface ILayerNode extends INode
{
	URL getLayerURL();

	URL getLegendURL();

	void setLegendURL(URL legendURL);

	URL getQueryURL();

	void setQueryURL(URL queryURL);

	boolean isEnabled();

	void setEnabled(boolean enabled);

	double getOpacity();

	void setOpacity(double opacity);

	boolean hasError();

	Exception getError();

	void setError(Exception error);

	boolean isLayerLoading();

	void setLayerLoading(boolean layerLoading);
	
	boolean isLayerDataLoading();
	
	void setLayerDataLoading(boolean layerDataLoading);
	
	void setLoader(Loader loader);

	Long getExpiryTime();

	void setExpiryTime(Long expiryTime);
}
