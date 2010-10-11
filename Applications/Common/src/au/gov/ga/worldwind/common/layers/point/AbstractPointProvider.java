package au.gov.ga.worldwind.common.layers.point;

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

/**
 * Basic implementation of the {@link PointProvider} interface. Handles
 * retrieving the point data from the layer's url, and once downloaded, calls an
 * abstract method which loads the points from the data.
 * 
 * @author Michael de Hoog
 */
public abstract class AbstractPointProvider implements PointProvider
{
	private boolean loaded = false;
	private FileStore dataFileStore = WorldWind.getDataFileStore();
	private final Object fileLock = new Object();

	@Override
	public void requestPoints(PointLayer layer)
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
	 * @param layer
	 * @param fileURL
	 * @param fileStore
	 * @return True if the file has expired
	 */
	protected boolean isFileExpired(PointLayer layer, URL fileURL, FileStore fileStore)
	{
		if (!WWIO.isFileOutOfDate(fileURL, layer.getExpiryTime()))
			return false;

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
	 * Downloads the points file for the layer.
	 * 
	 * @param layer
	 * @param postProcessor
	 */
	protected void downloadFile(PointLayer layer, RetrievalPostProcessor postProcessor)
	{
		if (!layer.isNetworkRetrievalEnabled())
			return;

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL url;
		try
		{
			url = layer.getUrl();
			if (url == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
				return;
		}
		catch (MalformedURLException e)
		{
			String message = "Exception creating point data URL";
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return;
		}

		Retriever retriever;

		if ("http".equalsIgnoreCase(url.getProtocol())
				|| "https".equalsIgnoreCase(url.getProtocol()))
		{
			if (postProcessor == null)
				postProcessor = new DownloadPostProcessor(this, layer);
			retriever = new HTTPRetriever(url, postProcessor);
		}
		else
		{
			Logging.logger().severe(
					Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol",
							url.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(layer, AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(layer, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(layer, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever);
	}

	/**
	 * Load the points from the file pointed to by the url. Delegates the
	 * loading to the subclass.
	 * 
	 * @param url
	 * @param layer
	 * @return True if the points were loaded successfully
	 */
	protected boolean loadPoints(URL url, PointLayer layer)
	{
		synchronized (getFileLock())
		{
			if (!loaded)
				loaded = doLoadPoints(url, layer);
			return loaded;
		}
	}

	/**
	 * Load the points from the file pointed to by the url.
	 * 
	 * @param url
	 * @param layer
	 * @return True if the points were loaded successfully
	 */
	protected abstract boolean doLoadPoints(URL url, PointLayer layer);

	/**
	 * {@link RetrievalPostProcessor} used when downloading the points data.
	 */
	protected static class DownloadPostProcessor extends AbstractRetrievalPostProcessor
	{
		protected final AbstractPointProvider provider;
		protected final PointLayer layer;

		public DownloadPostProcessor(AbstractPointProvider provider, PointLayer layer)
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
	 * Task which downloads and/or loads the points.
	 */
	private static class RequestTask implements Runnable
	{
		private final AbstractPointProvider provider;
		private final PointLayer layer;

		private RequestTask(AbstractPointProvider provider, PointLayer layer)
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
				URL url = layer.getUrl();
				if ("file".equalsIgnoreCase(url.getProtocol()))
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
				if (provider.loadPoints(fileUrl, layer))
				{
					layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
			}
			else
			{
				//otherwise, check the cache for the downloaded points, and load or download
				
				URL url = provider.getDataFileStore().findFile(dataCacheName, false);
				if (url != null
						&& !this.provider.isFileExpired(layer, url, provider.getDataFileStore()))
				{
					if (provider.loadPoints(url, layer))
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

				provider.downloadFile(layer, null);
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

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
