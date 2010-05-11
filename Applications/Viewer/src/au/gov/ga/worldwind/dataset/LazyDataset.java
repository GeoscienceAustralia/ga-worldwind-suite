package au.gov.ga.worldwind.dataset;

import java.net.URL;

import au.gov.ga.worldwind.dataset.downloader.Downloader;
import au.gov.ga.worldwind.dataset.downloader.RetrievalResult;

public class LazyDataset extends Dataset implements ILazyDataset
{
	private URL url;

	public LazyDataset(String name, URL url, URL descriptionURL, URL iconURL)
	{
		super(name, descriptionURL, iconURL);
		this.url = url;
	}

	public void load() throws Exception
	{
		//download immediately, checking for modifications
		RetrievalResult result = Downloader.downloadImmediatelyIfModified(url);
		if (result != null)
		{
			IDataset dataset = DatasetReader.read(result.getAsInputStream());
			if (dataset != null)
			{
				getDatasets().addAll(dataset.getDatasets());
				getLayers().addAll(dataset.getLayers());
			}
		}
	}
}
