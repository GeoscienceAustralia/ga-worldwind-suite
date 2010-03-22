package au.gov.ga.worldwind.layers.local;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;

/**
 * 
 * @author Michael de Hoog
 * Modifications and documentation: Chris Thorne
 * There are two parallel versions of a tiled layer set (tiled pyramid of surface images):
 * LocalLayerDefinitions: 
 * effectively extends LocalLayer which extends *TiledImageLayer* which extends AbstractLayer.
 * FileLayer: 
 * effectively extends FileBasicTiledImageLayer which extends ImmediateBasicTiledImageLayer
 *   which extends BasicTiledImageLayer which extends *TiledImageLayer* ... so ...
 *   
 * Calls LocalLayer (a TiledImageLayer) to set the properties that will be used
 * by the WW renderer to render a tiled image set. It is equivalent to creating a FileLayer.
 * 
 * This form of Layer successfully implements transparent colour (at least the detault = black)
 * Whereas FileLayer does not. There appears to be no AVList attribute for this - just the 
 * isHasTransparentColor(); This implies something in the rendering looks specifically for 
 * isHasTransparentColor() whereas FileLayer does not have the equivalent.
 * On the other hand, there is no support here for a mask layer set whereas FileLayer does.
 * 
 * e.g. :
 * 	LocalLayerDefinition def = new LocalLayerDefinition();
								
	//
	// This assumes scenarioSector is set already
	// Name
	// cache dir
	// cache format suffix
	// File directory (of "tile" pyramid) - absolute path?
	// image format/extension
	// number levels 
	// The lzt in Angle format
	// sector
								def.setDirectory(checkFile.getAbsolutePath());
								def.setName(elements[3]);
								def.setExtension(elements[7]);
								def.setMaxLat(sector.getMaxLatitude().degrees);
								def.setMaxLon(sector.getMaxLongitude().degrees);
								def.setMinLat(sector.getMinLatitude().degrees);
								def.setMaxLon(sector.getMinLongitude().degrees);
								//def.setTilesize(tilesizeField.getValue());
								
								def.setLztsd(Double.parseDouble(elements[9]));
								def.setLevelcount(Integer.parseInt(elements[9]));
								def.setHasTransparentColor(true);
								//def.setTransparentColor(transparentColor.getColor());
								//def.setTransparentFuzz(fuzzField.getValue());
								LocalLayer layer = def.createLayer();

is equevalent to:

								Layer tiled = 
									FileLayer.createLayer(
										elements[3], // Name
										elements[4], // cache dir
										elements[5], // cache format suffix
										checkFile,   // File directory (of tile pyramid)
										elements[7], // image format/extension
										Integer.parseInt(elements[8]), // number levels 
										// The lzt in Angle format
										LatLon.fromDegrees(
											Double.parseDouble(elements[9]),
											Double.parseDouble(elements[10])), // LatLon.fromDegrees(36d,
																			// 36d),
												sector);

							
 *
 */
public class LocalLayerDefinition implements Serializable
{
	private String name = "";
	private String directory = "";
	private String extension = "JPG";
	private String maskDirectory = "";
	private String maskExtension = "png";
	private int tilesize = 512;
	private double lztsd = 36d;
	private int levelcount = 1;
	private double minLat = -90;
	private double minLon = -180;
	private double maxLat = 90;
	private double maxLon = 180;
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

	public int getLevelcount()
	{
		return levelcount;
	}

	public void setLevelcount(int levelcount)
	{
		this.levelcount = levelcount;
	}

	public double getMinLat()
	{
		return minLat;
	}

	public void setMinLat(double minlat)
	{
		this.minLat = minlat;
	}

	public double getMinLon()
	{
		return minLon;
	}

	public void setMinLon(double minlon)
	{
		this.minLon = minlon;
	}

	public double getMaxLat()
	{
		return maxLat;
	}

	public void setMaxLat(double maxlat)
	{
		this.maxLat = maxlat;
	}

	public double getMaxLon()
	{
		return maxLon;
	}

	public void setMaxLon(double maxlon)
	{
		this.maxLon = maxlon;
	}

	public String getMaskDirectory() {
		return maskDirectory;
	}

	public void setMaskDirectory(String maskDirectory) {
		this.maskDirectory = maskDirectory;
	}

	public String getMaskExtension() {
		return maskExtension;
	}

	public void setMaskExtension(String maskExtension) {
		this.maskExtension = maskExtension;
	}
}
