/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import java.util.Collection;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An animation.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Animation
{

	/**
	 * Returns the list of all key frames associated with this animation, ordered by
	 * frame.
	 * 
	 * @return All key frames, ordered by frame.
	 */
	List<KeyFrame> getKeyFrames();
	
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
	 * @return A collection of all parameters associated with this animation
	 */
	Collection<Parameter> getAllParameters();
	
	/**
	 * @return A collection of all parameters associated with this animation that are currently enabled
	 */
	Collection<Parameter> getEnabledParameters();
	
	/**
	 * @return A collection of all objects being animated in this animation
	 */
	Collection<Animatable> getAnimatableObjects();
	
	/**
	 * @return The number of frames in this animation
	 */
	int getFrameCount();
	
	/**
	 * Set the frame count of this animation.
	 * <p/>
	 * If this will result in a decrease in the frame count of the animation, any key frames at a 
	 * frame > <code>newCount</code> will be lost.
	 * 
	 * @param newCount The new frame count to set.
	 */
	void setFrameCount(int newCount);
	
	/**
	 * Apply this animation's state at the given frame to the 'world'
	 * 
	 * @param frame The frame at which the state of the animation is to be applied
	 * 
	 * @throws IllegalArgumentException if <code>frame</code> is less than 0 or greater than <code>frameCount</code>
	 */
	void applyFrame(int frame);
	
	/**
	 * Record a key frame at the given frame with the current value of all <em>enabled</em> parameters.
	 * <p/>
	 * If there are no enabled parameters, no key frame will be created.
	 * <p/>
	 * Equivalent to the call <code>recordKeyFrame(frame, getEnabledParameters());</code>
	 * 
	 * @param frame The frame to record the key frame at
	 */
	void recordKeyFrame(int frame);
	
	/**
	 * Record a key frame at the given frame with the current value of the parameters 
	 * provided in the given collection.
	 * <p/>
	 * If the provided list of parameters is empty, no key frame will be created.
	 * 
	 * @param frame The frame to record the key frame at
	 * @param parameters The collection of parameters to record on the key frame
	 */
	void recordKeyFrame(int frame, Collection<Parameter> parameters);
	
	/**
	 * @return The render parameters for this animation.
	 */
	RenderParameters getRenderParameters();
	
	/**
	 * @return Whether zoom scaling should be applied in this animation
	 */
	boolean isZoomScalingRequired();
}
