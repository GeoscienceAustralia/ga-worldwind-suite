package au.gov.ga.worldwind.dataset;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.gov.ga.worldwind.dataset.downloader.Downloader;
import au.gov.ga.worldwind.dataset.downloader.RetrievalHandler;
import au.gov.ga.worldwind.dataset.downloader.RetrievalResult;

public abstract class AbstractData implements IData
{
	private String name;
	private URL descriptionURL;
	private URL iconURL;
	private boolean iconLoaded = false;
	private ImageIcon icon;
	private boolean iconDownloading = false;

	private Object iconLock = new Object();

	public AbstractData(String name, URL descriptionURL, URL iconURL)
	{
		this.name = name;
		this.descriptionURL = descriptionURL;
		this.iconURL = iconURL;
		iconLoaded = iconURL == null;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getDescriptionURL()
	{
		return descriptionURL;
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
	public boolean loadIcon(final Runnable afterLoad)
	{
		synchronized (iconLock)
		{
			if (!iconDownloading && !iconLoaded)
			{
				iconDownloading = true;
				RetrievalHandler setIconHandler = new RetrievalHandler()
				{
					@Override
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

	@Override
	public ImageIcon getIcon()
	{
		synchronized (iconLock)
		{
			return icon;
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
