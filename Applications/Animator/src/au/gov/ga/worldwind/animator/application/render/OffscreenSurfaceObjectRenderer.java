package au.gov.ga.worldwind.animator.application.render;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectTileBuilder;
import gov.nasa.worldwind.util.OGLRenderToTextureSupport;

import javax.media.opengl.GL;

/**
 * This {@link SurfaceObjectTileBuilder} subclass uses the
 * {@link FrameBufferStack} when binding the framebuffer used to render surface
 * objects.
 * <p>
 * The default {@link SurfaceObjectTileBuilder} uses an fbo, but doesn't use our
 * {@link FrameBufferStack}. This means that the {@link FrameBuffer} used when
 * rendering the animation is lost, causing the render to fail.
 * </p>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OffscreenSurfaceObjectRenderer extends SurfaceObjectTileBuilder
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
			int[] framebuffers = new int[] { this.framebufferObject };

			GL gl = dc.getGL();
			FrameBufferStack.pop(gl);
			gl.glDeleteFramebuffersEXT(1, framebuffers, 0);

			this.framebufferObject = 0;
		}
	}
}
