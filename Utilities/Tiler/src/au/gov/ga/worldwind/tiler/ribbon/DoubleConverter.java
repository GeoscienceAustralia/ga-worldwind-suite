package au.gov.ga.worldwind.tiler.ribbon;

import com.beust.jcommander.IStringConverter;

public class DoubleConverter implements IStringConverter<Double>
{

	@Override
	public Double convert(String s)
	{
		return Double.parseDouble(s);
	}

}
