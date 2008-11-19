package path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import path.vector.Vector2;
import path.vector.Vector3;

public class AnimationPath implements Serializable
{
	//1. moves between consecutive points at a constant speed
	//2. calculate bezier distance between consecutive points
	//3. change heading

	public double positionAcceleration = 10;

	private List<AnimationPoint> points = new ArrayList<AnimationPoint>();
	private List<Bezier<Vector3>> beziers = new ArrayList<Bezier<Vector3>>();
	private double length = 0;

	private boolean dirty = false;

	public void addPoint(AnimationPoint point)
	{
		points.add(point);
		dirty = true;
	}

	public AnimationPoint getPoint(int index)
	{
		return points.get(index);
	}

	public int getPointCount()
	{
		return points.size();
	}

	public void removePoint(int index)
	{
		points.remove(index);
		dirty = true;
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	private void refreshIfDirty()
	{
		if (isDirty())
		{
			refresh();
		}
	}

	private void refresh()
	{
		length = 0;
		beziers.clear();

		for (int i = 1; i < points.size(); i++)
		{
			AnimationPoint p0 = points.get(i - 1);
			AnimationPoint p1 = points.get(i);
			Bezier<Vector3> bezier = new Bezier<Vector3>(p0.position,
					p0.position.add(p0.out), p1.position.add(p1.in),
					p1.position);
			beziers.add(bezier);
			length += bezier.getLength();
		}

		dirty = false;
	}

	public Point getPositionAt(double percent)
	{
		refreshIfDirty();
		percent = Math.min(1, Math.max(0, percent));

		double length = percent * this.length;
		double cumulativeLength = 0;
		double currentLength = 0;

		int b;
		for (b = 0; b < beziers.size(); b++)
		{
			currentLength = beziers.get(b).getLength();
			if (currentLength + cumulativeLength >= length)
			{
				break;
			}
			cumulativeLength += currentLength;
		}

		if (b < beziers.size())
		{
			AnimationPoint p0 = points.get(b);
			AnimationPoint p1 = points.get(b + 1);
			Bezier<Vector3> bezier = beziers.get(b);

			percent = (length - cumulativeLength) / currentLength;
			percent = Math.min(1, Math.max(0, percent));

			Vector3 position = bezier.linearPointAt(percent);
			Vector2 orientation = p0.orientation.interpolate(p1.orientation,
					percent);
			
			return new Point(position, orientation);
		}

		return null;
	}
}
