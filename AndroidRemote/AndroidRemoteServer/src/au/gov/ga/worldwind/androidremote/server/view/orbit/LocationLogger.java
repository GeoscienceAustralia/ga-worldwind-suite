package au.gov.ga.worldwind.androidremote.server.view.orbit;

import gov.nasa.worldwind.geom.Position;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class that logs the GPS location messages sent by the phone.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LocationLogger
{
	private static File output = new File("location_" + System.currentTimeMillis() + ".log");
	private static DataOutputStream dos;

	static
	{
		try
		{
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static void close()
	{
		try
		{
			dos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static synchronized void logLocation(Position location)
	{
		long millis = System.currentTimeMillis();
		long nanos = System.nanoTime();
		try
		{
			dos.writeLong(millis);
			dos.writeLong(nanos);
			for (int i = 0; i < 2; i++)
			{
				dos.writeDouble(location.latitude.degrees);
				dos.writeDouble(location.longitude.degrees);
				dos.writeDouble(location.elevation);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
