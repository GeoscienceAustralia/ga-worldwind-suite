package au.gov.ga.worldwind.application;

import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.event.InputHandler;

import java.lang.reflect.Field;

public class CameraSmoothHack
{
	public static void hackCameraSmoothing(InputHandler inputHandler)
	{
		if (inputHandler instanceof AWTInputHandler)
		{
			try
			{
				Field viewInputHandlerField = AWTInputHandler.class
						.getDeclaredField("viewInputHandler");
				viewInputHandlerField.setAccessible(true);
				Object viewInputHandlerObject = viewInputHandlerField
						.get(inputHandler);
				Field orbitViewInputSupportField = ViewInputHandler.class
						.getDeclaredField("orbitViewInputSupport");
				orbitViewInputSupportField.setAccessible(true);
				Object orbitViewInputSupportObject = orbitViewInputSupportField
						.get(viewInputHandlerObject);
				Class<?> orbitViewInputSupportClass = (Class
						.forName("gov.nasa.worldwind.awt.OrbitViewInputSupport"));
				Field centerMinEpsilonField = orbitViewInputSupportClass
						.getDeclaredField("centerMinEpsilon");
				Field headingMinEpsilonField = orbitViewInputSupportClass
						.getDeclaredField("headingMinEpsilon");
				Field pitchMinEpsilonField = orbitViewInputSupportClass
						.getDeclaredField("pitchMinEpsilon");
				Field zoomMinEpsilonField = orbitViewInputSupportClass
						.getDeclaredField("zoomMinEpsilon");
				centerMinEpsilonField.setAccessible(true);
				headingMinEpsilonField.setAccessible(true);
				pitchMinEpsilonField.setAccessible(true);
				zoomMinEpsilonField.setAccessible(true);
				centerMinEpsilonField.set(orbitViewInputSupportObject,
						new Double(1e-10));
				headingMinEpsilonField.set(orbitViewInputSupportObject,
						new Double(1e-2));
				pitchMinEpsilonField.set(orbitViewInputSupportObject,
						new Double(1e-2));
				zoomMinEpsilonField.set(orbitViewInputSupportObject,
						new Double(1e-2));
			}
			catch (Exception e)
			{
			}
		}
	}
}
