package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getOpacityParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link LayerParameter} that controls the opacity of an {@link AbstractLayer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@EditableParameter(bound=true, minValue=0.0, maxValue=1.0)
public class LayerOpacityParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 20100907L;

	/**
	 * Constructor.
	 */
	public LayerOpacityParameter(Animation animation, Layer layer)
	{
		super(getMessage(getOpacityParameterNameKey()), animation, layer);
		setDefaultValue(1.0);
	}

	/**
	 * Constructor for de-serialisation.
	 */
	private LayerOpacityParameter(){};

	@Override
	public Type getType()
	{
		return Type.OPACITY;
	}

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		ParameterValue opacityValue = getValueAtFrame(frame);
		
		applyValue(opacityValue.getValue());
	}
	
	@Override
	public void doApplyValue(double value)
	{
		getLayer().setOpacity(value);
	}

	@Override
	public ParameterValue getCurrentValue(AnimationContext context)
	{
		return ParameterValueFactory.createParameterValue(this, getLayer().getOpacity(), context.getCurrentFrame());
	}

	@Override
	protected ParameterBase createParameter(AVList context, AnimationIOConstants constants)
	{
		Layer parameterLayer = (Layer)context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer, "No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		
		LayerOpacityParameter result = new LayerOpacityParameter();
		result.setLayer(parameterLayer);
		return result;
	}

	
	
}
