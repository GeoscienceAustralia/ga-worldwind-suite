/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.panels.dataset;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;

/**
 * Abstract implementation of the {@link IIconItem} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
				Downloader.downloadIfModified(iconURL, setIconHandler, setIconHandler, true);
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
