package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class IfModifiedHTTPRetriever extends HTTPRetriever
{
	private Long ifModifiedSince;

	public IfModifiedHTTPRetriever(URL url, Long ifModifiedSince,
			RetrievalPostProcessor postProcessor)
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
