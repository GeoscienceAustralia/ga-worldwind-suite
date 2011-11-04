package au.gov.ga.worldwind.animator.view;

/**
 * An interface for views that have a configurable clipping distance
 */
public interface ClipConfigurableView
{
	
	/**
	 * Sets whether or not to auto-calculate the near clipping distance. If <code>true</code>,
	 * values set previously via {@link #setNearClipDistance(double)} will be ignored and 
	 * the view will auto-calculate an appropriate value.
	 */
	void setAutoCalculateNearClipDistance(boolean autoCalculate);

	/**
	 * Sets the near clipping distance. Calling this method will disable near clip auto-calculation in the same way as 
	 * calling {@link #setAutoCalculateNearClipDistance(boolean)} with <code>false</code>.
	 * 
	 * @param nearClip the near clipping distance to set. If this is >= <code>farClip</code>, <code>farClip</code> will be set to <code>nearClip</code>+1.
	 */
	void setNearClipDistance(double nearClip);
	
	/**
	 * Sets whether or not to auto-calculate the far clipping distance. If <code>true</code>,
	 * values set previously via {@link #setFarClipDistance(double)} will be ignored and 
	 * the view will auto-calculate an appropriate value.
	 */
	void setAutoCalculateFarClipDistance(boolean autoCalculate);

	/**
	 * Sets the near clipping distance. Calling this method will disable near clip auto-calculation in the same way as 
	 * calling {@link #setAutoCalculateNearClipDistance(boolean)} with <code>false</code>.
	 * 
	 * @param farClip the far clipping distance to set. If this is <= <code>nearClip</code>, <code>nearClip</code> will be set to <code>farClip</code>-1.
	 */
	void setFarClipDistance(double farClip);
	
}
