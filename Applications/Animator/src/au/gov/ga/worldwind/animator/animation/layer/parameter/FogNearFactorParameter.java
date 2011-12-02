package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getFogNearParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.FogLayer;
import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A layer parameter controlling the near factor of a {@link FogLayer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@EditableParameter
public class FogNearFactorParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 1L;

	public FogNearFactorParameter(Animation animation, FogLayer layer)
	{
		this(null, animation, layer);
	}

	public FogNearFactorParameter(String name, Animation animation, FogLayer layer)
	{
		super(name, animation, layer);
		setDefaultValue(layer.getNearFactor());
	}

	@SuppressWarnings("unused")
	private FogNearFactorParameter()
	{
	}
	
	@Override
	protected String getDefaultName()
	{
		return getMessage(getFogNearParameterNameKey());
	}

	@Override
	public Type getType()
	{
		return Type.NEAR;
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return ParameterValueFactory.createParameterValue(this, ((FogLayer) getLayer()).getNearFactor(),
				animation.getCurrentFrame());
	}

	@Override
	protected void doApplyValue(double value)
	{
		((FogLayer) getLayer()).setNearFactor((float) value);
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		Layer parameterLayer = (Layer) context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer,
				"No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");
		Validate.isTrue(parameterLayer instanceof FogLayer, "Layer found in context is incorrect type: '"
				+ parameterLayer.getClass().getCanonicalName() + "'");

		return new FogNearFactorParameter(name, animation, (FogLayer) parameterLayer);
	}

}
