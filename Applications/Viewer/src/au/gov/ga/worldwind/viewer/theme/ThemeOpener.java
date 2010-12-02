package au.gov.ga.worldwind.viewer.theme;

import java.awt.Component;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.HttpException;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.util.XMLUtil;

public class ThemeOpener
{
	public static void openTheme(final URL url, final ThemeOpenDelegate delegate)
	{
		if (url == null)
			throw new IllegalArgumentException("Theme URL cannot be null");

		final ProgressMonitor progress = new IndeterminateProgressMonitor(null, "Downloading theme", null);
		progress.setMillisToDecideToPopup(0);
		progress.setMillisToPopup(0);

		final ThemeOpenDelegate innerDelegate = new ThemeOpenDelegate()
		{
			@Override
			public void opened(Theme theme, Element themeElement, URL themeUrl)
			{
				delegate.opened(theme, themeElement, themeUrl);
			}
		};

		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					RetrievalResult result = Downloader.downloadImmediatelyIfModified(url, true);
					if (progress.isCanceled())
						return;

					InputStream is = result.getAsInputStream();
					Element element = XMLUtil.getElementFromSource(is);
					Theme theme = ThemeFactory.createFromXML(element, url);
					if (theme == null)
						throw new Exception("Could not create theme from XML document");

					progress.close();
					innerDelegate.opened(theme, element, url);
				}
				catch (Exception e)
				{
					progress.close();

					//TODO if response is 403 (forbidden) or 407 (authentication required), allow setting of proxy?
					//for now, just display a message

					int response = (e instanceof HttpException) ? ((HttpException) e).getResponseCode() : -1;
					boolean forbidden = response == 403 || response == 407;
					String message =
							"Could not open theme "
									+ url
									+ ": "
									+ e.getLocalizedMessage()
									+ ".\n\nUsing default theme."
									+ (forbidden
											? " If you are behind a proxy server, please setup the proxy in Options/Preferences/Network."
											: "");
					JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
					openDefault(innerDelegate);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public static void openDefault(ThemeOpenDelegate delegate)
	{
		URL url = ThemeOpener.class.getResource("/config/DefaultTheme.xml");
		try
		{
			InputStream is = url.openStream();
			Element element = XMLUtil.getElementFromSource(is);
			Theme theme = ThemeFactory.createFromXML(element, url);
			if (theme == null)
				throw new Exception("Could not create theme from XML document");
			delegate.opened(theme, element, url);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open default theme: " + e.getLocalizedMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static interface ThemeOpenDelegate
	{
		public void opened(Theme theme, Element themeElement, URL themeUrl);
	}

	public static class IndeterminateProgressMonitor extends ProgressMonitor
	{
		private final static int MAX = 100;
		private boolean closed = false;
		private final Object lock = new Object();

		public IndeterminateProgressMonitor(Component parentComponent, Object message, String note)
		{
			super(parentComponent, message, note, 0, MAX);

			Thread progressThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					int i = 0;
					while (true)
					{
						synchronized (lock)
						{
							if (isCanceled() || closed)
							{
								break;
							}

							i = (i + 1) % MAX;
							setProgress(i);
						}

						try
						{
							Thread.sleep(10);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
			});
			progressThread.setDaemon(true);
			progressThread.start();
		}

		@Override
		public void close()
		{
			synchronized (lock)
			{
				closed = true;
				super.close();
			}
		}
	}
}
