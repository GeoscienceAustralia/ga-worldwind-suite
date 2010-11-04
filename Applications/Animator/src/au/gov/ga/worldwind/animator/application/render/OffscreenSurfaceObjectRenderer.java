package au.gov.ga.worldwind.animator.application.render;

import javax.media.opengl.GL;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectRenderer;
import gov.nasa.worldwind.util.OGLRenderToTextureSupport;

public class OffscreenSurfaceObjectRenderer extends SurfaceObjectRenderer
{
	public OffscreenSurfaceObjectRenderer()
	{
		super();
		this.rttSupport = new OffscreenOGLRenderToTextureSupport();
	}
	
	public class OffscreenOGLRenderToTextureSupport extends OGLRenderToTextureSupport
	{
		@Override
		protected void beginFramebufferObjectRendering(DrawContext dc)
		{
			int[] framebuffers = new int[1];

	        GL gl = dc.getGL();
	        gl.glGenFramebuffersEXT(1, framebuffers, 0);
	        FrameBufferStack.push(gl, framebuffers[0]);

	        this.framebufferObject = framebuffers[0];
	        if (this.framebufferObject == 0)
	        {
	            throw new IllegalStateException("Frame Buffer Object not created.");
	        }
		}
		
		@Override
		protected void endFramebufferObjectRendering(DrawContext dc)
		{
			int[] framebuffers = new int[] {this.framebufferObject};

	        GL gl = dc.getGL();
	        FrameBufferStack.pop(gl);
	        gl.glDeleteFramebuffersEXT(1, framebuffers, 0);

	        this.framebufferObject = 0;
		}
	}
}
