package layers.immediate;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

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
			downloadTexture(tile, null);
		loadTextureFromStore(tile);

		// Add to list of tiles to be drawn. Usually this is done by addTile(),
		// but if texture needs to be requested, it must be forced. See
		// addTile().
		addTileToCurrent(tile);
	}

	private boolean loadTextureFromStore(TextureTile tile)
	{
		// from BasicTiledImageLayer.requestTask.run()

		final URL textureURL = getDataFileStore().findFile(tile.getPath(),
				false);
		if (textureURL != null
				&& !isTextureFileExpired(tile, textureURL, getDataFileStore()))
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
				getDataFileStore().removeFile(textureURL);
				String message = Logging.getMessage(
						"generic.DeletedCorruptDataFile", textureURL);
				Logging.logger().info(message);
			}
		}
		return false;
	}
}
