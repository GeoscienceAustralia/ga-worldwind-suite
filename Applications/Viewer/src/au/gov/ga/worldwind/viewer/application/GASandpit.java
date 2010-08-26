package au.gov.ga.worldwind.viewer.application;

import java.net.MalformedURLException;
import java.net.URL;

public class GASandpit
{
	private static boolean SANDPIT = false;
	
	public static void setSandpitMode(boolean enabled)
	{
		SANDPIT = enabled;
	}	

	public static boolean isSandpitMode()
	{
		return SANDPIT;
	}

	public static URL replace(URL url) throws MalformedURLException
	{
		if (url == null || !SANDPIT)
			return url;

		return new URL(replace(url.toExternalForm()));
	}

	public static String replace(String url)
	{
		if (url == null || !SANDPIT)
			return url;

		String externalga = "http://www.ga.gov.au";
		String sandpitga = externalga + ":8500";
		if (url.startsWith(externalga + "/"))
		{
			url = sandpitga + url.substring(externalga.length());
		}
		return url;
	}
}
