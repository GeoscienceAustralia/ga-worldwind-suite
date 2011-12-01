package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;
import java.io.InputStream;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.application.effects.Shader;

public class DepthOfFieldShader extends Shader
{
	private int cameraNearUniform = -1;
	private int cameraFarUniform = -1;
	private int focalLengthUniform = -1;
	private int pixelSizeUniform = -1;
	private int blurTextureScaleUniform = -1;

	public void use(DrawContext dc, Dimension dimensions, float focus, float near, float far, float blurTextureScale)
	{
		GL gl = dc.getGL();
		super.use(gl);

		/*float nearClipDistance = (float) dc.getView().getNearClipDistance();
		float farClipDistance = (float) dc.getView().getFarClipDistance();

		float focalLength;
		Vec4 eyePoint = dc.getView().getEyePoint();
		Vec4 centerPoint = dc.getView().getCenterPoint();
		if (centerPoint != null && eyePoint != null)
		{
			focalLength = (float) centerPoint.distanceTo3(eyePoint);
		}
		else
		{
			focalLength = (farClipDistance + nearClipDistance) / 2f;
		}

		gl.glUniform1f(cameraNearUniform, nearClipDistance);
		gl.glUniform1f(cameraFarUniform, farClipDistance);
		gl.glUniform1f(focalLengthUniform, focalLength);*/
		
		gl.glUniform1f(cameraNearUniform, near);
		gl.glUniform1f(cameraFarUniform, far);
		gl.glUniform1f(focalLengthUniform, focus);
		
		gl.glUniform2f(pixelSizeUniform, 1f / dimensions.width, 1f / dimensions.height);
		gl.glUniform1f(blurTextureScaleUniform, blurTextureScale);
	}

	@Override
	protected InputStream getVertexSource()
	{
		return this.getClass().getResourceAsStream("GenericVertexShader.glsl");
	}

	@Override
	protected InputStream getFragmentSource()
	{
		return this.getClass().getResourceAsStream("DepthOfFieldFragmentShader.glsl");
	}

	@Override
	protected void getUniformLocations(GL gl)
	{
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "colorTexture"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "depthTexture"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "blurTexture"), 2);
		cameraNearUniform = gl.glGetUniformLocation(shaderProgram, "cameraNear");
		cameraFarUniform = gl.glGetUniformLocation(shaderProgram, "cameraFar");
		focalLengthUniform = gl.glGetUniformLocation(shaderProgram, "focalLength");
		pixelSizeUniform = gl.glGetUniformLocation(shaderProgram, "pixelSize");
		blurTextureScaleUniform = gl.glGetUniformLocation(shaderProgram, "blurTextureScale");
	}
}
