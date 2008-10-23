package mask;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class MaskLevelSet extends LevelSet
{
	public final static String MASK_CACHE_NAME = "MaskCacheNameKey";
	public final static String MASK_SERVICE = "MaskServiceKey";
	public final static String MASK_FORMAT_SUFFIX = "MaskFormatSuffixKey";
	public final static String MASK_TILE_URL_BUILDER = "MaskTileUrlBuilderKey";

	private String maskCacheName;
	private String maskService;
	private String maskFormatSuffix;
	private TileUrlBuilder maskTileUrlBuilder;

	public MaskLevelSet(AVList params)
	{
		super(params);

		Object o = params.getValue(MASK_CACHE_NAME);
		if (o != null && o instanceof String)
			maskCacheName = (String) o;

		o = params.getValue(MASK_SERVICE);
		if (o != null && o instanceof String)
			maskService = (String) o;

		o = params.getValue(MASK_FORMAT_SUFFIX);
		if (o != null && o instanceof String)
			maskFormatSuffix = (String) o;

		o = params.getValue(MASK_TILE_URL_BUILDER);
		if (o != null && o instanceof TileUrlBuilder)
			maskTileUrlBuilder = (TileUrlBuilder) o;

		if (maskTileUrlBuilder == null)
		{
			maskTileUrlBuilder = new TileUrlBuilder()
			{
				public URL getURL(Tile tile, String altImageFormat)
						throws MalformedURLException
				{
					String service = getMaskService();
					if (service == null || service.length() < 1)
						return null;

					StringBuffer sb = new StringBuffer(service);
					if (sb.lastIndexOf("?") != sb.length() - 1)
						sb.append("?");
					sb.append("T=");
					sb.append(tile.getLevel().getDataset());
					sb.append("&L=");
					sb.append(tile.getLevel().getLevelName());
					sb.append("&X=");
					sb.append(tile.getColumn());
					sb.append("&Y=");
					sb.append(tile.getRow());

					// Convention for NASA WWN tiles is to request them with common dataset name but without dds.
					return new URL(altImageFormat == null ? sb.toString() : sb
							.toString().replace("dds", ""));
				}
			};
		}
	}

	public MaskLevelSet(MaskLevelSet levelSet)
	{
		super(levelSet);
		this.maskCacheName = levelSet.maskCacheName;
		this.maskService = levelSet.maskService;
		this.maskFormatSuffix = levelSet.maskFormatSuffix;
		this.maskTileUrlBuilder = levelSet.maskTileUrlBuilder;
	}

	public String getMaskCacheName()
	{
		return maskCacheName;
	}

	public String getMaskService()
	{
		return maskService;
	}

	public String getMaskFormatSuffix()
	{
		return maskFormatSuffix;
	}

	public TileUrlBuilder getMaskTileUrlBuilder()
	{
		return maskTileUrlBuilder;
	}
}
