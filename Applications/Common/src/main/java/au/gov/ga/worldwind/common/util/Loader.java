package au.gov.ga.worldwind.common.util;

import java.util.ArrayList;

public interface Loader
{
	boolean isLoading();

	void addLoadingListener(LoadingListener listener);

	void removeLoadingListener(LoadingListener listener);

	public static interface LoadingListener
	{
		void loadingStateChanged(boolean isLoading);
	}

	public static class LoadingListenerList extends ArrayList<LoadingListener>
	{
		public void notifyListeners(boolean isLoading)
		{
			for (int i = size() - 1; i >= 0; i--)
				get(i).loadingStateChanged(isLoading);
		}
	}
}
