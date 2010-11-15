package au.gov.ga.worldwind.common.layers.curtain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.sun.opengl.util.BufferUtil;

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
			double bottom, boolean followTerrain)
	{
		top *= dc.getVerticalExaggeration();
		bottom *= dc.getVerticalExaggeration();
		double height = top - bottom;
		double e = top - segment.getVerticalCenter() * height;
		LatLon ll = getPercentLatLon(segment.getHorizontalCenter());

		if (followTerrain)
		{
			e +=
					dc.getGlobe().getElevation(ll.latitude, ll.longitude)
							* dc.getVerticalExaggeration();
		}

		return dc.getGlobe().computePointFromPosition(ll, e);
	}

	public synchronized SegmentGeometry getGeometry(DrawContext dc, Segment segment, double top,
			double bottom, int subsegments, boolean followTerrain)
	{
		NavigableMap<Double, LatLon> betweenMap = segmentMap(segment, subsegments);
		int numVertices = betweenMap.size() * 2;

		Globe globe = dc.getGlobe();

		DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);
		DoubleBuffer texCoords = BufferUtil.newDoubleBuffer(numVertices * 2);
		Vec4 refCenter = getSegmentCenterPoint(dc, segment, top, bottom, followTerrain);

		//calculate exaggerated segment top/bottom elevations
		top *= dc.getVerticalExaggeration();
		bottom *= dc.getVerticalExaggeration();
		double height = top - bottom;
		double t = top - segment.getTop() * height;
		double b = top - segment.getBottom() * height;

		double percentDistance = segment.getHorizontalDelta();
		for (Entry<Double, LatLon> entry : betweenMap.entrySet())
		{
			LatLon ll = entry.getValue();

			double e = 0;
			if (followTerrain)
			{
				e = globe.getElevation(ll.latitude, ll.longitude) * dc.getVerticalExaggeration();
			}

			Vec4 point1 = globe.computePointFromPosition(ll, t + e);
			Vec4 point2 = globe.computePointFromPosition(ll, b + e);
			double percent = (entry.getKey() - segment.getStart()) / percentDistance;

			verts.put(point1.x - refCenter.x).put(point1.y - refCenter.y)
					.put(point1.z - refCenter.z);
			verts.put(point2.x - refCenter.x).put(point2.y - refCenter.y)
					.put(point2.z - refCenter.z);
			texCoords.put(percent).put(1);
			texCoords.put(percent).put(0);
		}

		return new SegmentGeometry(verts, texCoords, refCenter);
	}

	public synchronized Vec4[] getPointsInSegment(DrawContext dc, Segment segment, double top,
			double bottom, int subsegments, boolean followTerrain)
	{
		//TODO ?? cache value returned from this method, and if called twice with same input parameters, return cached value ??
		//TODO create a new function to return some object with a vertex buffer and texture buffer instead of just a Vec4[] array

		NavigableMap<Double, LatLon> betweenMap = segmentMap(segment, subsegments);

		Globe globe = dc.getGlobe();
		Vec4[] points = new Vec4[betweenMap.size() * 2];

		//calculate exaggerated segment top/bottom elevations
		top *= dc.getVerticalExaggeration();
		bottom *= dc.getVerticalExaggeration();
		double height = top - bottom;
		double t = top - segment.getTop() * height;
		double b = top - segment.getBottom() * height;

		//add top points, and add bottom points (add them backwards, so it's a loop)
		int j = 0, k = betweenMap.size() * 2;
		for (LatLon ll : betweenMap.values())
		{
			double e = 0;
			if (followTerrain)
			{
				e = globe.getElevation(ll.latitude, ll.longitude) * dc.getVerticalExaggeration();
			}

			points[j++] = globe.computePointFromPosition(ll, t + e);
			points[--k] = globe.computePointFromPosition(ll, b + e);
		}

		return points;
	}

	protected NavigableMap<Double, LatLon> segmentMap(Segment segment, int subsegments)
	{
		LatLon start = getPercentLatLon(segment.getStart());
		LatLon end = getPercentLatLon(segment.getEnd());

		NavigableMap<Double, LatLon> betweenMap = new TreeMap<Double, LatLon>();
		//get a sublist of all the points between start and end (non-inclusive)
		betweenMap.putAll(positions.subMap(segment.getStart(), false, segment.getEnd(), false));
		//add the start and end points
		betweenMap.put(segment.getStart(), start);
		betweenMap.put(segment.getEnd(), end);

		//insert any subsegment points
		for (int i = 0; i < subsegments - 1; i++)
		{
			double subsegment = (i + 1) / (double) subsegments;
			double percent = segment.getStart() + subsegment * segment.getHorizontalDelta();
			LatLon pos = getPercentLatLon(percent);
			betweenMap.put(percent, pos);
		}

		return betweenMap;
	}

	public synchronized Extent getSegmentExtent(DrawContext dc, Segment segment, double top,
			double bottom, int subsegments, boolean followTerrain)
	{
		Vec4[] points = getPointsInSegment(dc, segment, top, bottom, subsegments, followTerrain);
		return Box.computeBoundingBox(Arrays.asList(points));
	}

	public synchronized Angle getSegmentLength(Segment segment)
	{
		return Angle.fromRadians(getSegmentLengthInRadians(segment));
	}

	public synchronized double getSegmentLengthInRadians(Segment segment)
	{
		return segment.getHorizontalDelta() * length.radians;
	}

	public synchronized Angle getPercentLength(double percent)
	{
		return Angle.fromRadians(getPercentLengthInRadians(percent));
	}

	public synchronized double getPercentLengthInRadians(double percent)
	{
		return length.radians * percent;
	}
}
