package au.gov.ga.worldwind.common.layers.geometry.provider;

import static au.gov.ga.worldwind.common.util.URLUtil.*;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.retrieve.AbstractRetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.layers.geometry.GeometryLayer;
import au.gov.ga.worldwind.common.layers.geometry.ShapeProvider;

/**
 * A base class for shape providers. Handles the download and cache management, leaving subclasses
 * to implement specific file format handling logic. 
 */
public abstract class ShapeProviderBase implements ShapeProvider
{
	private boolean loaded = false;
	private FileStore dataFileStore = WorldWind.getDataFileStore();
	private final Object fileLock = new Object();

	@Override
	public void requestShapes(GeometryLayer layer)
	{
		if (!loaded)
		{
			RequestTask task = new RequestTask(this, layer);
			WorldWind.getTaskService().addTask(task);
		}
	}

	/**
	 * Is the cached file expired (download time is earlier than layer's last
	 * update time)?
	 * 
	 * @return <code>true</code> if the file has expired
	 */
	protected boolean isFileExpired(GeometryLayer layer, URL fileURL, FileStore fileStore)
	{
		if (!WWIO.isFileOutOfDate(fileURL, layer.getExpiryTime()))
		{
			return false;
		}

		// The file has expired. Delete it.
		fileStore.removeFile(fileURL);
		String message = Logging.getMessage("generic.DataFileExpired", fileURL);
		Logging.logger().fine(message);
		return true;
	}

	public FileStore getDataFileStore()
	{
		return this.dataFileStore;
	}

	public Object getFileLock()
	{
		return fileLock;
	}

	public void setDataFileStore(FileStore fileStore)
	{
		if (fileStore == null)
		{
			String message = Logging.getMessage("nullValue.FileStoreIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		this.dataFileStore = fileStore;
	}

	/**
	 * Downloads the shape source file for the layer.
	 */
	protected void downloadShapeSourceFile(GeometryLayer layer, RetrievalPostProcessor postProcessor)
	{
		if (!layer.isNetworkRetrievalEnabled() || !WorldWind.getRetrievalService().isAvailable())
		{
			return;
		}

		URL url;
		try
		{
			url = layer.getShapeSourceUrl();
			if (url == null || WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				return;
			}
		}
		catch (MalformedURLException e)
		{
			String message = "Exception creating point data URL";
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return;
		}

		Retriever retriever;

		if (isHttpUrl(url) || isHttpsUrl(url))
		{
			if (postProcessor == null)
			{
				postProcessor = new DownloadPostProcessor(this, layer);
			}
			retriever = new HTTPRetriever(url, postProcessor);
		}
		else
		{
			Logging.logger().severe(Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", url.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer timeout = AVListImpl.getIntegerValue(layer, AVKey.URL_CONNECT_TIMEOUT);
		if (timeout != null && timeout > 0)
		{
			retriever.setConnectTimeout(timeout);
		}
		
		timeout = AVListImpl.getIntegerValue(layer, AVKey.URL_READ_TIMEOUT);
		if (timeout != null && timeout > 0)
		{
			retriever.setReadTimeout(timeout);
		}
		
		Integer staleRequestLimit = AVListImpl.getIntegerValue(layer, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (staleRequestLimit != null && staleRequestLimit > 0)
		{
			retriever.setStaleRequestLimit(staleRequestLimit);
		}

		WorldWind.getRetrievalService().runRetriever(retriever);
	}

	/**
	 * Load the shapes from the file pointed to by the url. Delegates the
	 * loading to the subclass.
	 * 
	 * @return <code>true</code> if the shapes were loaded successfully
	 */
	protected boolean loadShapes(URL url, GeometryLayer layer)
	{
		synchronized (getFileLock())
		{
			if (!loaded)
			{
				loaded = doLoadShapes(url, layer);
			}
			return loaded;
		}
	}

	/**
	 * Load the shapes from the file pointed to by the url.
	 * 
	 * @return <code>true</code> if the shapes were loaded successfully
	 */
	protected abstract boolean doLoadShapes(URL url, GeometryLayer layer);

	/**
	 * {@link RetrievalPostProcessor} used when downloading the shape data.
	 */
	protected static class DownloadPostProcessor extends AbstractRetrievalPostProcessor
	{
		protected final ShapeProviderBase provider;
		protected final GeometryLayer layer;

		public DownloadPostProcessor(ShapeProviderBase provider, GeometryLayer layer)
		{
			this.provider = provider;
			this.layer = layer;
		}

		@Override
		protected Object getFileLock()
		{
			return provider.getFileLock();
		}

		@Override
		protected File doGetOutputFile()
		{
			return provider.getDataFileStore().newFile(layer.getDataCacheName());
		}
	}

	/**
	 * Task which downloads and/or loads the shapes.
	 */
	private static class RequestTask implements Runnable
	{
		private final ShapeProviderBase provider;
		private final GeometryLayer layer;

		private RequestTask(ShapeProviderBase provider, GeometryLayer layer)
		{
			this.provider = provider;
			this.layer = layer;
		}

		@Override
		public void run()
		{
			String dataCacheName = layer.getDataCacheName();

			//first check if the layer URL is pointing to a local file (has file:// protocol)
			URL fileUrl = null;
			try
			{
				URL url = layer.getShapeSourceUrl();
				if (isFileUrl(url))
				{
					fileUrl = url;
				}
			}
			catch (MalformedURLException e)
			{
			}

			if (fileUrl != null)
			{
				//if the layer url is a local file, load the points straight away
				if (provider.loadShapes(fileUrl, layer))
				{
					layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
			}
			else
			{
				//otherwise, check the cache for the downloaded points, and load or download

				URL url = provider.getDataFileStore().findFile(dataCacheName, false);
				if (url != null && !this.provider.isFileExpired(layer, url, provider.getDataFileStore()))
				{
					if (provider.loadShapes(url, layer))
					{
						layer.firePropertyChange(AVKey.LAYER, null, this);
						return;
					}
					else
					{
						// Assume that something's wrong with the file and delete it.
						provider.getDataFileStore().removeFile(url);
						String message = Logging.getMessage("generic.DeletedCorruptDataFile", url);
						Logging.logger().info(message);
					}
				}

				provider.downloadShapeSourceFile(layer, null);
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}

			final RequestTask that = (RequestTask) o;

			//assumes each layer only has a single file to request
			return !(layer != null ? !layer.equals(that.layer) : that.layer != null);
		}

		@Override
		public int hashCode()
		{
			return (layer != null ? layer.hashCode() : 0);
		}
	}

}
