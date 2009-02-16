package au.gov.ga.worldwind.layers.shader;

import gov.nasa.worldwind.geom.LatLon;

import java.util.Calendar;
import java.util.GregorianCalendar;

import au.gov.ga.worldwind.util.SunCalculator;


public class BasicSunPositionProvider implements SunPositionProvider
{
	private LatLon position;
	private Calendar calendar;

	public BasicSunPositionProvider()
	{
		calendar = new GregorianCalendar();
		updatePosition();

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(60000);
					}
					catch (InterruptedException e)
					{
					}
					calendar.setTimeInMillis(System.currentTimeMillis());
					updatePosition();
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private synchronized void updatePosition()
	{
		position = SunCalculator.subsolarPoint(calendar);
	}

	public synchronized LatLon getPosition()
	{
		return position;
	}
}
