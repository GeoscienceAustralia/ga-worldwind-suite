package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicSurfaceTileRenderer;
import gov.nasa.worldwind.terrain.SectorGeometry;

import javax.media.opengl.GL;

public class OffsetSurfaceTileRenderer extends GeographicSurfaceTileRenderer
{
	protected double elevationOffset = 0;

	@Override
	protected void preComputeTextureTransform(DrawContext dc, SectorGeometry sg, Transform t)
	{
		super.preComputeTextureTransform(dc, sg, t);

		double exaggeratedOffset = elevationOffset * dc.getVerticalExaggeration();
		if (exaggeratedOffset != 0)
		{
			GL gl = dc.getGL();
			gl.glMatrixMode(GL.GL_MODELVIEW);

			Globe globe = dc.getGlobe();
			Sector sector = sg.getSector();
			LatLon centroid = sector.getCentroid();
			Vec4 v1 = globe.computePointFromPosition(centroid.latitude, sector.getMinLongitude(), 0);
			Vec4 v2 = globe.computePointFromPosition(centroid.latitude, sector.getMinLongitude(), exaggeratedOffset);
			Vec4 v3 = globe.computePointFromPosition(centroid.latitude, sector.getMaxLongitude(), 0);
			Vec4 v4 = globe.computePointFromPosition(centroid.latitude, sector.getMaxLongitude(), exaggeratedOffset);

			double elevationDelta = v1.distanceTo3(v2);
			if (exaggeratedOffset < 0)
			{
				elevationDelta = -elevationDelta;
			}
			Vec4 translation = globe.computePointFromLocation(centroid).normalize3().multiply3(elevationDelta);
			gl.glTranslated(translation.x, translation.y, translation.z);

			double longitudeScale = v2.distanceTo3(v4) / v1.distanceTo3(v3);
			gl.glScaled(longitudeScale, longitudeScale, longitudeScale);
		}
	}

	public double getElevationOffset()
	{
		return elevationOffset;
	}

	public void setElevationOffset(double elevationOffset)
	{
		this.elevationOffset = elevationOffset;
	}
}
