package au.gov.ga.worldwind.application;

import gov.nasa.worldwind.Configuration;

public class Executable
{
	public static void main(String[] args)
	{
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeJOGLLibs.init();
		Application.start();
	}
}
