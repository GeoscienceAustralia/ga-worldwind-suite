package au.gov.ga.worldwind.panels.layers;

import java.net.URL;

public interface ILayerNode extends INode
{
	public URL getLayerURL();

	//public void setLayerURL(URL layerURL);

	public URL getDescriptionURL();

	//public void setDescriptionURL(URL descriptionURL);

	public boolean isEnabled();

	public void setEnabled(boolean enabled);
}
