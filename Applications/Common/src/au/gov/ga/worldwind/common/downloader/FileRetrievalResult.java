package au.gov.ga.worldwind.common.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Extension of the {@link ByteBufferRetrievalResult} which fills the ByteBuffer
 * from a File. Used for returning results from the local cache.
 * 
 * @author Michael de Hoog
 */
public class FileRetrievalResult extends ByteBufferRetrievalResult
{
	private final File file;

	public FileRetrievalResult(URL sourceURL, File file, boolean fromCache)
	{
		super(sourceURL, readFile(file), fromCache, false, null, null);
		this.file = file;
	}

	private static ByteBuffer readFile(File file)
	{
		if (!file.exists() || file.isDirectory() || !file.canRead())
			return null;
		ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
		try
		{
			FileInputStream fis = new FileInputStream(file);
			fis.read(buffer.array());
			fis.close();
			return buffer;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * @return Last Modification Date of the file
	 */
	public Long lastModified()
	{
		if (hasData())
			return file.lastModified();
		return null;
	}
}
