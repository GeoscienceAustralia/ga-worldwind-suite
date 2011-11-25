package au.gov.ga.worldwind.animator.application.effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.common.util.Validate;

public abstract class EffectBase extends AnimatableBase implements Effect
{
	private final List<Parameter> parameters = new ArrayList<Parameter>();

	@Override
	public Collection<Parameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public void addParameter(EffectParameter parameter)
	{
		if (parameter == null)
		{
			return;
		}
		Validate.isTrue(this.equals(parameter.getEffect()), "Parameter is not linked to the correct layer. Expected '"
				+ this + "'.");
		parameters.add(parameter);

		parameter.addChangeListener(this);
	}

	@Override
	protected void doApply()
	{
		for (Parameter parameter : parameters)
		{
			Validate.isTrue(parameter instanceof EffectParameter, "Incorrect Parameter type"); //should never occur
			((EffectParameter) parameter).apply();
		}
	}
}
