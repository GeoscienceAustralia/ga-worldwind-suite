package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Path
{
	protected final NavigableMap<Double, LatLon> positions = new TreeMap<Double, LatLon>();
	protected Angle length;

	public Path(List<LatLon> positions)
	{
		setPositions(positions);
	}

	public synchronized void setPositions(List<LatLon> positions)
	{
		this.positions.clear();
		double[] distances = new double[positions.size()]; //last array value is unused, but required for simple second loop

		//calculate total distance
		double total = 0d; //in radians
		for (int i = 0; i < positions.size() - 1; i++)
		{
			Angle distance = LatLon.greatCircleDistance(positions.get(i), positions.get(i + 1));
			distances[i] = distance.radians;
			total += distance.radians;
		}
		this.length = Angle.fromRadians(total);

		//calculate percent positions
		double sum = 0d;
		for (int i = 0; i < positions.size(); i++)
		{
			this.positions.put(sum / total, positions.get(i));
			sum += distances[i];
		}
	}

	public synchronized LatLon getPercentLatLon(double percent)
	{
		if (percent <= 0)
			return positions.firstEntry().getValue();
		if (percent >= 1)
			return positions.lastEntry().getValue();
		if (positions.containsKey(percent))
			return positions.get(percent);

		Entry<Double, LatLon> lower = positions.lowerEntry(percent);
		Entry<Double, LatLon> higher = positions.higherEntry(percent);
		double p = (percent - lower.getKey()) / (higher.getKey() - lower.getKey());
		//TODO add different interpolation methods
		return LatLon.interpolateGreatCircle(p, lower.getValue(), higher.getValue());
	}

	public synchronized Vec4 getSegmentCenterPoint(DrawContext dc, Segment segment, double top,
			double bottom)
	{
		top *= dc.getVerticalExaggeration();
		bottom *= dc.getVerticalExaggeration();
		double height = top - bottom;
		double e = top - segment.getVerticalCenter() * height;
		LatLon ll = getPercentLatLon(segment.getHorizontalCenter());
		return dc.getGlobe().computePointFromPosition(ll, e);
	}

	public synchronized Vec4[] getPointsInSegment(DrawContext dc, Segment segment, double top,
			double bottom)
	{
		//TODO ?? cache value returned from this method, and if called twice with same input parameters, return cached value ??

		top *= dc.getVerticalExaggeration();
		bottom *= dc.getVerticalExaggeration();
		double height = top - bottom;
		double t = top - segment.getTop() * height;
		double b = top - segment.getBottom() * height;

		LatLon start = getPercentLatLon(segment.getStart());
		LatLon end = getPercentLatLon(segment.getEnd());

		//get a sublist of all the points between start and end
		List<LatLon> between =
				new ArrayList<LatLon>(positions.subMap(segment.getStart(), false, segment.getEnd(),
						false).values());

		Globe globe = dc.getGlobe();
		Vec4[] points = new Vec4[4 + between.size() * 2];

		//add top points
		int j = 0;
		points[j++] = globe.computePointFromPosition(start, t);
		for (int i = 0; i < between.size(); i++)
		{
			points[j++] = globe.computePointFromPosition(between.get(i), t);
		}
		points[j++] = globe.computePointFromPosition(end, t);

		//add bottom points
		points[j++] = globe.computePointFromPosition(end, b);
		for (int i = between.size() - 1; i >= 0; i--)
		{
			points[j++] = globe.computePointFromPosition(between.get(i), b);
		}
		points[j++] = globe.computePointFromPosition(start, b);

		return points;
	}

	public synchronized Extent getSegmentExtent(DrawContext dc, Segment segment, double top,
			double bottom)
	{
		Vec4[] points = getPointsInSegment(dc, segment, top, bottom);
		return Box.computeBoundingBox(Arrays.asList(points));
	}

	public synchronized Angle getSegmentLength(Segment segment)
	{
		return Angle.fromRadians(getSegmentLengthInRadians(segment));
	}

	public synchronized double getSegmentLengthInRadians(Segment segment)
	{
		return (segment.getEnd() - segment.getStart()) * length.radians;
	}

	/*public synchronized Angle getPercentLength(double percent)
	{
		return Angle.fromRadians(length.radians * percent);
	}

	public synchronized double getPercentLengthInRadians(double percent)
	{
		return length.radians * percent;
	}*/
}
