package au.gov.ga.worldwind.common.layers.stereo;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL;

import nasa.worldwind.layers.ProjectionStarsLayer;
import au.gov.ga.worldwind.common.view.stereo.StereoView;

/**
 * An extension of the {@link ProjectionStarsLayer} that supports stereo rendering of stars
 */
public class StereoStarsLayer extends ProjectionStarsLayer
{
	@Override
	protected void applyDrawProjection(DrawContext dc, OGLStackHandler ogsh)
	{
		boolean loaded = false;
		if (dc.getView() instanceof StereoView)
		{
			StereoView stereo = (StereoView) dc.getView();
			//near is the distance from the origin
			double near = stereo.getEyePoint().getLength3();
			double far = this.radius + near;
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
	public void doRender(DrawContext dc)
	{
		float pointSize = 1f;
		if (dc.getView() instanceof StereoView && ((StereoView) dc.getView()).isStereo())
		{
			pointSize *= 2f;
		}
		dc.getGL().glPointSize(pointSize);

		super.doRender(dc);
	}
}
