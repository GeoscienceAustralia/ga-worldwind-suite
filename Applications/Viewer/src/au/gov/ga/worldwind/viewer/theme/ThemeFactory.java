package au.gov.ga.worldwind.viewer.theme;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.viewer.panels.dataset.IDataset;
import au.gov.ga.worldwind.viewer.panels.dataset.LazyDataset;
import au.gov.ga.worldwind.viewer.util.Icons;

public class ThemeFactory
{
	public static Theme createFromXML(Object source, URL context)
	{
		Element element = XMLUtil.getElementFromSource(source);
		if (element == null)
			return null;

		BasicTheme theme = new BasicTheme(XMLUtil.getText(element, "ThemeName"));

		theme.setMenuBar(XMLUtil.getBoolean(element, "MenuBar", false));
		theme.setToolBar(XMLUtil.getBoolean(element, "ToolBar", false));
		theme.setStatusBar(XMLUtil.getBoolean(element, "StatusBar", false));

		theme.setPersistLayers(XMLUtil.getBoolean(element, "PersistLayers", true));
		theme.setLayerPersistanceFilename(XMLUtil.getText(element, "LayersFilename"));
		theme.setCacheLocations(parseCacheLocations(element, "CacheLocation"));

		theme.setHUDs(parseHUDs(element, "HUD"));
		theme.setPanels(parsePanels(element, "Panel"));
		theme.setDatasets(parseDatasets(element, "Dataset", context));
		theme.setLayers(parseLayers(element, "Layer", context));

		theme.setInitialLatitude(XMLUtil.getDouble(element, "InitialLatitude", null));
		theme.setInitialLongitude(XMLUtil.getDouble(element, "InitialLongitude", null));
		theme.setInitialAltitude(XMLUtil.getDouble(element, "InitialAltitude", null));
		theme.setInitialHeading(XMLUtil.getDouble(element, "InitialHeading", null));
		theme.setInitialPitch(XMLUtil.getDouble(element, "InitialPitch", null));
		theme.setVerticalExaggeration(XMLUtil.getDouble(element, "VerticalExaggeration", null));
		theme.setFieldOfView(XMLUtil.getDouble(element, "FieldOfView", null));

		return theme;
	}

	private static List<String> parseCacheLocations(Element context, String path)
	{
		List<String> locations = new ArrayList<String>();
		Element[] elements = XMLUtil.getElements(context, path, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				String location = element.getTextContent();
				if (location != null && location.length() > 0)
					locations.add(location);
			}
		}
		return locations;
	}

	private static List<ThemeHUD> parseHUDs(Element context, String path)
	{
		List<ThemeHUD> huds = new ArrayList<ThemeHUD>();
		Element[] elements = XMLUtil.getElements(context, path, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				String className = XMLUtil.getText(element, "@className");
				String position = XMLUtil.getText(element, "@position");
				boolean enabled = XMLUtil.getBoolean(element, "@enabled", true);
				String name = XMLUtil.getText(element, "@name");

				try
				{
					Class<?> c = Class.forName(className);
					Class<? extends ThemeHUD> tc = c.asSubclass(ThemeHUD.class);
					ThemeHUD hud = tc.newInstance();
					if (position != null)
						hud.setPosition(position);
					hud.setOn(enabled);
					hud.setDisplayName(name);
					huds.add(hud);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return huds;
	}

	private static List<ThemePanel> parsePanels(Element context, String path)
	{
		List<ThemePanel> panels = new ArrayList<ThemePanel>();
		Element[] elements = XMLUtil.getElements(context, path, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				String className = XMLUtil.getText(element, "@className");
				boolean enabled = XMLUtil.getBoolean(element, "@enabled", true);
				String name = XMLUtil.getText(element, "@name");
				Double weightD = XMLUtil.getDouble(element, "@weight", null);
				float weight = weightD != null ? weightD.floatValue() : 1f;
				boolean expanded = XMLUtil.getBoolean(element, "@expanded", true);

				try
				{
					Class<?> c = Class.forName(className);
					Class<? extends ThemePanel> tc = c.asSubclass(ThemePanel.class);
					ThemePanel panel = tc.newInstance();
					panel.setDisplayName(name);
					panel.setOn(enabled);
					panel.setWeight(weight);
					panel.setExpanded(expanded);
					panels.add(panel);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return panels;
	}

	private static List<IDataset> parseDatasets(Element context, String path, URL urlContext)
	{
		List<IDataset> datasets = new ArrayList<IDataset>();
		Element[] elements = XMLUtil.getElements(context, path, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				try
				{
					String name = XMLUtil.getText(element, "@name");
					URL url = XMLUtil.getURL(element, "@url", urlContext);
					URL info = XMLUtil.getURL(element, "@info", urlContext);
					String icon = XMLUtil.getText(element, "@icon");
					URL iconURL = null;
					if (icon == null)
						iconURL = Icons.earth.getURL();
					else
						iconURL = XMLUtil.getURL(icon, urlContext);

					IDataset dataset = new LazyDataset(name, url, info, iconURL, true);
					datasets.add(dataset);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return datasets;
	}

	private static List<ThemeLayer> parseLayers(Element context, String path, URL urlContext)
	{
		List<ThemeLayer> layers = new ArrayList<ThemeLayer>();
		Element[] elements = XMLUtil.getElements(context, path, null);
		if (elements != null)
		{
			for (Element element : elements)
			{
				try
				{
					String name = XMLUtil.getText(element, "@name");
					URL url = XMLUtil.getURL(element, "@url", urlContext);
					URL info = XMLUtil.getURL(element, "@info", urlContext);
					URL icon = XMLUtil.getURL(element, "@icon", urlContext);
					boolean enabled = XMLUtil.getBoolean(element, "@enabled", true);
					boolean visible = XMLUtil.getBoolean(element, "@visible", true);

					ThemeLayer layer = new BasicThemeLayer(name, url, info, icon, enabled, visible);
					layers.add(layer);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return layers;
	}
}
