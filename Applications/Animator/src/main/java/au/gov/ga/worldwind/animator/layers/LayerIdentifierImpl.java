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
package au.gov.ga.worldwind.animator.layers;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * Default (immutable) implementation of the {@link LayerIdentifier} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayerIdentifierImpl implements LayerIdentifier
{
	
	private String name;
	private String location;

	public LayerIdentifierImpl(String name, String location)
	{
		Validate.notBlank(name, "A layer name is required");
		Validate.notBlank(location, "A layer location is required");
		
		this.name = name;
		this.location = location;
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getLocation()
	{
		return location;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof LayerIdentifier &&
				location.equals(((LayerIdentifier)obj).getLocation());
	}
	
	@Override
	public int hashCode()
	{
		return location.hashCode();
	}
	
}
