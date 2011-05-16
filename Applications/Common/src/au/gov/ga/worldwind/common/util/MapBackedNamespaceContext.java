package au.gov.ga.worldwind.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A simple implementation of the {@link NamespaceContext} that maintains a map of prefix->URI mappings
 */
public class MapBackedNamespaceContext implements NamespaceContext
{
	private Map<String, String> prefixToUriMap = new HashMap<String, String>();
	private Map<String, String> uriToPrefixMap = new HashMap<String, String>();

	public void addMapping(String prefix, String uri)
	{
		prefixToUriMap.put(prefix, uri);
		uriToPrefixMap.put(uri, prefix);
	}
	
	@Override
	public String getNamespaceURI(String prefix)
	{
		String result = prefixToUriMap.get(prefix);
		if (result != null)
		{
			return result;
		}
		return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI)
	{
		return uriToPrefixMap.get(namespaceURI);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getPrefixes(String namespaceURI)
	{
		return prefixToUriMap.keySet().iterator();
	}
	
}