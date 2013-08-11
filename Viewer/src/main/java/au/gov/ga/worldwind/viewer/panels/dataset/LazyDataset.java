/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.viewer.panels.dataset;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.downloader.Downloader;
import au.gov.ga.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.worldwind.common.ui.lazytree.LazyLoadListener;

/**
 * Basic implementation of the {@link ILazyDataset} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyDataset extends Dataset implements ILazyDataset
{
	private final URL url;
	private final List<LazyLoadListener> listeners = new ArrayList<LazyLoadListener>();
	private boolean loaded = false;

	public LazyDataset(String name, URL url, URL infoURL, URL iconURL, boolean base)
	{
		super(name, infoURL, iconURL, base);
		this.url = url;
	}
	
	@Override
	public boolean isLoaded()
	{
		return loaded;
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
		
		loaded = true;
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
