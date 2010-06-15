package au.gov.ga.worldwind.theme;

import gov.nasa.worldwind.WorldWindow;

import java.util.List;

import au.gov.ga.worldwind.panels.dataset.IDataset;
import au.gov.ga.worldwind.panels.layers.LayersPanel;
import au.gov.ga.worldwind.panels.layers.ThemeLayersPanel;

public class BasicTheme implements Theme
{
	private WorldWindow wwd;

	private String name;
	private boolean menuBar;
	private boolean toolBar;
	private boolean statusBar;
	private List<IDataset> datasets;
	private List<ThemeLayer> layers;
	private List<ThemeHUD> HUDs;
	private List<ThemePanel> panels;

	private Double initialLatitude;
	private Double initialLongitude;
	private Double initialAltitude;
	private Double initialHeading;
	private Double initialPitch;
	private Double verticalExaggeration;
	private Double fieldOfView;

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

	@Override
	public boolean hasToolBar()
	{
		return toolBar;
	}

	public void setToolBar(boolean toolBar)
	{
		this.toolBar = toolBar;
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

	public List<ThemeLayer> getLayers()
	{
		return layers;
	}

	public void setLayers(List<ThemeLayer> layers)
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

	public Double getInitialLatitude()
	{
		return initialLatitude;
	}

	public void setInitialLatitude(Double initialLatitude)
	{
		this.initialLatitude = initialLatitude;
	}

	public Double getInitialLongitude()
	{
		return initialLongitude;
	}

	public void setInitialLongitude(Double initialLongitude)
	{
		this.initialLongitude = initialLongitude;
	}

	public Double getInitialAltitude()
	{
		return initialAltitude;
	}

	public void setInitialAltitude(Double initialAltitude)
	{
		this.initialAltitude = initialAltitude;
	}

	public Double getInitialHeading()
	{
		return initialHeading;
	}

	public void setInitialHeading(Double initialHeading)
	{
		this.initialHeading = initialHeading;
	}

	public Double getInitialPitch()
	{
		return initialPitch;
	}

	public void setInitialPitch(Double initialPitch)
	{
		this.initialPitch = initialPitch;
	}

	public Double getVerticalExaggeration()
	{
		return verticalExaggeration;
	}

	public void setVerticalExaggeration(Double verticalExaggeration)
	{
		this.verticalExaggeration = verticalExaggeration;
	}

	public Double getFieldOfView()
	{
		return fieldOfView;
	}

	public void setFieldOfView(Double fieldOfView)
	{
		this.fieldOfView = fieldOfView;
	}

	@Override
	public boolean hasLayersPanel()
	{
		for (ThemePanel panel : getPanels())
			if (panel instanceof LayersPanel)
				return true;
		return false;
	}

	@Override
	public boolean hasThemeLayersPanel()
	{
		for (ThemePanel panel : getPanels())
			if (panel instanceof ThemeLayersPanel)
				return true;
		return false;
	}

	@Override
	public LayersPanel getLayersPanel()
	{
		for (ThemePanel panel : getPanels())
			if (panel instanceof LayersPanel)
				return (LayersPanel) panel;
		return null;
	}
}
