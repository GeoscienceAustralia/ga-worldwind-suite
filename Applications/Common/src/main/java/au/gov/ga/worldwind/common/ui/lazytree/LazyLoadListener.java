package au.gov.ga.worldwind.common.ui.lazytree;

/**
 * The listener interface for receiving lazy load events from the
 * {@link LazyTree}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface LazyLoadListener
{
	/**
	 * Called when the given object has been loaded.
	 * 
	 * @param object
	 */
	public void loaded(ILazyTreeObject object);
}
