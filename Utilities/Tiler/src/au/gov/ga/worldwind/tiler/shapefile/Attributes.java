package au.gov.ga.worldwind.tiler.shapefile;

import gistoolkit.features.AttributeType;
import gistoolkit.features.Record;

public class Attributes
{
	private final AttributeType[] attributeTypes;
	private final String[] attributeNames;
	private final Object[] attributes;

	public Attributes(Record fromRecord)
	{
		attributeTypes = fromRecord.getAttributeTypes();
		attributeNames = fromRecord.getAttributeNames();
		attributes = fromRecord.getAttributes();
	}

	public AttributeType[] getAttributeTypes()
	{
		return attributeTypes;
	}

	public String[] getAttributeNames()
	{
		return attributeNames;
	}

	public Object[] getAttributes()
	{
		return attributes;
	}
}
