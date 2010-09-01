package au.gov.ga.worldwind.animator.util.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the {@link MessageSource} interface that uses a backing map
 * to source messages.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class StaticMessageSource extends MessageSourceBase implements MessageSource
{
	/** The backing map */
	private Map<String, MessageFormat> messages = new HashMap<String, MessageFormat>();

	/**
	 * Add the provided message to this source
	 * 
	 * @param key The key for the message
	 * @param messageFormat The message format, formatted according to {@link MessageFormat} conventions.
	 */
	public void addMessage(String key, String messageFormat)
	{
		this.messages.put(key, new MessageFormat(messageFormat));
	}
	
	@Override
	protected MessageFormat getMessageInternal(String key)
	{
		return messages.get(key);
	}
}
