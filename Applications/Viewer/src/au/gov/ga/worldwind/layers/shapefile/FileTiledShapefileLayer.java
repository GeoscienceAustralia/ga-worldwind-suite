package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class FileTiledShapefileLayer extends TiledShapefileLayer
{
	public FileTiledShapefileLayer(LevelSet levelSet)
	{
		super(levelSet);
		setName("Shapefile");
	}

	@Override
	protected void forceTileLoad(ShapefileTile tile)
	{
		//temporary test implementation
		try
		{
			URL url = tile.getResourceURL();
			loadTile(url, tile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void requestTile(DrawContext dc, ShapefileTile tile)
	{
		//temporary test implementation
		forceTileLoad(tile);
	}

	protected void loadTile(URL url, ShapefileTile tile)
	{
		if (url != null)
		{
			try
			{
				File file = new File(url.toURI());
				Shapefile shapefile = loadFromZip(file);
				ShapefileTileData data = new ShapefileTileData(shapefile);
				tile.setData(data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static Shapefile loadFromZip(File file) throws ZipException, IOException
	{
		ZipFile zip = null;
		InputStream shpIS = null, shxIS = null, dbfIS = null;
		try
		{
			zip = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zip.entries();
			ZipEntry shpEntry = null, shxEntry = null, dbfEntry = null;
			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				if (entry.getName().toLowerCase().endsWith(".shp"))
					shpEntry = entry;
				else if (entry.getName().toLowerCase().endsWith(".shx"))
					shxEntry = entry;
				else if (entry.getName().toLowerCase().endsWith(".dbf"))
					dbfEntry = entry;
			}

			if (shpEntry == null || shxEntry == null || dbfEntry == null)
				throw new IllegalArgumentException(
						"zip file doesn't contain required shapefile elements");

			shpIS = zip.getInputStream(shpEntry);
			shxIS = zip.getInputStream(shxEntry);
			dbfIS = zip.getInputStream(dbfEntry);
			return new Shapefile(shpIS, shxIS, dbfIS);
		}
		finally
		{
			if (shpIS != null)
				shpIS.close();
			if (shxIS != null)
				shxIS.close();
			if (dbfIS != null)
				dbfIS.close();
			if (zip != null)
				zip.close();
		}
	}
}
