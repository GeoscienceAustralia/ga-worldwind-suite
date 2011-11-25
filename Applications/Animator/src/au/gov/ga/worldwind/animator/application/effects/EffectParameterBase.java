package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.common.util.Validate;

public abstract class EffectParameterBase extends ParameterBase implements EffectParameter
{
	/** The effect this parameter is associated with */
	private Effect effect;

	/**
	 * @param name
	 *            The name of this parameter
	 * @param animation
	 *            The animation associated with this parameter
	 * @param effect
	 *            The effect associated with this parameter
	 */
	public EffectParameterBase(String name, Animation animation, Effect effect)
	{
		super(name, animation);
		Validate.notNull(effect, "An effect is required");
		this.effect = effect;
	}

	/**
	 * Constructor used for deserialization. Not for general consumption.
	 */
	protected EffectParameterBase()
	{
		super();
	}

	@Override
	public Effect getEffect()
	{
		return effect;
	}

	protected void setEffect(Effect effect)
	{
		this.effect = effect;
	}

	@Override
	public void apply()
	{
		int frame = animation.getCurrentFrame();
		ParameterValue value = getValueAtFrame(frame);
		applyValueIfEnabled(value.getValue(), frame);
	}
}
