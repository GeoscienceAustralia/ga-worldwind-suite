package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.components.lazytree.LazyLoadListener;
import au.gov.ga.worldwind.downloader.Downloader;
import au.gov.ga.worldwind.downloader.RetrievalResult;

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
		RetrievalResult result = Downloader.downloadImmediatelyIfModified(url);
		if (result.getError() != null)
			throw result.getError();

		if (result != null)
		{
			IDataset dataset = DatasetReader.read(result.getAsInputStream(), url);
			if (dataset != null)
			{
				getDatasets().addAll(dataset.getDatasets());
				getLayers().addAll(dataset.getLayers());
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
