package au.gov.ga.worldwind.common.layers.kml.relativeio;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.io.KMLFile;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The {@link RelativeKMLFile} class is a subclass of {@link KMLFile} that
 * supports better resolving of relative KML references.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativeKMLFile extends KMLFile implements RelativeKMLDoc
{
	private final KMLDoc parent;
	private final String href;

	public RelativeKMLFile(File file, String href, KMLDoc parent)
	{
		super(file);
		this.href = href;
		this.parent = parent;
	}

	@Override
	public String getHref()
	{
		return href;
	}

	@Override
	public KMLDoc getParent()
	{
		return parent;
	}

	@Override
	public boolean isContainer()
	{
		return false;
	}

	@Override
	public synchronized InputStream getSupportFileStream(String path) throws IOException
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			return relativized.relativeTo.getSupportFileStream(path);
		}

		InputStream inputStream = super.getSupportFileStream(path);
		if (inputStream != null)
			return inputStream;

		File parentFile = kmlFile.getParentFile();
		if (parentFile != null)
		{
			File childFile = new File(parentFile, path);
			if (childFile.exists())
			{
				return new FileInputStream(childFile);
			}
		}

		//converting the path to a URL will only work if the path has a protocol
		URL url = WWIO.makeURL(path);
		if (url != null)
			return url.openStream();

		return null;
	}

	@Override
	public synchronized String getSupportFilePath(String path)
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			try
			{
				return relativized.relativeTo.getSupportFilePath(path);
			}
			catch (IOException e)
			{
				//ignore
			}
		}

		String superPath = super.getSupportFilePath(path);
		if (superPath != null)
			return superPath;

		File parentFile = kmlFile.getParentFile();
		if (parentFile != null)
		{
			File childFile = new File(parentFile, path);
			if (childFile.exists())
			{
				return childFile.getAbsolutePath();
			}
		}

		//converting the path to a URL will only work if the path has a protocol
		URL url = WWIO.makeURL(path);
		if (url != null)
			return url.toExternalForm();

		return null;
	}
}
