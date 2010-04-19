package layers.file;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;

import java.net.URL;

import javax.media.opengl.GL;

import layers.immediate.ImmediateBasicTiledImageLayer;

public class FileBasicTiledImageLayer extends ImmediateBasicTiledImageLayer
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
	protected void downloadTexture(final TextureTile tile,
			DownloadPostProcessor postProcessor)
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

		// if protocol is not "file", then let superclass handle it
		if (url == null || !"file".equalsIgnoreCase(url.getProtocol()))
		{
			super.downloadTexture(tile, postProcessor);
			return;
		}

		if (postProcessor == null)
			postProcessor = new DownloadPostProcessor(tile, this);
		Retriever retriever = new FileRetriever(url, postProcessor);
		WorldWind.getRetrievalService().runRetriever(retriever,
				tile.getPriority() - 1e100);
	}

	protected void setBlendingFunction(DrawContext dc)
	{
		GL gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	}

	/*@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		super.requestTexture(dc, tile);

		Runnable task = getRequestQ().poll();
		while (task != null)
		{
			if (!WorldWind.getTaskService().isFull())
			{
				WorldWind.getTaskService().addTask(task);
			}
			task = getRequestQ().poll();
		}
	}*/
}
