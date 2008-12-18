package layers.other;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenAerialMapLayer extends BasicTiledImageLayer
{
	public OpenAerialMapLayer()
	{
		super(makeLevels());
	}

	public static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Earth/Open Aerial Map");
		params.setValue(AVKey.SERVICE,
				"http://tile.openaerialmap.org/tiles/1.0.0");
		params.setValue(AVKey.DATASET_NAME, "openaerialmap");
		params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
		params.setValue(AVKey.NUM_LEVELS, 25);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(180d), Angle.fromDegrees(180d)));
		params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
		params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

		return new LevelSet(params);
	}

	private static class URLBuilder implements TileUrlBuilder
	{
		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			StringBuffer sb = new StringBuffer(tile.getLevel().getService());
			if (sb.lastIndexOf("/") != sb.length() - 1)
				sb.append("/");
			sb.append(tile.getLevel().getDataset());
			sb.append("/");
			sb.append(tile.getLevel().getLevelName());
			sb.append("/");
			sb.append(tile.getColumn());
			sb.append("/");
			sb.append(tile.getRow());

			return new java.net.URL(sb.toString());
		}
	}

	@Override
	public String toString()
	{
		return "Open Aerial Map";
	}
}
