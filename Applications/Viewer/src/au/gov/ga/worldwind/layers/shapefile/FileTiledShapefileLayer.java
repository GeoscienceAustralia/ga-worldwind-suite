package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;

import java.io.File;
import java.net.URL;

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
				Shapefile shapefile = ShapefileUtils.openZippedShapefile(file);
				ShapefileTileData data = new ShapefileTileData(tile.getSector(), shapefile);
				tile.setData(data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
