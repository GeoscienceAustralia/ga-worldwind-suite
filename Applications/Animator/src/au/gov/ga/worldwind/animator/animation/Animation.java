/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An animation.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface Animation
{

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
	
}
