package au.gov.ga.worldwind.layers.shapefile;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class PolygonTessellator
{
	public static List<FastShape> tessellateShapefile(Shapefile shapefile)
	{
		GLU glu = new GLU();
		GLUtessellator tess = glu.gluNewTess();
		TessellatorCallback callback = new TessellatorCallback(glu);

		glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
		//glu.gluTessProperty(tessellator, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);

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
		private List<Position> currentPositions;
		private int currentType;
		private List<FastShape> shapes = new ArrayList<FastShape>();

		public TessellatorCallback(GLU glu)
		{
			this.glu = glu;
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
				currentPositions.add(Position.fromDegrees(v[1], v[0], v[2]));
			}
		}

		@Override
		public void error(int error)
		{
			String message = "GLU error: " + glu.gluErrorString(error) + " (" + error + ")";
			Logging.logger().severe(message);
		}
	}
}
