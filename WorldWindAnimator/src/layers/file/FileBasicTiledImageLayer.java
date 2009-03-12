package layers.file;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

import nasa.worldwind.layers.BasicTiledImageLayer;

public class FileBasicTiledImageLayer extends BasicTiledImageLayer
{
	public FileBasicTiledImageLayer(LevelSet levelSet)
	{
		super(levelSet);
	}

	public FileBasicTiledImageLayer(AVList params)
	{
		super(params);
	}

	public FileBasicTiledImageLayer(String stateInXml)
	{
		super(stateInXml);
	}

	@Override
	protected void downloadTexture(final TextureTile tile)
	{
		URL url;
		try
		{
			url = tile.getResourceURL();
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(
					java.util.logging.Level.SEVERE,
					Logging.getMessage(
							"layers.TextureLayer.ExceptionCreatingTextureUrl",
							tile), e);
			return;
		}

		//if protocol is not "file", then let superclass handle it
		if (url == null || !"file".equalsIgnoreCase(url.getProtocol()))
		{
			super.downloadTexture(tile);
			return;
		}

		Retriever retriever = new FileRetriever(url, new DownloadPostProcessor(
				tile, this));
		WorldWind.getRetrievalService().runRetriever(retriever,
				tile.getPriority() - 1e100);
	}
}
