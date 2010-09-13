package au.gov.ga.worldwind.common.util;

import au.gov.ga.worldwind.common.util.URLTransformer.URLTransform;

public class GASandpit
{
	private static boolean SANDPIT = false;
	private static URLTransform transform = new SandpitURLTransform();
	
	public static void setSandpitMode(boolean enabled)
	{
		SANDPIT = enabled;
		
		if(enabled)
			URLTransformer.addTransform(transform);
		else
			URLTransformer.removeTransform(transform);
	}	

	public static boolean isSandpitMode()
	{
		return SANDPIT;
	}
	
	private static class SandpitURLTransform implements URLTransform
	{
		@Override
		public String transformURL(String url)
		{
			String externalga = "http://www.ga.gov.au";
			if (url.startsWith(externalga + "/"))
			{
				String sandpitga = externalga + ":8500";
				url = sandpitga + url.substring(externalga.length());
			}
			return url;
		}
	}
}
