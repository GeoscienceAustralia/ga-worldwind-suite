package au.gov.ga.worldwind.common.util;

import java.net.URL;

/**
 * Utility method for working with URLs
 */
public class URLUtil
{

	public static boolean isHttpsUrl(URL url)
	{
		return "https".equalsIgnoreCase(url.getProtocol());
	}

	public static boolean isHttpUrl(URL url)
	{
		return "http".equalsIgnoreCase(url.getProtocol());
	}
	
	public static boolean isFileUrl(URL url)
	{
		return "file".equalsIgnoreCase(url.getProtocol());
	}
	
}
