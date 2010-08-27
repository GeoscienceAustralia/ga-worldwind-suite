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
		if (GASandpit.isSandpitMode())
		{
			//Sandpit mode is enabled, so insert '.sandpit' in the filename
			String prefix, suffix;
			int lastIndexOfPeriod = filename.lastIndexOf('.');
			if (lastIndexOfPeriod >= 0)
			{
				prefix = filename.substring(0, lastIndexOfPeriod);
				suffix = filename.substring(lastIndexOfPeriod, filename.length());
			}
			else
			{
				prefix = filename;
				suffix = "";
			}
			filename = prefix + ".sandpit" + suffix;
		}

		return new File(getUserDirectory(), filename);
	}
}
