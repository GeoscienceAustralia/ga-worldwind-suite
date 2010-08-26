package au.gov.ga.worldwind.animator.animation.io;

import java.util.HashMap;
import java.util.Map;

import au.gov.ga.worldwind.animator.util.Validate;

/**
 * The set of supported animation file format versions
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum AnimationFileVersion
{
	VERSION010("1.0"),
	VERSION020("2.0");

	// The map of display names -> enums
	private static final Map<String, AnimationFileVersion> DISPLAY_NAME_MAP = new HashMap<String, AnimationFileVersion>();
	static
	{
		DISPLAY_NAME_MAP.put(VERSION010.getDisplayName(), VERSION010);
		DISPLAY_NAME_MAP.put(VERSION020.getDisplayName(), VERSION020);
	}
	
	/** The display name of this enum */
	private String displayName;

	private AnimationFileVersion(String display)
	{
		this.displayName = display;
	}
	
	/**
	 * @return The display name of this enum
	 */
	public String getDisplayName()
	{
		return displayName;
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
	
	@Override
	public String toString()
	{
		return "Version " + getDisplayName();
	}
}
