package util;

public class Sector
{
	private final double minLatitude;
	private final double minLongitude;
	private final double maxLatitude;
	private final double maxLongitude;

	public Sector(double minLatitude, double minLongitude, double maxLatitude,
			double maxLongitude)
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
}
