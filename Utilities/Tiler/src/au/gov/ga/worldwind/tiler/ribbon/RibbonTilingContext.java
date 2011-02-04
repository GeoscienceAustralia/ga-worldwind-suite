package au.gov.ga.worldwind.tiler.ribbon;

import java.awt.Insets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import au.gov.ga.worldwind.tiler.util.Util;

/**
 * Contains the contextual information for a ribbon tiling job
 */
public class RibbonTilingContext {

	private int tilesize = 512;
	private File sourceFile;
	private File outputLocation;
	private File destination;
	private Insets insets = new Insets(0,0,0,0);
	private String format = null;
	
	private boolean copySource = true; 
	private boolean mask = false;
	private boolean generateTilingLog = true;
	private boolean hideStdOut = false;
	
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
				logWriter = new OutputStreamWriter(new FileOutputStream(new File(getDestination(), getLogFileName())));
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

	public File getDestination()
	{
		if (destination == null)
		{
			destination = new File(outputLocation, getTilesetName());
		}
		return destination;
	}
	
	public boolean isMasked() {
		return mask;
	}

	public void setMasked(boolean mask) {
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

	public void setDestination(File destination) {
		this.destination = destination;
	}
}
