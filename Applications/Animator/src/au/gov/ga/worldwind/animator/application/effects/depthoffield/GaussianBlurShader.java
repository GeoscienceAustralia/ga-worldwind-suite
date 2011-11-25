package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.io.InputStream;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.application.effects.Shader;

public class GaussianBlurShader extends Shader
{
	private int sigmaUniform;
	private int blurSizeUniform;
	private int horizontalUniform;
	
	public void use(DrawContext dc, Dimension dimensions, boolean horizontal)
	{
		GL gl = dc.getGL();
		super.use(gl);
		
		gl.glUniform1f(sigmaUniform, 4.0f);
		gl.glUniform1i(horizontalUniform, horizontal ? 1 : 0);
		float blurSize = 1.0f / (horizontal ? dimensions.width : dimensions.height);
		gl.glUniform1f(blurSizeUniform, blurSize);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("GenericVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("GaussianBlurFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "blurSampler"), 0);
		sigmaUniform = gl.glGetUniformLocation(shaderProgram, "sigma");
		blurSizeUniform = gl.glGetUniformLocation(shaderProgram, "blurSize");
		horizontalUniform = gl.glGetUniformLocation(shaderProgram, "horizontal");
	}
}
