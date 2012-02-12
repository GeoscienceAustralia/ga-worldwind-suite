package au.gov.ga.worldwind.common.util;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.StatusBar;

/**
 * {@link StatusBar} subclass that displays altitude in meters if below 10 kms.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MetersStatusBar extends StatusBar
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
				s = String.format(altitude + " %,7d m", (int) Math.round(metersAltitude));
			else
				s = String.format(altitude + " %,7d km", (int) Math.round(metersAltitude / 1e3));
		}
		return s;
	}
}
