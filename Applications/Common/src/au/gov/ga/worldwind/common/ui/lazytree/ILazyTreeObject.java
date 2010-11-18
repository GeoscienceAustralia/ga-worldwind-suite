package au.gov.ga.worldwind.common.ui.lazytree;

public interface ILazyTreeObject extends ITreeObject
{
	/**
	 * This method is called by the lazy tree when this node is expanded for the
	 * first time. Subclasses should use this method to load their own children.
	 * If a known error occurs, this method should throw a LazyLoadException,
	 * which will display the exception's message as a child node.
	 * 
	 * @throws Exception
	 *             If loading the children fails for some reason
	 */
	public void load() throws Exception;

	public void addListener(LazyLoadListener listener);

	public void removeListener(LazyLoadListener listener);
}
