package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.util.Logging;

public class MetersStatusBar extends gov.nasa.worldwind.util.StatusBar
{
	@Override
	protected String makeEyeAltitudeDescription(double metersAltitude)
	{
		String s;
		String altitude = Logging.getMessage("term.Altitude");
		if (UNIT_IMPERIAL.equals(getElevationUnit()))
			return super.makeEyeAltitudeDescription(metersAltitude);
		else
		{
			if (metersAltitude < 1e4)
				s = String.format(altitude + " %,7d m", (int) Math
						.round(metersAltitude));
			else
				s = String.format(altitude + " %,7d km", (int) Math
						.round(metersAltitude / 1e3));
		}
		return s;
	}
}
