package au.gov.ga.worldwind.viewer.layers.shapefile.tessellator;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import au.gov.ga.worldwind.viewer.layers.shapefile.FastShape;

public class TessellatorCallback extends GLUtessellatorCallbackAdapter
{
	private GLU glu;
	private List<Position> positions = new ArrayList<Position>();
	private Position[] currentTriangle = new Position[3];
	private int currentTriangleIndex = 0;

	public TessellatorCallback(GLU glu)
	{
		this.glu = glu;
	}
	
	public FastShape getShape()
	{
		return new FastShape(positions, GL.GL_TRIANGLES);
	}

	@Override
	public void begin(int type)
	{
	}

	@Override
	public void end()
	{
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
				addTriangle();
				currentTriangleIndex = 0;
			}
		}
	}

	private void addTriangle()
	{
		positions.add(currentTriangle[0]);
		positions.add(currentTriangle[1]);
		positions.add(currentTriangle[2]);
	}

	@Override
	public void error(int error)
	{
		String message = "GLU error: " + glu.gluErrorString(error) + " (" + error + ")";
		Logging.logger().severe(message);
	}
}
