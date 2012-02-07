package au.gov.ga.worldwind.tiler.util;

public class LatLon
{
	public final static LatLon DEFAULT_ORIGIN = new LatLon(-90, -180);

	private final double latitude;
	private final double longitude;

	public LatLon(double latitude, double longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	@Override
	public String toString()
	{
		return "(" + latitude + "," + longitude + ")";
	}
}
