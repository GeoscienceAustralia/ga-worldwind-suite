package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getOutlineOpacityParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.Renderable;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link LayerParameter} that controls the Outline Opacity of any shapes
 * contained in a {@link RenderableLayer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ShapeOutlineOpacityParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 20110621L;

	public ShapeOutlineOpacityParameter(Animation animation, RenderableLayer layer)
	{
		this(null, animation, layer);
	}

	public ShapeOutlineOpacityParameter(String name, Animation animation, RenderableLayer layer)
	{
		super(name, animation, layer);
		for (Renderable renderable : layer.getRenderables())
		{
			if (renderable instanceof AbstractSurfaceShape)
			{
				setDefaultValue(((AbstractSurfaceShape) renderable).getAttributes().getOutlineOpacity());
				return;
			}
		}
	}

	@SuppressWarnings("unused")
	private ShapeOutlineOpacityParameter()
	{
	}

	@Override
	protected String getDefaultName()
	{
		return getMessage(getOutlineOpacityParameterNameKey());
	}

	@Override
	public Type getType()
	{
		return Type.OUTLINE_OPACITY;
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		for (Renderable renderable : getRenderableLayer().getRenderables())
		{
			if (renderable instanceof AbstractSurfaceShape)
			{
				return ParameterValueFactory.createParameterValue(this, ((AbstractSurfaceShape) renderable)
						.getAttributes().getOutlineOpacity(), animation.getCurrentFrame());
			}
		}
		return null;
	}

	@Override
	protected void doApplyValue(double value)
	{
		for (Renderable renderable : getRenderableLayer().getRenderables())
		{
			if (renderable instanceof AbstractSurfaceShape)
			{
				setDefaultValue(((AbstractSurfaceShape) renderable).getAttributes().getOutlineOpacity());
			}
		}
	}

	private RenderableLayer getRenderableLayer()
	{
		return ((RenderableLayer) getLayer());
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		Layer parameterLayer = (Layer) context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer,
				"No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		Validate.isTrue(parameterLayer instanceof RenderableLayer, "Layer found in context is incorrect type: '"
				+ parameterLayer.getClass().getCanonicalName() + "'");

		return new ShapeOutlineOpacityParameter(name, animation, (RenderableLayer) parameterLayer);
	}

}
