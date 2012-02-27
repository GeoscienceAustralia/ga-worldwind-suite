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
package au.gov.ga.worldwind.wmsbrowser.wmsserver;

import java.net.URL;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * Default implementation of the {@link WmsServerIdentifier} interface
 */
public class WmsServerIdentifierImpl implements WmsServerIdentifier
{
	private String name;
	private URL capabilitiesUrl;
	
	public WmsServerIdentifierImpl(String name, URL capabilitiesUrl)
	{
		Validate.notNull(capabilitiesUrl, "A capabilities URL must be provided");
		this.capabilitiesUrl = capabilitiesUrl;
		
		if (Util.isBlank(name))
		{
			this.name = capabilitiesUrl.toExternalForm();
		}
		else
		{
			this.name = name;
		}
	}
	
	public WmsServerIdentifierImpl(URL capabilitiesUrl)
	{
		this(null, capabilitiesUrl);
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getCapabilitiesUrl()
	{
		return capabilitiesUrl;
	}
	
	@Override
	public int hashCode()
	{
		return capabilitiesUrl.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof WmsServerIdentifier))
		{
			return false;
		}
		
		return ((WmsServerIdentifier)obj).getCapabilitiesUrl().equals(this.getCapabilitiesUrl());
	}

}
