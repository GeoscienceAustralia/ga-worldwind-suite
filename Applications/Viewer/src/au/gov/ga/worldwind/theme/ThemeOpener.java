package au.gov.ga.worldwind.theme;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import au.gov.ga.worldwind.downloader.Downloader;
import au.gov.ga.worldwind.downloader.RetrievalResult;

public class ThemeOpener
{
	public static void openTheme(final URL url, final ThemeOpenDelegate delegate)
	{
		if (url == null)
			throw new IllegalArgumentException("Theme URL cannot be null");
		
		//TODO create loading frame

		SwingWorker<Theme, ?> worker = new SwingWorker<Theme, Object>()
		{
			@Override
			protected Theme doInBackground() throws Exception
			{
				RetrievalResult result = Downloader.downloadImmediatelyIfModified(url);
				InputStream is = result.getAsInputStream();
				Theme theme = ThemeFactory.createFromXML(is, url);
				if (theme == null)
					throw new Exception("Could not create theme from XML document");
				return theme;
			}

			@Override
			protected void done()
			{
				Theme theme = null;
				try
				{
					try
					{
						theme = get();
					}
					catch (InterruptedException e)
					{
						//ignore
					}
					catch (ExecutionException ee)
					{
						Throwable e = ee.getCause();
						if (e == null)
							e = ee;
						throw e;
					}
				}
				catch (Throwable e)
				{
					JOptionPane.showMessageDialog(null, "Could not open theme " + url + ": "
							+ e.getLocalizedMessage() + "\n\nUsing default theme", "Error",
							JOptionPane.ERROR_MESSAGE);
					openDefault(delegate);
				}

				if (theme != null)
					delegate.opened(theme);
			}
		};
		worker.execute();
	}

	public static void openDefault(ThemeOpenDelegate delegate)
	{
		URL url = ThemeOpener.class.getResource("/config/DefaultTheme.xml");
		try
		{
			InputStream is = url.openStream();
			Theme theme = ThemeFactory.createFromXML(is, url);
			if (theme == null)
				throw new Exception("Could not create theme from XML document");
			delegate.opened(theme);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Could not open default theme: "
					+ e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static interface ThemeOpenDelegate
	{
		public void opened(Theme theme);
	}
}
