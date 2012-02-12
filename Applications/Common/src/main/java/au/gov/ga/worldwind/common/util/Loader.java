package au.gov.ga.worldwind.common.util;

import java.util.ArrayList;

/**
 * Represents any object that loads data. Used to display loading indicators to
 * the user.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Loader
{
	/**
	 * @return Is this object loading its data?
	 */
	boolean isLoading();

	/**
	 * Add a listener to listen for load events.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	void addLoadingListener(LoadingListener listener);

	/**
	 * Remove a loading listener.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	void removeLoadingListener(LoadingListener listener);

	/**
	 * The listener interface for receiving load events.
	 */
	public static interface LoadingListener
	{
		void loadingStateChanged(boolean isLoading);
	}

	/**
	 * Simple helper class for creating lists of listeners and notifying them.
	 */
	public static class LoadingListenerList extends ArrayList<LoadingListener>
	{
		public void notifyListeners(boolean isLoading)
		{
			for (int i = size() - 1; i >= 0; i--)
				get(i).loadingStateChanged(isLoading);
		}
	}
}
