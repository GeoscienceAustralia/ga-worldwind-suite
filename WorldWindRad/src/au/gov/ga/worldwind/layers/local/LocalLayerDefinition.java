package au.gov.ga.worldwind.layers.local;

import java.awt.Color;
import java.io.Serializable;

public class LocalLayerDefinition implements Serializable
{
	private String name = "";
	private String directory = "";
	private String extension = "JPG";
	private int tilesize = 512;
	private double lztsd = 36d;
	private boolean hasTransparentColor = false;
	private Color transparentColor = Color.black;
	private int transparentFuzz = 10;

	public LocalLayerDefinition()
	{
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getExtension()
	{
		return extension;
	}

	public void setExtension(String extension)
	{
		this.extension = extension;
	}

	public boolean isHasTransparentColor()
	{
		return hasTransparentColor;
	}

	public void setHasTransparentColor(boolean hasTransparentColor)
	{
		this.hasTransparentColor = hasTransparentColor;
	}

	public Color getTransparentColor()
	{
		return transparentColor;
	}

	public void setTransparentColor(Color transparentColor)
	{
		this.transparentColor = transparentColor;
	}

	public int getTransparentFuzz()
	{
		return transparentFuzz;
	}

	public void setTransparentFuzz(int transparentFuzz)
	{
		this.transparentFuzz = transparentFuzz;
	}

	public double getLztsd()
	{
		return lztsd;
	}

	public void setLztsd(double lztsd)
	{
		this.lztsd = lztsd;
	}

	public int getTilesize()
	{
		return tilesize;
	}

	public void setTilesize(int tilesize)
	{
		this.tilesize = tilesize;
	}

	public LocalLayer createLayer()
	{
		return new LocalLayer(this);
	}
}
