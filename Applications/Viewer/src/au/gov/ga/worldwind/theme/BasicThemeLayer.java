package au.gov.ga.worldwind.theme;

import java.net.URL;

import au.gov.ga.worldwind.panels.dataset.LayerDefinition;

public class BasicThemeLayer extends LayerDefinition implements ThemeLayer
{
	private boolean visible;

	public BasicThemeLayer(String name, URL url, URL infoURL, URL iconURL, boolean enabled,
			boolean visible)
	{
		super(name, url, infoURL, iconURL, true, true, enabled);
		setVisible(visible);
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
}
