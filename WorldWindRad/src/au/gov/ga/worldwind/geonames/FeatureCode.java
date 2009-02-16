package au.gov.ga.worldwind.geonames;

public class FeatureCode
{
	public final String code;
	public final String name;
	public final String description;

	public FeatureCode(String code, String name)
	{
		this(code, name, name);
	}

	public FeatureCode(String code, String name, String description)
	{
		this.code = code;
		this.name = name;
		this.description = description;
	}

	@Override
	public int hashCode()
	{
		return code.hashCode();
	}
}
