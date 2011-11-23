package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getOutlineOpacityParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.Renderable;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link LayerParameter} that controls the Outline Opacity of any shapes contained
 * in a {@link RenderableLayer}.
 */
public class ShapeOutlineOpacityParameter extends LayerParameterBase
{

	private static final long serialVersionUID = 20110621L;

	public ShapeOutlineOpacityParameter(Animation animation, RenderableLayer layer)
	{
		super(getMessage(getOutlineOpacityParameterNameKey()), animation, layer);
		for (Renderable renderable : layer.getRenderables())
		{
			if (renderable instanceof AbstractSurfaceShape)
			{
				setDefaultValue(((AbstractSurfaceShape) renderable).getAttributes().getOutlineOpacity());
				return;
			}
		}
	}
	
	// Private constructor for layer initialisation
	private ShapeOutlineOpacityParameter(){}

	@Override
	public Type getType()
	{
		return Type.OUTLINE_OPACITY;
	}

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		ParameterValue outlineOpacityValue = getValueAtFrame(frame);
		
		applyValue(outlineOpacityValue.getValue());
	}

	@Override
	public ParameterValue getCurrentValue(AnimationContext context)
	{
		for (Renderable renderable : getRenderableLayer().getRenderables())
		{
			if (renderable instanceof AbstractSurfaceShape)
			{
				return ParameterValueFactory.createParameterValue(this, ((AbstractSurfaceShape) renderable).getAttributes().getOutlineOpacity(), context.getCurrentFrame());
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

	@Override
	protected ParameterBase createParameter(AVList context, AnimationIOConstants constants)
	{
		Layer parameterLayer = (Layer)context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer, "No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		
		ShapeOutlineOpacityParameter result = new ShapeOutlineOpacityParameter();
		result.setLayer(parameterLayer);
		return result;
	}
	
	private RenderableLayer getRenderableLayer()
	{
		return ((RenderableLayer)getLayer());
	}

}
