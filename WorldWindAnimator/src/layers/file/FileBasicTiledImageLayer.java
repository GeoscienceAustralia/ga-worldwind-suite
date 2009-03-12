package layers.file;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileBasicTiledImageLayer extends BasicTiledImageLayer
{
	private Object fileLock = new Object();

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

		File file = null;
		try
		{
			file = new File(url.toURI());
		}
		catch (URISyntaxException e)
		{
			return;
		}
		final File inFile = file;

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				final File outFile = WorldWind.getDataFileStore().newFile(
						tile.getPath());
				if (outFile == null)
					return;
				if (outFile.exists())
					return;

				try
				{
					if (outFile.getName().toLowerCase().endsWith(".dds"))
					{
						ByteBuffer buffer = DDSCompressor
								.compressImageFile(inFile);
						if (buffer != null)
						{
							saveBuffer(buffer, outFile);
						}
					}
					else
					{
						RandomAccessFile out = new RandomAccessFile(inFile, "r");
						FileChannel ch = out.getChannel();
						ByteBuffer buffer = ch.map(
								FileChannel.MapMode.READ_ONLY, 0, inFile
										.length());
						saveBuffer(buffer, outFile);
						ch.close();
						buffer = null;
					}

					firePropertyChange(AVKey.LAYER, null, this);
				}
				catch (IOException e)
				{
					getLevels().markResourceAbsent(tile);
					Logging
							.logger()
							.log(
									java.util.logging.Level.SEVERE,
									Logging
											.getMessage(
													"layers.TextureLayer.ExceptionSavingRetrievedTextureFile",
													tile.getPath()), e);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		synchronized (fileLock)
		{
			super.requestTexture(dc, tile);
		}
	}

	private void saveBuffer(ByteBuffer buffer, File outFile) throws IOException
	{
		synchronized (this.fileLock)
		{
			WWIO.saveBuffer(buffer, outFile);
		}
	}
}
