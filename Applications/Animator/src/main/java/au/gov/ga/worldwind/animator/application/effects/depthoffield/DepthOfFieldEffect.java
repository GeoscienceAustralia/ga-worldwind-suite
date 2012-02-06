package au.gov.ga.worldwind.animator.application.effects.depthoffield;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getDepthOfFieldNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Dimension;

import javax.media.opengl.GL;
import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.effects.EffectBase;
import au.gov.ga.worldwind.animator.application.render.FrameBuffer;

/**
 * {@link Effect} implementation that provides a depth-of-field effect, with
 * animatable near, far, and focus length parameters. The parameter values
 * default to the near clipping plane, far clipping plane, and animation camera
 * look-at point respectively.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DepthOfFieldEffect extends EffectBase
{
	private final DepthOfFieldShader depthOfFieldShader = new DepthOfFieldShader();
	private final GaussianBlurShader gaussianBlurShader = new GaussianBlurShader();
	private final FrameBuffer blurFrameBuffer = new FrameBuffer();

	private double focus = 5;
	private double near = 0;
	private double far = 10;

	private Parameter focusParameter;
	private Parameter nearParameter;
	private Parameter farParameter;

	public DepthOfFieldEffect(String name, Animation animation)
	{
		super(name, animation);
		initializeParameters();
	}

	@SuppressWarnings("unused")
	private DepthOfFieldEffect()
	{
		super();
	}

	@Override
	public String getDefaultName()
	{
		return getMessageOrDefault(getDepthOfFieldNameKey(), "Depth of Field");
	}

	/**
	 * Initialize the parameters for this effect (create them if they don't
	 * exist). The parameters are unarmed and disabled by default.
	 */
	protected void initializeParameters()
	{
		if (focusParameter == null || nearParameter == null || farParameter == null)
		{
			focusParameter = new DepthOfFieldFocusParameter(null, animation, this);
			nearParameter = new DepthOfFieldNearParameter(null, animation, this);
			farParameter = new DepthOfFieldFarParameter(null, animation, this);
		}

		focusParameter.setArmed(false);
		focusParameter.setEnabled(false);
		nearParameter.setArmed(false);
		nearParameter.setEnabled(false);
		farParameter.setArmed(false);
		farParameter.setEnabled(false);

		parameters.clear();
		parameters.add(focusParameter);
		parameters.add(nearParameter);
		parameters.add(farParameter);
	}

	@Override
	protected void resizeExtraFrameBuffers(DrawContext dc, Dimension dimensions)
	{
		blurFrameBuffer.resize(dc.getGL(), new Dimension(dimensions.width / 4, dimensions.height / 4)); //1/16 of the size
	}

	/**
	 * @return The near blur limit (everything closer than this distance is
	 *         fully blurred)
	 */
	public double getNear()
	{
		return near;
	}

	/**
	 * Set the near blur limit
	 * 
	 * @param near
	 */
	public void setNear(double near)
	{
		this.near = near;
	}

	/**
	 * @return The far blur limit (everything beyond this is fully blurred)
	 */
	public double getFar()
	{
		return far;
	}

	/**
	 * Set the far blur limit
	 * 
	 * @param far
	 */
	public void setFar(double far)
	{
		this.far = far;
	}

	/**
	 * @return The focus distance (everything at this depth is in focus)
	 */
	public double getFocus()
	{
		return focus;
	}

	/**
	 * Set the focus distance
	 * 
	 * @param focus
	 */
	public void setFocus(double focus)
	{
		this.focus = focus;
	}

	@Override
	public String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getDepthOfFieldEffectElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		XPath xpath = WWXML.makeXPath();

		DepthOfFieldEffect effect = new DepthOfFieldEffect(name, animation);
		context.setValue(constants.getCurrentEffectKey(), effect);

		effect.focusParameter =
				new DepthOfFieldFocusParameter()
						.fromXml(WWXML.getElement(element, constants.getDepthOfFieldFocusElementName(), xpath),
								version, context);
		effect.nearParameter =
				new DepthOfFieldNearParameter().fromXml(
						WWXML.getElement(element, constants.getDepthOfFieldNearElementName(), xpath), version, context);
		effect.farParameter =
				new DepthOfFieldFarParameter().fromXml(
						WWXML.getElement(element, constants.getDepthOfFieldFarElementName(), xpath), version, context);

		effect.initializeParameters();

		return effect;
	}

	@Override
	public Effect createWithAnimation(Animation animation)
	{
		return new DepthOfFieldEffect(null, animation);
	}

	@Override
	protected void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions, FrameBuffer frameBuffer)
	{
		GL gl = dc.getGL();

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
				FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTextureId());
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
			depthOfFieldShader.use(dc, frameBuffer.getDimensions(), (float) focus, (float) near, (float) far, 1f / 4f);
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTextureId(), frameBuffer.getDepthId(),
					blurFrameBuffer.getTextureId());
		}
		finally
		{
			depthOfFieldShader.unuse(gl);
		}
	}

	@Override
	protected void releaseEffect(DrawContext dc)
	{
		GL gl = dc.getGL();
		if (depthOfFieldShader.isCreated())
		{
			depthOfFieldShader.delete(gl);
		}
		if (gaussianBlurShader.isCreated())
		{
			gaussianBlurShader.delete(gl);
		}
		if (blurFrameBuffer.isCreated())
		{
			blurFrameBuffer.delete(gl);
		}
	}
}
