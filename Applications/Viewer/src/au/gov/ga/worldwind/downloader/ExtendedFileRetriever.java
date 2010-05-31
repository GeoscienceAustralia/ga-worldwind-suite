package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import au.gov.ga.worldwind.util.Util;

public class ExtendedFileRetriever extends FileRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;
	private boolean notModified = false;

	public ExtendedFileRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		try
		{
			if (ifModifiedSince != null)
			{
				notModified = checkIfModified(connection.getURL(), ifModifiedSince);
				if (notModified)
					return null;
			}

			return super.doRead(connection);
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	private boolean checkIfModified(URL url, long ifModifiedSince)
	{
		File file = Util.urlToFile(url);
		return file != null && file.exists() && file.lastModified() <= ifModifiedSince;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public boolean isNotModified()
	{
		return notModified;
	}
}
