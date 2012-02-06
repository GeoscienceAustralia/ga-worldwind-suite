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
