package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

public class RectangularTessellatorAccessible extends RectangularTessellator
{
	public static RenderInfo getRenderInfo(RectTile tile)
	{
		return tile.ri;
	}
	
	public static int getLevel(RectTile tile)
	{
		return tile.level;
	}
	
	public static int getDensity(RectTile tile)
	{
		return tile.density;
	}
	
	public static void setRenderInfo(RectTile tile, RenderInfo ri)
	{
		tile.ri = ri;
	}
	
	public static RenderInfo createRenderInfo(DrawContext dc, int density, DoubleBuffer vertices, Integer verticesBuffer, Vec4 refCenter)
	{
		return new RenderInfo(dc, density, vertices, verticesBuffer, refCenter);
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
	
	public static int getDensity(RenderInfo ri)
	{
		return ri.density;
	}
	
	public static int getBufferIdVertices(RenderInfo ri)
	{
		return ri.bufferIdVertices;
	}
}
