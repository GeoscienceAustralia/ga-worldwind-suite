package layers.immediate;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import nasa.worldwind.layers.BasicTiledImageLayer;

public class ImmediateBasicTiledImageLayer extends BasicTiledImageLayer
{
	public ImmediateBasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public ImmediateBasicTiledImageLayer(AVList params)
	{
		super(params);
	}

	public ImmediateBasicTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		if (!ImmediateMode.isImmediate())
		{
			super.requestTexture(dc, tile);
			return;
		}

		if (!loadTextureFromStore(tile))
			downloadTexture(tile);
		loadTextureFromStore(tile);

		//Add to list of tiles to be drawn. Usually this is done by addTile(),
		//but if texture needs to be requested, it must be forced. See addTile().
		addTileToCurrent(tile);
	}

	private boolean loadTextureFromStore(TextureTile tile)
	{
		//from BasicTiledImageLayer.requestTask.run()

		final java.net.URL textureURL = WorldWind.getDataFileStore().findFile(
				tile.getPath(), false);
		if (textureURL != null && !isTextureExpired(tile, textureURL))
		{
			if (loadTexture(tile, textureURL))
			{
				getLevels().unmarkResourceAbsent(tile);
				firePropertyChange(AVKey.LAYER, null, this);
				return true;
			}
			else
			{
				// Assume that something's wrong with the file and delete it.
				gov.nasa.worldwind.WorldWind.getDataFileStore().removeFile(
						textureURL);
				getLevels().markResourceAbsent(tile);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", textureURL);
				Logging.logger().info(message);
			}
		}
		return false;
	}
}
