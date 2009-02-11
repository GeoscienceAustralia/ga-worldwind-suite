package util;

import java.lang.reflect.Method;
import java.net.URI;

import javax.swing.JOptionPane;

public class BrowserLauncher
{
	private static final String errMsg = "Error attempting to launch web browser";

	public static void openURL(String url)
	{
		boolean desktopSupported = false;

		try
		{
			Class<?> desktop = Class.forName("java.awt.Desktop");
			Method getDesktopMethod = desktop.getMethod("getDesktop",
					new Class<?>[] {});
			Object desktopInstance = getDesktopMethod.invoke(null,
					new Object[] {});
			String methodName = "browse";
			if (url.toLowerCase().startsWith("mailto:"))
				methodName = "mail";
			Method method = desktop.getMethod(methodName,
					new Class<?>[] { URI.class });
			URI uri = new URI(url);
			method.invoke(desktopInstance, new Object[] { uri });
			desktopSupported = true;
		}
		catch (Exception cnfe)
		{
		}

		if (!desktopSupported)
		{
			String osName = System.getProperty("os.name");
			try
			{
				if (osName.startsWith("Mac OS"))
				{
					Class<?> fileMgr = Class
							.forName("com.apple.eio.FileManager");
					Method openURL = fileMgr.getDeclaredMethod("openURL",
							new Class[] { String.class });
					openURL.invoke(null, new Object[] { url });
				}
				else if (osName.startsWith("Windows"))
					Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler " + url);
				else
				{
					//assume Unix or Linux
					String[] browsers = { "firefox", "opera", "konqueror",
							"epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length
							&& browser == null; count++)
						if (Runtime.getRuntime().exec(
								new String[] { "which", browsers[count] })
								.waitFor() == 0)
							browser = browsers[count];
					if (browser == null)
						throw new Exception("Could not find web browser");
					else
						Runtime.getRuntime()
								.exec(new String[] { browser, url });
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, errMsg + ":\n"
						+ e.getLocalizedMessage());
			}
		}
	}
}
