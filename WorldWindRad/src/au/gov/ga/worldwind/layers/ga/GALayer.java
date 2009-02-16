package au.gov.ga.worldwind.layers.ga;

import gov.nasa.worldwind.util.LevelSet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.application.Offline;
import au.gov.ga.worldwind.layers.mask.MaskTiledImageLayer;

public class GALayer extends MaskTiledImageLayer
{
	private final static String METADATA_BASE_URL_STRING = "http://www.ga.gov.au/apps/world-wind/metadata/";
	private final static String TILES_SCRIPT_URL = "http://www.ga.gov.au/apps/world-wind/tiles.jsp";
	//private final static String METADATA_BASE_URL_STRING = "http://sandpit:8500/apps/world-wind/metadata/";
	//private final static String TILES_SCRIPT_URL = "http://sandpit:8500/apps/world-wind/tiles.jsp";
	private final static double SPLIT_SCALE = 0.9;

	private final static URL metadataBaseUrl;
	private static List<GALayer> gaLayers = new ArrayList<GALayer>();
	private static LogoLayer logoLayer = new LogoLayer();

	static
	{
		URL url = null;
		try
		{
			url = new URL(METADATA_BASE_URL_STRING);
		}
		catch (MalformedURLException e)
		{
		}
		metadataBaseUrl = url;
	}

	public GALayer(LevelSet levelSet)
	{
		super(levelSet);
		gaLayers.add(this);
		updateLogoLayer();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		updateLogoLayer();
	}

	private static void updateLogoLayer()
	{
		boolean anyEnabled = false;
		for (GALayer layer : gaLayers)
		{
			if (layer.isEnabled())
			{
				anyEnabled = true;
				break;
			}
		}
		logoLayer.setLayerOn(anyEnabled);
	}

	public static LogoLayer getLogoLayer()
	{
		return logoLayer;
	}

	public static String getTilesScriptUrl()
	{
		return Offline.isOfflineVersion() ? "" : TILES_SCRIPT_URL;
	}

	public static URL getMetadataBaseUrl()
	{
		URL url = metadataBaseUrl;
		if (Offline.isOfflineVersion())
		{
			String userDir = System.getProperty("user.dir");
			File userDirFile = new File(userDir);
			try
			{
				url = new File(userDirFile, "metadata").toURI().toURL();
			}
			catch (MalformedURLException e)
			{
				url = null;
			}
		}
		return url;
	}

	public static double getSplitScale()
	{
		return SPLIT_SCALE;
	}
}
