package au.gov.ga.worldwind.viewer.exporter.command;

import au.gov.ga.worldwind.viewer.exporter.command.CommandExecutionResult.Status;

/**
 * A factory class with convenience methods for obtaining {@link CommandExecutionResult} instances
 */
public class CommandExecutionResultFactory
{
	private static final DefaultCommandExecutionResultImpl CONTINUE_INSTANCE = new DefaultCommandExecutionResultImpl(Status.CONTINUE);
	
	/** @return A continue result with no message */
	public static CommandExecutionResult getContinueResult()
	{
		return CONTINUE_INSTANCE;
	}
	
	/** @return A halt result with the provided message */
	public static CommandExecutionResult getHaltResult(String message)
	{
		return new DefaultCommandExecutionResultImpl(Status.HALT, message);
	}
	
	/** @return A warn result with the provided message */
	public static CommandExecutionResult getWarnResult(String message)
	{
		return new DefaultCommandExecutionResultImpl(Status.WARN, message);
	}
}
