package au.gov.ga.worldwind.common.layers.delegate.retriever;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Extended {@link HTTPRetriever} which simply stores incoming zip files
 * directly to the ByteBuffer, instead of decompressing them.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PassThroughZipRetriever extends HTTPRetriever
{
	public PassThroughZipRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}

	@Override
	protected ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
	{
		return readNonSpecificStream(inputStream, getConnection());
	}
}
