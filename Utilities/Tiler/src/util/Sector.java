package util;

public class Sector
{
	public static final Sector FULL_SPHERE = new Sector(-90, -180, 90, 180);

	private final double minLatitude;
	private final double minLongitude;
	private final double maxLatitude;
	private final double maxLongitude;

	public Sector(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude)
	{
		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.maxLatitude = maxLatitude;
		this.maxLongitude = maxLongitude;
	}

	public double getMinLatitude()
	{
		return minLatitude;
	}

	public double getMinLongitude()
	{
		return minLongitude;
	}

	public double getMaxLatitude()
	{
		return maxLatitude;
	}

	public double getMaxLongitude()
	{
		return maxLongitude;
	}

	public double getDeltaLatitude()
	{
		return maxLatitude - minLatitude;
	}

	public double getDeltaLongitude()
	{
		return maxLongitude - minLongitude;
	}

	public double getCenterLatitude()
	{
		return minLatitude + getDeltaLatitude() / 2d;
	}

	public double getCenterLongitude()
	{
		return minLongitude + getDeltaLongitude() / 2d;
	}

	public boolean containsPoint(double latitude, double longitude)
	{
		return getMinLatitude() <= latitude && latitude <= getMaxLatitude()
				&& getMinLongitude() <= longitude && longitude <= getMaxLongitude();
	}

	@Override
	public String toString()
	{
		return "(" + minLatitude + "," + minLongitude + "," + maxLatitude + "," + maxLongitude
				+ ")";
	}
}
