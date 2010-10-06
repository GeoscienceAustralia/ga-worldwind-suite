package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.geom.Vec4;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

public class RectangularTessellatorAccessible extends RectangularTessellator
{
	public static RenderInfo getRenderInfo(RectTile tile)
	{
		return tile.ri;
	}
	
	public static void setRenderInfo(RectTile tile, RenderInfo ri)
	{
		tile.ri = ri;
	}

	public static Vec4 getReferenceCenter(RenderInfo ri)
	{
		return ri.referenceCenter;
	}

	public static IntBuffer getIndices(RenderInfo ri)
	{
		return ri.indices;
	}

	public static DoubleBuffer getVertices(RenderInfo ri)
	{
		return ri.vertices;
	}
}
