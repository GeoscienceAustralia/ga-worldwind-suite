package tiler;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;

import javax.swing.JOptionPane;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class Executable
{
	public interface WinLibC extends Library
	{
		public int _putenv(String name);
	}

	public static void main(String[] args)
	{
		try
		{
			start();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void start() throws Exception
	{
		File dataDir = new File("data");
		File gdalDir = new File("gdal");

		if (!dataDir.exists())
		{
			throw new FileNotFoundException("Directory not found: "
					+ dataDir.getAbsolutePath());
		}
		if (!gdalDir.exists())
		{
			throw new FileNotFoundException("Directory not found: "
					+ gdalDir.getAbsolutePath());
		}

		// set the GDAL_DATA environment variable to enable reprojection support
		Object libc = Native.loadLibrary("msvcrt", WinLibC.class);
		((WinLibC) libc)._putenv("GDAL_DATA=" + dataDir.getAbsolutePath());

		// set the PATH environment variable so that the gdaljni.dll can find
		// it's dependencies
		String path = System.getenv("PATH");
		path = gdalDir.getAbsolutePath() + ";" + path;
		((WinLibC) libc)._putenv("PATH=" + path);

		// reset the java.library.path variable so the gdal.jar loads the
		// library from the correct directory
		String newLibPath = gdalDir.getAbsolutePath() + File.pathSeparator
				+ System.getProperty("java.library.path");
		System.setProperty("java.library.path", newLibPath);
		Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
		if (fieldSysPath != null)
		{
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(System.class.getClassLoader(), null);
		}

		// start the application
		Application.start();
	}
}
