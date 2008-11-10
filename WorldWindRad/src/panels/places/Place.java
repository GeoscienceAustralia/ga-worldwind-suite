package panels.places;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;

import java.awt.Color;
import java.awt.Font;

import util.ColorFont;

public class Place implements GeographicText
{
	public final String name;
	public final String country;
	public final int id;
	public final LatLon latlon;
	public final String fcode;
	public final String fclass;
	public final String fcodename;
	public final String fclassname;
	public final ColorFont colorFont;

	public String[] parents;

	public Place(String name, String country, int id, LatLon latlon,
			String fclass, String fclassname, String fcode, String fcodename, ColorFont colorFont)
	{
		this.name = name;
		this.country = country;
		this.id = id;
		this.latlon = latlon;
		this.fclass = fclass;
		this.fclassname = fclassname;
		this.fcode = fcode;
		this.fcodename = fcodename;
		this.colorFont = colorFont;
	}

	public Color getBackgroundColor()
	{
		return colorFont.backgroundColor;
	}

	public Color getColor()
	{
		return colorFont.color;
	}

	public Font getFont()
	{
		return colorFont.font;
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
}
