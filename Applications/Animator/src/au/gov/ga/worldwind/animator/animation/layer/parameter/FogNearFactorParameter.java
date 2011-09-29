package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getFogNearParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.io.XmlAnimationWriter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A layer parameter controlling the near factor of a {@link FogLayer}
 */
@EditableParameter
public class FogNearFactorParameter extends LayerParameterBase
{

	private static final long serialVersionUID = 1L;

	public FogNearFactorParameter(Animation animation, FogLayer layer)
	{
		super(getMessage(getFogNearParameterNameKey()), animation, layer);
		setDefaultValue(layer.getNearFactor());
	}

	public FogNearFactorParameter()
	{
		super();
	}

	@Override
	public Type getType()
	{
		return Type.NEAR;
	}

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		ParameterValue nearFactorValue = getValueAtFrame(frame);
		
		applyValue(nearFactorValue.getValue());
	}

	@Override
	public ParameterValue getCurrentValue(AnimationContext context)
	{
		return ParameterValueFactory.createParameterValue(this, ((FogLayer)getLayer()).getNearFactor(), context.getCurrentFrame());
	}

	@Override
	protected void doApplyValue(double value)
	{
		((FogLayer)getLayer()).setNearFactor((float)value);
	}

	@Override
	protected ParameterBase createParameter(AVList context)
	{
		AnimationIOConstants constants = XmlAnimationWriter.getCurrentFileVersion().getConstants();
		Layer parameterLayer = (Layer)context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer, "No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		
		FogNearFactorParameter result = new FogNearFactorParameter();
		result.setLayer(parameterLayer);
		return result;
	}

}
