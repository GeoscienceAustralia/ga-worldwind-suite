package au.gov.ga.worldwind.common.util;

import java.awt.Color;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ColorMap extends TreeMap<Double, Color>
{
	private boolean interpolateHue = true;

	public boolean isInterpolateHue()
	{
		return interpolateHue;
	}

	public void setInterpolateHue(boolean interpolateHue)
	{
		this.interpolateHue = interpolateHue;
	}
	
	public Color calculateColor(double value)
	{
		Entry<Double, Color> lessEntry = floorEntry(value);
		Entry<Double, Color> greaterEntry = ceilingEntry(value);
		double mixer = 0;
		if (lessEntry != null && greaterEntry != null)
		{
			double window = greaterEntry.getKey() - lessEntry.getKey();
			if (window > 0)
			{
				mixer = (value - lessEntry.getKey()) / window;
			}
		}
		Color color0 = lessEntry == null ? null : lessEntry.getValue();
		Color color1 = greaterEntry == null ? null : greaterEntry.getValue();
		return Util.interpolateColor(color0, color1, mixer, interpolateHue);
	}
}
