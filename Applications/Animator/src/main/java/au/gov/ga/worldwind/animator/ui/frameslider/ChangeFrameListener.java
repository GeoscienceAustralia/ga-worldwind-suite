package au.gov.ga.worldwind.animator.ui.frameslider;

/**
 * A listener interface that allows clients to listen for changes to frames in the {@link FrameSlider}.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public interface ChangeFrameListener
{
	/**
	 * Invoked when a frame has been changed on the slider. Usually this occurs
	 * when a key frame has been dragged-and-dropped.
	 * 
	 * @param index The index of the key frame in question
	 * @param oldFrame The previous frame of the key frame
	 * @param newFrame The new frame of the key frame
	 */
	public void frameChanged(int index, int oldFrame, int newFrame);
}
