package path;

public class Point
{
	public Position position;
	public double heading;
	public double pitch;

	public Point(Point point)
	{
		this(new Position(point.position), point.heading, point.pitch);
	}

	public Point(Position position, double heading, double pitch)
	{
		this.position = position;
		this.heading = heading;
		this.pitch = pitch;
	}
}
