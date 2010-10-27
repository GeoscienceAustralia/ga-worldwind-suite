package au.gov.ga.worldwind.animator.application.render;

import java.io.File;

import au.gov.ga.worldwind.animator.animation.Animation;

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
	 * Render the provided animation.
	 * <p/>
	 * This should be done asynchronously, usually in a thread other than the one used to invoke the method.
	 * 
	 * @param animation The animation to render
	 * @param firstFrame The frame to start rendering at
	 * @param lastFrame The frame to stop rendering at
	 * @param outputDir The location to save rendered frames to
	 * @param frameName The name to give to rendered frames
	 * @param detailHint The detail hint to use for anything that uses one
	 * @param alpha Whether or not to render an alpha channel
	 */
	void render(Animation animation, int firstFrame, int lastFrame, File outputDir, String frameName, double detailHint, boolean alpha);
	
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
