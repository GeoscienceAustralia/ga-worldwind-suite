package application;

import java.awt.GraphicsDevice;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;

import com.sun.opengl.util.BufferUtil;

public class FBOCanvas extends GLCanvas
{
	private int fboId;
	private int depthRbId;
	private int tex;
	private boolean inited = false;

	public FBOCanvas(GLCapabilities capabilities)
	{
		super(capabilities);
	}

	public FBOCanvas(GLCapabilities capabilities,
			GLCapabilitiesChooser chooser, GLContext shareWith,
			GraphicsDevice device)
	{
		super(capabilities, chooser, shareWith, device);
	}

	@Override
	public void display()
	{
		GL gl = getGL();

		if (!inited)
		{
			super.display();
			inited = true;

			boolean isSupported = gl
					.isExtensionAvailable("GL_EXT_framebuffer_object");
			boolean supportsMultiDraw = gl
					.isExtensionAvailable("GL_ARB_draw_buffers");
			if (supportsMultiDraw)
			{
				IntBuffer buf = BufferUtil.newIntBuffer(16);
				gl.glGetIntegerv(GL.GL_MAX_COLOR_ATTACHMENTS_EXT, buf); // TODO Check for integer
				int maxDrawBuffers = buf.get(0);
				if (maxDrawBuffers > 1)
				{
					IntBuffer attachBuffer = BufferUtil
							.newIntBuffer(maxDrawBuffers);
					for (int i = 0; i < maxDrawBuffers; i++)
					{
						attachBuffer.put(GL.GL_COLOR_ATTACHMENT0_EXT + i);
					}
				}
				else
				{
					maxDrawBuffers = 1;
				}
			}
			if (!isSupported)
			{
				System.out.println("FBO not supported");
				return;
			}
			else
			{
				System.out.println("FBO support detected.");
			}

			int width = getWidth();
			int height = getHeight();

			//create objects
			IntBuffer buffer = BufferUtil.newIntBuffer(1);
			gl.glGenFramebuffersEXT(1, buffer);
			fboId = buffer.get(0);

			if (fboId <= 0)
			{
				System.out.println("Invalid FBO id " + fboId + " returned");
				return;
			}

			gl.glGenRenderbuffersEXT(1, buffer);
			depthRbId = buffer.get(0);

			gl.glGenTextures(1, buffer);
			tex = buffer.get(0);
			gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);

			// initialize texture
			gl.glBindTexture(GL.GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0,
					GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, 0);
			//TODO set texture parameters here

			// attach texture to framebuffercolor buffer
			gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
					GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, tex, 0);

			// initialize depth renderbuffer
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthRbId);
			gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,
					GL.GL_DEPTH_COMPONENT, width, height);

			// attach renderbufferto framebufferdepth buffer
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT,
					GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT,
					depthRbId);

			int check = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
			if (check != GL.GL_FRAMEBUFFER_COMPLETE_EXT)
			{
				System.out.println("Frame buffer unsupported");
				return;
			}
		}

		// render to the FBO
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);

		super.display();

		// render to the window, using the texture
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glColor3f(0.5f, 0, 0);
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glVertex3i(-1, -1, -1);
			gl.glVertex3i(1, -1, -1);
			gl.glVertex3i(1, 1, -1);
			gl.glVertex3i(-1, 1, -1);
		}
		gl.glEnd();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();
	}
}
