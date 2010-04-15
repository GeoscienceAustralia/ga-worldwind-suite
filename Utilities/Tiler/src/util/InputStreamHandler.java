package util;

import java.io.InputStream;

public abstract class InputStreamHandler
{
	public InputStreamHandler(final InputStream is)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				byte[] text = new byte[10240];
				int len = 0;
				try
				{
					while ((len = is.read(text)) > 0)
					{
						handle(new String(text, 0, len));
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
