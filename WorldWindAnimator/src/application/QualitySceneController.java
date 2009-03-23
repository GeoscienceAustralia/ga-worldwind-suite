package application;

import javax.media.opengl.GL;

import gov.nasa.worldwind.BasicSceneController;
import gov.nasa.worldwind.render.DrawContext;

public class QualitySceneController extends BasicSceneController
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
