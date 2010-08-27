package au.gov.ga.worldwind.viewer.application;

import gov.nasa.worldwind.Configuration;
import au.gov.ga.worldwind.common.util.DirectDraw;
import au.gov.ga.worldwind.common.util.NativeJOGLLibs;

public class Executable
{
	public static void main(String[] args)
	{
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeJOGLLibs.init();
		Application.main(args);
	}
}
