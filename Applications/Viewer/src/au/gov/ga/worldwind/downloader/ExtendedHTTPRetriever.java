package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class ExtendedHTTPRetriever extends HTTPRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;

	public ExtendedHTTPRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		if (ifModifiedSince != null)
			connection.setIfModifiedSince(ifModifiedSince.longValue());
		try
		{
			return super.doRead(connection);
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	public boolean isNotModified()
	{
		return getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED;
	}

	public Exception getError()
	{
		return error;
	}
}
