package au.gov.ga.worldwind.tiler.util;

import java.io.InputStream;

/**
 * Helper class that creates a daemon thread which reads from an InputStream,
 * and passes the read data as a string to an abstract function.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class InputStreamHandler
{
	public InputStreamHandler(final InputStream is)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				byte[] buffer = new byte[10240];
				try
				{
					int len;
					while ((len = is.read(buffer)) > 0)
					{
						handle(new String(buffer, 0, len));
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public abstract void handle(String string);
}
