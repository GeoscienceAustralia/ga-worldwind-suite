package au.gov.ga.worldwind.common.layers;

/**
 * Indicates that an object can be set to be displayed as wireframe.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Wireframeable
{
	/**
	 * @return Is wireframe enabled for this object?
	 */
	boolean isWireframe();

	/**
	 * Enable/disable wireframe for this object
	 * 
	 * @param wireframe
	 */
	void setWireframe(boolean wireframe);
}
