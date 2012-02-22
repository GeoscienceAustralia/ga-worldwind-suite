package au.gov.ga.worldwind.tiler.ribbon;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.tiler.util.Util;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaSeparatedConverter;
import com.beust.jcommander.converters.FileConverter;

/**
 * Contains the contextual information for a ribbon tiling job
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RibbonTilingContext {

	// Tiling parameters
	@Parameter(names="-tilesize", description="The output dimensions of the tiles (in pixels)")
	private int tilesize = 512;
	private Dimension sourceImageSize;
	@Parameter(names="-source", description="The source image", converter=FileConverter.class)
	private File sourceFile;
	@Parameter(names="-output", description="The output location", required=true, converter=FileConverter.class)
	private File outputLocation;
	@Parameter(names="-tileset", description="The name to use for the tileset folder")
	private String tilesetName;
	private File tilesetRoot;
	private Insets insets = new Insets(0,0,0,0);
	@Parameter(names="-format", description="Override the output format")
	private String format = null;
	private int numLevels;
	
	// Flags
	@Parameter(names="-removeConstantColumns", description="Remove constant colour from the top and bottom of the image")
	private boolean removeConstantColumns = false;
	@Parameter(names="-copySource", description="Copy the source image to the tileset folder?")
	private boolean copySource = false; 
	@Parameter(names="-mask", description="Generate a mask tileset?")
	private boolean mask = false;
	@Parameter(names="-noLog", description="Suppress the generation of a tiling log?")
	private boolean suppressTilingLog = false;
	@Parameter(names="-hideStdOut", description="Hide the standard console output?")
	private boolean hideStdOut = false;
	@Parameter(names="-noLayerDef", description="Suppress the generation of a layer definition file?")
	private boolean suppressLayerDefinition = false;
	
	// Layer definition parameters
	@Parameter(names="-elementCreators", description="The fully qualified classname of element creators to use", converter=CommaSeparatedConverter.class)
	private List<String> elementCreatorClasses = new ArrayList<String>();
	@Parameter(names="-layerDefinition", description="Override the default location for the layer definition file", converter=FileConverter.class)
	private File layerDefinitionFile;
	@Parameter(names="-delegate", description="Add delegate strings to the layer definition")
	private List<String> delegateStrings = new ArrayList<String>();
	@Parameter(names="-dataCache", description="Override the default cache location")
	private String dataCache;
	@Parameter(names="-path", description="Provide pipe-separated lat-lon pairs to specify the path", converter=CommaSeparatedConverter.class)
	private List<String> pathLatLons = new ArrayList<String>();
	@Parameter(names="-top", description="The curtain top elevation", converter=DoubleConverter.class)
	private Double curtainTop = 0d;
	@Parameter(names="-bottom", description="The curtain bottom elevation", converter=DoubleConverter.class)
	private Double curtainBottom = -100d;
	@Parameter(names="-followTerrain", description="Follow the terrain?")
	private boolean followTerrain = false;
	@Parameter(names="-subsegments", description="The number of subsegments to use")
	private int subsegments = 10;
	@Parameter(names="-useTransparent")
	private boolean useTransparentTextures = true;
	@Parameter(names="-forceLevelZeroLoads")
	private boolean forceLevelZeroLoads = true;
	@Parameter(names="-retainLevelZeroTiles")
	private boolean retainLevelZeroTiles = true;
	@Parameter(names="-useMipMaps")
	private boolean useMipMaps = true;
	@Parameter(names="-detailHint", description="The detail hint to use for the layer")
	private double detailHint = 0.5;
	@Parameter(names="-textureFormat")
	private String textureFormat = "image/dds";
	
	// Writing streams
	private static OutputStream NULL_STREAM = new OutputStream()
	{
		@Override
		public void write(int b) throws IOException {}
	};
	private static Writer NULL_WRITER = new OutputStreamWriter(NULL_STREAM);
	private static Writer STD_WRITER = new OutputStreamWriter(System.out);
	private Writer logWriter = null;
	
	public Writer getStdWriter()
	{
		return hideStdOut ? NULL_WRITER : STD_WRITER;
	}
	
	public Writer getLogWriter()
	{
		if (suppressTilingLog)
		{
			return NULL_WRITER;
		}
		if (logWriter == null)
		{
			try
			{
				logWriter = new OutputStreamWriter(new FileOutputStream(new File(getTilesetRoot(), getLogFileName())));
			}
			catch (FileNotFoundException e)
			{
				return NULL_WRITER;
			}
		}
		return logWriter; 
	}
	
	private String getLogFileName()
	{
		return getTilesetName() + ".log";
	}
	
	public String getFormat()
	{
		if (format == null)
		{
			format = mask ? "png" : "jpg";
		}
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getTilesetName()
	{
		if (tilesetName == null)
		{
			tilesetName = sourceFile == null ? null : Util.stripExtension(sourceFile.getName());
		}
		return tilesetName;
	}
	
	public void setTilesetName(String tilesetName)
	{
		this.tilesetName = tilesetName;
	}
	
	public File getSourceLocation()
	{
		return sourceFile == null ? null : sourceFile.getParentFile();
	}

	public File getTilesetRoot()
	{
		if (tilesetRoot == null)
		{
			tilesetRoot = new File(getOutputLocation(), getTilesetName());
			tilesetRoot.mkdirs();
		}
		return tilesetRoot;
	}
	
	public boolean isMask() {
		return mask;
	}

	public void setIsMask(boolean mask) {
		this.mask = mask;
	}

	public int getTilesize() {
		return tilesize;
	}

	public void setTilesize(int tilesize) {
		this.tilesize = tilesize;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public File getOutputLocation() {
		return outputLocation;
	}

	public void setOutputLocation(File outputLocation) {
		this.outputLocation = outputLocation;
	}
	
	public Insets getInsets() {
		return insets;
	}
	
	public void setInsets(Insets insets) {
		this.insets = insets;
	}

	public void setCopySource(boolean copySource) {
		this.copySource = copySource;
	}

	public boolean isCopySource() {
		return copySource;
	}

	public void setSuppressTilingLog(boolean suppressTilingLog)
	{
		this.suppressTilingLog = suppressTilingLog;
	}
	
	public boolean isSuppressTilingLog()
	{
		return suppressTilingLog;
	}
	
	public void setHideStdOut(boolean hideStdOut) {
		this.hideStdOut = hideStdOut;
	}

	public boolean isHideStdOut() {
		return hideStdOut;
	}

	public void setTilesetRoot(File destination) {
		this.tilesetRoot = destination;
	}
	
	public boolean isSuppressLayerDefinition()
	{
		return suppressLayerDefinition;
	}
	
	public void setSuppressLayerDefinition(boolean suppressLayerDefinition)
	{
		this.suppressLayerDefinition = suppressLayerDefinition;
	}
	
	public File getLayerDefinitionFile() {
		if (layerDefinitionFile == null)
		{
			layerDefinitionFile = new File(getOutputLocation(), getTilesetName() + ".xml");
			try
			{
				layerDefinitionFile.createNewFile();
			}
			catch (IOException e)
			{
				
			}
		}
		return layerDefinitionFile;
	}

	public void setLayerDefinitionFile(File layerDefinitionFile) {
		this.layerDefinitionFile = layerDefinitionFile;
	}
	
	public List<String> getDelegateStrings()
	{
		return delegateStrings;
	}
	
	public void addDelegateString(String delegateString)
	{
		if (Util.isBlank(delegateString))
		{
			return;
		}
		delegateStrings.add(delegateString);
	}
	
	public void setDelegateStrings(List<String> delegateStrings)
	{
		this.delegateStrings = delegateStrings;
	}
	
	public String getDataCacheName()
	{
		if (dataCache == null)
		{
			return "GA/Curtain Tiles/" + getTilesetName();
		}
		return dataCache.replaceAll("&TILESET&", getTilesetName());
	}
	
	public void setDataCache(String dataCache)
	{
		this.dataCache = dataCache;
	}
	
	public int getNumLevels()
	{
		return numLevels;
	}
	
	public void setNumLevels(int numLevels)
	{
		this.numLevels = numLevels;
	}

	public void setSourceImageSize(Dimension sourceImageSize)
	{
		this.sourceImageSize = sourceImageSize;
	}

	public Dimension getSourceImageSize()
	{
		return sourceImageSize;
	}

	public void setPathLatLons(List<String> pathLatLons)
	{
		this.pathLatLons = pathLatLons;
	}

	public List<String> getPathLatLons()
	{
		return pathLatLons;
	}

	public void setCurtainTop(Double curtainTop)
	{
		this.curtainTop = curtainTop;
	}

	public Double getCurtainTop()
	{
		return curtainTop;
	}

	public void setCurtainBottom(Double curtainBottom)
	{
		this.curtainBottom = curtainBottom;
	}

	public Double getCurtainBottom()
	{
		return curtainBottom;
	}
	
	public boolean isFollowTerrain()
	{
		return followTerrain;
	}
	
	public void setFollowTerrain(boolean followTerrain)
	{
		this.followTerrain = followTerrain;
	}
	
	public int getSubsegments()
	{
		return subsegments;
	}

	public void setSubsegments(int subsegments)
	{
		this.subsegments = subsegments;
	}

	public boolean isUseTransparentTextures()
	{
		return useTransparentTextures;
	}

	public void setUseTransparentTextures(boolean useTransparentTextures)
	{
		this.useTransparentTextures = useTransparentTextures;
	}

	public boolean isForceLevelZeroLoads()
	{
		return forceLevelZeroLoads;
	}

	public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
	{
		this.forceLevelZeroLoads = forceLevelZeroLoads;
	}

	public boolean isRetainLevelZeroTiles()
	{
		return retainLevelZeroTiles;
	}

	public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
	{
		this.retainLevelZeroTiles = retainLevelZeroTiles;
	}

	public boolean isUseMipMaps()
	{
		return useMipMaps;
	}

	public void setUseMipMaps(boolean useMipMaps)
	{
		this.useMipMaps = useMipMaps;
	}

	public double getDetailHint()
	{
		return detailHint;
	}

	public void setDetailHint(double detailHint)
	{
		this.detailHint = detailHint;
	}
	
	public String getTextureFormat()
	{
		return textureFormat;
	}
	
	public void setTextureFormat(String textureFormat)
	{
		this.textureFormat = textureFormat;
	}

	public void setElementCreatorClasses(List<String> elementCreatorClasses)
	{
		this.elementCreatorClasses = elementCreatorClasses;
	}

	public void addElementCreatorClass(String className)
	{
		if (Util.isBlank(className))
		{
			return;
		}
		elementCreatorClasses.add(className);
	}
	
	public List<String> getElementCreatorClasses()
	{
		return elementCreatorClasses;
	}

	public boolean isRemoveConstantColumns()
	{
		return removeConstantColumns;
	}

	public void setRemoveConstantColumns(boolean removeConstantColumns)
	{
		this.removeConstantColumns = removeConstantColumns;
	}
}
