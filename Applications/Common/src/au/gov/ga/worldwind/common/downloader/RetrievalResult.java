package au.gov.ga.worldwind.common.downloader;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Represents a result from a download.
 * 
 * @author Michael de Hoog
 */
public interface RetrievalResult
{
	/**
	 * Source URL from which this result was downloaded.
	 * 
	 * @return Source URL
	 */
	public URL getSourceURL();

	/**
	 * Does this download result have any data (ie was it successful)?
	 * 
	 * @return true if this result has data
	 */
	public boolean hasData();

	/**
	 * Get the downloaded data as a ByteBuffer.
	 * 
	 * @return ByteBuffer containing the downloaded data
	 */
	public ByteBuffer getAsBuffer();

	/**
	 * Get the downloaded data as a String.
	 * 
	 * @return String representation of the downloaded data
	 */
	public String getAsString();

	/**
	 * Get the downloaded data as an InputStream.
	 * 
	 * @return Downloaded data wrapped in an InputStream
	 */
	public InputStream getAsInputStream();

	/**
	 * Was this download result retrieved from the cache?
	 * 
	 * @return True if this result was from the cache
	 */
	public boolean isFromCache();

	/**
	 * Was a NOT MODIFIED result returned from the server (ie a HTTP 304)? The
	 * not modified since date is read from the modification date of the cached
	 * version of the file.
	 * 
	 * @return True if the server returned a NOT MODIFIED status
	 */
	public boolean isNotModified();

	/**
	 * Gets the exception if the attempted download resulted in an error.
	 * Returns null if the download was successful.
	 * 
	 * @return The download error
	 */
	public Exception getError();
}
