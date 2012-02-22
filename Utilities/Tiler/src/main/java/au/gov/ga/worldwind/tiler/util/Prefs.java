package au.gov.ga.worldwind.tiler.util;

import java.util.prefs.Preferences;

/**
 * Helper class providing easy access to the {@link Preferences#userRoot()}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Prefs
{
	private static Preferences preferences = Preferences.userRoot().node("au/gov/ga/worldwindtiler");

	public static Preferences getPreferences()
	{
		return preferences;
	}
}
