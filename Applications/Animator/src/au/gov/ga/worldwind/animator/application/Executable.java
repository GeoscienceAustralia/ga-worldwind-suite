package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;
import au.gov.ga.worldwind.common.util.DirectDraw;
import au.gov.ga.worldwind.common.util.NativeJOGLLibs;

public class Executable
{
	public static void main(String[] args)
	{
		Logging.logger().info("Starting Animator");
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeJOGLLibs.init();
		Animator.main(args);
	}
}
