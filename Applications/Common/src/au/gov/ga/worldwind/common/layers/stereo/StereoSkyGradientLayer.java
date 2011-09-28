package au.gov.ga.worldwind.common.layers.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.stereo.StereoView;

/**
 * An extension of the {@link SkyGradientLayer} that is modified to use {@link View#computeHorizonDistance()} rather than
 * {@link View#getFarClipDistance()} to make it suitable for use in views that dynamically modify the far clip distance 
 * (e.g. for occasions when {@link View#getFarClipDistance()} != {@link View#computeHorizonDistance()}).
 * 
 * @deprecated The standard {@link SkyGradientLayer} makes use of the horizon distance rather than the clip plane 
 * (as of build worldwind-71-66). This change makes this class redundant, and it will be removed in future releases.
 */
@Deprecated
public class StereoSkyGradientLayer extends SkyGradientLayer
{
	private static final double PI_ON_2 = Math.PI / 2;
	private static final double ONE80_ON_PI = 180.0 / Math.PI;
	
	/* *******************************************************************************************************
	 * The following methods are overridden to use computeHorizonDistance() in place of getFarClipDistance() *
	 ******************************************************************************************************* */
	
	@Override
	protected void applyDrawProjection(DrawContext dc, OGLStackHandler ogsh)
	{
		boolean loaded = false;
		if (dc.getView() instanceof StereoView)
		{
			StereoView stereo = (StereoView) dc.getView();

			//near is the distance from the origin
			double near = 100;
			double far = stereo.getHorizonDistance() + 10e3;
			Matrix projection = stereo.calculateProjectionMatrix(near, far);

			if (projection != null)
			{
				double[] matrixArray = new double[16];
				GL gl = dc.getGL();
				gl.glMatrixMode(GL.GL_PROJECTION);
				gl.glPushMatrix();

				projection.toArray(matrixArray, 0, false);
				gl.glLoadMatrixd(matrixArray, 0);

				loaded = true;
			}
		}

		if (!loaded)
		{
			super.applyDrawProjection(dc, ogsh);
		}
	}
	
	@Override
	protected int[] updateSkyDome(DrawContext dc)
    {
        View view = dc.getView();

        double tangentialDistance = view.getHorizonDistance();
        double distToCenterOfPlanet = view.getEyePoint().getLength3();
        Position camPos = dc.getGlobe().computePositionFromPoint(view.getEyePoint());
        double worldRadius = dc.getGlobe().getRadiusAt(camPos);
        double camAlt = camPos.getElevation();

        // horizon latitude degrees
        double horizonLat = (-PI_ON_2 + Math.acos(tangentialDistance / distToCenterOfPlanet)) * ONE80_ON_PI;
        
        // zenith latitude degrees
        double zenithLat = 90;
        float zenithOpacity = 1f;
        float gradientBias = 2f;
        
        if (camAlt >= thickness)
        {
            // Eye is above atmosphere
            double tangentalDistanceZenith = Math.sqrt(distToCenterOfPlanet * distToCenterOfPlanet - (worldRadius + thickness) * (worldRadius + thickness));
            zenithLat = (-PI_ON_2 + Math.acos(tangentalDistanceZenith / distToCenterOfPlanet)) * ONE80_ON_PI;
            zenithOpacity = 0f;
            gradientBias = 1f;
        }
        
        if (camAlt < thickness && camAlt > thickness * 0.7)
        {
            // Eye is entering atmosphere - outer 30%
            double factor = (thickness - camAlt) / (thickness - thickness * 0.7);
            zenithLat = factor * 90;
            zenithOpacity = (float)factor;
            gradientBias = 1f + (float)factor;
        }

        int[] dlid = this.makeSkyDome(dc, (float) (tangentialDistance), horizonLat, zenithLat, SLICES, STACKS, zenithOpacity, gradientBias);
        this.lastRebuildHorizon = tangentialDistance;
        
        return dlid;
    }
}
