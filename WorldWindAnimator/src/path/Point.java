package path;

public class Point
{
	public Vector3 position;
	public Vector3 lookAt;

	public Point(Point point)
	{
		this(new Vector3(point.position), new Vector3(point.lookAt));
	}

	public Point(Vector3 position, Vector3 lookAt)
	{
		this.position = position;
		this.lookAt = lookAt;
	}
}
