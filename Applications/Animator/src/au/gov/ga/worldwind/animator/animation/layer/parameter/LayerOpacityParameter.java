package au.gov.ga.worldwind.animator.animation.layer.parameter;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * A {@link LayerParameter} that controls the opacity of an {@link AbstractLayer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerOpacityParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 20100907L;

	/**
	 * Constructor.
	 */
	public LayerOpacityParameter(Animation animation, Layer layer)
	{
		super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getOpacityParameterNameKey()), animation, layer);
		setDefaultValue(1.0);
	}

	/**
	 * Constructor for de-serialisation.
	 */
	private LayerOpacityParameter(Layer layer)
	{
		super(layer);
	}

	@Override
	public Type getType()
	{
		return Type.OPACITY;
	}

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		ParameterValue opacityValue = getValueAtFrame(animationContext, frame);
		
		getLayer().setOpacity(opacityValue.getValue());
	}

	@Override
	public ParameterValue getCurrentValue(AnimationContext context)
	{
		return ParameterValueFactory.createParameterValue(this, getLayer().getOpacity(), context.getCurrentFrame());
	}

	@Override
	protected ParameterBase createParameter(AVList context)
	{
		AnimationIOConstants constants = XmlAnimationWriter.getCurrentFileVersion().getConstants();
		AbstractLayer parameterLayer = (AbstractLayer)context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer, "No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		
		return new LayerOpacityParameter(parameterLayer);
	}

	
	
}
