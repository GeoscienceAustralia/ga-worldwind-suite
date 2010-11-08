package au.gov.ga.worldwind.animator.application.render;

import java.util.Stack;

import javax.media.opengl.GL;

public class FrameBufferStack
{
	private static Stack<Integer> stack = new Stack<Integer>();
	private static int currentFrameBufferId = 0;

	public static synchronized void push(GL gl, int frameBufferId)
	{
		stack.push(currentFrameBufferId);
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
		currentFrameBufferId = frameBufferId;
	}

	public static synchronized void pop(GL gl)
	{
		int frameBufferId = 0;
		if (!stack.isEmpty())
		{
			frameBufferId = stack.pop();
		}
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
		currentFrameBufferId = frameBufferId;
	}
}
