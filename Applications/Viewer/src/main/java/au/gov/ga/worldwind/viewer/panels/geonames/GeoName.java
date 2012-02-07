package au.gov.ga.worldwind.viewer.panels.geonames;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;

import java.awt.Color;
import java.awt.Font;

import au.gov.ga.worldwind.viewer.util.ColorFont;

public class GeoName implements GeographicText
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

	public GeoName(String name, String country, int id, LatLon latlon,
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

	@Override
	public Color getBackgroundColor()
	{
		return colorFont.backgroundColor;
	}

	@Override
	public Color getColor()
	{
		return colorFont.color;
	}

	@Override
	public Font getFont()
	{
		return colorFont.font;
	}

	@Override
	public Position getPosition()
	{
		return new Position(latlon, 0);
	}

	@Override
	public CharSequence getText()
	{
		return name;
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}
	
	@Override
	public double getPriority()
	{
		return 0;
	}

	@Override
	public void setBackgroundColor(Color background)
	{
	}

	@Override
	public void setColor(Color color)
	{
	}

	@Override
	public void setFont(Font font)
	{
	}

	@Override
	public void setPosition(Position position)
	{
	}

	@Override
	public void setText(CharSequence text)
	{
	}

	@Override
	public void setVisible(boolean visible)
	{
	}

	@Override
	public void setPriority(double d)
	{
	}
}
