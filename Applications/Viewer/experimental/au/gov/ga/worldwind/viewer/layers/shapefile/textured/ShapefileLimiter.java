package au.gov.ga.worldwind.viewer.layers.shapefile.textured;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.viewer.layers.shapefile.indextessellator.IndexedLatLon;
import au.gov.ga.worldwind.viewer.layers.shapefile.indextessellator.SubTile;
import au.gov.ga.worldwind.viewer.util.SettingsUtil;

public class ShapefileLimiter
{
	public static List<Shape> limit(Shapefile shapefile, Sector sector)
	{
		List<Shape> shapes = new ArrayList<Shape>();
		boolean polygon = Shapefile.isPolygonType(shapefile.getShapeType());

		Point zeroPoint = new Point(0, 0);

		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();

			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				VecBuffer buffer = record.getPointBuffer(part);
				int size = buffer.getSize();
				int last = polygon ? size + 1 : size; //if polygon, sample first point twice

				LatLon lastLatlon = null;
				Point lastPoint = null;
				List<Point> affectedPoints = new ArrayList<Point>();

				for (int i = 0; i < last; i++)
				{
					LatLon latlon = buffer.getLocation(i % size);

					Point point = zeroPoint;
					if (!sector.contains(latlon))
					{
						int x =
								(int) Math.floor((latlon.getLongitude().degrees - sector
										.getMinLongitude().degrees)
										/ sector.getDeltaLonDegrees());
						int y =
								(int) Math.floor((latlon.getLatitude().degrees - sector
										.getMinLatitude().degrees)
										/ sector.getDeltaLatDegrees());
						point = new Point(x, y);
					}

					if (!point.equals(lastPoint))
					{
						if (lastPoint != null)
						{
							//get the fractional position of the points within their corresponding tile's
							//sector, in the range -0.5 to 0.5
							float x1 =
									(float) (longitudeFract(lastLatlon, lastPoint, sector) - 0.5);
							float y1 = (float) (latitudeFract(lastLatlon, lastPoint, sector) - 0.5);
							float x2 = (float) (longitudeFract(latlon, point, sector) - 0.5);
							float y2 = (float) (latitudeFract(latlon, point, sector) - 0.5);

							List<Point> line =
									linePoints(lastPoint.x + x1, lastPoint.y + y1, point.x + x2,
											point.y + y2);

							Point lastCrossPoint = lastPoint;
							for (int j = 1; j < line.size() - 1; j++)
							{
								Point crossPoint = line.get(j);

								//ignore first and last (and repeats?)
								if (lastPoint.equals(crossPoint) || point.equals(crossPoint)
										|| lastCrossPoint.equals(crossPoint))
									continue;

								LatLon edge = edgePoint(lastLatlon, latlon, crossPoint, sector);
								if (edge != null)
								{
									if (lastCrossPoint.x != crossPoint.x
											&& lastCrossPoint.y != crossPoint.y)
									{
										String message = "Crossed tiles are not adjacent";
										Logging.logger().warning(message);
									}

									if (lastCrossPoint.equals(zeroPoint))
									{
										//TODO ADD edge AS EXIT
									}
									else if (crossPoint.equals(zeroPoint))
									{
										//TODO ADD edge AS ENTRY
									}

									affectedPoints.add(crossPoint);
									lastCrossPoint = crossPoint;
								}
							}

							LatLon edge = edgePoint(lastLatlon, latlon, point, sector);
							if (edge == null)
							{
								String message = "Did not find an edge point in the sector";
								Logging.logger().warning(message);

								//edge = latlon; TODO ???
							}

							if (edge != null)
							{
								if (lastCrossPoint.equals(zeroPoint))
								{
									//TODO ADD edge AS EXIT
								}
								else if (point.equals(zeroPoint))
								{
									//TODO ADD edge AS ENTRY
								}
							}
						}

						affectedPoints.add(point);
					}

					if (point.equals(zeroPoint))
					{
						//TODO ADD latlon AS POINT
					}

					lastLatlon = latlon;
					lastPoint = point;
				}
			}
		}

		return shapes;
	}

	protected static double latitudeFract(LatLon latlon, Point point, Sector sector)
	{
		return point.y + (latlon.latitude.degrees - sector.getMinLatitude().degrees)
				/ sector.getDeltaLatDegrees();
	}

	protected static double longitudeFract(LatLon latlon, Point point, Sector sector)
	{
		return point.x + (latlon.longitude.degrees - sector.getMinLongitude().degrees)
				/ sector.getDeltaLonDegrees();
	}

	protected static List<Point> linePoints(float x1, float y1, float x2, float y2)
	{
		List<Point> points = new ArrayList<Point>();

		boolean steep = Math.abs(x2 - x1) < Math.abs(y2 - y1);
		if (steep)
		{
			//swap x/y variables
			float temp = x1;
			x1 = y1;
			y1 = temp;
			temp = x2;
			x2 = y2;
			y2 = temp;
		}
		boolean swap = x1 > x2;
		if (swap)
		{
			//swap p1/p2 points
			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		int y1i = Math.round(y1);
		int y2i = Math.round(y2);
		if (y1i == y2i)
		{
			//handle special horizontal/vertical line case

			int x1i = Math.round(x1);
			int x2i = Math.round(x2);
			for (int x = x1i; x <= x2i; x++)
			{
				points.add(steep ? new Point(y1i, x) : new Point(x, y1i));
			}
		}
		else
		{
			float dx = x2 - x1;
			float dy = y2 - y1;
			float gradient = dy / dx;
			float intery = y1;
			int startX = Math.round(x1);
			int endX = Math.round(x2);
			float centerDiff = (startX - x1) * gradient;

			for (int x = startX; x <= endX; x++)
			{
				int y = Math.round(intery);
				int add = (intery + centerDiff - y < 0) ? -1 : 1;
				boolean first = add > 0 ^ gradient < 0;

				Point p1 = steep ? new Point(y, x) : new Point(x, y);
				Point p2 = steep ? new Point(y + add, x) : new Point(x, y + add);

				if (first)
				{
					points.add(p1);
					points.add(p2);
				}
				else
				{
					points.add(p2);
					points.add(p1);
				}

				intery += gradient;
			}
		}

		//if points were swapped, reverse the list
		if (swap)
			Collections.reverse(points);

		return points;
	}

	protected static LatLon edgePoint(LatLon p1, LatLon p2, Point crossPoint, Sector sector)
	{
		double deltaLat = sector.getDeltaLatDegrees();
		double deltaLon = sector.getDeltaLonDegrees();
		double minLat = sector.getMinLatitude().degrees + crossPoint.y * deltaLat;
		double minLon = sector.getMinLongitude().degrees + crossPoint.x * deltaLon;
		double maxLat = minLat + deltaLat;
		double maxLon = minLon + deltaLon;

		LatLon minmin = LatLon.fromDegrees(minLat, minLon);
		LatLon minmax = LatLon.fromDegrees(minLat, maxLon);
		LatLon maxmax = LatLon.fromDegrees(maxLat, maxLon);
		LatLon maxmin = LatLon.fromDegrees(maxLat, minLon);

		LatLon minXintersect = lineSegmentIntersection(p1, p2, minmin, maxmin);
		LatLon minYintersect = lineSegmentIntersection(p1, p2, minmin, minmax);
		LatLon maxXintersect = lineSegmentIntersection(p1, p2, minmax, maxmax);
		LatLon maxYintersect = lineSegmentIntersection(p1, p2, maxmin, maxmax);
		LatLon[] intersects =
				new LatLon[] { minXintersect, minYintersect, maxXintersect, maxYintersect };
		double minDistance = Double.MAX_VALUE;
		LatLon minDistanceIntersect = null;

		for (LatLon intersect : intersects)
		{
			if (intersect != null)
			{
				double distance = distanceSquared(p1, intersect);
				if (distance < minDistance)
				{
					minDistance = distance;
					minDistanceIntersect = intersect;
				}
			}
		}

		return minDistanceIntersect;
	}

	protected static LatLon lineSegmentIntersection(LatLon p1, LatLon p2, LatLon p3, LatLon p4)
	{
		double p1x = p1.longitude.degrees;
		double p1y = p1.latitude.degrees;
		double p2x = p2.longitude.degrees;
		double p2y = p2.latitude.degrees;
		double p3x = p3.longitude.degrees;
		double p3y = p3.latitude.degrees;
		double p4x = p4.longitude.degrees;
		double p4y = p4.latitude.degrees;

		double ud = (p4y - p3y) * (p2x - p1x) - (p4x - p3x) * (p2y - p1y);

		//lines are parallel (don't care about coincident lines)
		if (ud == 0)
			return null;

		double uan = (p4x - p3x) * (p1y - p3y) - (p4y - p3y) * (p1x - p3x);
		double ubn = (p2x - p1x) * (p1y - p3y) - (p2y - p1y) * (p1x - p3x);
		double ua = uan / ud;
		double ub = ubn / ud;

		//intersection is not within the line segments
		if (ua < 0 || 1 < ua || ub < 0 || 1 < ub)
			return null;

		double x = p1x + ua * (p2x - p1x);
		double y = p1y + ua * (p2y - p1y);
		return LatLon.fromDegrees(y, x);
	}

	protected static double distanceSquared(LatLon p1, LatLon p2)
	{
		double delta;
		double result = 0d;
		delta = p1.latitude.degrees - p2.latitude.degrees;
		result += delta * delta;
		delta = p1.longitude.degrees - p2.longitude.degrees;
		result += delta * delta;
		return result;
	}
}
