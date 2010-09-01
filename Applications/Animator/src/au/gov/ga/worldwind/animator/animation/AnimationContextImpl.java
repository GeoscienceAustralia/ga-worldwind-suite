/**
 * 
 */
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.View;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The default implementation of the {@link AnimationContext} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AnimationContextImpl implements AnimationContext
{

	/** The animation this context is associated with */
	private Animation animation;
	
	/**
	 * Constructor. Initialses the mandatory fields.
	 * 
	 * @param animation The animation this context is associated with
	 */
	public AnimationContextImpl(Animation animation)
	{
		Validate.notNull(animation, "An animation instance is required");
		this.animation = animation;
	}
	
	@Override
	public KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame)
	{
		return animation.getKeyFrameWithParameterBeforeFrame(p, frame);
	}

	@Override
	public KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame)
	{
		return animation.getKeyFrameWithParameterAfterFrame(p, frame);
	}

	@Override
	public View getView()
	{
		return ((WorldWindAnimationImpl)animation).getWorldWindow().getView();
	}
	
	@Override
	public double applyZoomScaling(double unzoomed)
	{
		return animation.applyZoomScaling(unzoomed);
	}
	
	@Override
	public double unapplyZoomScaling(double zoomed)
	{
		return animation.unapplyZoomScaling(zoomed);
	}
	
	@Override
	public int getCurrentFrame()
	{
		return animation.getCurrentFrame();
	}
	
}
