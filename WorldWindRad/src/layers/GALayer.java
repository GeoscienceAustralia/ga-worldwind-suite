package layers;

import gov.nasa.worldwind.util.LevelSet;

import java.util.ArrayList;
import java.util.List;

import layers.mask.MaskTiledImageLayer;
import layers.other.LogoLayer;

public class GALayer extends MaskTiledImageLayer
{
	private final static String TILES_SCRIPT_URL = "http://sandpit:8500/apps/world-wind/tiles.jsp";
	private final static double SPLIT_SCALE = 0.9;
	
	private static List<GALayer> gaLayers = new ArrayList<GALayer>();
	private static LogoLayer logoLayer = new LogoLayer();

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
		return TILES_SCRIPT_URL;
	}

	public static double getSplitScale()
	{
		return SPLIT_SCALE;
	}
}
