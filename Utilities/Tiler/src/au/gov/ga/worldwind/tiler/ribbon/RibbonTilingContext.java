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

/**
 * Contains the contextual information for a ribbon tiling job
 */
public class RibbonTilingContext {

	// Tiling parameters
	private int tilesize = 512;
	private Dimension sourceImageSize;
	private File sourceFile;
	private File outputLocation;
	private File tilesetRoot;
	private Insets insets = new Insets(0,0,0,0);
	private String format = null;
	private int numLevels;
	
	// Flags
	private boolean copySource = true; 
	private boolean mask = false;
	private boolean generateTilingLog = true;
	private boolean hideStdOut = false;
	private boolean generateLayerDefinition = true;
	
	// Layer definition parameters
	private List<String> elementCreatorClasses = new ArrayList<String>();
	private File layerDefinitionFile;
	private List<String> delegateStrings = new ArrayList<String>();
	private String dataCache;
	private List<String> pathLatLons = new ArrayList<String>();
	private Double curtainTop = 0d;
	private Double curtainBottom = -100d;
	private boolean followTerrain = false;
	private int subsegments = 10;
	private boolean useTransparentTextures = true;
	private boolean forceLevelZeroLoads = true;
	private boolean retainLevelZeroTiles = true;
	private boolean useMipMaps = true;
	private double detailHint = 0.5;
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
		if (!generateTilingLog)
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
		return sourceFile == null ? null : Util.stripExtension(sourceFile.getName());
	}
	
	public File getSourceLocation()
	{
		return sourceFile == null ? null : sourceFile.getParentFile();
	}

	public File getTilesetRoot()
	{
		if (tilesetRoot == null)
		{
			tilesetRoot = new File(outputLocation, getTilesetName());
			tilesetRoot.mkdir();
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

	public void setGenerateTilingLog(boolean generateTilingLog) {
		this.generateTilingLog = generateTilingLog;
	}

	public boolean isGenerateTilingLog() {
		return generateTilingLog;
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
	
	public boolean isGenerateLayerDefinition() {
		return generateLayerDefinition;
	}

	public void setGenerateLayerDefinition(boolean generateLayerDefinition) {
		this.generateLayerDefinition = generateLayerDefinition;
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
		return dataCache;
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
}
