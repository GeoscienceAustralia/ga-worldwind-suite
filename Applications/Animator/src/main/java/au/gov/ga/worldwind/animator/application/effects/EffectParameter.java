package au.gov.ga.worldwind.animator.application.effects;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface for parameters that control properties of an {@link Effect}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface EffectParameter extends Parameter
{
	/**
	 * @return The {@link Effect} this parameter is associated with
	 */
	Effect getEffect();
	
	/**
	 * Apply this parameter's state to it's associated {@link Effect} for the current frame
	 */
	void apply();
}
