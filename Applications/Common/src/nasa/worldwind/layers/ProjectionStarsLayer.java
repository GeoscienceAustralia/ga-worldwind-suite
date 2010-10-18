package nasa.worldwind.layers;

import javax.media.opengl.GL;

import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.render.DrawContext;

public class ProjectionStarsLayer extends StarsLayer
{
	protected void applyDrawProjection(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        double ditanceFromOrigin = dc.getView().getEyePoint().getLength3();
        //noinspection UnnecessaryLocalVariable
        double near = ditanceFromOrigin;
        double far = this.radius + ditanceFromOrigin;
        dc.getGLU().gluPerspective(dc.getView().getFieldOfView().degrees,
            dc.getView().getViewport().getWidth() / dc.getView().getViewport().getHeight(),
            near, far);
	}
	
	//COPIED FROM StarsLayer (replaced setting the projection matrix with a call to the function above):
	
	@Override
    public void doRender(DrawContext dc)
    {
        GL gl = dc.getGL();
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        // Load or reload stars if needed
        if ((this.starsBuffer == null && this.starsBufferId == 0) || this.rebuild)
        {
            this.loadStars(dc); // Create glList
            this.rebuild = false;
        }

        // Still no stars to render ?
        if (this.starsBuffer == null && this.starsBufferId == 0)
            return;

        try
        {
            // GL set up
            // Save GL state
/*            gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
                | GL.GL_CURRENT_BIT);    */
            gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT | GL.GL_POLYGON_BIT);
            attribsPushed = true;
            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
            gl.glDisable(GL.GL_DEPTH_TEST);        // no depth testing

            // Set far clipping far enough - is this the right way to do it ?
            //CHANGE HERE
            projectionPushed = true;
            applyDrawProjection(dc);
            //CHANGE HERE

            // Rotate sphere
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glRotatef((float) this.longitudeOffset.degrees, 0.0f, 1.0f, 0.0f);
            gl.glRotatef((float) -this.latitudeOffset.degrees, 1.0f, 0.0f, 0.0f);

            // Draw
            gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);

            if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
            {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.starsBufferId);
                gl.glInterleavedArrays(GL.GL_C3F_V3F, 0, 0);
                gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
            }
            else
            {
                gl.glInterleavedArrays(GL.GL_C3F_V3F, 0, this.starsBuffer);
                gl.glDrawArrays(GL.GL_POINTS, 0, this.numStars);
            }

            gl.glPopClientAttrib();
        }
        finally
        {
            // Restore GL state
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }
}
