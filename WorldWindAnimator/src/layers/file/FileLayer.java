package layers.file;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import layers.elevation.textured.ExtendedBasicElevationModel;
import nasa.worldwind.layers.TiledImageLayer;
import util.FileUtil;

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

		TiledImageLayer layer = new FileMaskTiledImageLayer(params);
		layer.setName(name);
		layer.setUseTransparentTextures(true);
		layer.setUseMipMaps(true);
		return layer;
	}

	public static ExtendedBasicElevationModel createElevationModel(String name,
			String cacheName, File directory, int levels, int tilesize,
			LatLon lztd, Sector sector, double minElevation, double maxElevation)
	{
		AVList params = new AVListImpl();
		params.setValue(AVKey.TILE_WIDTH, tilesize);
		params.setValue(AVKey.TILE_HEIGHT, tilesize);
		params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
		params.setValue(AVKey.SERVICE, null);
		params.setValue(AVKey.DATASET_NAME, name);
		params.setValue(AVKey.FORMAT_SUFFIX, ".bil");
		params.setValue(AVKey.NUM_LEVELS, levels);
		params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
		params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, lztd);
		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.TILE_URL_BUILDER,
				new FileBasicElevationModel.FileUrlBuilder(directory));
		params.setValue(AVKey.ELEVATION_MIN, minElevation);
		params.setValue(AVKey.ELEVATION_MAX, maxElevation);

		FileBasicElevationModel fbem = new FileBasicElevationModel(params);
		fbem.setName(name);
		return fbem;
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
					+ FileUtil.paddedInt(tile.getRow(), 4)
					+ File.separator
					+ FileUtil.paddedInt(tile.getRow(), 4)
					+ "_"
					+ FileUtil.paddedInt(tile.getColumn(), 4)
					+ "."
					+ (mask ? maskExtension : extension));
			if (file.exists())
				return file.toURI().toURL();
			return FileLayer.class.getResource("blank."
					+ (mask ? maskExtension : extension));
		}
	}
}
