/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.theme;

import gov.nasa.worldwind.WorldWindow;

import java.net.URL;
import java.util.List;

import javax.swing.JFrame;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.layers.ThemeLayersPanel;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPanel;

/**
 * Basic implementation of the {@link Theme} interface. Contains setters for
 * each of the Theme property getters.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicTheme implements Theme
{
	private JFrame frame;
	private WorldWindow wwd;

	private String name;
	private boolean menuBar;
	private boolean toolBar;
	private boolean statusBar;
	private boolean sideBar = true;
	private boolean fullscreen = false;
	private boolean allowWms = true;

	private boolean persistLayers;
	private String layerPersistanceFilename;
	private List<String> cacheLocations;

	private boolean persistPlaces;
	private String placesPersistanceFilename;
	private URL placesInitialisationPath;

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
		{
			for (ThemeHUD hud : getHUDs())
			{
				hud.dispose();
			}
		}

		if (getPanels() != null)
		{
			for (ThemePanel panel : getPanels())
			{
				panel.dispose();
			}
		}
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
	public boolean hasSideBar()
	{
		return sideBar;
	}

	public void setSideBar(boolean sideBar)
	{
		this.sideBar = sideBar;
	}

	@Override
	public boolean isFullscreen()
	{
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen)
	{
		this.fullscreen = fullscreen;
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
	public String getPlacesPersistanceFilename()
	{
		return placesPersistanceFilename;
	}

	public void setPlacesPersistanceFilename(String placesPersistanceFilename)
	{
		this.placesPersistanceFilename = placesPersistanceFilename;
	}

	@Override
	public boolean isPlacesPersistanceFilenameSet()
	{
		return !Util.isBlank(placesPersistanceFilename);
	}

	@Override
	public URL getPlacesInitialisationPath()
	{
		return placesInitialisationPath;
	}

	public void setPlacesInitialisationPath(URL placesInitialisationPath)
	{
		this.placesInitialisationPath = placesInitialisationPath;
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
		return getPanel(LayersPanel.class);
	}

	@Override
	public ThemeLayersPanel getThemeLayersPanel()
	{
		return getPanel(ThemeLayersPanel.class);
	}

	@Override
	public DatasetPanel getDatasetPanel()
	{
		return getPanel(DatasetPanel.class);
	}

	@Override
	public PlacesPanel getPlacesPanel()
	{
		return getPanel(PlacesPanel.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T getPanel(Class<T> c)
	{
		for (ThemePanel panel : getPanels())
		{
			if (c.isAssignableFrom(panel.getClass()))
			{
				return (T) panel;
			}
		}
		return null;
	}

	@Override
	public boolean isPersistPlaces()
	{
		return persistPlaces;
	}

	public void setPersistPlaces(boolean persistPlaces)
	{
		this.persistPlaces = persistPlaces;
	}
}
