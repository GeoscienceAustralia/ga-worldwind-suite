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
