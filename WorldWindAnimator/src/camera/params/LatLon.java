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
		return new gov.nasa.worldwind.geom.LatLon(gov.nasa.worldwind.geom.Angle
				.fromDegreesLatitude(latitude.degrees),
				gov.nasa.worldwind.geom.Angle
						.fromDegreesLongitude(longitude.degrees));
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
}
