package au.gov.ga.worldwind.common.layers.stereo;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.view.stereo.StereoView;
import gov.nasa.worldwind.geom.Matrix;
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
}
