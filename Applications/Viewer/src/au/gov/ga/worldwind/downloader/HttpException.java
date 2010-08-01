package au.gov.ga.worldwind.downloader;

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
