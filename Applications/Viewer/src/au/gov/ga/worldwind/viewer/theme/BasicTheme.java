package au.gov.ga.worldwind.viewer.theme;

import gov.nasa.worldwind.WorldWindow;

import java.util.List;

import javax.swing.JFrame;

import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.layers.ThemeLayersPanel;

public class BasicTheme implements Theme
{
	private JFrame frame;
	private WorldWindow wwd;

	private String name;
	private boolean menuBar;
	private boolean toolBar;
	private boolean statusBar;
	private boolean allowWms = true;
	
	private boolean persistLayers;
	private String layerPersistanceFilename;
	private List<String> cacheLocations;
	
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
	public void setup(JFrame frame, WorldWindow wwd)
	{
		setWwd(wwd);
		setFrame(frame);

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

	@Override
	public WorldWindow getWwd()
	{
		return wwd;
	}

	public void setWwd(WorldWindow wwd)
	{
		this.wwd = wwd;
	}

	@Override
	public boolean hasWms()
	{
		return allowWms;
	}
	
	public void setHasWms(boolean allowWms)
	{
		this.allowWms = allowWms;
	}
	
	@Override
	public JFrame getFrame()
	{
		return frame;
	}

	public void setFrame(JFrame frame)
	{
		this.frame = frame;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
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

	@Override
	public boolean hasStatusBar()
	{
		return statusBar;
	}

	public void setStatusBar(boolean statusBar)
	{
		this.statusBar = statusBar;
	}

	@Override
	public boolean isPersistLayers()
	{
		return persistLayers;
	}

	public void setPersistLayers(boolean persistLayers)
	{
		this.persistLayers = persistLayers;
	}

	@Override
	public String getLayerPersistanceFilename()
	{
		return layerPersistanceFilename;
	}

	public void setLayerPersistanceFilename(String layerPersistanceFilename)
	{
		this.layerPersistanceFilename = layerPersistanceFilename;
	}

	@Override
	public List<String> getCacheLocations()
	{
		return cacheLocations;
	}

	public void setCacheLocations(List<String> cacheLocations)
	{
		this.cacheLocations = cacheLocations;
	}

	@Override
	public List<IDataset> getDatasets()
	{
		return datasets;
	}

	public void setDatasets(List<IDataset> datasets)
	{
		this.datasets = datasets;
	}

	@Override
	public List<ThemeLayer> getLayers()
	{
		return layers;
	}

	public void setLayers(List<ThemeLayer> layers)
	{
		this.layers = layers;
	}

	@Override
	public List<ThemeHUD> getHUDs()
	{
		return HUDs;
	}

	public void setHUDs(List<ThemeHUD> hUDs)
	{
		HUDs = hUDs;
	}

	@Override
	public List<ThemePanel> getPanels()
	{
		return panels;
	}

	public void setPanels(List<ThemePanel> panels)
	{
		this.panels = panels;
	}

	@Override
	public Double getInitialLatitude()
	{
		return initialLatitude;
	}

	public void setInitialLatitude(Double initialLatitude)
	{
		this.initialLatitude = initialLatitude;
	}

	@Override
	public Double getInitialLongitude()
	{
		return initialLongitude;
	}

	public void setInitialLongitude(Double initialLongitude)
	{
		this.initialLongitude = initialLongitude;
	}

	@Override
	public Double getInitialAltitude()
	{
		return initialAltitude;
	}

	public void setInitialAltitude(Double initialAltitude)
	{
		this.initialAltitude = initialAltitude;
	}

	@Override
	public Double getInitialHeading()
	{
		return initialHeading;
	}

	public void setInitialHeading(Double initialHeading)
	{
		this.initialHeading = initialHeading;
	}

	@Override
	public Double getInitialPitch()
	{
		return initialPitch;
	}

	public void setInitialPitch(Double initialPitch)
	{
		this.initialPitch = initialPitch;
	}

	@Override
	public Double getVerticalExaggeration()
	{
		return verticalExaggeration;
	}

	public void setVerticalExaggeration(Double verticalExaggeration)
	{
		this.verticalExaggeration = verticalExaggeration;
	}

	@Override
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
		return getLayersPanel() != null;
	}

	@Override
	public boolean hasThemeLayersPanel()
	{
		return getThemeLayersPanel() != null;
	}

	@Override
	public LayersPanel getLayersPanel()
	{
		for (ThemePanel panel : getPanels())
			if (panel instanceof LayersPanel)
				return (LayersPanel) panel;
		return null;
	}
	
	public ThemeLayersPanel getThemeLayersPanel()
	{
		for (ThemePanel panel : getPanels())
			if (panel instanceof ThemeLayersPanel)
				return (ThemeLayersPanel) panel;
		return null;
	}
}
