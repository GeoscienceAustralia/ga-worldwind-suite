package au.gov.ga.worldwind.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

public class DefaultLauncher
{
	public static void openFile(final File file)
	{
		boolean desktopSupported = false;
		try
		{
			Class<?> desktopClass = getDesktopClassIfSupported();
			Object desktopInstance = getDesktopInstance(desktopClass);
			if (desktopClass != null && desktopInstance != null)
			{
				Method openMethod = desktopClass.getMethod("open",
						new Class<?>[] { File.class });
				openMethod.invoke(desktopInstance, new Object[] { file });
				desktopSupported = true;
			}
		}
		catch (Exception cnfe)
		{
		}

		if (!desktopSupported)
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					String osName = System.getProperty("os.name").toLowerCase();
					try
					{
						if (osName.startsWith("windows"))
						{
							Runtime.getRuntime().exec(
									"cmd.exe /C " + file.getAbsolutePath());
						}
						else if (osName.startsWith("mac"))
						{
							macFileManagerOpen(file.toURI().toURL());
						}
						else
						{
							linuxBrowserOpen(file.toURI().toURL());
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null,
								"Error attempting to open file" + ":\n"
										+ e.getLocalizedMessage());
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static void openURL(final URL url)
	{
		boolean desktopSupported = false;
		try
		{
			Class<?> desktopClass = getDesktopClassIfSupported();
			Object desktopInstance = getDesktopInstance(desktopClass);
			if (desktopClass != null && desktopInstance != null)
			{
				String methodName = "browse";
				if (url.toExternalForm().toLowerCase().startsWith("mailto:"))
					methodName = "mail";
				Method method = desktopClass.getMethod(methodName,
						new Class<?>[] { URI.class });
				URI uri = url.toURI();
				method.invoke(desktopInstance, new Object[] { uri });
				desktopSupported = true;
			}
		}
		catch (Exception cnfe)
		{
		}

		if (!desktopSupported)
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					String osName = System.getProperty("os.name").toLowerCase();
					try
					{
						if (osName.startsWith("windows"))
						{
							Runtime.getRuntime().exec(
									"rundll32 url.dll,FileProtocolHandler "
											+ url.toExternalForm());
						}
						else if (osName.startsWith("mac"))
						{
							macFileManagerOpen(url);
						}
						else
						{
							linuxBrowserOpen(url);
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null,
								"Error attempting to launch web browser"
										+ ":\n" + e.getLocalizedMessage());
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	private static Class<?> getDesktopClassIfSupported()
	{
		try
		{
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Method isDesktopSupportedMethod = desktopClass.getMethod(
					"isDesktopSupported", new Class<?>[] {});
			Object isSupported = isDesktopSupportedMethod.invoke(null,
					new Object[] {});
			if (isSupported instanceof Boolean
					&& ((Boolean) isSupported).booleanValue() == true)
				return desktopClass;
		}
		catch (Exception e)
		{
		}
		return null;
	}

	private static Object getDesktopInstance(Class<?> desktopClass)
	{
		if (desktopClass != null)
		{
			try
			{
				Method getDesktopMethod = desktopClass.getMethod("getDesktop",
						new Class<?>[] {});
				Object desktopInstance = getDesktopMethod.invoke(null,
						new Object[] {});
				return desktopInstance;
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	private static void macFileManagerOpen(URL url) throws Exception
	{
		Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
		Method openURL = fileMgr.getDeclaredMethod("openURL",
				new Class[] { String.class });
		openURL.invoke(null, new Object[] { url.toExternalForm() });
	}

	private static void linuxBrowserOpen(URL url) throws Exception
	{
		String[] browsers = { "firefox", "opera", "konqueror", "epiphany",
				"mozilla", "netscape" };
		String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++)
			if (Runtime.getRuntime().exec(
					new String[] { "which", browsers[count] }).waitFor() == 0)
				browser = browsers[count];
		if (browser == null)
			throw new Exception("Could not find web browser");
		else
		{
			Process process = Runtime.getRuntime().exec(
					new String[] { browser, url.toExternalForm() });
			new InputStreamGobbler(process.getInputStream());
			new InputStreamGobbler(process.getErrorStream());
		}
	}
}
