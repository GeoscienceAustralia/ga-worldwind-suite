package gov.nasa.worldwind.ogc.custom.kml.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import gov.nasa.worldwind.ogc.kml.io.KMZInputStream;
import gov.nasa.worldwind.util.WWIO;

public class CustomKMZInputStream extends KMZInputStream
{
	/** The URI of this KMZ document. May be {@code null}. */
    protected URI uri;
	
	public CustomKMZInputStream(InputStream sourceStream, URI uri) throws IOException
	{
		super(sourceStream);
		this.uri = uri;
	}
	
	@Override
	public synchronized String getSupportFilePath(String path) throws IOException
	{
		//first search for path within KMZ zip file
		String superPath = super.getSupportFilePath(path);
		if(superPath != null)
			return superPath;
		
		//the following is copied from KMLInputStream:
		if (this.uri != null)
        {
            URI remoteFile = uri.resolve(path);
            if (remoteFile != null)
                return remoteFile.toString();
        }
        return null;
	}
	
	@Override
	public synchronized InputStream getSupportFileStream(String path) throws IOException
	{
		//first search for path within KMZ zip file
		InputStream inputStream = super.getSupportFileStream(path);
		if(inputStream != null)
			return inputStream;
		
		//the following is copied from KMLInputStream:
		String ref = this.getSupportFilePath(path);
        if (ref != null)
        {
            URL url = WWIO.makeURL(path);
            if (url != null)
                return url.openStream();
        }
        return null;
	}
}
