package au.gov.ga.worldwind.common.util;

import java.io.InputStream;

/**
 * Helper class that reads from an {@link InputStream} on a separate thread,
 * discarding all data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InputStreamGobbler
{
	/**
	 * Create a daemon thread that reads and discards all data from the given
	 * {@link InputStream}.
	 * 
	 * @param is
	 */
	public InputStreamGobbler(final InputStream is)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				byte[] text = new byte[1024];
				try
				{
					while (is.read(text) >= 0)
						;
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
}
