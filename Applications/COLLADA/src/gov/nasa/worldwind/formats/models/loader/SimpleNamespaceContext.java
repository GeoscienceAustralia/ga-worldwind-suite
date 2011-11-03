package gov.nasa.worldwind.formats.models.loader;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * @author Java & XML 3e
 * @version $Id: SimpleNamespaceContext.java,v 1.1.2.1 2009-03-20 15:40:25 glabuser Exp $
 */
public class SimpleNamespaceContext implements NamespaceContext
{

    private Map<String, String> urisByPrefix = new HashMap<String, String>();
    private Map<String, Set<String>> prefixesByURI = new HashMap<String, Set<String>>();

    public SimpleNamespaceContext()
    {
        // prepopulate with xml and xmlns prefixes
        // per JavaDoc of NamespaceContext interface
        addNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        addNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        addNamespace("xlink", "http://www.w3.org/1999/xlink");
        addNamespace("col", "http://www.collada.org/2005/11/COLLADASchema");
        addNamespace(XMLConstants.DEFAULT_NS_PREFIX, "http://www.collada.org/2005/11/COLLADASchema");
    }

    public synchronized void addNamespace(String prefix, String namespaceURI)
    {
        urisByPrefix.put(prefix, namespaceURI);
        if (prefixesByURI.containsKey(namespaceURI))
        {
            (prefixesByURI.get(namespaceURI)).add(prefix);
        }
        else
        {
            Set<String> set = new HashSet<String>();
            set.add(prefix);
            prefixesByURI.put(namespaceURI, set);
        }
    }

    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
            throw new IllegalArgumentException("prefix cannot be null");
        if (urisByPrefix.containsKey(prefix))
            return urisByPrefix.get(prefix);
        else
            return XMLConstants.NULL_NS_URI;
    }

    public String getPrefix(String namespaceURI)
    {
        return (String) getPrefixes(namespaceURI).next();
    }

    public Iterator getPrefixes(String namespaceURI)
    {
        if (namespaceURI == null)
            throw new IllegalArgumentException("namespaceURI cannot be null");
        if (prefixesByURI.containsKey(namespaceURI))
        {
            return (prefixesByURI.get(namespaceURI)).iterator();
        }
        else
        {
            return Collections.EMPTY_SET.iterator();
        }
    }
}