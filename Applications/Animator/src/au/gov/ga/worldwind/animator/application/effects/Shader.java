package au.gov.ga.worldwind.animator.application.effects;

import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.IOUtil;

public abstract class Shader
{
	protected int shaderProgram = 0;
	protected int vertexShader = 0;
	protected int fragmentShader = 0;
	
	protected abstract InputStream getVertexSource();
	protected abstract InputStream getFragmentSource();
	protected abstract void getUniformLocations(GL gl);

	public void create(GL gl)
	{
		if (isCreated())
			return;
		
		InputStream vertex = getVertexSource();
		InputStream fragment = getFragmentSource();

		String vsrc = null, fsrc = null;
		try
		{
			vsrc = IOUtil.readStreamToStringKeepingNewlines(vertex, null);
			fsrc = IOUtil.readStreamToStringKeepingNewlines(fragment, null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		vertexShader = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		if (vertexShader <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating vertex shader");
		}
		fragmentShader = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		if (fragmentShader <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating fragment shader");
		}

		gl.glShaderSource(vertexShader, 1, new String[] { vsrc }, new int[] { vsrc.length() }, 0);
		gl.glCompileShader(vertexShader);
		gl.glShaderSource(fragmentShader, 1, new String[] { fsrc }, new int[] { fsrc.length() }, 0);
		gl.glCompileShader(fragmentShader);

		shaderProgram = gl.glCreateProgram();
		if (shaderProgram <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating shader program");
		}

		gl.glAttachShader(shaderProgram, vertexShader);
		gl.glAttachShader(shaderProgram, fragmentShader);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		int[] status = new int[1];
		gl.glGetProgramiv(shaderProgram, GL.GL_VALIDATE_STATUS, status, 0);
		if (status[0] != GL.GL_TRUE)
		{
			int maxLength = 10240;
			int[] length = new int[1];
			byte[] bytes = new byte[maxLength];
			gl.glGetProgramInfoLog(shaderProgram, maxLength, length, 0, bytes, 0);
			String info = new String(bytes, 0, length[0]);
			System.out.println(info);
			
			delete(gl);
			throw new IllegalStateException("Validation of shader program failed");
		}
		
		gl.glUseProgram(shaderProgram);
		getUniformLocations(gl);
		gl.glUseProgram(0);
	}

	public boolean isCreated()
	{
		return shaderProgram > 0;
	}

	//this must be called by subclasses
	protected void use(GL gl)
	{
		gl.glUseProgram(shaderProgram); //if !isCreated(), then shaderProgram == 0
	}

	public void unuse(GL gl)
	{
		gl.glUseProgram(0);
	}

	public void delete(GL gl)
	{
		if (shaderProgram > 0)
		{
			gl.glDeleteProgram(shaderProgram);
		}
		if (vertexShader > 0)
		{
			gl.glDeleteShader(vertexShader);
		}
		if (fragmentShader > 0)
		{
			gl.glDeleteShader(fragmentShader);
		}
		shaderProgram = 0;
		vertexShader = 0;
		fragmentShader = 0;
	}
}
