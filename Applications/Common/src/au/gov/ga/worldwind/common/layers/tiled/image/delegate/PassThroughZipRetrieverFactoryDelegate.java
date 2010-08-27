package au.gov.ga.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

/**
 * Implementation of {@link RetrieverFactoryDelegate} which creates
 * {@link PassThroughZipRetriever}s.
 * 
 * @author Michael de Hoog
 */
public class PassThroughZipRetrieverFactoryDelegate implements RetrieverFactoryDelegate
{
	private final static String DEFINITION_STRING = "PassThroughZipRetriever";

	@Override
	public Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		return new PassThroughZipRetriever(url, postProcessor);
	}

	@Override
	public Delegate fromDefinition(String definition)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
			return new PassThroughZipRetrieverFactoryDelegate();
		return null;
	}

	@Override
	public String toDefinition()
	{
		return DEFINITION_STRING;
	}
}
