package au.gov.ga.worldwind.common.terrain;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.AVKeyMore;
import au.gov.ga.worldwind.common.util.Util;

/**
 * Elevation model that retrieves its elevation data from elevation tiles stored
 * in a directory in the local file system.
 * 
 * @author Michael de Hoog
 */
public class FileElevationModel extends BoundedBasicElevationModel
{
	public FileElevationModel(Element domElement, AVList params)
	{
		super(domElement, createURLBuilderParam(params));
	}

	protected static AVList createURLBuilderParam(AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		params.setValue(AVKey.TILE_URL_BUILDER, new FileURLBuilder(context));

		return params;
	}

	/**
	 * TileUrlBuilder implementation that creates file:// URLs pointing to
	 * elevation tiles stored locally.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class FileURLBuilder implements TileUrlBuilder
	{
		private URL context;

		public FileURLBuilder(URL context)
		{
			this.context = context;
		}

		@Override
		public URL getURL(Tile tile, String imageFormat) throws MalformedURLException
		{
			String service = tile.getLevel().getService();
			String dataset = tile.getLevel().getDataset();

			if (dataset == null || dataset.length() <= 0)
				dataset = service;
			else if (service != null && service.length() > 0)
				dataset = service + "/" + dataset;

			if (dataset == null)
				dataset = "";

			File directory = Util.getPathWithinContext(dataset, context);
			if (directory == null)
				return null;

			//default to BIL
			String ext = "bil";
			if (imageFormat != null)
			{
				imageFormat = imageFormat.toLowerCase();
				if (imageFormat.contains("zip"))
					ext = "zip";
			}

			File file =
					new File(directory, tile.getLevelNumber() + File.separator
							+ Util.paddedInt(tile.getRow(), 4) + File.separator
							+ Util.paddedInt(tile.getRow(), 4) + "_"
							+ Util.paddedInt(tile.getColumn(), 4) + "." + ext);
			return file.toURI().toURL();
		}
	}

	protected String getImageFormat()
	{
		AVList params = (AVList) getValue(AVKey.CONSTRUCTION_PARAMETERS);
		if (params != null)
		{
			return params.getStringValue(AVKey.IMAGE_FORMAT);
		}
		return null;
	}

	@Override
	protected void requestTile(TileKey key)
	{
		if (WorldWind.getTaskService().isFull())
			return;

		if (this.getLevels().isResourceAbsent(key))
			return;

		RequestTask request = new RequestTask(key, this);
		WorldWind.getTaskService().addTask(request);
	}

	@Override
	protected BufferWrapper readElevations(URL url) throws IOException
	{
		//overridden to handle unzipping the file if required

		if (!url.toExternalForm().toLowerCase().endsWith(".zip"))
			return super.readElevations(url);

		try
		{
			InputStream is = url.openStream();
			ZipRetriever zr = new ZipRetriever(url);
			ByteBuffer byteBuffer = zr.readZipStream(is, url);

			// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
			AVList bufferParams = new AVListImpl();
			bufferParams.setValue(AVKey.DATA_TYPE, this.getElevationDataPixelType());
			bufferParams.setValue(AVKey.BYTE_ORDER, this.getElevationDataByteOrder());
			return BufferWrapper.wrap(byteBuffer, bufferParams);
		}
		catch (java.io.IOException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					"ElevationModel.ExceptionReadingElevationFile", url.toString());
			throw e;
		}
	}

	/**
	 * URLRetriever that makes the readZipStream function accessible.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class ZipRetriever extends URLRetriever
	{
		public ZipRetriever(URL url)
		{
			super(url, null);
		}

		@Override
		public ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
		{
			return super.readZipStream(inputStream, url);
		}
	}

	/**
	 * This {@link RequestTask} creates elevation tile requests passing the
	 * image format returned by the getImageFormat() function. It also skips
	 * calling the downloadElevations() function, as the tiles are stored
	 * locally and don't need to be downloaded.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class RequestTask implements Runnable
	{
		protected final FileElevationModel elevationModel;
		protected final TileKey tileKey;

		protected RequestTask(TileKey tileKey, FileElevationModel elevationModel)
		{
			this.elevationModel = elevationModel;
			this.tileKey = tileKey;
		}

		@Override
		public final void run()
		{
			//modified to load tiles directly from ResourceURL instead of checking
			//cache and downloading non-existant tiles

			try
			{
				// check to ensure load is still needed
				if (elevationModel.areElevationsInMemory(tileKey))
					return;

				ElevationTile tile = elevationModel.createTile(tileKey);
				final URL url = tile.getResourceURL(elevationModel.getImageFormat());

				if (url != null && elevationModel.loadElevations(tile, url))
				{
					elevationModel.getLevels().unmarkResourceAbsent(tile);
					elevationModel.firePropertyChange(AVKey.ELEVATION_MODEL, null, this);
				}
				else
				{
					elevationModel.getLevels().markResourceAbsent(tile);
				}
			}
			catch (IOException e)
			{
				String msg =
						Logging.getMessage("ElevationModel.ExceptionRequestingElevations",
								tileKey.toString());
				Logging.logger().log(java.util.logging.Level.FINE, msg, e);
			}
		}

		@Override
		public final boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			//noinspection RedundantIfStatement
			if (this.tileKey != null ? !this.tileKey.equals(that.tileKey) : that.tileKey != null)
				return false;

			return true;
		}

		@Override
		public final int hashCode()
		{
			return (this.tileKey != null ? this.tileKey.hashCode() : 0);
		}

		@Override
		public final String toString()
		{
			return this.tileKey.toString();
		}
	}
}
