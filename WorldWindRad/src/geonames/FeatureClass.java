package geonames;

import java.util.ArrayList;
import java.util.List;

public class FeatureClass
{
	public final List<FeatureCode> featureCodes = new ArrayList<FeatureCode>();
	public final String code;
	public final String description;

	public FeatureClass(String code, String description)
	{
		this.code = code;
		this.description = description;
	}
	
	public void add(FeatureCode featureCode)
	{
		featureCodes.add(featureCode);
	}
}
