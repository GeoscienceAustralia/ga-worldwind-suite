package au.gov.ga.worldwind.panels.layers;

import java.net.URL;

import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;

public class LayerNode extends AbstractNode implements ILayerNode
{
	private URL layerURL;
	private URL descriptionURL;
	private boolean enabled;

	public LayerNode(String name, URL iconURL, boolean expanded, URL layerURL, URL descriptionURL,
			boolean enabled)
	{
		super(name, iconURL, expanded);
		setLayerURL(layerURL);
		setDescriptionURL(descriptionURL);
		setEnabled(enabled);
	}

	public URL getLayerURL()
	{
		return layerURL;
	}

	public void setLayerURL(URL layerURL)
	{
		this.layerURL = layerURL;
	}

	public URL getDescriptionURL()
	{
		return descriptionURL;
	}

	public void setDescriptionURL(URL descriptionURL)
	{
		this.descriptionURL = descriptionURL;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public static LayerNode createFromLayerDefinition(ILayerDefinition definition)
	{
		return new LayerNode(definition.getName(), definition.getIconURL(), true, definition
				.getURL(), definition.getDescriptionURL(), true);
	}
}