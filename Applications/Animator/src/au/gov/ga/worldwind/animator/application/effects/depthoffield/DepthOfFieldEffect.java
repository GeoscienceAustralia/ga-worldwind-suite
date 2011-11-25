package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.application.effects.EffectBase;
import au.gov.ga.worldwind.animator.application.render.FrameBuffer;

public class DepthOfFieldEffect extends EffectBase
{
	private boolean enabled = true;
	private boolean enabledInPreDraw = false;

	private final DepthOfFieldShader depthOfFieldShader = new DepthOfFieldShader();
	private final GaussianBlurShader gaussianBlurShader = new GaussianBlurShader();
	private final FrameBuffer mainFrameBuffer = new FrameBuffer();
	private final FrameBuffer blurFrameBuffer = new FrameBuffer();
	
	private double near = 0;
	private double far = 10;
	private double focus = 5;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public void preDraw(DrawContext dc, Dimension dimensions)
	{
		enabledInPreDraw = isEnabled();

		if (!enabledInPreDraw)
		{
			return;
		}

		//create the frame buffers
		GL gl = dc.getGL();
		mainFrameBuffer.resize(gl, dimensions, true);
		//blurFrameBuffer.resize(gl, dimensions, true);
		blurFrameBuffer.resize(gl, new Dimension(dimensions.width / 4, dimensions.height / 4)); //1/16 of the size

		//bind the main frame buffer to render the scene/depth to
		mainFrameBuffer.bind(gl);
	}

	@Override
	public void postDraw(DrawContext dc, Dimension dimensions)
	{
		GL gl = dc.getGL();

		if (!enabledInPreDraw)
		{
			if (depthOfFieldShader.isCreated())
			{
				depthOfFieldShader.delete(gl);
			}
			if (gaussianBlurShader.isCreated())
			{
				gaussianBlurShader.delete(gl);
			}
			return;
		}

		//unbind the main frame buffer
		mainFrameBuffer.unbind(gl);

		if (!depthOfFieldShader.isCreated())
		{
			depthOfFieldShader.create(gl);
		}
		if (!gaussianBlurShader.isCreated())
		{
			gaussianBlurShader.create(gl);
		}

		try
		{
			//disable depth testing for the blur frame buffer, and change the viewport to match the frame buffer's dimensions
			gl.glPushAttrib(GL.GL_VIEWPORT_BIT | GL.GL_DEPTH_BUFFER_BIT);
			gl.glDepthMask(false);
			gl.glViewport(0, 0, blurFrameBuffer.getDimensions().width, blurFrameBuffer.getDimensions().height);

			//bind the blur frame buffer
			try
			{
				//bind and clear the blur frame buffer
				blurFrameBuffer.bind(gl);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT);
				
				//draw the scene, blurring vertically first, then horizontally
				gaussianBlurShader.use(dc, blurFrameBuffer.getDimensions(), false);
				FrameBuffer.renderTexturedQuad(gl, mainFrameBuffer.getTextureId());
				gaussianBlurShader.use(dc, blurFrameBuffer.getDimensions(), true);
				FrameBuffer.renderTexturedQuad(gl, blurFrameBuffer.getTextureId());
				gaussianBlurShader.unuse(gl);
			}
			finally
			{
				blurFrameBuffer.unbind(gl);
			}
		}
		finally
		{
			gl.glPopAttrib();
		}

		try
		{
			depthOfFieldShader.use(dc, mainFrameBuffer.getDimensions(), 1f / 4f);
			FrameBuffer.renderTexturedQuad(gl, mainFrameBuffer.getTextureId(), mainFrameBuffer.getDepthId(),
					blurFrameBuffer.getTextureId());
		}
		finally
		{
			depthOfFieldShader.unuse(gl);
		}
	}

	public double getNear()
	{
		return near;
	}

	public void setNear(double near)
	{
		this.near = near;
	}

	public double getFar()
	{
		return far;
	}

	public void setFar(double far)
	{
		this.far = far;
	}

	public double getFocus()
	{
		return focus;
	}

	public void setFocus(double focus)
	{
		this.focus = focus;
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		// TODO Auto-generated method stub
		return null;
	}	
}
