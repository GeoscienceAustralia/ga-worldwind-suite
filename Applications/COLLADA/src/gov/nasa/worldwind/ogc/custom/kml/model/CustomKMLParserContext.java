package gov.nasa.worldwind.ogc.custom.kml.model;

import gov.nasa.worldwind.ogc.kml.KMLParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

public class CustomKMLParserContext extends KMLParserContext
{
	public CustomKMLParserContext(XMLEventReader eventReader, String defaultNamespace)
	{
		super(eventReader, defaultNamespace);
	}
	
	public CustomKMLParserContext(String defaultNamespace)
	{
		super(defaultNamespace);
	}
	
	public CustomKMLParserContext(CustomKMLParserContext ctx)
    {
        super(ctx);
    }

	@Override
	protected void initializeParsers(String ns)
	{
		super.initializeParsers(ns);
		this.parsers.remove(new QName(ns, "Placemark"));
		this.parsers.put(new QName(ns, "Placemark"), new CustomKMLPlacemark2(ns));
		this.parsers.remove(new QName(ns, "Region"));
		this.parsers.put(new QName(ns, "Region"), new CustomKMLRegion(ns));
	}
}
