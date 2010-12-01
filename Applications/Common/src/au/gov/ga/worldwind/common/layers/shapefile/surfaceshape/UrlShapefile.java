package au.gov.ga.worldwind.common.layers.shapefile.surfaceshape;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.util.Logging;

public class UrlShapefile extends Shapefile
{
	public UrlShapefile(Object source)
	{
		super(source);
	}
	
	@Override
	protected String validateURLConnection(URLConnection connection, String[] acceptedContentTypes)
	{
		try
        {
            if (connection instanceof HttpURLConnection &&
                ((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return Logging.getMessage("HTTP.ResponseCode", ((HttpURLConnection) connection).getResponseCode(),
                    connection.getURL());
            }
        }
        catch (Exception e)
        {
            return Logging.getMessage("URLRetriever.ErrorOpeningConnection", connection.getURL());
        }
        
        //ignore content type checking
        
        return null;
	}
}
