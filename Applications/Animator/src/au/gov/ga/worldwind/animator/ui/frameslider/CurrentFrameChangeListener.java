package au.gov.ga.worldwind.animator.ui.frameslider;

/**
 * An interface for listeners who want to detect changes to the slider's current frame.
 * <p/>
 * This can occur through user interaction (e.g. clicking or dragging), 
 * or via a programmatic setter invocation.
 */
public interface CurrentFrameChangeListener
{

	/**
	 * Respond to the new current frame
	 */
	void currentFrameChanged(int newCurrentFrame);
	
}
