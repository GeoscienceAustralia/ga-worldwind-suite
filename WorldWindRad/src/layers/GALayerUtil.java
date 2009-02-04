package layers;

public class GALayerUtil
{
	private final static String TILES_SCRIPT_URL = "http://sandpit:8500/apps/world-wind/tiles.jsp";
	private final static double SPLIT_SCALE = 1.0;

	public static String getTilesScriptUrl()
	{
		return TILES_SCRIPT_URL;
	}

	public static double getSplitScale()
	{
		return SPLIT_SCALE;
	}
}
