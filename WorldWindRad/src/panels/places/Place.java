package panels.places;

import java.awt.Color;
import java.awt.Font;

import geonames.FeatureClass;
import geonames.FeatureCode;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;

public class Place implements GeographicText
{
	public final String name;
	public final String country;
	public final int id;
	public final LatLon latlon;
	public final FeatureClass featureClass;
	public final FeatureCode featureCode;
	
	public String[] parents;

	public Place(String name, String country, int id, LatLon latlon, FeatureClass featureClass,
			FeatureCode featureCode)
	{
		this.name = name;
		this.country = country;
		this.id = id;
		this.latlon = latlon;
		this.featureClass = featureClass;
		this.featureCode = featureCode;
	}

	public Color getBackgroundColor()
	{
		return Color.black;
	}

	public Color getColor()
	{
		return featureClass.color;
	}

	public Font getFont()
	{
		return featureClass.font;
	}

	public Position getPosition()
	{
		return new Position(latlon, 0);
	}

	public CharSequence getText()
	{
		return name;
	}

	public boolean isVisible()
	{
		return true;
	}

	public void setBackgroundColor(Color background)
	{
	}

	public void setColor(Color color)
	{
	}

	public void setFont(Font font)
	{
	}

	public void setPosition(Position position)
	{
	}

	public void setText(CharSequence text)
	{
	}

	public void setVisible(boolean visible)
	{
	}

	@Override
	public String toString()
	{
		return name + " (" + featureCode.name + ")";
	}
}
