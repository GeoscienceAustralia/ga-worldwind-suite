package au.gov.ga.worldwind.theme;

import gov.nasa.worldwind.WorldWindow;

import java.util.List;

import au.gov.ga.worldwind.panels.dataset.IDataset;
import au.gov.ga.worldwind.panels.dataset.ILayerDefinition;

public class BasicTheme implements Theme
{
	private WorldWindow wwd;

	private String name;
	private boolean menuBar;
	private boolean statusBar;
	private List<IDataset> datasets;
	private List<ILayerDefinition> layers;
	private List<ThemeHUD> HUDs;
	private List<ThemePanel> panels;

	public BasicTheme(String name)
	{
		setName(name);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		setWwd(wwd);

		for (ThemePanel panel : getPanels())
		{
			panel.setup(this);
		}
		for (ThemeHUD hud : getHUDs())
		{
			hud.setup(this);
		}
	}

	/*-- GETTERS AND SETTERS --*/

	public WorldWindow getWwd()
	{
		return wwd;
	}

	public void setWwd(WorldWindow wwd)
	{
		this.wwd = wwd;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean hasMenuBar()
	{
		return menuBar;
	}

	public void setMenuBar(boolean menuBar)
	{
		this.menuBar = menuBar;
	}

	public boolean hasStatusBar()
	{
		return statusBar;
	}

	public void setStatusBar(boolean statusBar)
	{
		this.statusBar = statusBar;
	}

	public List<IDataset> getDatasets()
	{
		return datasets;
	}

	public void setDatasets(List<IDataset> datasets)
	{
		this.datasets = datasets;
	}

	public List<ILayerDefinition> getLayers()
	{
		return layers;
	}

	public void setLayers(List<ILayerDefinition> layers)
	{
		this.layers = layers;
	}

	public List<ThemeHUD> getHUDs()
	{
		return HUDs;
	}

	public void setHUDs(List<ThemeHUD> hUDs)
	{
		HUDs = hUDs;
	}

	public List<ThemePanel> getPanels()
	{
		return panels;
	}

	public void setPanels(List<ThemePanel> panels)
	{
		this.panels = panels;
	}

	@Override
	public void dispose()
	{
		if (getHUDs() != null)
			for (ThemeHUD hud : getHUDs())
				hud.dispose();
		if (getPanels() != null)
			for (ThemePanel panel : getPanels())
				panel.dispose();
	}
}
