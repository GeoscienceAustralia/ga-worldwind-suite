package au.gov.ga.worldwind.common.util;

import au.gov.ga.worldwind.common.util.URLTransformer.URLTransform;

public class GASandpit
{
	private static boolean SANDPIT = false;
	private static final URLTransform transform = new SandpitURLTransform();
	
	public static void setSandpitMode(boolean enabled)
	{
		if(enabled && !SANDPIT) // true->false, add transform
		{
			URLTransformer.addTransform(transform);
		}
		else if (SANDPIT && !enabled) // false->true, remove transform
		{
			URLTransformer.removeTransform(transform);
		}
		
		SANDPIT = enabled;
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
