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

/**
 * Abstract implementation of the {@link IData} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractData extends AbstractIconItem implements IData
{
	private String name;
	private URL infoURL;
	private boolean base;

	public AbstractData(String name, URL infoURL, URL iconURL, boolean base)
	{
		super(iconURL);
		this.name = name;
		this.infoURL = infoURL;
		this.base = base;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getInfoURL()
	{
		return infoURL;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public boolean isBase()
	{
		return base;
	}
}
