package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class OldPolygonTessellator
{
	public static List<FastShape> tessellateShapefile(Shapefile shapefile, Sector sector, int subdivisions)
	{
		GLU glu = new GLU();
		GLUtessellator tess = glu.gluNewTess();
		TessellatorCallback callback = new TessellatorCallback(glu, sector, subdivisions);

		glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
		//glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);

		glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_END, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, callback);

		glu.gluTessBeginPolygon(tess, null);
		List<ShapefileRecord> records = shapefile.getRecords();
		for (ShapefileRecord record : records)
		{
			for (int part = 0; part < record.getNumberOfParts(); part++)
			{
				glu.gluTessBeginContour(tess);

				//go through shapefile vertices backwards, as GLU tessellator expects polygons
				//in counter-clockwise order, but shapefiles specify vertices clockwise
				VecBuffer buffer = record.getBuffer(part);
				int size = buffer.getSize();
				for (int i = size - 1; i >= 0; i--)
				{
					Position position = buffer.getPosition(i);
					double[] v =
							new double[] { position.longitude.degrees, position.latitude.degrees,
									position.elevation };
					glu.gluTessVertex(tess, v, 0, v);
				}

				glu.gluTessEndContour(tess);
			}
		}
		glu.gluTessEndPolygon(tess);
		glu.gluDeleteTess(tess);

		return callback.shapes;
	}

	private static class TessellatorCallback extends GLUtessellatorCallbackAdapter
	{
		private GLU glu;
		private Sector sector;
		private double deltaLat, deltaLon;

		private List<Position> currentPositions;
		private int currentType;
		private Position[] currentTriangle = new Position[3];
		private int currentTriangleIndex = 0;

		private List<FastShape> shapes = new ArrayList<FastShape>();

		public TessellatorCallback(GLU glu, Sector sector, int subdivisions)
		{
			this.glu = glu;
			this.sector = sector;

			deltaLat = sector.getDeltaLatDegrees() / (subdivisions + 1);
			deltaLon = sector.getDeltaLonDegrees() / (subdivisions + 1);
		}

		@Override
		public void begin(int type)
		{
			currentType = type;
			currentPositions = new ArrayList<Position>();
		}

		@Override
		public void end()
		{
			FastShape shape = new FastShape(currentPositions, currentType);
			shapes.add(shape);
		}

		@Override
		public void vertex(Object o)
		{
			if (o instanceof double[])
			{
				double[] v = (double[]) o;
				currentTriangle[currentTriangleIndex++] = Position.fromDegrees(v[1], v[0], v[2]);
				if (currentTriangleIndex >= 3)
				{
					addTriangle(currentTriangle);
					currentTriangleIndex = 0;
				}
			}
		}

		private void addTriangle(Position[] triangle)
		{
			Position p0 = triangle[0];
			Position p1 = triangle[1];
			Position p2 = triangle[2];

			//if something is wrong with the sector
			if (deltaLat <= 0 || deltaLon <= 0)
			{
				currentPositions.add(p0);
				currentPositions.add(p1);
				currentPositions.add(p2);
				return;
			}

			List<Position> points = new ArrayList<Position>();

			points.add(p0);
			int count01 = addPointsAlongLine(p0, p1, points, sector, deltaLat, deltaLon) + 1;
			points.add(p1);
			int count12 = addPointsAlongLine(p1, p2, points, sector, deltaLat, deltaLon) + 1;
			points.add(p2);
			int count20 = addPointsAlongLine(p2, p0, points, sector, deltaLat, deltaLon) + 1;

			if (points.size() <= 3)
			{
				//no points were added, add triangle as is
				currentPositions.addAll(points);
			}
			else
			{
				int maxCount = Math.max(count01, Math.max(count12, count20));
				List<Position> triangles;
				if (count01 == maxCount)
				{
					triangles = createTriangles(points, count01, count12, count20, 0);
				}
				else if (count12 == maxCount)
				{
					triangles = createTriangles(points, count12, count20, count01, count01);
				}
				else
				{
					triangles =
							createTriangles(points, count20, count01, count12, count01 + count12);
				}

				for (int i = 0; i < triangles.size(); i += 3)
				{
					triangle[0] = triangles.get(i);
					triangle[1] = triangles.get(i + 1);
					triangle[2] = triangles.get(i + 2);

					//recurse!!
					addTriangle(triangle);
				}
			}
		}

		private static int addPointsAlongLine(Position p0, Position p1, List<Position> list,
				Sector sector, double deltaLat, double deltaLon)
		{
			double minlat = Math.min(p0.getLatitude().degrees, p1.getLatitude().degrees);
			double minlon = Math.min(p0.getLongitude().degrees, p1.getLongitude().degrees);
			double maxlat = Math.max(p0.getLatitude().degrees, p1.getLatitude().degrees) - 1e-5;
			double maxlon = Math.max(p0.getLongitude().degrees, p1.getLongitude().degrees) - 1e-5;

			int minLatDeltas = (int) ((minlat - sector.getMinLatitude().degrees) / deltaLat) + 1;
			int minLonDeltas = (int) ((minlon - sector.getMinLongitude().degrees) / deltaLon) + 1;
			int maxLatDeltas = (int) ((maxlat - sector.getMinLatitude().degrees) / deltaLat) - 1;
			int maxLonDeltas = (int) ((maxlon - sector.getMinLongitude().degrees) / deltaLon) - 1;

			int lats = Math.max(0, maxLatDeltas - minLatDeltas + 2);
			int lons = Math.max(0, maxLonDeltas - minLonDeltas + 2);
			int count = lats + lons;

			if (count > 0)
			{
				double[] percents = new double[count];
				double startLat = sector.getMinLatitude().degrees + deltaLat * minLatDeltas;
				double startLon = sector.getMinLongitude().degrees + deltaLon * minLonDeltas;
				boolean p0minlat = p0.getLatitude().degrees == minlat;
				boolean p0minlon = p0.getLongitude().degrees == minlon;

				int i = 0;
				for (int lat = 0; lat < lats; lat++)
				{
					double l = startLat + lat * deltaLat;
					double percent = (l - minlat) / (maxlat - minlat);
					percents[i++] = p0minlat ? percent : 1d - percent;
				}
				for (int lon = 0; lon < lons; lon++)
				{
					double l = startLon + lon * deltaLon;
					double percent = (l - minlon) / (maxlon - minlon);
					percents[i++] = p0minlon ? percent : 1d - percent;
				}

				Arrays.sort(percents);

				for (double percent : percents)
				{
					//Position p = Position.interpolate(percent, p0, p1);
					Position p =
							Position.fromDegrees(p0.getLatitude().degrees * (1d - percent)
									+ p1.getLatitude().degrees * percent, p0.getLongitude().degrees
									* (1d - percent) + p1.getLongitude().degrees * percent, p0
									.getElevation()
									* (1d - percent) + p1.getElevation() * percent);
					list.add(p);
				}
			}

			return count;
		}

		private static List<Position> createTriangles(List<Position> points, int countA,
				int countB, int countC, int startA)
		{
			int total = points.size();
			if (total != countA + countB + countC)
				throw new IllegalArgumentException("triangle side point counts are incorrect");

			List<Position> triangles = new ArrayList<Position>();

			int unusedA = countA + 1;
			int unusedOther = countB + countC - 1;

			int offset = 0;
			while (unusedA > 2 && unusedOther > 0)
			{
				int p0 = startA + total - offset;
				int p1 = startA + offset + 1;
				int p2 = p1 + 1;
				int p3 = p0 - 1;

				triangles.add(points.get(p0 % total));
				triangles.add(points.get(p1 % total));
				triangles.add(points.get(p3 % total));

				triangles.add(points.get(p1 % total));
				triangles.add(points.get(p2 % total));
				triangles.add(points.get(p3 % total));

				unusedA--;
				unusedOther--;
				offset++;
			}

			if (countC >= countA)
			{
				int p0 = startA + countA;
				int p1 = p0 + countB;
				int p2 = p1 + 1;

				triangles.add(points.get(p0 % total));
				triangles.add(points.get(p1 % total));
				triangles.add(points.get(p2 % total));
			}
			else if (countA > countB + countC)
			{
				int p0 = startA + countA - 1;
				int p1 = p0 + 1;
				int p2 = p1 + 1;

				triangles.add(points.get(p0 % total));
				triangles.add(points.get(p1 % total));
				triangles.add(points.get(p2 % total));
			}

			return triangles;
		}

		@Override
		public void error(int error)
		{
			String message = "GLU error: " + glu.gluErrorString(error) + " (" + error + ")";
			Logging.logger().severe(message);
		}
	}
}
