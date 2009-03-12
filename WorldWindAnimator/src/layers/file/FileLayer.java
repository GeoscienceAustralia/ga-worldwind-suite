package layers.file;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import layers.mask.MaskTiledImageLayer;

public class FileLayer
{
	public static TiledImageLayer createLayer(String name, String cacheName,
			String formatSuffix, File directory, String extension, int levels,
			LatLon lztd, Sector sector)
	{
		if (!formatSuffix.startsWith("."))
			formatSuffix = "." + formatSuffix;

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, name);
		params.setValue(AVKey.FORMAT_SUFFIX, formatSuffix);
		params.setValue(AVKey.NUM_LEVELS, levels);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, lztd);
		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.TILE_URL_BUILDER, new FileUrlBuilder(directory,
				extension, null, null));

		TiledImageLayer layer = new FileBasicTiledImageLayer(params);
		layer.setName(name);
		layer.setUseTransparentTextures(true);
		layer.setUseMipMaps(true);
		return layer;
	}

	public static TiledImageLayer createLayer(String name, String cacheName,
			String formatSuffix, File directory, String extension,
			File maskDirectory, String maskExtension, int levels, LatLon lztd,
			Sector sector)
	{
		if (!formatSuffix.startsWith("."))
			formatSuffix = "." + formatSuffix;

		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, 512);
		params.setValue(AVKey.TILE_HEIGHT, 512);
		params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, name);
		params.setValue(AVKey.FORMAT_SUFFIX, formatSuffix);
		params.setValue(AVKey.NUM_LEVELS, levels);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, lztd);
		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.TILE_URL_BUILDER, new FileUrlBuilder(directory,
				extension, maskDirectory, maskExtension));

		TiledImageLayer layer = new MaskTiledImageLayer(params);
		layer.setName(name);
		layer.setUseTransparentTextures(true);
		layer.setUseMipMaps(true);
		return layer;
	}

	private static class FileUrlBuilder implements TileUrlBuilder
	{
		private File directory;
		private String extension;
		private File maskDirectory;
		private String maskExtension;

		public FileUrlBuilder(File directory, String extension,
				File maskDirectory, String maskExtension)
		{
			this.directory = directory;
			this.extension = extension;
			this.maskDirectory = maskDirectory;
			this.maskExtension = maskExtension;
		}

		public URL getURL(Tile tile, String imageFormat)
				throws MalformedURLException
		{
			boolean mask = "mask".equalsIgnoreCase(imageFormat);
			File file = new File(mask ? maskDirectory : directory, tile
					.getLevelNumber()
					+ File.separator
					+ paddedInt(tile.getRow(), 4)
					+ File.separator
					+ paddedInt(tile.getRow(), 4)
					+ "_"
					+ paddedInt(tile.getColumn(), 4)
					+ "."
					+ (mask ? maskExtension : extension));
			if (file.exists())
				return file.toURI().toURL();
			return FileLayer.class.getResource("blank."
					+ (mask ? maskExtension : extension));
		}
	}

	private static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
}
