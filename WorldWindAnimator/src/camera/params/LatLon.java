package camera.params;

import java.io.Serializable;

import camera.vector.Vector2;


public class LatLon implements Serializable
{
	public Latitude latitude;
	public Longitude longitude;

	public LatLon(LatLon latlon)
	{
		this(new Latitude(latlon.latitude), new Longitude(latlon.longitude));
	}

	public LatLon(Latitude latitude, Longitude longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public gov.nasa.worldwind.geom.LatLon getLatLon()
	{
		return new gov.nasa.worldwind.geom.LatLon(latitude.getAngle(),
				longitude.getAngle());
	}

	public double distance(LatLon ll)
	{
		double latdiff = Latitude.difference(latitude, ll.latitude);
		double londiff = Longitude.difference(longitude, ll.longitude);
		return Math.sqrt(latdiff * latdiff + londiff * londiff);
	}

	public double angleBetween(LatLon ll)
	{
		double latdiff = Latitude.difference(latitude, ll.latitude);
		double londiff = Longitude.difference(longitude, ll.longitude);
		return Math.atan2(-latdiff, londiff) * 180d / Math.PI;
	}

	public static LatLon fromDegrees(double degreesLatitude,
			double degreesLongitude)
	{
		return new LatLon(Latitude.fromDegrees(degreesLatitude), Longitude
				.fromDegrees(degreesLongitude));
	}

	public static LatLon fromVector2(Vector2 v)
	{
		return fromDegrees(v.y, v.x);
	}

	public static LatLon fromLatLon(gov.nasa.worldwind.geom.LatLon latlon)
	{
		return fromDegrees(latlon.getLatitude().degrees,
				latlon.getLongitude().degrees);
	}

	public Vector2 toVector2()
	{
		return new Vector2(longitude.degrees, latitude.degrees);
	}

	public Vector2 toVector2(LatLon reference)
	{
		double longDelta = Longitude.difference(reference.longitude, longitude);
		double latDelta = Latitude.difference(reference.latitude, latitude);
		return new Vector2(longDelta, latDelta).addLocal(reference.toVector2());
	}

	public Vector2 toVector2(Vector2 reference)
	{
		double longDelta = Longitude.difference(Longitude
				.fromDegrees(reference.x), longitude);
		double latDelta = Latitude.difference(
				Latitude.fromDegrees(reference.y), latitude);
		return new Vector2(longDelta, latDelta).addLocal(reference);
	}

	public static LatLon interpolate(LatLon l1, LatLon l2, double percent)
	{
		double latdiff = Latitude.difference(l1.latitude, l2.latitude);
		double londiff = Longitude.difference(l1.longitude, l2.longitude);
		return LatLon.fromDegrees(l1.latitude.degrees + latdiff * percent,
				l1.longitude.degrees + londiff * percent);
	}
}
