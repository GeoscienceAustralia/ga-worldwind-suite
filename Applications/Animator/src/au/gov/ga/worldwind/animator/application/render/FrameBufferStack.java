package au.gov.ga.worldwind.animator.application.render;

import java.util.Stack;

import javax.media.opengl.GL;

public class FrameBufferStack
{
	private static Stack<Integer> stack = new Stack<Integer>();
	
	public static void push(GL gl, int frameBufferId)
	{
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
		stack.push(frameBufferId);
	}
	
	public static void pop(GL gl)
	{
		int frameBufferId = 0;
		if(!stack.isEmpty())
		{
			frameBufferId = stack.pop();
		}
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, frameBufferId);
	}
}
