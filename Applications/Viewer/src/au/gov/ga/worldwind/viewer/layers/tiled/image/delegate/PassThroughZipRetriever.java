package au.gov.ga.worldwind.viewer.layers.tiled.image.delegate;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class PassThroughZipRetriever extends HTTPRetriever
{
	public PassThroughZipRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}
	
	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		return super.doRead(connection);
	}

	@Override
	protected ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
	{
		return readNonSpecificStream(inputStream, getConnection());
	}
}
