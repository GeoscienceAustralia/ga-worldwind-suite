package path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnimationPath implements Serializable
{
	private List<AnimationPoint> points = new ArrayList<AnimationPoint>();
	private List<AnimationSection> sections = new ArrayList<AnimationSection>();
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
		sections.clear();

		for (int i = 0; i < points.size() - 1; i++)
		{
			AnimationPoint p0 = points.get(i);
			AnimationPoint p1 = points.get(i + 1);
			
			AnimationSection section = new AnimationSection(p0, p1);
			sections.add(section);
			double d = section.getLength();
			length += d;
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
		for (b = 0; b < sections.size(); b++)
		{
			currentLength = sections.get(b).getLength();
			if (currentLength + cumulativeLength >= length)
			{
				break;
			}
			cumulativeLength += currentLength;
		}

		if (b < sections.size())
		{
			AnimationSection section = sections.get(b);

			percent = (length - cumulativeLength) / currentLength;
			percent = Math.min(1, Math.max(0, percent));

			return section.linearPointAt(percent);
		}

		return null;
	}
}
