package gov.nasa.worldwind.ogc.custom.kml.model;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLParserContext;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.io.KMLFile;
import gov.nasa.worldwind.ogc.kml.io.KMLInputStream;
import gov.nasa.worldwind.ogc.kml.io.KMZFile;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.xml.XMLEventParserContextFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

public class CustomKMLRoot extends KMLRoot
{
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
    public static KMLRoot create(Object docSource) throws IOException
    {
        return create(docSource, true);
    }

    /**
     * Creates a KML root for an untyped source. The source must be either a {@link File}, a {@link URL}, a {@link
     * InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     *
     * @param docSource      either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String}
     *                       identifying a file path or URL.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     */
    public static KMLRoot create(Object docSource, boolean namespaceAware) throws IOException
    {
        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (docSource instanceof File)
            return new CustomKMLRoot((File) docSource, namespaceAware);
        else if (docSource instanceof URL)
            return new CustomKMLRoot((URL) docSource, null, namespaceAware);
        else if (docSource instanceof InputStream)
            return new CustomKMLRoot((InputStream) docSource, null, namespaceAware);
        else if (docSource instanceof String)
        {
            File file = new File((String) docSource);
            if (file.exists())
                return new CustomKMLRoot(file, namespaceAware);

            URL url = WWIO.makeURL(docSource);
            if (url != null)
                return new CustomKMLRoot(url, null, namespaceAware);
        }

        return null;
    }

    /**
     * Creates a KML root for an untyped source and parses it. The source must be either a {@link File}, a {@link URL},
     * a {@link InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     * <p/>
     * Note: Because there are so many incorrectly formed KML files in distribution, it's often not possible to parse
     * with a namespace aware parser. This method first tries to use a namespace aware parser, but if a severe problem
     * occurs during parsing, it will try again using a namespace unaware parser. Namespace unaware parsing typically
     * bypasses many problems, but it also causes namespace qualified elements in the XML to be unrecognized.
     *
     * @param docSource either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String} identifying
     *                  a file path or URL.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     * @throws javax.xml.stream.XMLStreamException
     *                                  if the KML file has severe errors.
     */
    public static KMLRoot createAndParse(Object docSource) throws IOException, XMLStreamException
    {
        KMLRoot kmlRoot = CustomKMLRoot.create(docSource);

        if (kmlRoot == null)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                docSource.toString());
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Try with a namespace aware parser.
            kmlRoot.parse();
        }
        catch (XMLStreamException e)
        {
            // Try without namespace awareness.
            kmlRoot = CustomKMLRoot.create(docSource, false);
            kmlRoot.parse();
        }

        return kmlRoot;
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files from
     * either files or input streams.
     *
     * @param docSource the KMLDoc instance representing the KML document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(KMLDoc docSource) throws IOException
    {
        this(docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files from
     * either files or input streams.
     *
     * @param docSource      the KMLDoc instance representing the KML document.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(KMLDoc docSource, boolean namespaceAware) throws IOException
    {
        super(docSource, namespaceAware);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link File}.
     *
     * @param docSource the File containing the document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(File docSource) throws IOException
    {
        this(docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link File}.
     *
     * @param docSource      the File containing the document.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(File docSource, boolean namespaceAware) throws IOException
    {
        this(KMLConstants.KML_NAMESPACE, createDocSource(docSource), namespaceAware);
    }
    
    protected static KMLDoc createDocSource(File docSource) throws IOException
    {
    	if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWIO.isContentType(docSource, KMLConstants.KML_MIME_TYPE))
            return new KMLFile(docSource);
        else if (WWIO.isContentType(docSource, KMLConstants.KMZ_MIME_TYPE))
        {
            try
            {
                return new KMZFile(docSource);
            }
            catch (ZipException e)
            {
                // We've encountered some zip files that will not open with ZipFile, but will open
                // with ZipInputStream. Try again, this time opening treating the file as a stream.
                // See WWJINT-282.
                return new CustomKMZInputStream(new FileInputStream(docSource), docSource.toURI());
            }
        }
        else
            throw new WWUnrecognizedException(Logging.getMessage("KML.UnrecognizedKMLFileType"));
    }

    /**
     * Create a new <code>KMLRoot</code> for an {@link InputStream}.
     *
     * @param docSource   the input stream containing the document.
     * @param contentType the content type of the stream data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML
     *                    and {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is treated as KML for any other
     *                    value or a value of null.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(InputStream docSource, String contentType) throws IOException
    {
        this(docSource, contentType, true);
    }

    /**
     * Create a new <code>KMLRoot</code> for an {@link InputStream}.
     *
     * @param docSource      the input stream containing the document.
     * @param contentType    the content type of the stream data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain
     *                       KML and {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is treated as KML for any
     *                       other value or a value of null.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public CustomKMLRoot(InputStream docSource, String contentType, boolean namespaceAware) throws IOException
    {
        this(KMLConstants.KML_NAMESPACE, createDocSource(docSource, contentType), namespaceAware);
    }
    
    protected static KMLDoc createDocSource(InputStream docSource, String contentType) throws IOException
    {
    	if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (contentType != null && contentType.equals(KMLConstants.KMZ_MIME_TYPE))
            return new CustomKMZInputStream(docSource, null);
        else if (contentType == null && docSource instanceof ZipInputStream)
        	return new CustomKMZInputStream(docSource, null);
        else
        	return new KMLInputStream(docSource, null);
    }

    /**
     * Create a <code>KMLRoot</code> for a {@link URL}.
     *
     * @param docSource   the URL identifying the document.
     * @param contentType the content type of the data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML and
     *                    {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. Any other non-null value causes the content to be
     *                    treated as plain KML. If null is specified the content type is read from the server or other
     *                    end point of the URL. When a content type is specified, the content type returned by the URL's
     *                    end point is ignored. You can therefore force the content to be treated as KML or KMZ
     *                    regardless of what a server declares it to be.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the document.
     */
    public CustomKMLRoot(URL docSource, String contentType) throws IOException
    {
        this(docSource, contentType, true);
    }

    /**
     * Create a <code>KMLRoot</code> for a {@link URL}.
     *
     * @param docSource      the URL identifying the document.
     * @param contentType    the content type of the data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML and
     *                       {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. Any other non-null value causes the content to
     *                       be treated as plain KML. If null is specified the content type is read from the server or
     *                       other end point of the URL. When a content type is specified, the content type returned by
     *                       the URL's end point is ignored. You can therefore force the content to be treated as KML or
     *                       KMZ regardless of what a server declares it to be.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the document.
     */
    public CustomKMLRoot(URL docSource, String contentType, boolean namespaceAware) throws IOException
    {
        this(KMLConstants.KML_NAMESPACE, createDocSource(docSource, contentType), namespaceAware);
    }
    
    protected static KMLDoc createDocSource(URL docSource, String contentType) throws IOException
    {
    	if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URLConnection conn = docSource.openConnection();
        if (contentType == null)
            contentType = conn.getContentType();

        if (!(KMLConstants.KMZ_MIME_TYPE.equals(contentType) || KMLConstants.KML_MIME_TYPE.equals(contentType)))
            contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(docSource.getPath()));

        if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
            return new CustomKMZInputStream(conn.getInputStream(), WWIO.makeURI(docSource));
        else
        	return new KMLInputStream(conn.getInputStream(), WWIO.makeURI(docSource));
    }

    /**
     * Create a new <code>KMLRoot</code> with a specific namespace. (The default namespace is defined by {@link
     * gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
     *
     * @param namespaceURI the default namespace URI.
     * @param docSource    the KML source specified via a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files
     *                     from either files or input streams.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws java.io.IOException      if an I/O error occurs attempting to open the document source.
     */
    public CustomKMLRoot(String namespaceURI, KMLDoc docSource) throws IOException
    {
        this(namespaceURI, docSource, true);
    }

    /**
     * Create a new <code>KMLRoot</code> with a specific namespace. (The default namespace is defined by {@link
     * gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
     *
     * @param namespaceURI   the default namespace URI.
     * @param docSource      the KML source specified via a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ
     *                       files from either files or input streams.
     * @param namespaceAware specifies whether to use a namespace-aware XML parser. <code>true</code> if so,
     *                       <code>false</code> if not.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws java.io.IOException      if an I/O error occurs attempting to open the document source.
     */
    public CustomKMLRoot(String namespaceURI, KMLDoc docSource, boolean namespaceAware) throws IOException
    {
        super(namespaceURI, docSource, namespaceAware);
    }
    
    @Override
	protected KMLParserContext createParserContext(XMLEventReader reader)
    {
    	/*String[] mimeTypes = new String[] {KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE};
        XMLEventParserContextFactory.addParserContext(mimeTypes, new CustomKMLParserContext(this.getNamespaceURI()));
    	return super.createParserContext(reader);*/
    	
    	return this.parserContext = new CustomKMLParserContext(reader, this.getNamespaceURI());
	}

	@Override
	public Object resolveLocalReference(String linkBase, String linkRef)
	{
		if (linkBase == null)
		{
			String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			File file = new File(linkBase);

			// Determine whether the file is a KML or KMZ. If it's not just return the original address.
			if (!file.exists() || !WWIO.isContentType(file, KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE))
				return linkBase;

			// Attempt to open and parse the KML/Z file, trying both namespace aware and namespace unaware stream
			// readers if necessary.
			KMLRoot refRoot = CustomKMLRoot.createAndParse(file);
			// An exception is thrown if parsing fails, so no need to check for null.

			// Add the parsed file to the session cache so it doesn't have to be parsed again.
			WorldWind.getSessionCache().put(linkBase, refRoot);

			// Now check the newly opened KML/Z file for the referenced item, if a reference was specified.
			if (linkRef != null)
				return refRoot.getItemByID(linkRef);
			else
				return refRoot;
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
			Logging.logger().warning(message);
			return null;
		}
	}

	@Override
	protected KMLRoot parseCachedKMLFile(URL url, String linkBase, String contentType, boolean namespaceAware)
			throws IOException, XMLStreamException
	{
		KMLDoc kmlDoc;

		InputStream refStream = url.openStream();

		if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
			kmlDoc = new CustomKMZInputStream(refStream, WWIO.makeURI(linkBase));
		else
			// Attempt to parse as KML
			kmlDoc = new KMLInputStream(refStream, WWIO.makeURI(linkBase));

		try
		{
			KMLRoot refRoot = new CustomKMLRoot(kmlDoc, namespaceAware);
			refRoot.parse(); // also closes the URL's stream
			return refRoot;
		}
		catch (XMLStreamException e)
		{
			refStream.close(); // parsing failed, so explicitly close the stream
			throw e;
		}
	}
}
