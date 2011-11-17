package gov.nasa.worldwind.ogc.custom.kml.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLEventReader;

import gov.nasa.worldwind.ogc.kml.KMLParserContext;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

public class CustomKMLRoot extends KMLRoot {

	public CustomKMLRoot(File docSource) throws IOException {
		super(docSource);
	}
	
	public CustomKMLRoot(InputStream in, String contentType) throws IOException {
		super(in,contentType);
	}
	
	public CustomKMLRoot(URL docSource, String contentType) throws IOException {
		super(docSource,contentType);
	}

	@Override
	protected KMLParserContext createParserContext(XMLEventReader reader) {
		 return this.parserContext = new CustomKMLParserContext(reader, this.getNamespaceURI());
	}
	
	/**
     * Creates a KML root for an untyped source. The source must be either a {@link File}, a {@link URL}, a {@link
     * InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     *
     * @param docSource either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String} identifying
     *                  a file path or URL.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     */
    public static CustomKMLRoot create(Object docSource) throws IOException
    {
        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (docSource instanceof File)
            return new CustomKMLRoot((File) docSource);
        else if (docSource instanceof URL)
            return new CustomKMLRoot((URL) docSource, null);
        else if (docSource instanceof InputStream)
            return new CustomKMLRoot((InputStream) docSource, null);
        else if (docSource instanceof String)
        {
            File file = new File((String) docSource);
            if (file.exists())
                return new CustomKMLRoot(file);

            URL url = WWIO.makeURL(docSource);
            if (url != null)
                return new CustomKMLRoot(url, null);
        }

        return null;
    }
}
