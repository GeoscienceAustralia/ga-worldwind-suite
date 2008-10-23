package stereo;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL;

import stereo.StereoOrbitView.Eye;
import stereo.StereoOrbitView.StereoMode;

public class StereoSceneController extends AbstractSceneController
{
	@Override
	protected void doRepaint(DrawContext dc)
	{
		GL gl = dc.getGL();
		this.initializeFrame(dc);
		try
		{
			this.applyView(dc);
			this.createTerrain(dc);
			this.clearFrame(dc);
			this.pick(dc);
			this.clearFrame(dc);

			View view = dc.getView();
			StereoOrbitView stereo = null;
			if (view instanceof StereoOrbitView)
			{
				stereo = (StereoOrbitView) view;
			}
			if (stereo == null || stereo.getMode() == StereoMode.NONE)
			{
				this.draw(dc);
			}
			else
			{
				stereo.setDrawing(true);
				stereo.setEye(Eye.LEFT);
				view.apply(dc);

				//gl.glColorMask(true, false, false, true);
				gl.glDrawBuffer(GL.GL_BACK_LEFT);
				this.draw(dc);

				gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
				gl.glDisable(GL.GL_FOG);

				stereo.setEye(Eye.RIGHT);
				view.apply(dc);

				//gl.glColorMask(false, true, true, true);
				gl.glDrawBuffer(GL.GL_BACK_RIGHT);
				this.draw(dc);

				stereo.setDrawing(false);
				view.apply(dc);

				//gl.glColorMask(true, true, true, true);
				gl.glDrawBuffer(GL.GL_BACK);
			}
		}
		finally
		{
			this.finalizeFrame(dc);
		}
	}
}
