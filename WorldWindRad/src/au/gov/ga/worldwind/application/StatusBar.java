package au.gov.ga.worldwind.application;

public class StatusBar extends gov.nasa.worldwind.util.StatusBar
{
	@Override
	protected String makeEyeAltitudeDescription(double metersAltitude)
	{
		String s;
		if (UNIT_IMPERIAL.equals(getElevationUnit()))
			return super.makeEyeAltitudeDescription(metersAltitude);
		else
		{
			if (metersAltitude < 1e4)
				s = String.format("Altitude %,7d meters", (int) Math
						.round(metersAltitude));
			else
				s = String.format("Altitude %,7d km", (int) Math
						.round(metersAltitude / 1e3));
		}
		return s;
	}
}
