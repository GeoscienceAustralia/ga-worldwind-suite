package au.gov.ga.worldwind.viewer.util;

import java.io.InputStream;

public class InputStreamGobbler
{
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
