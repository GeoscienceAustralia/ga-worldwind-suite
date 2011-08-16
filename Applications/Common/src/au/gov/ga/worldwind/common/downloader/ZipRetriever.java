package au.gov.ga.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.URLRetriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * URLRetriever that makes the readZipStream function accessible.
 * 
 * @author Michael de Hoog
 */
public class ZipRetriever extends URLRetriever
{
	public ZipRetriever(URL url)
	{
		super(url, null);
	}

	@Override
	public ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
	{
		return super.readZipStream(inputStream, url);
	}
}