package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.util.Util;

public class Sandpit extends ApplicationTemplate
{
	public static class AppFrame extends ApplicationTemplate.AppFrame
	{
		public AppFrame()
		{
			super(true, true, false);

			AVList params = new AVListImpl();
			params.setValue(AVKey.TILE_WIDTH, 512);
			params.setValue(AVKey.TILE_HEIGHT, 512);
			Angle delta = Angle.fromDegrees(36);
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
			params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
			params.setValue(AVKey.NUM_LEVELS, 5);
			params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
			params.setValue(AVKey.DATASET_NAME, "shapefile");
			params.setValue(AVKey.DATA_CACHE_NAME, "shapefile");
			params.setValue(AVKey.TILE_ORIGIN, LatLon.fromDegrees(-90, -180));
			params.setValue(AVKey.FORMAT_SUFFIX, ".zip");
			params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder()
			{
				@Override
				public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
				{
					String filename =
							"C:/WINNT/Profiles/u97852/Desktop/GSHHS_shp/tiled/"
									+ tile.getLevelNumber() + "/"
									+ Util.paddedInt(tile.getRow(), 4) + "/"
									+ Util.paddedInt(tile.getRow(), 4) + "_"
									+ Util.paddedInt(tile.getColumn(), 4) + ".zip";
					File file = new File(filename);
					if (file.exists())
						return file.toURI().toURL();

					return null;
				}
			});

			LevelSet levels = new LevelSet(params);
			TiledShapefileLayer shapefile = new FileTiledShapefileLayer(levels);
			insertAfterPlacenames(getWwd(), shapefile);

			// Update layer panel
			this.getLayerPanel().update(getWwd());
		}
	}

	public static void main(String[] args)
	{
		Configuration.setValue(AVKey.VERTICAL_EXAGGERATION, 100d);

		ApplicationTemplate.start("Sandpit", AppFrame.class);
	}
}
