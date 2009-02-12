package application;

import java.lang.reflect.Field;

public class DirectDraw
{
	public static void disableDirectDraw()
	{
		String noddraw = System.getProperty("sun.java2d.noddraw");
		if (noddraw == null || !noddraw.equalsIgnoreCase("true"))
		{
			System.setProperty("sun.java2d.noddraw", "true");

			Class<?> windowsFlags = null;
			try
			{
				windowsFlags = Class.forName("sun.awt.WindowsFlags");
			}
			catch (ClassNotFoundException e1)
			{
				try
				{
					windowsFlags = Class
							.forName("sun.java2d.windows.WindowsFlags");
				}
				catch (ClassNotFoundException e2)
				{
				}
			}
			if (windowsFlags != null)
			{
				try
				{
					Field field = windowsFlags.getDeclaredField("ddEnabled");
					field.setAccessible(true);
					field.set(null, false);
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
