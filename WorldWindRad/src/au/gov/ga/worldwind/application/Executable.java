package au.gov.ga.worldwind.application;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;

public class Executable
{
	public static void main(String[] args)
	{
		if (Configuration.isWindowsOS())
		{
			DirectDraw.disableDirectDraw();
		}
		final SplashScreen splashScreen = new SplashScreen();
		NativeJOGLLibs.init();
		Application application = Application.start();
		final WorldWindow wwd = application.getWwd();
		wwd.addRenderingListener(new RenderingListener()
		{
			public void stageChanged(RenderingEvent event)
			{
				splashScreen.dispose();
				wwd.removeRenderingListener(this);
			}
		});
	}
}
