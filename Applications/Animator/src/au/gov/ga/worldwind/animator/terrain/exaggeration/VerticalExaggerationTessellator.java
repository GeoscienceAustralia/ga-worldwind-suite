package au.gov.ga.worldwind.animator.terrain.exaggeration;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;

import nasa.worldwind.terrain.RectangularTessellator;

import com.sun.opengl.util.BufferUtil;

public class VerticalExaggerationTessellator extends RectangularTessellator
{
	private Double elevationExaggerationOverride;
	private Double bathymetryExaggerationOverride;

	public Double getElevationExaggerationOverride()
	{
		return elevationExaggerationOverride;
	}

	public void setElevationExaggerationOverride(Double elevationExaggerationOverride)
	{
		this.elevationExaggerationOverride = elevationExaggerationOverride;
	}

	public Double getBathymetryExaggerationOverride()
	{
		return bathymetryExaggerationOverride;
	}

	public void setBathymetryExaggerationOverride(Double bathymetryExaggerationOverride)
	{
		this.bathymetryExaggerationOverride = bathymetryExaggerationOverride;
	}

	@Override
	public RenderInfo buildVerts(DrawContext dc, RectTile tile, boolean makeSkirts)
	{
		int density = tile.density;
		int numVertices = (density + 3) * (density + 3);

		DoubleBuffer verts;

		//Re-use the RenderInfo vertices buffer. If it has not been set or the density has changed, create a new buffer
		if (tile.ri == null || tile.ri.vertices == null || density != tile.ri.density)
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
		dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(), elevations);

		int iv = 0;
		double verticalExaggeration = dc.getVerticalExaggeration();
		Double exaggeratedMinElevation =
				makeSkirts ? globe.getMinElevation()
						* (bathymetryExaggerationOverride != null ? bathymetryExaggerationOverride
								: verticalExaggeration) : null;

		LatLon centroid = tile.sector.getCentroid();
		Vec4 refCenter =
				globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);

		int ie = 0;
		Iterator<LatLon> latLonIter = latlons.iterator();
		for (int j = 0; j <= density + 2; j++)
		{
			for (int i = 0; i <= density + 2; i++)
			{
				LatLon latlon = latLonIter.next();
				double elevation = elevations[ie++];
				if (elevation > 0 && elevationExaggerationOverride != null)
					elevation *= elevationExaggerationOverride;
				else if (elevation < 0 && bathymetryExaggerationOverride != null)
					elevation *= bathymetryExaggerationOverride;
				else
					elevation *= verticalExaggeration;

				// Tile edges use min elevation to draw the skirts
				if (exaggeratedMinElevation != null
						&& (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2))
					elevation = exaggeratedMinElevation;

				Vec4 p =
						globe.computePointFromPosition(latlon.getLatitude(), latlon.getLongitude(),
								elevation);
				verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
						.put(iv++, p.z - refCenter.z);
			}
		}

		verts.rewind();

		Integer bufferIdVertices = null;

		//Vertex Buffer Objects are supported in versions 1.5 and greater
		if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			GL gl = dc.getGL();

			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL.GL_CLIENT_VERTEX_ARRAY_BIT);

				//Create a new bufferId
				int glBuf[] = new int[1];
				gl.glGenBuffers(1, glBuf, 0);
				bufferIdVertices = glBuf[0];

				gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIdVertices);
				gl.glBufferData(GL.GL_ARRAY_BUFFER, verts.limit() * 8, verts, GL.GL_DYNAMIC_DRAW);
			}
			finally
			{
				ogsh.pop(gl);
			}
		}

		return new RenderInfo(dc, density, verts, bufferIdVertices, refCenter);
	}
}
