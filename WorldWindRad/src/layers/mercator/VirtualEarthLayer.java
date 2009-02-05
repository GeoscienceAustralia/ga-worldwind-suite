package layers.mercator;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public class VirtualEarthLayer extends BasicMercatorTiledImageLayer
{
	private VirtualEarthLogo logo = new VirtualEarthLogo();

	public VirtualEarthLayer()
	{
		super(makeLevels());
		setSplitScale(1.3);
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
	public void render(DrawContext dc)
	{
		super.render(dc);
		if (isEnabled())
		{
			dc.addOrderedRenderable(logo);
		}
	}

	@Override
	protected boolean isTileValid(BufferedImage image)
	{
		int width = image.getWidth() - 1;
		int height = image.getHeight() - 1;
		return !(isWhite(image.getRGB(0, 0))
				&& isWhite(image.getRGB(0, height))
				&& isWhite(image.getRGB(width, 0))
				&& isWhite(image.getRGB(width, height)) && isWhite(image
				.getRGB(width / 2, height / 2)));
	}

	private boolean isWhite(int rgb)
	{
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb >> 0) & 0xff;
		return r + b + g > 215 * 3;
	}

	@Override
	public String toString()
	{
		return "Microsoft Virtual Earth";
	}
}
