package au.gov.ga.worldwind.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility method for working with URLs
 */
public class URLUtil
{

	public static boolean isHttpsUrl(URL url)
	{
		return url != null && "https".equalsIgnoreCase(url.getProtocol());
	}

	public static boolean isHttpUrl(URL url)
	{
		return url != null && "http".equalsIgnoreCase(url.getProtocol());
	}
	
	public static boolean isFileUrl(URL url)
	{
		return url != null && "file".equalsIgnoreCase(url.getProtocol());
	}
	
	/**
	 * @return A new URL with the same protocol and path, but devoid of the query string
	 */
	public static URL stripQuery(URL url)
	{
		if (url == null || url.getQuery() == null)
		{
			return url;
		}
		try
		{
			String urlString = url.toExternalForm();
			return new URL(urlString.substring(0, urlString.indexOf('?')));
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	/**
	 * Obtain a {@link File} instance that points to the provided file:// URL.
	 * <p/>
	 * If the provided URL is not a file, will return <code>null</code>.
	 */
	public static File urlToFile(URL url)
	{
		if (!URLUtil.isFileUrl(url))
		{
			return null;
		}
		
		try
		{
			return new File(url.toURI());
		}
		catch (Exception e1)
		{
			try
			{
				return new File(url.getPath());
			}
			catch (Exception e2)
			{
			}
		}
		
		return null;
	}
}
