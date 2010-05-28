package au.gov.ga.worldwind.layers.file;

import org.w3c.dom.Element;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.LayerConfiguration;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

public abstract class AVListTiledImageLayer extends TiledImageLayer
{
	public AVListTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	//-----------------------------------------------------------------------------------
	//The following constructor and methods are copied directly from BasicTiledImageLayer
	//-----------------------------------------------------------------------------------

	public AVListTiledImageLayer(AVList params)
	{
		this(new LevelSet(params));

		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			this.setName(s);

		String[] strings = (String[]) params.getValue(AVKey.AVAILABLE_IMAGE_FORMATS);
		if (strings != null && strings.length > 0)
			this.setAvailableImageFormats(strings);

		Double d = (Double) params.getValue(AVKey.OPACITY);
		if (d != null)
			this.setOpacity(d);

		d = (Double) params.getValue(AVKey.MAX_ACTIVE_ALTITUDE);
		if (d != null)
			this.setMaxActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MIN_ACTIVE_ALTITUDE);
		if (d != null)
			this.setMinActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MAP_SCALE);
		if (d != null)
			this.setValue(AVKey.MAP_SCALE, d);

		d = (Double) params.getValue(AVKey.SPLIT_SCALE);
		if (d != null)
			this.setSplitScale(d);

		Boolean b = (Boolean) params.getValue(AVKey.FORCE_LEVEL_ZERO_LOADS);
		if (b != null)
			this.setForceLevelZeroLoads(b);

		b = (Boolean) params.getValue(AVKey.RETAIN_LEVEL_ZERO_TILES);
		if (b != null)
			this.setRetainLevelZeroTiles(b);

		b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
			this.setNetworkRetrievalEnabled(b);

		b = (Boolean) params.getValue(AVKey.COMPRESS_TEXTURES);
		if (b != null)
			this.setCompressTextures(b);

		b = (Boolean) params.getValue(AVKey.USE_MIP_MAPS);
		if (b != null)
			this.setUseMipMaps(b);

		b = (Boolean) params.getValue(AVKey.USE_TRANSPARENT_TEXTURES);
		if (b != null)
			this.setUseTransparentTextures(b);

		Object o = params.getValue(AVKey.URL_CONNECT_TIMEOUT);
		if (o != null)
			this.setValue(AVKey.URL_CONNECT_TIMEOUT, o);

		o = params.getValue(AVKey.URL_READ_TIMEOUT);
		if (o != null)
			this.setValue(AVKey.URL_READ_TIMEOUT, o);

		o = params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (o != null)
			this.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, o);

		ScreenCredit sc = (ScreenCredit) params.getValue(AVKey.SCREEN_CREDIT);
		if (sc != null)
			this.setScreenCredit(sc);

		if (params.getValue(AVKey.TRANSPARENCY_COLORS) != null)
			this.setValue(AVKey.TRANSPARENCY_COLORS, params.getValue(AVKey.TRANSPARENCY_COLORS));

		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());

		// If any resources should be retrieved for this Layer, start a task to retrieve those resources, and initialize
		// this Layer once those resources are retrieved.
		/*if (this.isRetrieveResources())
		{
		    this.startResourceRetrieval();
		}*/
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		LayerConfiguration.getLayerParams(domElement, params);
		LayerConfiguration.getTiledImageLayerParams(domElement, params);
		setFallbacks(params);

		return params;
	}

	protected static void setFallbacks(AVList params)
	{
		if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
		{
			Angle delta = Angle.fromDegrees(36);
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
		}

		if (params.getValue(AVKey.TILE_WIDTH) == null)
			params.setValue(AVKey.TILE_WIDTH, 512);

		if (params.getValue(AVKey.TILE_HEIGHT) == null)
			params.setValue(AVKey.TILE_HEIGHT, 512);

		if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
			params.setValue(AVKey.FORMAT_SUFFIX, ".dds");

		if (params.getValue(AVKey.NUM_LEVELS) == null)
			params.setValue(AVKey.NUM_LEVELS, 19); // approximately 0.1 meters per pixel

		if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
			params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
	}
}
