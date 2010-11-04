package au.gov.ga.worldwind.animator.application.stereo;

import java.io.InputStream;

import javax.media.opengl.GL;

public class EdgeShader extends Shader
{
	private int textureWidthUniform;
	private int textureHeightUniform;
	
	public void create(GL gl)
	{
		InputStream vertex = this.getClass().getResourceAsStream("edge_vertex.glsl");
		InputStream fragment = this.getClass().getResourceAsStream("edge_fragment.glsl");
		super.create(gl, vertex, fragment);
		use(gl);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex0"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex1"), 1);
		textureWidthUniform = gl.glGetUniformLocation(shaderProgram, "textureWidth");
		textureHeightUniform = gl.glGetUniformLocation(shaderProgram, "textureHeight");
		unuse(gl);
	}
	
	public void use(GL gl, int textureWidth, int textureHeight)
	{
		super.use(gl);
		gl.glUniform1f(textureWidthUniform, (float)textureWidth);
		gl.glUniform1f(textureHeightUniform, (float)textureHeight);
	}
}
