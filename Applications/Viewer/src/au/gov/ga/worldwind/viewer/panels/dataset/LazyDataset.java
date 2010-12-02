package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadListener;

public class LazyDataset extends Dataset implements ILazyDataset
{
	private final URL url;
	private final List<LazyLoadListener> listeners = new ArrayList<LazyLoadListener>();

	public LazyDataset(String name, URL url, URL infoURL, URL iconURL, boolean base)
	{
		super(name, infoURL, iconURL, base);
		this.url = url;
	}

	@Override
	public void load() throws Exception
	{
		//download immediately, checking for modifications
		RetrievalResult result = Downloader.downloadImmediatelyIfModified(url, true);
		if (result.getError() != null)
			throw result.getError();

		if (result != null)
		{
			IDataset dataset = DatasetReader.read(result.getAsInputStream(), url);
			if (dataset != null)
			{
				List<IData> children = dataset.getChildren();
				for (IData child : children)
					addChild(child);
			}
		}

		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).loaded(this);
	}

	@Override
	public void addListener(LazyLoadListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(LazyLoadListener listener)
	{
		listeners.remove(listener);
	}
}
