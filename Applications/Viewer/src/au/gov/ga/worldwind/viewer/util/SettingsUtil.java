package au.gov.ga.worldwind.viewer.util;

import gov.nasa.worldwind.geom.LatLon;

import java.io.File;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.settings.Settings;

public class SettingsUtil
{
	public static long getScaledLengthMillis(LatLon beginLatLon, LatLon endLatLon)
	{
		return Util.getScaledLengthMillis(Settings.get().getViewIteratorSpeed(), beginLatLon,
				endLatLon);
	}

	public static File getUserDirectory()
	{
		String home = System.getProperty("user.home");
		File homeDir = new File(home);
		File dir = new File(homeDir, ".gaww");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	public static File getSettingsFile(String filename)
	{
		return new File(getUserDirectory(), filename);
	}
}
