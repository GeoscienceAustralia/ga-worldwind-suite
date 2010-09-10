package au.gov.ga.worldwind.common.util.message;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * An implementation of the {@link MessageSource} interface that uses the Java
 * {@link ResourceBundle} mechanism to load messages.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class ResourceBundleMessageSource extends MessageSourceBase implements MessageSource
{
	/** The list of resource bundles to inspect */
	private List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

	/** The cache of retrieved messages */
	private Map<String, MessageFormat> cachedMessages = new HashMap<String, MessageFormat>();

	/**
	 * Constructor. Initialises the resource bundles to inspect for messages.
	 * 
	 * @param resourceBundles
	 *            The names of resource bundles to inspect.
	 */
	public ResourceBundleMessageSource(String... resourceBundles)
	{
		if (resourceBundles == null || resourceBundles.length == 0)
		{
			return;
		}

		for (String bundleName : resourceBundles)
		{
			this.bundles.add(ResourceBundle.getBundle(bundleName));
		}
	}

	@Override
	public void addBundle(String baseName)
	{
		this.bundles.add(ResourceBundle.getBundle(baseName));
	}

	/**
	 * Check the cache for a message with the given key.
	 * <p/>
	 * If not found, check each bundle for the key.
	 * 
	 * @param key
	 *            The key of the message to find
	 * 
	 * @return The message with the given key, or <code>null</code> if one
	 *         cannot be found
	 */
	@Override
	protected MessageFormat getMessageInternal(String key)
	{
		// Check the cache first
		if (cachedMessages.containsKey(key))
		{
			return cachedMessages.get(key);
		}

		// Check each bundle in turn. First one wins.
		synchronized (cachedMessages)
		{
			// Just in case another thread populated the cache while we were blocked...
			if (cachedMessages.containsKey(key))
			{
				return cachedMessages.get(key);
			}
			for (ResourceBundle bundle : this.bundles)
			{
				if (bundle.containsKey(key))
				{
					MessageFormat message = new MessageFormat(bundle.getString(key));
					cachedMessages.put(key, message);
					return message;
				}
			}
		}
		return null;
	}
}
