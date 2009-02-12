package application;

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
		Application.main(args);
	}
}
