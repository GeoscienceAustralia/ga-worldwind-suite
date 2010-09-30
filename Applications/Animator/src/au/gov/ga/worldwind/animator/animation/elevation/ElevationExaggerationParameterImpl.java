package au.gov.ga.worldwind.animator.animation.elevation;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getElevationExaggerationNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.terrain.exaggeration.ElevationExaggeration;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * Default implementation of the {@link ElevationExaggerationParameter} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
@EditableParameter(bound=true, minValue=0, maxValue=Double.MAX_VALUE, units="x")
public class ElevationExaggerationParameterImpl extends ParameterBase implements ElevationExaggerationParameter
{
	private static final long serialVersionUID = 2010L;

	private static final String DEFAULT_NAME = "Exaggeration";
	
	private ElevationExaggeration exaggerator;
	
	public ElevationExaggerationParameterImpl(Animation animation, ElevationExaggeration exaggerator)
	{
		super(getName(exaggerator), animation);
		
		Validate.notNull(exaggerator, "An exaggerator is required");
		this.exaggerator = exaggerator;
		
		setDefaultValue(exaggerator.getExaggeration());
	}
	
	private static String getName(ElevationExaggeration exaggerator)
	{
		if (getMessage(getElevationExaggerationNameKey()) == null)
		{
			return DEFAULT_NAME;
		}
		return getMessage(getElevationExaggerationNameKey(), exaggerator.getElevationBoundary());
	}
	
	@Override
	public ParameterValue getCurrentValue(AnimationContext context)
	{
		return ParameterValueFactory.createParameterValue(this, exaggerator.getExaggeration(), context.getCurrentFrame());
	}

	@Override
	public ElevationExaggeration getElevationExaggeration()
	{
		return exaggerator;
	}

	@Override
	public void apply(AnimationContext animationContext, int frame)
	{
		if (!isEnabled())
		{
			return;
		}
		ParameterValue value = getValueAtFrame(animationContext, frame);
		applyValue(value.getValue());
	}
	
	@Override
	protected void doApplyValue(double value)
	{
		exaggerator.setExaggeration(value);
	}

	@Override
	protected ParameterBase createParameter(AVList context)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getElevationExaggerationName());
		WWXML.setDoubleAttribute(result, constants.getElevationExaggerationAttributeBoundary(), exaggerator.getElevationBoundary());
		
		result.appendChild(super.toXml(result, version));
		
		return result;
	}
	
}
