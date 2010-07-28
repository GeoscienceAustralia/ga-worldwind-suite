package au.gov.ga.worldwind.application;

import java.net.MalformedURLException;
import java.net.URL;

public class Sandpit
{
	public final static boolean SANDPIT = true;

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
