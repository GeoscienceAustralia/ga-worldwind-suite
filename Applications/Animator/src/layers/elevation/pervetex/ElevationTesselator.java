package layers.elevation.pervetex;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometryList;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Iterator;

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

		protected RenderInfo(int density, DoubleBuffer vertices,
				double[] elevations, double minElevation, double maxElevation,
				DoubleBuffer texCoords, Vec4 refCenter)
		{
			super(density, vertices, texCoords, refCenter);
			this.elevations = elevations;
			this.vertices = vertices;
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
		DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);
		ArrayList<LatLon> latlons = this.computeLocations(tile);
		double[] elevations = new double[latlons.size()];
		dc.getGlobe().getElevations(tile.sector, latlons, tile.getResolution(),
				elevations);

		double verticalExaggeration = dc.getVerticalExaggeration();
		double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation()
				* verticalExaggeration : 0;

		LatLon centroid = tile.sector.getCentroid();
		Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(),
				centroid.getLongitude(), 0d);

		double minElevation = Double.MAX_VALUE;
		double maxElevation = -Double.MAX_VALUE;
		int ie = 0, iv = 0;
		Iterator<LatLon> latLonIter = latlons.iterator();
		for (int j = 0; j <= density + 2; j++)
		{
			for (int i = 0; i <= density + 2; i++)
			{
				LatLon latlon = latLonIter.next();
				double elevation = elevations[ie++];

				minElevation = Math.min(minElevation, elevation);
				maxElevation = Math.max(maxElevation, elevation);

				//add exaggeration and skirts
				elevation *= verticalExaggeration;
				if (j == 0 || j >= tile.density + 2 || i == 0
						|| i >= tile.density + 2)
				{ // use abs to account for negative elevation.
					elevation -= exaggeratedMinElevation >= 0 ? exaggeratedMinElevation
							: -exaggeratedMinElevation;
				}

				Vec4 p = globe.computePointFromPosition(latlon.getLatitude(),
						latlon.getLongitude(), elevation);
				verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y)
						.put(iv++, p.z - refCenter.z);
			}
		}

		return new RenderInfo(density, verts, elevations, minElevation,
				maxElevation, getTextureCoordinates(density), refCenter);
	}
}
