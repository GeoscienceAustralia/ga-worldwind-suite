package au.gov.ga.worldwind.theme;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.WorldWindow;

import java.util.List;

import au.gov.ga.worldwind.panels.dataset.IDataset;
import au.gov.ga.worldwind.panels.layers.LayersPanel;

public interface Theme extends Disposable
{
	public void setup(WorldWindow wwd);
	public WorldWindow getWwd();
	
	public String getName();
	public boolean hasMenuBar();
	public boolean hasToolBar();
	public boolean hasStatusBar();
	
	public List<IDataset> getDatasets();
	public List<ThemeLayer> getLayers();
	public List<ThemeHUD> getHUDs();
	public List<ThemePanel> getPanels();
	
	public boolean hasLayersPanel();
	public boolean hasThemeLayersPanel();
	public LayersPanel getLayersPanel();
	
	public Double getInitialLatitude();
	public Double getInitialLongitude();
	public Double getInitialAltitude();
	public Double getInitialHeading();
	public Double getInitialPitch();
	public Double getVerticalExaggeration();
}
