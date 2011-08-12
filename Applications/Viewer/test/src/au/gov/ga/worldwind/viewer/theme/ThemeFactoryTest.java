package au.gov.ga.worldwind.viewer.theme;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gov.nasa.worldwind.avlist.AVKey;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.worldwind.viewer.panels.dataset.DatasetPanel;
import au.gov.ga.worldwind.viewer.panels.geonames.GeoNamesSearchPanel;
import au.gov.ga.worldwind.viewer.panels.layers.LayersPanel;
import au.gov.ga.worldwind.viewer.panels.other.ExaggerationPanel;
import au.gov.ga.worldwind.viewer.panels.places.PlacesPanel;
import au.gov.ga.worldwind.viewer.settings.Settings;
import au.gov.ga.worldwind.viewer.theme.hud.CompassHUD;
import au.gov.ga.worldwind.viewer.theme.hud.ControlsHUD;
import au.gov.ga.worldwind.viewer.theme.hud.CrosshairHUD;
import au.gov.ga.worldwind.viewer.theme.hud.GraticuleHUD;
import au.gov.ga.worldwind.viewer.theme.hud.ScalebarHUD;
import au.gov.ga.worldwind.viewer.theme.hud.WorldMapHUD;

/**
 * Unit tests for the {@link ThemeFactory} class
 */
public class ThemeFactoryTest
{
	private static final double DELTA = 0.0001;

	@Before
	public void setup()
	{
		Settings.set(new Settings());
	}
	
	@After
	public void tearDown()
	{
		Settings.set(null);
	}
	
	@Test
	public void testCreateFromXMLWithNullSource()
	{
		URL context = null;
		Object source = null;
		
		Theme result = ThemeFactory.createFromXML(source, context);
		
		assertNull(result);
	}
	
	@Test
	public void testCreateFromXMLSampleSource() throws Exception
	{
		URL sampleFileUrl = getClass().getResource("sampleThemeFile.xml"); 
		
		URL context = sampleFileUrl;
		Object source = sampleFileUrl;
		
		Theme result = ThemeFactory.createFromXML(source, context);
		
		assertNotNull(result);
		
		assertEquals("Sample theme", result.getName());
		
		assertEquals("layers.sample.xml", result.getLayerPersistanceFilename());
		assertEquals(true, result.isPersistLayers());
		assertEquals("places.sample.xml", result.getPlacesPersistanceFilename());
		
		assertEquals(true, result.isPlacesPersistanceFilenameSet());
		assertEquals(true, result.hasMenuBar());
		assertEquals(false, result.hasToolBar());
		assertEquals(true, result.hasStatusBar());
		assertEquals(false, result.hasWms());
		
		assertEquals(-12, result.getInitialLatitude(), DELTA);
		assertEquals(123.4, result.getInitialLongitude(), DELTA);
		assertEquals(null, result.getFieldOfView());
		assertEquals(null, result.getInitialAltitude());
		assertEquals(null, result.getInitialHeading());
		
		assertEquals(1, result.getCacheLocations().size());
		assertEquals("Cache", result.getCacheLocations().get(0));
		
		assertEquals(6, result.getHUDs().size());
		assertThemeHudCorrect(result.getHUDs().get(0), WorldMapHUD.class, "World Map", AVKey.NORTHWEST, false);
		assertThemeHudCorrect(result.getHUDs().get(1), CompassHUD.class, "Compass", AVKey.NORTHEAST, false);
		assertThemeHudCorrect(result.getHUDs().get(2), ScalebarHUD.class, "Scalebar", AVKey.SOUTHEAST, false);
		assertThemeHudCorrect(result.getHUDs().get(3), ControlsHUD.class, "Navigation Controls", AVKey.SOUTHWEST, false);
		assertThemeHudCorrect(result.getHUDs().get(4), GraticuleHUD.class, "Graticule", null, false);
		assertThemeHudCorrect(result.getHUDs().get(5), CrosshairHUD.class, "Crosshair", null, false);
		
		assertEquals(1, result.getDatasets().size());
		assertEquals("Datasets", result.getDatasets().get(0).getName());
		
		assertEquals(5, result.getPanels().size());
		assertPanelCorrect(result.getPanels().get(0), GeoNamesSearchPanel.class, "Search", false);
		assertPanelCorrect(result.getPanels().get(1), PlacesPanel.class, "Places", false);
		assertPanelCorrect(result.getPanels().get(2), LayersPanel.class, "Layers", true);
		assertPanelCorrect(result.getPanels().get(3), DatasetPanel.class, "Datasets", true);
		assertPanelCorrect(result.getPanels().get(4), ExaggerationPanel.class, "Vertical Exaggeration", false);
		
		assertTrue(result.hasLayersPanel());
		assertFalse(result.hasThemeLayersPanel());
		assertNotNull(result.getLayersPanel());
		
		assertEquals(1, result.getLayers().size());
		assertLayerCorrect(result.getLayers().get(0), "Sample Layer", "http://somewhere.com/layer.xml", "http://somewhere.com/info.xml", "http://somewhere.com/icon.xml", true, false);
	}
	
	private void assertThemeHudCorrect(ThemeHUD hud, Class<? extends ThemeHUD> type, String name, String position, boolean enabled)
	{
		assertNotNull(hud);
		assertEquals(type, hud.getClass());
		assertEquals(name, hud.getDisplayName());
		assertEquals(position, hud.getPosition());
		assertEquals(enabled, hud.isOn());
	}
	
	private void assertPanelCorrect(ThemePanel panel, Class<? extends ThemePanel> type, String name, boolean enabled)
	{
		assertNotNull(panel);
		assertEquals(type, panel.getClass());
		assertEquals(name, panel.getDisplayName());
		assertEquals(enabled, panel.isOn());
		assertNotNull(panel.getPanel());
	}
	
	private void assertLayerCorrect(ThemeLayer layer, String name, String url, String info, String icon, boolean enabled, boolean visible)
	{
		assertNotNull(layer);
		assertEquals(name, layer.getName());
		assertEquals(url, layer.getLayerURL().toExternalForm());
		assertEquals(info, layer.getInfoURL().toExternalForm());
		assertEquals(icon, layer.getIconURL().toExternalForm());
		assertEquals(enabled, layer.isEnabled());
		assertEquals(visible, layer.isVisible());
	}
}
