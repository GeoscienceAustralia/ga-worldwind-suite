package au.gov.ga.worldwind.layers.shapefile.textured;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.layers.TextureTile;
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
	protected void forceTextureLoad(TextureTile tile)
	{
		//temporary test implementation

		if (!(tile instanceof ShapefileTile))
			throw new IllegalArgumentException("Tile must be an instance of ShapefileTile");

		try
		{
			URL url = tile.getResourceURL();
			loadTile(url, (ShapefileTile) tile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		//temporary test implementation
		forceTextureLoad(tile);
	}

	protected void loadTile(URL url, ShapefileTile tile)
	{
		if (url != null)
		{
			try
			{
				File file = new File(url.toURI());
				Shapefile shapefile = ShapefileUtils.openZippedShapefile(file);
				tile.setShapefile(shapefile, isUseMipMaps());
				shapefile.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
