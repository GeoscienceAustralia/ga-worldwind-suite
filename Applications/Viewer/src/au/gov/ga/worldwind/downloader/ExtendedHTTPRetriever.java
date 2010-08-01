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
			ByteBuffer buffer = super.doRead(connection);
			if (buffer == null && !isOk() && !isNotModified())
			{
				throw new HttpException(getResponseCode() + ": " + getResponseMessage(),
						getResponseCode());
			}
			return buffer;
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	public boolean isOk()
	{
		return getResponseCode() == HttpURLConnection.HTTP_OK;
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
