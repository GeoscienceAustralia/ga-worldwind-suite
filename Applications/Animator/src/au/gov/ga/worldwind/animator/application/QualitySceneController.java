package au.gov.ga.worldwind.animator.application;

import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.application.stereo.StereoSceneController;

public class QualitySceneController extends StereoSceneController
{
	@Override
	public void doRepaint(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glHint(GL.GL_FOG_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
		gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
		super.doRepaint(dc);
	}
}
