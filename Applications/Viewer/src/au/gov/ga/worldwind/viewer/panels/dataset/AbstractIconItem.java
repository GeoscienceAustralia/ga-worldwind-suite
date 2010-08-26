package au.gov.ga.worldwind.viewer.panels.dataset;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.gov.ga.worldwind.viewer.downloader.Downloader;
import au.gov.ga.worldwind.viewer.downloader.RetrievalHandler;
import au.gov.ga.worldwind.viewer.downloader.RetrievalResult;

public abstract class AbstractIconItem implements IIconItem
{
	private URL iconURL;
	private boolean iconLoaded = false;
	private ImageIcon icon;
	private boolean iconDownloading = false;
	private Object iconLock = new Object();

	public AbstractIconItem(URL iconURL)
	{
		this.iconURL = iconURL;
		iconLoaded = iconURL == null;
	}

	@Override
	public boolean isIconLoaded()
	{
		synchronized (iconLock)
		{
			return iconLoaded;
		}
	}

	@Override
	public void loadIcon(final Runnable afterLoad)
	{
		synchronized (iconLock)
		{
			if (!iconDownloading && !iconLoaded)
			{
				iconDownloading = true;
				RetrievalHandler setIconHandler = new RetrievalHandler()
				{
					@Override
					public void handle(RetrievalResult result)
					{
						synchronized (iconLock)
						{
							try
							{
								Image image = ImageIO.read(result.getAsInputStream());
								icon = new ImageIcon(image);
							}
							catch (Exception e)
							{
							}
							iconLoaded = true;
							iconDownloading = false;
						}
						afterLoad.run();
					}
				};
				Downloader.downloadIfModified(iconURL, setIconHandler, setIconHandler);
			}
		}
	}

	@Override
	public ImageIcon getIcon()
	{
		synchronized (iconLock)
		{
			return icon;
		}
	}

	@Override
	public URL getIconURL()
	{
		return iconURL;
	}

	@Override
	public void setIconURL(URL iconURL)
	{
		if (iconURL == null && this.iconURL == null)
			return;
		if (iconURL != null && iconURL.equals(this.iconURL))
			return;

		synchronized (iconLock)
		{
			this.iconURL = iconURL;

			iconLoaded = iconURL == null;
			if (iconLoaded)
				icon = null;
		}
	}

	@Override
	public boolean isLoading()
	{
		synchronized (iconLock)
		{
			return iconDownloading;
		}
	}
}
