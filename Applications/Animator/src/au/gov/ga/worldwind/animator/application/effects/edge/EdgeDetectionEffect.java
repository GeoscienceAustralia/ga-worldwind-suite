package au.gov.ga.worldwind.animator.application.effects.edge;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getEdgeDetectionNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.application.effects.Effect;
import au.gov.ga.worldwind.animator.application.effects.EffectBase;
import au.gov.ga.worldwind.animator.application.render.FrameBuffer;

public class EdgeDetectionEffect extends EffectBase
{
	private boolean enabledInPreDraw;
	private EdgeShader edgeShader = new EdgeShader();
	private FrameBuffer frameBuffer = new FrameBuffer();

	public EdgeDetectionEffect(String name, Animation animation)
	{
		super(name, animation);
	}

	protected EdgeDetectionEffect()
	{
		super();
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
		frameBuffer.resize(gl, dimensions, true);
		frameBuffer.bind(gl);
	}

	@Override
	public void postDraw(DrawContext dc, Dimension dimensions)
	{
		GL gl = dc.getGL();

		if (!enabledInPreDraw)
		{
			if (edgeShader.isCreated())
			{
				edgeShader.delete(gl);
			}
			return;
		}

		//unbind the main frame buffer
		frameBuffer.unbind(gl);

		if (!edgeShader.isCreated())
		{
			edgeShader.create(gl);
		}

		try
		{
			edgeShader.use(gl, dimensions.width, dimensions.height);
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTextureId(), frameBuffer.getDepthId());
		}
		finally
		{
			edgeShader.unuse(gl);
		}
	}

	@Override
	public Effect createWithAnimation(Animation animation)
	{
		return new EdgeDetectionEffect(null, animation);
	}

	@Override
	public String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getEdgeDetectionEffectElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		return new EdgeDetectionEffect(name, animation);
	}

	@Override
	public String getDefaultName()
	{
		return getMessageOrDefault(getEdgeDetectionNameKey(), "Edge Detection");
	}
}
