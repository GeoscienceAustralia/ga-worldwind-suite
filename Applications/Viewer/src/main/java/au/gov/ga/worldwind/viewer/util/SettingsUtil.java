package au.gov.ga.worldwind.viewer.util;

import gov.nasa.worldwind.geom.LatLon;

import java.io.File;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.viewer.settings.Settings;

/**
 * Helper class with some static settings functions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SettingsUtil
{
	/**
	 * Calculate an appropriate time length (in milliseconds) for an animation
	 * between the given latlons, taking into account the Settings view iterator
	 * speed.
	 * 
	 * @param beginLatLon
	 * @param endLatLon
	 * @return Time to animate between the two latlons
	 */
	public static long getScaledLengthMillis(LatLon beginLatLon, LatLon endLatLon)
	{
		return Util.getScaledLengthMillis(Settings.get().getViewIteratorSpeed(), beginLatLon, endLatLon);
	}

	/**
	 * @return Directory to store user settings
	 */
	public static File getUserDirectory()
	{
		String home = System.getProperty("user.home");
		File homeDir = new File(home);
		File dir = new File(homeDir, ".gaww");
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	/**
	 * Calculate a File that is stored in the user settings directory with the
	 * given filename.
	 * 
	 * @param filename
	 * @return File in user settings directory with given filename
	 */
	public static File getSettingsFile(String filename)
	{
		return new File(getUserDirectory(), filename);
	}
}
