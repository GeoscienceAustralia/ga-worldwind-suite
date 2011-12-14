package au.gov.ga.worldwind.common.layers.borehole;

import java.awt.Color;

public class BoreholeSampleImpl implements BoreholeSample
{
	private final Borehole borehole;
	private double depthFrom;
	private double depthTo;
	private Color color;
	private String text;
	private String link;

	public BoreholeSampleImpl(Borehole borehole)
	{
		this.borehole = borehole;
	}

	@Override
	public Borehole getBorehole()
	{
		return borehole;
	}

	@Override
	public double getDepthFrom()
	{
		return depthFrom;
	}

	public void setDepthFrom(double depthFrom)
	{
		this.depthFrom = depthFrom;
	}

	@Override
	public double getDepthTo()
	{
		return depthTo;
	}

	public void setDepthTo(double depthTo)
	{
		this.depthTo = depthTo;
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	@Override
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}
}
