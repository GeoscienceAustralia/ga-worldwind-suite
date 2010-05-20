package au.gov.ga.worldwind.panels.dataset;

import java.net.URL;

import au.gov.ga.worldwind.downloader.Downloader;
import au.gov.ga.worldwind.downloader.RetrievalResult;

public class LazyDataset extends Dataset implements ILazyDataset
{
	private final URL url;

	public LazyDataset(String name, URL url, URL descriptionURL, URL iconURL, boolean root)
	{
		super(name, descriptionURL, iconURL, root);
		this.url = url;
	}

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
	}
}
