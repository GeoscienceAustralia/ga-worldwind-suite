package application;

import gov.nasa.worldwind.WorldWindowGLAutoDrawable;

import java.nio.IntBuffer;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.sun.opengl.util.BufferUtil;

public class WorldWindowFBO extends WorldWindowGLAutoDrawable
{
	private int fboId;
	private int depthRbId;
	private int tex;
	private boolean inited = false;

	@Override
	public void init(GLAutoDrawable glAutoDrawable)
	{
		super.init(glAutoDrawable);
		glAutoDrawable.setGL(new DebugGL(glAutoDrawable.getGL()));
		initFBO(glAutoDrawable);
	}

	@Override
	public void display(GLAutoDrawable glAutoDrawable)
	{
		GL gl = glAutoDrawable.getGL();

		// render to the FBO
		//gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);

		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

		super.display(glAutoDrawable);

		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

		// render to the window, using the texture
		/*gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		//gl.glColor3f(0.5f, 0, 0);
		gl.glBegin(GL.GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);
			gl.glVertex3i(-1, -1, -1);
			gl.glTexCoord2f(0, 1);
			gl.glVertex3i(1, -1, -1);
			gl.glTexCoord2f(1, 1);
			gl.glVertex3i(1, 1, -1);
			gl.glTexCoord2f(1, 0);
			gl.glVertex3i(-1, 1, -1);
		}
		gl.glEnd();
		gl.glPopMatrix();
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();*/
	}

	private void initFBO(GLAutoDrawable glAutoDrawable)
	{
		GL gl = glAutoDrawable.getGL();

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

		/*int width = glAutoDrawable.getWidth();
		int height = glAutoDrawable.getHeight();*/
		int width = 512;
		int height = 512;

		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glEnable(GL.GL_DEPTH_TEST);

		//create objects
		IntBuffer buffer = BufferUtil.newIntBuffer(1);
		gl.glGenFramebuffersEXT(1, buffer);
		fboId = buffer.get(0);

		if (fboId <= 0)
		{
			System.out.println("Invalid FBO id " + fboId + " returned");
			return;
		}

		// initialize texture
		gl.glGenTextures(1, buffer);
		tex = buffer.get(0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);
		//TODO set texture parameters here
		/*boolean wrap = false;
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
				GL.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
				GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
				wrap ? GL.GL_REPEAT : GL.GL_CLAMP);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
				wrap ? GL.GL_REPEAT : GL.GL_CLAMP);*/
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0,
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);

		// initialize depth renderbuffer
		gl.glGenRenderbuffersEXT(1, buffer);
		depthRbId = buffer.get(0);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, depthRbId);
		gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,
				GL.GL_DEPTH_COMPONENT, width, height);

		// bind the FBO
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fboId);

		// attach texture to framebuffer color buffer
		gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
				GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_TEXTURE_2D, tex, 0);

		// attach renderbuffer to framebuffer depth buffer
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT,
				GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, depthRbId);

		int check = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		if (check == GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT)
		{
			System.out.println("Frame buffer unsupported");
		}
		else if (check != GL.GL_FRAMEBUFFER_COMPLETE_EXT)
		{
			System.out.println("Frame buffer incomplete " + check);
		}
	}
}
