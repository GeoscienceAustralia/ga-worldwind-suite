package layers.virtualearth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class VirtualEarthLayer extends BasicMercatorTiledImageLayer
{
	public VirtualEarthLayer()
	{
		super(makeLevels());
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME, "Microsoft Virtual Earth");
		params.setValue(AVKey.SERVICE,
				"http://a0.ortho.tiles.virtualearth.net/tiles/");
		params.setValue(AVKey.DATASET_NAME, "h");
		params.setValue(AVKey.FORMAT_SUFFIX, ".jpg");
		params.setValue(AVKey.NUM_LEVELS, 16);
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
			String quadkey = tileToQuadKey(tile.getColumn(), tile.getRow(),
					tile.getLevelNumber() + 2);
			return new URL(tile.getLevel().getService()
					+ tile.getLevel().getDataset() + quadkey + ".jpeg?g=1");
		}
	}

	protected static String tileToQuadKey(int col, int row, int level)
	{
		String quad = "";
		for (int i = level; i >= 0; i--)
		{
			int mask = 1 << i;
			int cell = 0;
			if ((col & mask) != 0)
			{
				cell++;
			}
			if ((row & mask) == 0)
			{
				cell += 2;
			}
			quad += cell;
		}
		return quad;
	}
	
	@Override
	public String toString()
	{
		return "Microsoft Virtual Earth";
	}
}
