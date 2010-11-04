package au.gov.ga.worldwind.animator.application.render;

import java.awt.Dimension;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.util.Validate;

/**
 * Helper class for the creation and binding of an OpenGL Frame Buffer Object.
 * 
 * @author Michael de Hoog
 * @author James Navin
 */
public class FrameBuffer
{
	private int frameBufferId = 0;
	private int textureId = 0;
	private int depthId = 0;
	private Dimension currentDimensions = null;

	/**
	 * Create a frame buffer, and its texture and depth buffer (but don't bind)
	 * 
	 * @param gl
	 *            GL context
	 * @param dimensions
	 *            Frame buffer dimensions
	 */
	public void create(GL gl, Dimension dimensions)
	{
		create(gl, dimensions, false);
	}

	public void create(GL gl, Dimension dimensions, boolean depthAsTexture)
	{
		//generate a texture, depth buffer, and frame buffer
		textureId = generateTexture(gl, dimensions);
		depthId = generateDepthBuffer(gl, dimensions, depthAsTexture);
		frameBufferId = generateFrameBuffer(gl);

		//bind the frame buffer
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
		//bind the color and depth attachments to the frame buffer
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT,
				GL.GL_TEXTURE_2D, textureId, 0);
		if (depthAsTexture)
		{
			gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
					GL.GL_TEXTURE_2D, depthId, 0);
		}
		else
		{
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT,
					GL.GL_RENDERBUFFER_EXT, depthId);
		}

		//check to see if the frame buffer is supported and complete
		checkFrameBuffer(gl);

		//unbind the frame buffer (bound later in the prerender task)
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

		currentDimensions = dimensions;
	}

	/**
	 * Ensure this frame buffer has the correct dimensions. If the frame buffer
	 * has not yet been created, it is created now. If it has already been
	 * created, and the dimensions are different, it is deleted and recreated
	 * with the new dimensions.
	 * 
	 * @param gl
	 * @param dimensions
	 */
	public void resize(GL gl, Dimension dimensions)
	{
		resize(gl, dimensions, false);
	}

	public void resize(GL gl, Dimension dimensions, boolean depthAsTexture)
	{
		Validate.notNull(dimensions, "Dimensions cannot be null");

		if (isCreated() && dimensions.equals(currentDimensions))
			return; //already the correct dimensions

		delete(gl);
		create(gl, dimensions, depthAsTexture);
	}

	/**
	 * Bind the frame buffer
	 */
	public void bind(GL gl)
	{
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
	}

	/**
	 * Unbind the frame buffer
	 */
	public void unbind(GL gl)
	{
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
	}

	/**
	 * Performs necessary cleanup to remove the frame buffer
	 */
	public void delete(GL gl)
	{
		if (isCreated())
		{
			gl.glDeleteFramebuffersEXT(1, new int[] { frameBufferId }, 0);
			gl.glDeleteRenderbuffersEXT(1, new int[] { depthId }, 0);
			gl.glDeleteTextures(1, new int[] { textureId }, 0);
		}
		frameBufferId = 0;
		textureId = 0;
		depthId = 0;
		currentDimensions = null;
	}

	public boolean isCreated()
	{
		return frameBufferId > 0;
	}

	/**
	 * @return OpenGL texture ID for this frame buffer's texture
	 */
	public int getTextureId()
	{
		return textureId;
	}

	public int getDepthId()
	{
		return depthId;
	}

	/**
	 * @return The ID of the generated frame buffer object
	 */
	private int generateFrameBuffer(GL gl)
	{
		int[] frameBuffers = new int[1];
		gl.glGenFramebuffersEXT(1, frameBuffers, 0);
		if (frameBuffers[0] <= 0)
		{
			throw new IllegalStateException("Error generating frame buffer");
		}
		return frameBuffers[0];
	}

	/**
	 * Check that the frame buffer is complete; if not, throws an exception
	 */
	private void checkFrameBuffer(GL gl)
	{
		int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		if (status == GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT)
		{
			throw new IllegalStateException("Frame buffer unsupported, or parameters incorrect");
		}
		else if (status != GL.GL_FRAMEBUFFER_COMPLETE_EXT)
		{
			throw new IllegalStateException("Frame buffer incomplete");
		}
	}

	/**
	 * @return The ID of the generated texture
	 */
	private int generateTexture(GL gl, Dimension renderDimensions)
	{
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		if (textures[0] <= 0)
		{
			throw new IllegalStateException("Error generating texture for frame buffer");
		}
		gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, renderDimensions.width,
				renderDimensions.height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		return textures[0];
	}

	/**
	 * @return The ID of the generated depth buffer
	 */
	private int generateDepthBuffer(GL gl, Dimension renderDimensions, boolean asTexture)
	{
		int[] renderBuffers = new int[1];
		if (asTexture)
		{
			gl.glGenTextures(1, renderBuffers, 0);
		}
		else
		{
			gl.glGenRenderbuffersEXT(1, renderBuffers, 0);
		}
		if (renderBuffers[0] <= 0)
		{
			throw new IllegalStateException("Error generating depth buffer for frame buffer");
		}

		if (asTexture)
		{
			gl.glBindTexture(GL.GL_TEXTURE_2D, renderBuffers[0]);

			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_DEPTH_TEXTURE_MODE, GL.GL_INTENSITY);

			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_DEPTH_COMPONENT24, renderDimensions.width,
					renderDimensions.height, 0, GL.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_BYTE, null);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		}
		else
		{
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, renderBuffers[0]);
			gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT24,
					renderDimensions.width, renderDimensions.height);
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, 0);
		}

		return renderBuffers[0];
	}

	/**
	 * Draw a texture on a quad, covering the entire viewport
	 */
	public static void renderTexturedQuad(GL gl, int... textureIds)
	{
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glPushAttrib(GL.GL_ENABLE_BIT);

		try
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
			for (int i = 0; i < textureIds.length; i++)
			{
				gl.glActiveTexture(GL.GL_TEXTURE0 + i);
				gl.glBindTexture(GL.GL_TEXTURE_2D, textureIds[i]);

				gl.glBegin(GL.GL_QUADS);
				{
					gl.glTexCoord2f(0, 0);
					gl.glVertex3i(-1, -1, -1);
					gl.glTexCoord2f(1, 0);
					gl.glVertex3i(1, -1, -1);
					gl.glTexCoord2f(1, 1);
					gl.glVertex3i(1, 1, -1);
					gl.glTexCoord2f(0, 1);
					gl.glVertex3i(-1, 1, -1);
				}
				gl.glEnd();
			}
		}
		finally
		{
			gl.glPopMatrix();
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glPopMatrix();
			gl.glPopAttrib();
		}
	}
}
