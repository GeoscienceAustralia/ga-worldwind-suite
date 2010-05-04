package au.gov.ga.worldwind.dataset;

import java.net.URL;

public class LazyDataset extends Dataset implements ILazyDataset
{
	private URL url;

	public LazyDataset(String name, URL url)
	{
		super(name);
		this.url = url;
	}

	@Override
	public void load() throws Exception
	{
		IDataset dataset = DatasetReader.read(url);
		if (dataset != null)
		{
			getDatasets().addAll(dataset.getDatasets());
			getLayers().addAll(dataset.getLayers());
		}
	}
}
