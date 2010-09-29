package au.gov.ga.worldwind.animator.animation.elevation;

import gov.nasa.worldwind.avlist.AVList;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
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
@EditableParameter(bound=true, minValue=0, maxValue=Double.MAX_VALUE)
public class ElevationExaggerationParameterImpl extends ParameterBase implements ElevationExaggerationParameter
{
	private static final long serialVersionUID = 2010L;
	
	private ElevationExaggeration exaggerator;
	
	public ElevationExaggerationParameterImpl(ElevationExaggeration exaggerator)
	{
		Validate.notNull(exaggerator, "An exaggerator is required");
		this.exaggerator = exaggerator;
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

	
}
