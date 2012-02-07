package au.gov.ga.worldwind.tiler.util;

import java.util.prefs.Preferences;

public class Prefs
{
	private static Preferences preferences = Preferences.userRoot().node(
			"au/gov/ga/worldwindtiler");

	public static Preferences getPreferences()
	{
		return preferences;
	}
}
