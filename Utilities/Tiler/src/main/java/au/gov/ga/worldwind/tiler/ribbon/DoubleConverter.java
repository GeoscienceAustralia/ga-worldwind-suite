package au.gov.ga.worldwind.tiler.ribbon;

import com.beust.jcommander.IStringConverter;

/**
 * {@link IStringConverter} implementation for converting strings to doubles.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DoubleConverter implements IStringConverter<Double>
{
	@Override
	public Double convert(String s)
	{
		return Double.parseDouble(s);
	}
}
