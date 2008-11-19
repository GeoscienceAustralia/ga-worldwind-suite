package path.globe;

import gov.nasa.worldwind.geom.Angle;

import java.io.Serializable;

public class Position implements Serializable
{
	public Latitude latitude;
	public Longitude longitude;
	public double elevation;

	public Position(double degreesLatitude, double degreesLongitude,
			double elevation)
	{
		this(new Latitude(degreesLatitude), new Longitude(degreesLongitude),
				elevation);
	}

	public Position(Position position)
	{
		this(new Latitude(position.latitude),
				new Longitude(position.longitude), position.elevation);
	}

	public Position(Latitude latitude, Longitude longitude, double elevation)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
	}

	public gov.nasa.worldwind.geom.Position getPosition()
	{
		return new gov.nasa.worldwind.geom.Position(Angle
				.fromDegreesLatitude(latitude.degrees), Angle
				.fromDegreesLongitude(longitude.degrees), elevation);
	}
}
