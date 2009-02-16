package au.gov.ga.worldwind.geonames;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FeatureClass
{
	public final Map<String, FeatureCode> featureCodes = new HashMap<String, FeatureCode>();
	public final String code;
	public final String description;
	public final Color color;
	public final Font font;

	public FeatureClass(String code, String description, Font font, Color color)
	{
		this.code = code;
		this.description = description;
		this.color = color;
		this.font = font;
	}

	public void add(FeatureCode featureCode)
	{
		featureCodes.put(featureCode.code, featureCode);
	}

	@Override
	public int hashCode()
	{
		return code.hashCode();
	}

	public FeatureCode getCode(String featureCode)
	{
		return featureCodes.get(featureCode);
	}
	
	public Collection<FeatureCode> getCodes()
	{
		return featureCodes.values();
	}
}
