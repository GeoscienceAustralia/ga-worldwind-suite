package au.gov.ga.worldwind.tiler.shapefile;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

public class Attributes
{
	private final Map<String, Object> attributeMap = new HashMap<String, Object>();

	public void loadAttributes(Feature source, FeatureSchema destinationSchema)
	{
		for (int i = 0; i < destinationSchema.getAttributeCount(); i++)
		{
			if (destinationSchema.getAttributeType(i) != AttributeType.GEOMETRY)
			{
				String name = destinationSchema.getAttributeName(i);
				Object attribute = source.getAttribute(name);
				attributeMap.put(name, attribute);
			}
		}
	}

	public void saveAttributes(Feature destination)
	{
		for (Entry<String, Object> entry : attributeMap.entrySet())
		{
			destination.setAttribute(entry.getKey(), entry.getValue());
		}
	}
}
