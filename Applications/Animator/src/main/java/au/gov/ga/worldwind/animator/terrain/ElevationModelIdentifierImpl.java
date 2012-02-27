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
package au.gov.ga.worldwind.animator.terrain;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * The default implementation of the {@link ElevationModelIdentifier} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ElevationModelIdentifierImpl implements ElevationModelIdentifier
{

	private String name;
	private String location;
	
	public ElevationModelIdentifierImpl(String name, String location)
	{
		Validate.notBlank(name, "A name is required");
		Validate.notBlank(location, "A location is required");
		this.name = name;
		this.location = location;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		// Do nothing - required from Nameable
	}
	
	@Override
	public String getLocation()
	{
		return location;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof ElevationModelIdentifier &&
				location.equals(((ElevationModelIdentifier)obj).getLocation());
	}
	
	@Override
	public int hashCode()
	{
		return location.hashCode();
	}
	
}
