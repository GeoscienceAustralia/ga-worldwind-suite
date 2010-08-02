package au.gov.ga.worldwind.layers.shapefile.textured;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class ImageTessellator
{
	public static BufferedImage tessellate(Shapefile shapefile, int width, int height, Sector sector)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

		Graphics2D g = null;
		try
		{
			g = image.createGraphics();
			g.setColor(Color.white);

			String shapeType = shapefile.getShapeType();
			if (Shapefile.isPolygonType(shapeType))
			{
				drawPolygons(g, shapefile, width, height, sector);
			}
			else if (Shapefile.isPolylineType(shapeType))
			{
				drawLines(g, shapefile, width, height, sector);
			}
			else
			{
				drawPoints(g, shapefile, width, height, sector);
			}
		}
		finally
		{
			if (g != null)
				g.dispose();
		}

		return image;

		/*BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

		g = null;
		try
		{
			g = image2.createGraphics();
			g.setColor(new Color(255, 255, 0, 128));
			g.fillRect(0, 0, width, height);

			g.setComposite(AlphaComposite.DstIn);
			g.drawImage(image, 0, 0, null);
		}
		finally
		{
			if (g != null)
				g.dispose();
		}

		return image2;*/
	}

	protected static void drawLines(Graphics2D g, Shapefile shapefile, int width, int height,
			Sector sector)
	{

	}

	protected static void drawPoints(Graphics2D g, Shapefile shapefile, int width, int height,
			Sector sector)
	{
		// TODO Auto-generated method stub	
	}

	protected static void drawPolygons(Graphics2D g, Shapefile shapefile, int width, int height,
			Sector sector)
	{
		GLU glu = new GLU();
		GLUtessellator tess = glu.gluNewTess();
		TessellatorCallback callback = new TessellatorCallback(glu);

		glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
		//glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);

		glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_END, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, callback);
		glu.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG, callback);
		glu.gluTessNormal(tess, 0.0, 0.0, 1.0);

		double[] vert = new double[3];
		while (shapefile.hasNext())
		{
			glu.gluTessBeginPolygon(tess, null);

			ShapefileRecord record = shapefile.nextRecord();
			for (int i = 0; i < record.getNumberOfParts(); i++)
			{
				glu.gluTessBeginContour(tess);

				VecBuffer buffer = record.getPointBuffer(i);
				int size = buffer.getSize();
				for (int j = 0; j < size; j++)
				{
					LatLon latlon = buffer.getLocation(j);
					vert[0] = latlon.longitude.degrees;
					vert[1] = latlon.latitude.degrees;
					vert[2] = 0;

					glu.gluTessVertex(tess, vert, 0, latlon);
				}

				glu.gluTessEndContour(tess);
			}

			glu.gluTessEndPolygon(tess);
		}

		glu.gluDeleteTess(tess);

		List<LatLon> triangles = callback.triangles;
		for (int i = 0; i < triangles.size(); i += 3)
		{
			LatLon ll0 = triangles.get(i);
			LatLon ll1 = triangles.get(i + 1);
			LatLon ll2 = triangles.get(i + 2);

			Path2D.Double shape = new Path2D.Double(Path2D.WIND_NON_ZERO, 4);
			shape.moveTo(scaledX(ll0, width, sector), scaledY(ll0, height, sector));
			shape.lineTo(scaledX(ll1, width, sector), scaledY(ll1, height, sector));
			shape.lineTo(scaledX(ll2, width, sector), scaledY(ll2, height, sector));
			shape.closePath();

			g.fill(shape);
		}
	}

	protected static void drawPolygons2(Graphics2D g, Shapefile shapefile, int width, int height,
			Sector sector)
	{
		while (shapefile.hasNext())
		{
			Path2D.Double shape = new Path2D.Double(Path2D.WIND_EVEN_ODD);
			ShapefileRecord record = shapefile.nextRecord();
			for (int i = 0; i < record.getNumberOfParts(); i++)
			{
				VecBuffer buffer = record.getPointBuffer(i);
				int size = buffer.getSize();
				for (int j = 0; j < size; j++)
				{
					LatLon latlon = buffer.getLocation(j);
					if (j == 0)
						shape.moveTo(latlon.longitude.degrees, latlon.latitude.degrees);
					else
						shape.lineTo(latlon.longitude.degrees, latlon.latitude.degrees);
				}
				if(size > 0)
					shape.closePath();
			}
			
			
		}
	}

	protected static double scaledX(LatLon ll, int width, Sector sector)
	{
		double fract =
				(ll.getLongitude().degrees - sector.getMinLongitude().degrees)
						/ sector.getDeltaLonDegrees();
		return fract * width;
	}

	protected static double scaledY(LatLon ll, int height, Sector sector)
	{
		double fract =
				(ll.getLatitude().degrees - sector.getMinLatitude().degrees)
						/ sector.getDeltaLatDegrees();
		return (1 - fract) * height;
	}

	protected static class TessellatorCallback extends GLUtessellatorCallbackAdapter
	{
		private final GLU glu;
		public final List<LatLon> triangles = new ArrayList<LatLon>();

		public TessellatorCallback(GLU glu)
		{
			this.glu = glu;
		}

		@Override
		public void vertex(Object o)
		{
			if (o instanceof LatLon)
				triangles.add((LatLon) o);
		}

		@Override
		public void error(int error)
		{
			String message = "GLU error: " + glu.gluErrorString(error) + " (" + error + ")";
			Logging.logger().severe(message);
		}
	}
}
