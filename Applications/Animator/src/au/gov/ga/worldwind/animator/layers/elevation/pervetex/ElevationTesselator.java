package layers.elevation.pervetex;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;

import nasa.worldwind.terrain.RectangularTessellator;

import com.sun.opengl.util.BufferUtil;

public class ElevationTesselator extends RectangularTessellator
{
	protected static class RenderInfo extends RectangularTessellator.RenderInfo
	{
		protected final DoubleBuffer vertices;
		protected final Vec4 refCenter;
		protected final double[] elevations;
		protected final double minElevation;
		protected final double maxElevation;

		protected RenderInfo(DrawContext dc, int density,
				DoubleBuffer vertices, Integer bufferIdVertices,
				double[] elevations, double minElevation, double maxElevation,
				Vec4 refCenter)
		{
			super(dc, density, vertices, bufferIdVertices, refCenter);
			this.vertices = vertices;
			this.elevations = elevations;
			this.minElevation = minElevation;
			this.maxElevation = maxElevation;
			this.refCenter = refCenter;
		}
	}

	private double minElevation;
	private double maxElevation;

	@Override
	public SectorGeometryList tessellate(DrawContext dc)
	{
		SectorGeometryList sgl = super.tessellate(dc);

		int count = 0;

		minElevation = Double.MAX_VALUE;
		maxElevation = -Double.MAX_VALUE;
		for (SectorGeometry sg : sgl)
		{
			RectTile tile = (RectTile) sg;
			RenderInfo ri = (RenderInfo) tile.ri;
			if (isSectorInScreen(dc, sg.getSector()))
			{
				minElevation = Math.min(minElevation, ri.minElevation);
				maxElevation = Math.max(maxElevation, ri.maxElevation);
			}
			else
			{
				ri.vertices.rewind();
				for (int i = 0; i < ri.vertices.limit() / 3; i++)
				{
					Vec4 point = new Vec4(ri.vertices.get(), ri.vertices.get(),
							ri.vertices.get()).add3(ri.refCenter);
					if (isPointInScreen(dc, point))
					{
						count++;
						minElevation = Math.min(minElevation, ri.elevations[i]);
						maxElevation = Math.max(maxElevation, ri.elevations[i]);
					}
				}
			}
		}
		if (minElevation == Double.MAX_VALUE)
			minElevation = dc.getGlobe().getMinElevation();
		if (maxElevation == -Double.MAX_VALUE)
			maxElevation = dc.getGlobe().getMaxElevation();

		return sgl;
	}

	public double getMinElevation()
	{
		return minElevation;
	}

	public double getMaxElevation()
	{
		return maxElevation;
	}

	private boolean isSectorInScreen(DrawContext dc, Sector sector)
	{
		Vec4[] cornerPoints = sector.computeCornerPoints(dc.getGlobe(), dc
				.getVerticalExaggeration());
		for (Vec4 point : cornerPoints)
		{
			if (!isPointInScreen(dc, point))
				return false;
		}
		return true;
	}

	private boolean isPointInScreen(DrawContext dc, Vec4 point)
	{
		Vec4 screenpoint = dc.getScreenPoint(point);
		return screenpoint.z < 1.0
				&& dc.getView().getViewport().contains(screenpoint.x,
						screenpoint.y);
	}

	@Override
	public RenderInfo buildVerts(DrawContext dc, RectTile tile,
			boolean makeSkirts)
	{
		int density = tile.density;
		int numVertices = (density + 3) * (density + 3);

		DoubleBuffer verts;

		// Re-use the RenderInfo vertices buffer. If it has not been set or the
		// density has changed, create a new buffer
		if (tile.ri == null || tile.ri.vertices == null
				|| density != tile.ri.density)
		{
			verts = BufferUtil.newDoubleBuffer(numVertices * 3);
		}
		else
		{
			verts = tile.ri.vertices;
			verts.rewind();
		}

		ArrayList<LatLon> latlons = this.computeLocations(tile);
		double[] elevations = new double[latlons.size()];
		dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(),
				elevations);

		int iv = 0;
		double verticalExaggeration = dc.getVerticalExaggeration();
		Double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation()
				* verticalExaggeration : null;

		LatLon centroid = tile.sector.getCentroid();
		Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(),
				centroid.getLongitude(), 0d);

		double minElevation = Double.MAX_VALUE;
		double maxElevation = -Double.MAX_VALUE;
		int ie = 0;
		Iterator<LatLon> latLonIter = latlons.iterator();
		for (int j = 0; j <= density + 2; j++)
		{
			for (int i = 0; i <= density + 2; i++)
			{
				LatLon latlon = latLonIter.next();
				double elevation = verticalExaggeration * elevations[ie++];

				minElevation = Math.min(minElevation, elevation);
				maxElevation = Math.max(maxElevation, elevation);

				// Tile edges use min elevation to draw the skirts
				if (exaggeratedMinElevation != null
						&& (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2))
					elevation = exaggeratedMinElevation;

				Vec4 p = globe.computePointFromPosition(latlon.getLatitude(),
						latlon.getLongitude(), elevation);
				verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
						.put(iv++, p.z - refCenter.z);
			}
		}

		verts.rewind();

		Integer bufferIdVertices = null;

		// Vertex Buffer Objects are supported in versions 1.5 and greater
		if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			GL gl = dc.getGL();

			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);

				// Create a new bufferId
				int glBuf[] = new int[1];
				gl.glGenBuffers(1, glBuf, 0);
				bufferIdVertices = glBuf[0];

				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIdVertices);
				gl.glBufferData(GL.GL_ARRAY_BUFFER, verts.limit() * 8, verts,
						GL.GL_DYNAMIC_DRAW);
			}
			finally
			{
				ogsh.pop(gl);
			}
		}

		return new RenderInfo(dc, density, verts, bufferIdVertices, elevations,
				minElevation, maxElevation, refCenter);
	}
}
