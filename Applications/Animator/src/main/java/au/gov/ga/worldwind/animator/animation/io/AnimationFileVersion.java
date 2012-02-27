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
package au.gov.ga.worldwind.animator.animation.io;

import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * The set of supported animation file format versions
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum AnimationFileVersion
{
	VERSION010("1.0", new AnimationIOConstants.V1()),
	VERSION020("2.0", new AnimationIOConstants.V2());

	// The map of display names -> enums
	private static final Map<String, AnimationFileVersion> DISPLAY_NAME_MAP = new HashMap<String, AnimationFileVersion>();
	static
	{
		DISPLAY_NAME_MAP.put(VERSION010.getDisplayName(), VERSION010);
		DISPLAY_NAME_MAP.put(VERSION020.getDisplayName(), VERSION020);
	}
	
	/** The display name of this enum */
	private String displayName;

	/** The IO constants to use for this version */
	private AnimationIOConstants constants;
	
	private AnimationFileVersion(String display, AnimationIOConstants constants)
	{
		this.displayName = display;
		this.constants = constants;
	}
	
	/**
	 * @return The display name of this enum
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @return the constants used in files of this version
	 */
	public AnimationIOConstants getConstants()
	{
		return constants;
	}
	
	@Override
	public String toString()
	{
		return "Version " + getDisplayName();
	}
	
	/**
	 * Get the {@link AnimationFileVersion} instance with the provided display name
	 * 
	 * @param displayName The display name of the instance to lookup
	 * 
	 * @return the instance with the provided display name
	 */
	public static AnimationFileVersion fromDisplayName(String displayName)
	{
		Validate.notBlank(displayName, "A display name must be provided");
		return DISPLAY_NAME_MAP.get(displayName);
	}
}
