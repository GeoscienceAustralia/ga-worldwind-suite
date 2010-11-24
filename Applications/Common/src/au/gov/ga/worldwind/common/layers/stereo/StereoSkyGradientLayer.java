package au.gov.ga.worldwind.common.layers.stereo;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.stereo.StereoView;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.render.DrawContext;

public class StereoSkyGradientLayer extends SkyGradientLayer
{
	@Override
	protected void applyDrawProjection(DrawContext dc)
	{
		boolean loaded = false;
		if (dc.getView() instanceof StereoView)
		{
			StereoView stereo = (StereoView) dc.getView();

			//near is the distance from the origin
			double near = 100;
			double far = stereo.getFarClipDistance() + 10e3;
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
			super.applyDrawProjection(dc);
		}
	}
	
	/* *******************************************************************************************************
	 * The following methods are overridden to use computeHorizonDistance() in place of getFarClipDistance() *
	 ******************************************************************************************************* */
	
	@Override
	protected boolean isValid(DrawContext dc)
    {
        // Build or rebuild sky dome if horizon distance changed more then 100m
        // Note: increasing this threshold may produce artefacts like far clipping at very low altitude
        return this.glListId != -1 && Math.abs(this.lastRebuildHorizon - dc.getView().computeHorizonDistance()) <= 100;
    }
	
	@Override
	protected void updateSkyDone(DrawContext dc)
    {
        GL gl = dc.getGL();
        View view = dc.getView();

        if (this.glListId != -1)
            gl.glDeleteLists(this.glListId, 1);

        double tangentalDistance = view.computeHorizonDistance();
        double distToCenterOfPlanet = view.getEyePoint().getLength3();
        Position camPos = dc.getGlobe().computePositionFromPoint(view.getEyePoint());
        double worldRadius = dc.getGlobe().getRadiusAt(camPos);
        double camAlt = camPos.getElevation();

        // horizon latitude degrees
        double horizonLat = (-Math.PI / 2 + Math.acos(tangentalDistance / distToCenterOfPlanet))
                * 180 / Math.PI;
        // zenith latitude degrees
        double zenithLat = 90;
        float zenithOpacity = 1f;
        float gradientBias = 2f;
        if (camAlt >= thickness)
        {
            // Eye is above atmosphere
            double tangentalDistanceZenith = Math.sqrt(distToCenterOfPlanet * distToCenterOfPlanet
                    - (worldRadius + thickness) * (worldRadius + thickness));
            zenithLat = (-Math.PI / 2 + Math.acos(tangentalDistanceZenith / distToCenterOfPlanet)) * 180 / Math.PI;
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

        this.makeSkyDome(dc, (float) (tangentalDistance), horizonLat, zenithLat, SLICES, STACKS,
            zenithOpacity, gradientBias);
        this.lastRebuildHorizon = tangentalDistance;
    }
}
