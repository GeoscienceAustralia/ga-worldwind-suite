package au.gov.ga.worldwind.common.util.message;

import java.text.MessageFormat;

/**
 * An interface for classes that can provide access to messages contained in
 * resource bundles.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public interface MessageSource
{
	/**
	 * Add a resource bundle to this source.
	 * 
	 * @param name
	 *            The resource bundle base name of the collection of messages to
	 *            add
	 */
	void addBundle(String baseName);

	/**
	 * Get the message with the provided key.
	 * 
	 * @param key
	 *            The key for the message to return
	 * 
	 * @return The message with the provided key, or <code>null</code> if a
	 *         message with the provided key cannot be found
	 */
	String getMessage(String key);

	/**
	 * Get the message with the provided key.
	 * <p/>
	 * If the message cannot be found, returns the provided default message.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param defaultMsg
	 *            The default message to return if the key cannot be found
	 * 
	 * @return The message with the provided key, or <code>defaultMsg</code> if
	 *         a message with the provided key cannot be found
	 */
	String getMessage(String key, String defaultMsg);

	/**
	 * Get the message with the provided key, using the provided params as
	 * substitutions.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param params
	 *            Params to use in substitution
	 * 
	 * @return The message with the provided key, with parameter substitution
	 *         applied
	 * 
	 * @see MessageFormat
	 */
	String getMessage(String key, Object... params);


	/**
	 * Get the message with the provided key, using the provided params as
	 * substitutions.
	 * <p/>
	 * If the message cannot be found, returns the provided default message.
	 * 
	 * @param key
	 *            The key for the message to return
	 * @param defaultMsg
	 *            The default message to return if the key cannot be found
	 * @param params
	 *            Params to use in substitution
	 * 
	 * @return The message with the provided key, or <code>defaultMsg</code> if
	 *         a message with the provided key cannot be found, with parameter
	 *         substitution applied
	 * 
	 * @see MessageFormat
	 */
	String getMessage(String key, String defaultMsg, Object... params);
}
