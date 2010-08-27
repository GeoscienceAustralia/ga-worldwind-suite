package au.gov.ga.worldwind.common.downloader;

/**
 * Exception subclass which also provides the HTTP status code returned from the
 * server.
 * 
 * @author Michael de Hoog
 */
public class HttpException extends Exception
{
	private final int responseCode;

	public HttpException(String message, int responseCode)
	{
		super(message);
		this.responseCode = responseCode;
	}

	public int getResponseCode()
	{
		return responseCode;
	}
}
