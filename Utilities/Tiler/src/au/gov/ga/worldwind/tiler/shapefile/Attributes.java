package au.gov.ga.worldwind.tiler.shapefile;

import com.vividsolutions.jump.feature.Feature;

public class Attributes
{
	private final Object[] attributes;

	public Attributes(Feature fromFeature)
	{
		attributes = fromFeature.getAttributes();
	}

	public Object[] getAttributes()
	{
		return attributes;
	}
}
