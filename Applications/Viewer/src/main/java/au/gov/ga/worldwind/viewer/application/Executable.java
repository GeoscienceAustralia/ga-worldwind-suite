package au.gov.ga.worldwind.viewer.application;

import gov.nasa.worldwind.Configuration;
import au.gov.ga.worldwind.common.util.DirectDraw;
import au.gov.ga.worldwind.common.util.NativeLibraries;

/**
 * Main application class for the executable jar version of the Viewer. Performs
 * some extra setup steps like disabling direct draw and initializing the native
 * binaries.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Executable
{
	public static void main(String[] args)
	{
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		NativeLibraries.init();
		Application.main(args);
	}
}
