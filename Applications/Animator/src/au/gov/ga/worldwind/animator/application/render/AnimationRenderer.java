package au.gov.ga.worldwind.animator.application.render;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.RenderParameters;

/**
 * An interface for animation renderers
 * <p/>
 * Provides a mechanism to start and stop rendering.
 */
public interface AnimationRenderer
{

	/** Stop the current render */
	void stop();
	
	/**
	 * Render the provided animation using the settings stored in the animation {@link RenderParameters}.
	 */
	void render(Animation animation);
	
	/**
	 * Render the provided animation using the settings in the provided {@link RenderParameters}.
	 */
	void render(Animation animation, RenderParameters renderParams);
	
	/**
	 * @return Whether the rendering is complete yet
	 */
	boolean isDone();
	
	/**
	 * @return A value in the range [0,1] indicating the percentage of the rendering that is complete
	 */
	double getPercentComplete();
	
	/**
	 * Add a render event listener to this renderer
	 */
	void addListener(RenderEventListener listener);
	
	/**
	 * Remove a render event listener from this renderer
	 */
	void removeListener(RenderEventListener listener);
	
	/**
	 * An interface for listeners that want to be notified of render events
	 */
	public static interface RenderEventListener
	{
		/** Notified when rendering begins */
		void started();
		
		/** Notified when a frame is begun rendering */
		void startingFrame(int frame);
		
		/** Notified when a frame is finished rendering */
		void finishedFrame(int frame);
		
		/** Notified when the rendering is aborted by an external agent */
		void stopped(int frame);
		
		/** Notified when rendering is completed */
		void completed();
	}
}
