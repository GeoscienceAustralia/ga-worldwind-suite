package au.gov.ga.worldwind.animator.layers.immediate;

/**
 * Helper class that stores whether immediate mode is enabled or not. Immediate
 * mode is switched on by the animator when it begins rendering an animation. It
 * causes layers to download and load textures immediately, instead of passing
 * the request off to the task service. This ensures that the highest resolution
 * imagery and elevation data is available when rendering each frame.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ImmediateMode
{
	private static boolean immediate = false;

	/**
	 * @return Is immediate mode switched on?
	 */
	public static boolean isImmediate()
	{
		return immediate;
	}

	/**
	 * Set immediate mode on/off
	 * 
	 * @param immediate
	 */
	public static void setImmediate(boolean immediate)
	{
		ImmediateMode.immediate = immediate;
	}
}
