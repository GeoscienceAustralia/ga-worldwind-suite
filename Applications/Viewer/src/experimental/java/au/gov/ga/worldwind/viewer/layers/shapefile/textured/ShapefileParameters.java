package au.gov.ga.worldwind.viewer.layers.shapefile.textured;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShapefileParameters
{
	private String colorIdentifierAttribute;
	private final Map<Object, Color> colorLookup = new HashMap<Object, Color>();

	private String shapeIdentifierAttribute;
	private final Map<Object, Color> pickingColorMap = new HashMap<Object, Color>();
	private final Set<Color> pickingColorSet = new HashSet<Color>();

	private int uniquePickNumber = 0;

	public String getColorIdentifierAttribute()
	{
		return colorIdentifierAttribute;
	}

	public void setColorIdentifierAttribute(String colorIdentifierAttribute)
	{
		this.colorIdentifierAttribute = colorIdentifierAttribute;
	}

	public Color getShapeColor(Object colorIdentifier)
	{
		return colorLookup.get(colorIdentifier);
	}

	public String getShapeIdentifierAttribute()
	{
		return shapeIdentifierAttribute;
	}

	public void setShapeIdentifierAttribute(String shapeIdentifierAttribute)
	{
		this.shapeIdentifierAttribute = shapeIdentifierAttribute;
	}

	public Color getPickingColor(Object shapeIdentifier)
	{
		if (pickingColorMap.containsKey(shapeIdentifier))
			return pickingColorMap.get(shapeIdentifier);

		//if more than 2^24 unique shapes, this will be an infinite loop (extremely unlikely)
		Color c;
		do
		{
			c = getUniquePickColor();
		} while (pickingColorSet.contains(c));

		pickingColorMap.put(shapeIdentifier, c);
		pickingColorSet.add(c);

		return c;
	}

	protected Color getUniquePickColor()
	{
		uniquePickNumber++;

		if (uniquePickNumber >= 0x00FFFFFF)
			uniquePickNumber = 1; // no black, no white

		return new Color(uniquePickNumber, true); // has alpha
	}
}
