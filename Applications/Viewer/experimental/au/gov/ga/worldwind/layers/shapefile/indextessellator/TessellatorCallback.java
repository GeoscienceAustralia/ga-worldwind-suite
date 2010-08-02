package au.gov.ga.worldwind.layers.shapefile.indextessellator;

import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

public class TessellatorCallback extends GLUtessellatorCallbackAdapter
{
	private final GLU glu;
	public final List<Integer> indices = new ArrayList<Integer>();

	public TessellatorCallback(GLU glu)
	{
		this.glu = glu;
	}

	@Override
	public void vertex(Object o)
	{
		if (o instanceof Integer)
			indices.add((Integer) o);
	}

	@Override
	public void error(int error)
	{
		String message = "GLU error: " + glu.gluErrorString(error) + " (" + error + ")";
		Logging.logger().severe(message);
	}
}
