package layers.file;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import layers.immediate.ImmediateBasicElevationModel;

public class FileBasicElevationModel extends ImmediateBasicElevationModel
{
	public FileBasicElevationModel(LevelSet levels)
	{
		super(levels);
	}

	public FileBasicElevationModel(AVList params)
	{
		super(params);
	}

	public FileBasicElevationModel(String stateInXml)
	{
		super(stateInXml);
	}

	@Override
	protected void downloadElevations(Tile tile)
	{
		java.net.URL url = null;
		try
		{
			url = tile.getResourceURL();
			if (url == null
					|| WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return;
			}
		}
		catch (java.net.MalformedURLException e)
		{
			Logging
					.logger()
					.log(
							java.util.logging.Level.SEVERE,
							Logging
									.getMessage(
											"TiledElevationModel.ExceptionCreatingElevationsUrl",
											url), e);
			return;
		}

		// if protocol is not "file", then let superclass handle it
		if (url == null || !"file".equalsIgnoreCase(url.getProtocol()))
		{
			super.downloadElevations(tile);
			return;
		}

		Retriever retriever = new FileRetriever(url,
				new FileDownloadPostProcessor(tile, this));
		if (WorldWind.getRetrievalService().contains(retriever))
			return;

		WorldWind.getRetrievalService().runRetriever(retriever, 0d);
	}

	public static class FileDownloadPostProcessor extends DownloadPostProcessor
	{
		public FileDownloadPostProcessor(Tile tile,
				BasicElevationModel elevationModel)
		{
			super(tile, elevationModel);
		}

		@Override
		protected boolean validateResponseCode()
		{
			if (this.retriever instanceof FileRetriever)
				return true;
			return super.validateResponseCode();
		}

		@Override
		protected ByteBuffer handleContent() throws IOException
		{
			this.saveBuffer();
			return this.getRetriever().getBuffer();
		}
	}

	public static class FileUrlBuilder implements TileUrlBuilder
	{
		private File directory;

		public FileUrlBuilder(File directory)
		{
			this.directory = directory;
		}

		public URL getURL(gov.nasa.worldwind.util.Tile tile, String imageFormat)
				throws MalformedURLException
		{
			File file = new File(directory, tile.getLevelNumber()
					+ File.separator + paddedInt(tile.getRow(), 4)
					+ File.separator + paddedInt(tile.getRow(), 4) + "_"
					+ paddedInt(tile.getColumn(), 4) + ".bil");
			if (file.exists())
				return file.toURI().toURL();
			return FileLayer.class.getResource("blank.bil");
		}
	}

	private static String paddedInt(int value, int charcount)
	{
		String str = String.valueOf(value);
		while (str.length() < charcount)
		{
			str = "0" + str;
		}
		return str;
	}
}
