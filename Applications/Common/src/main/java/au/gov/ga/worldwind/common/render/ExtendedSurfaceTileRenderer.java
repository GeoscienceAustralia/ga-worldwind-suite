package au.gov.ga.worldwind.common.render;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicSurfaceTileRenderer;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.terrain.SectorGeometry;

import javax.media.opengl.GL;

/**
 * {@link SurfaceTileRenderer} that supports rendering surface tiles at an
 * elevation offset. Also supports rendering surface tiles on flat geometry (ie
 * elevation model is ignored).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtendedSurfaceTileRenderer extends GeographicSurfaceTileRenderer
{
	protected double elevationOffset = 0;
	protected boolean ignoreElevation = false;

	@Override
	public void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> tiles)
	{
		ExtendedDrawContext.applyWireframePolygonMode(dc);

		if (ignoreElevation && dc instanceof ExtendedDrawContext)
		{
			try
			{
				((ExtendedDrawContext) dc).switchToFlatSurfaceGeometry();
				super.renderTiles(dc, tiles);
			}
			finally
			{
				((ExtendedDrawContext) dc).switchToStandardSurfaceGeometry();
			}
		}
		else
		{
			super.renderTiles(dc, tiles);
		}
	}

	@Override
	protected void preComputeTextureTransform(DrawContext dc, SectorGeometry sg, Transform t)
	{
		super.preComputeTextureTransform(dc, sg, t);

		//this is a bit dodgy to setup the ModelView matrix in this function, but the superclass calls the
		//preComputeTextureTransform function at exactly the right time, which is why it's done here

		double exaggeratedOffset = elevationOffset * dc.getVerticalExaggeration();
		if (exaggeratedOffset != 0)
		{
			GL gl = dc.getGL();
			gl.glMatrixMode(GL.GL_MODELVIEW);

			Globe globe = dc.getGlobe();
			Sector sector = sg.getSector();
			LatLon centroid = sector.getCentroid();
			Vec4 cn = globe.computePointFromLocation(centroid).normalize3();

			Vec4 v1 = globe.computePointFromPosition(centroid.latitude, sector.getMinLongitude(), 0);
			Vec4 v2 = globe.computePointFromPosition(centroid.latitude, sector.getMinLongitude(), exaggeratedOffset);
			Vec4 v3 = globe.computePointFromPosition(centroid.latitude, sector.getMaxLongitude(), 0);
			Vec4 v4 = globe.computePointFromPosition(centroid.latitude, sector.getMaxLongitude(), exaggeratedOffset);

			//translate the tile by the elevation offset
			double elevationDelta = v1.distanceTo3(v2);
			elevationDelta *= exaggeratedOffset < 0 ? -1 : 1;
			Vec4 translation = cn.multiply3(elevationDelta);
			gl.glTranslated(translation.x, translation.y, translation.z);

			//When translating the tile away from the center of the earth, gaps may appear
			//in between tiles. To fix this, scale them too.
			double longitudeScale = v2.distanceTo3(v4) / v1.distanceTo3(v3);
			gl.glScaled(longitudeScale, longitudeScale, longitudeScale);
		}
	}

	/**
	 * @return The elevation offset at which to render tiles. Uses the OpenGL
	 *         ModelView matrix to offset tiles.
	 */
	public double getElevationOffset()
	{
		return elevationOffset;
	}

	/**
	 * Set the elevation offset at which to render tiles.
	 * 
	 * @param elevationOffset
	 */
	public void setElevationOffset(double elevationOffset)
	{
		this.elevationOffset = elevationOffset;
	}

	/**
	 * @return Should the elevation model be ignored when rendering tiles? This
	 *         has the effect of rendering surface tiles on a flat surface.
	 */
	public boolean isIgnoreElevation()
	{
		return ignoreElevation;
	}

	/**
	 * Set whether the elevation model should be ignored when rendering tiles.
	 * 
	 * @param ignoreElevation
	 */
	public void setIgnoreElevation(boolean ignoreElevation)
	{
		this.ignoreElevation = ignoreElevation;
	}
}
