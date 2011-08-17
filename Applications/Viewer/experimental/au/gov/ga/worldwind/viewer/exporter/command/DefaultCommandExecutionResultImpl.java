package au.gov.ga.worldwind.viewer.exporter.command;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * An immutable implementation of the {@link CommandExecutionResult} class
 */
public class DefaultCommandExecutionResultImpl implements CommandExecutionResult
{
	private Status status;
	private String message;
	
	public DefaultCommandExecutionResultImpl(Status status)
	{
		this(status, null);
	}
	
	public DefaultCommandExecutionResultImpl(Status status, String message)
	{
		Validate.notNull(status, "A status is required");
		this.status = status;
		this.message = message;
	}

	@Override
	public Status getStatus()
	{
		return status;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

}
