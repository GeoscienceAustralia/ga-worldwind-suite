package au.gov.ga.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.Retriever;

/**
 * Extension of the World Wind {@link Retriever} interface which provides
 * additional getter methods required for the {@link Downloader}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ExtendedRetriever extends Retriever
{
	/**
	 * Gets the exception if the attempted download resulted in an error.
	 * Returns null if the download was successful.
	 * 
	 * @return The download error
	 */
	public Exception getError();

	/**
	 * Was a NOT MODIFIED result returned from the server (ie a HTTP 304)? The
	 * not modified since date is read from the modification date of the cached
	 * version of the file.
	 * 
	 * @return True if the server returned a NOT MODIFIED status
	 */
	public boolean isNotModified();
}
