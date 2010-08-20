package au.gov.ga.worldwind.animator.util.message;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * An implementation of the {@link MessageSource} interface that uses the Java {@link ResourceBundle} mechanism
 * to load messages.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class ResourceBundleMessageSource implements MessageSource
{
	/** The list of resource bundles to inspect */
	private List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
	
	/** The cache of retrieved messages */
	private Map<String, MessageFormat> cachedMessages = new HashMap<String, MessageFormat>();
	
	/**
	 * Constructor. Initialises the resource bundles to inspect for messages.
	 * 
	 * @param resourceBundles The names of resource bundles to inspect.
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
	public String getMessage(String key)
	{
		return getMessage(key, null, new Object[0]);
	}

	@Override
	public String getMessage(String key, String defaultMsg)
	{
		return getMessage(key, defaultMsg, new Object[0]);
	}

	@Override
	public String getMessage(String key, Object... params)
	{
		return getMessage(key, null, params);
	}

	@Override
	public String getMessage(String key, String defaultMsg, Object... params)
	{
		MessageFormat message = getMessageInternal(key);
		if (message == null)
		{
			if (defaultMsg == null)
			{
				return null;
			}
			else
			{
				return new MessageFormat(defaultMsg).format(params, new StringBuffer(), new FieldPosition(0)).toString();
			}
		}
		
		return message.format(params, new StringBuffer(), new FieldPosition(0)).toString();
	}
	
	/**
	 * Check the cache for a message with the given key.
	 * <p/>
	 * If not found, check each bundle for the key.
	 * 
	 * @param key The key of the message to find
	 * 
	 * @return The message with the given key, or <code>null</code> if one cannot be found
	 */
	private MessageFormat getMessageInternal(String key)
	{
		if (cachedMessages.containsKey(key))
		{
			return cachedMessages.get(key);
		}
		
		// Check each bundle in turn. First one wins.
		for (ResourceBundle bundle : this.bundles)
		{
			if (bundle.containsKey(key))
			{
				MessageFormat message = new MessageFormat(bundle.getString(key));
				cachedMessages.put(key, message);
				return message;
			}
		}
		return null;
	}

}
