/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.View;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * A context that holds information about an animation, and the 'world' in which the 
 * animation is to applied.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationContext
{
	// TODO: Give me some useful things!
	
	/**
	 * Returns the first {@link KeyFrame} <em>before</em> the provided frame that 
	 * contains a value for the provided {@link Parameter}.
	 * <p/>
	 * If (a) there are no {@link KeyFrame}s before the provided frame, 
	 * or (b) there are no {@link KeyFrame}s with a value recorded for the
	 * provided parameter before the provided frame, returns <code>null</code>.
	 * 
	 * @return the first {@link KeyFrame} <em>before</em> the provided frame that 
	 * contains a value for the provided {@link Parameter}, or <code>null</code> if
	 * one cannot be found
	 */
	KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame);
	
	/**
	 * Returns the first {@link KeyFrame} <em>after</em> the provided frame that 
	 * contains a value for the provided {@link Parameter}.
	 * <p/>
	 * If (a) there are no {@link KeyFrame}s after the provided frame, 
	 * or (b) there are no {@link KeyFrame}s with a value recorded for the
	 * provided parameter after the provided frame, returns <code>null</code>.
	 * 
	 * @return the first {@link KeyFrame} <em>after</em> the provided frame that 
	 * contains a value for the provided {@link Parameter}, or <code>null</code> if
	 * one cannot be found
	 */
	KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame);
	
	/**
	 * Returns the WorldWind {@link View} being used in the animation.
	 * 
	 * @return The view being used in the animation
	 */
	View getView();
	
	/**
	 * Applies zoom scaling to the provided un-zoomed value, as appropriate.
	 * <p/>
	 * If the animation is configured not to apply scaling, the returned
	 * value will be the same as the input.
	 * <p/>
	 * Otherwise the result will be a scaled version of the input.
	 * 
	 * @return the unzoomed value scaled according to the animation's current settings
	 */
	double applyZoomScaling(double unzoomed);
	
	/**
	 * Un-applies zoom scaling to the provided zoomed value, as appropriate.
	 * <p/>
	 * If the animation is configured not to apply scaling, the returned
	 * value will be the same as the input.
	 * <p/>
	 * Otherwise the result will be a scaled version of the input.
	 * 
	 * @return the zoom scaled according to the animation's current settings
	 */
	double unapplyZoomScaling(double zoomed);
}
