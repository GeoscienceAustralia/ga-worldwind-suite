package au.gov.ga.worldwind.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class URLTransformer
{
	public interface URLTransform
	{
		String transformURL(String url);
	}
	
	private static List<URLTransform> transforms = new ArrayList<URLTransform>();
	
	public synchronized static void addTransform(URLTransform transform)
	{
		transforms.add(transform);
	}
	
	public synchronized static void removeTransform(URLTransform transform)
	{
		transforms.remove(transform);
	}

	public synchronized static URL transform(URL url) throws MalformedURLException
	{
		if(url == null)
			return null;
		
		return new URL(transform(url.toExternalForm()));
	}

	public synchronized static String transform(String url)
	{
		if(url == null)
			return null;
		
		for(URLTransform transform : transforms)
		{
			url = transform.transformURL(url);
		}
		return url;
	}
}
