package au.gov.ga.worldwind.layers.mercator;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.Mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.Mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

public class OpenStreetMapTransparentLayer extends BasicMercatorTiledImageLayer
{
	public OpenStreetMapTransparentLayer()
	{
		super(makeLevels());
		setSplitScale(1.3);
		this.setUseTransparentTextures(true);
	}

	private static LevelSet makeLevels()
	{
		AVList params = new AVListImpl();

		params.setValue(AVKey.TILE_WIDTH, 256);
		params.setValue(AVKey.TILE_HEIGHT, 256);
		params.setValue(AVKey.DATA_CACHE_NAME,
				"OpenStreetMap Mapnik Transparent");
		params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/");
		params.setValue(AVKey.DATASET_NAME, "mapnik");
		params.setValue(AVKey.FORMAT_SUFFIX, ".png");
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
			int level = tile.getLevelNumber() + 3;
			int column = tile.getColumn();
			int row = (1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow();
			return new URL(tile.getLevel().getService() + level + "/" + column
					+ "/" + row + ".png");
		}
	}

	@Override
	protected BufferedImage modifyImage(BufferedImage image)
	{
		BufferedImage newImage = new BufferedImage(image.getWidth(), image
				.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++)
				newImage.setRGB(x, y, convertTransparent(image.getRGB(x, y)));
		return newImage;
	}

	private int convertTransparent(int rgb)
	{
		int a = 255;
		//transparent colours
		if (rgb == -921880 || rgb == -4861744 || rgb == -856344
				|| rgb == -856087)
			a = 0;
		return (rgb & 0xffffff) | (a << 24);
	}

	@Override
	public String toString()
	{
		return "OpenStreetMap Mapnik";
	}
}
