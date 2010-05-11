package au.gov.ga.worldwind.dataset;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.gov.ga.worldwind.dataset.downloader.Downloader;
import au.gov.ga.worldwind.dataset.downloader.RetrievalHandler;
import au.gov.ga.worldwind.dataset.downloader.RetrievalResult;

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
	
	public boolean isIconLoaded()
	{
		synchronized (iconLock)
		{
			return iconLoaded;
		}
	}

	public boolean loadIcon(final Runnable afterLoad)
	{
		synchronized (iconLock)
		{
			if (!iconDownloading && !iconLoaded)
			{
				iconDownloading = true;
				RetrievalHandler setIconHandler = new RetrievalHandler()
				{
					public void handle(RetrievalResult result, boolean cached)
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
				return true;
			}
		}
		return false;
	}

	public ImageIcon getIcon()
	{
		synchronized (iconLock)
		{
			return icon;
		}
	}

	public URL getIconURL()
	{
		return iconURL;
	}

	public void setIconURL(URL iconURL)
	{
		this.iconURL = iconURL;
	}
}
