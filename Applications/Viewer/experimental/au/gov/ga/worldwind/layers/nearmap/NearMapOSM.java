package au.gov.ga.worldwind.layers.nearmap;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class NearMapOSM extends TransparentMercatorTiledImageLayer
{
	public NearMapOSM()
	{
		super(makeLevels());
		setSplitScale(1.3);
		this.setForceLevelZeroLoads(true);
		this.setUseTransparentTextures(true);
		this.setRetainLevelZeroTiles(true);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "OpenStreetMap (NM)");
		params.setValue(AVKey.SERVICE, "http://www.nearmap.com/maps/");
		params.setValue(AVKey.DATASET_NAME, "MapT");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
		params.setValue(AVKey.NUM_LEVELS, 31 - 3);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
				.fromDegrees(22.5d), Angle.fromDegrees(45d)));
		params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0,
				Angle.NEG180, Angle.POS180));
		params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder());

		return new LevelSet(params);
	}

	private static class URLBuilder implements TileUrlBuilder
	{
		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			int level = tile.getLevelNumber() + 3;
			int column = tile.getColumn();
			int row = (1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow();
			return new URL(tile.getLevel().getService() + "hl=en&x=" + column
					+ "&y=" + row + "&z=" + level + "&nml="
					+ tile.getLevel().getDataset());
		}
	}

	@Override
	public String toString()
	{
		return "OpenStreetMap (NM)";
	}
}
