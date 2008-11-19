package path;

import path.vector.Vector2;
import path.vector.Vector3;

public class Point
{
	public Vector3 position;
	public Vector2 orientation;

	public Point(Point point)
	{
		this(new Vector3(point.position), new Vector2(point.orientation));
	}

	public Point(Vector3 position, Vector2 orientation)
	{
		this.position = position;
		this.orientation = orientation;
	}
}
