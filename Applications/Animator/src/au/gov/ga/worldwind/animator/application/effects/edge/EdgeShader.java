package au.gov.ga.worldwind.animator.application.effects.edge;

import java.io.InputStream;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.application.effects.Shader;

/**
 * Shader used by the {@link EdgeDetectionEffect}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EdgeShader extends Shader
{
	private int textureWidthUniform;
	private int textureHeightUniform;

	public void use(GL gl, int textureWidth, int textureHeight)
	{
		super.use(gl);
		gl.glUniform1f(textureWidthUniform, (float) textureWidth);
		gl.glUniform1f(textureHeightUniform, (float) textureHeight);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("EdgeDetectionVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("EdgeDetectionFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "colorTexture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "depthTexture"), 1);
		textureWidthUniform = gl.glGetUniformLocation(shaderProgram, "textureWidth");
		textureHeightUniform = gl.glGetUniformLocation(shaderProgram, "textureHeight");
	}
}
