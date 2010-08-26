package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;

public interface ILayerDefinition extends IData
{
	public URL getLayerURL();
	public boolean isEnabled();
	public boolean isDefault();
}
