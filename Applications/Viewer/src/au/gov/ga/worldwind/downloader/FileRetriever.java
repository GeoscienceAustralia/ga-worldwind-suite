package au.gov.ga.worldwind.downloader;

import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.URLRetriever;

public class FileRetriever extends URLRetriever
{
	private Long ifModifiedSince;

	public FileRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		this(url, null, postProcessor);
	}

	public FileRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		if (ifModifiedSince != null)
			connection.setIfModifiedSince(ifModifiedSince.longValue());
		return super.doRead(connection);
	}
}
