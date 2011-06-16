package au.gov.ga.worldwind.common.util;

import au.gov.ga.worldwind.common.util.URLTransformer.URLTransform;

/**
 * A utility class that enables settings specific to working within the GA
 * @author u09145
 *
 */
public class GASandpit
{
	private static boolean SANDPIT = false;
	private static final URLTransform transform = new SandpitURLTransform();
	
	public static void setSandpitMode(boolean enabled)
	{
		if(enabled && !SANDPIT) // true->false, do enable
		{
			doEnableSandpit();
		}
		else if (SANDPIT && !enabled) // false->true, do disable
		{
			doDisableSandpit();
		}
		
		SANDPIT = enabled;
	}	

	private static void doEnableSandpit()
	{
		URLTransformer.addTransform(transform);
		System.setProperty("http.nonProxyHosts", "*.ga.gov.au");
	}
	
	private static void doDisableSandpit()
	{
		URLTransformer.removeTransform(transform);
		System.clearProperty("http.nonProxyHosts");
	}
	
	public static boolean isSandpitMode()
	{
		return SANDPIT;
	}
	
	static class SandpitURLTransform implements URLTransform
	{
		private static final String EXTERNAL_GA_PREFIX = "http://www.ga.gov.au";
		private static final String SANDPIT_GA_PREFIX = "http://www.ga.gov.au:8500";
		
		@Override
		public String transformURL(String url)
		{
			if (Util.isBlank(url))
			{
				return null;
			}
			
			if (url.startsWith(EXTERNAL_GA_PREFIX + "/"))
			{
				url = SANDPIT_GA_PREFIX + url.substring(EXTERNAL_GA_PREFIX.length());
			}
			return url;
		}
	}
}
