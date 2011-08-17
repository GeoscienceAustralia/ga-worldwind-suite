package au.gov.ga.worldwind.viewer.exporter.command;

/**
 * Indicates the result of executing a command in the command chain.
 * <p/>
 * Dictates whether processing should continue or halt.
 */
public interface CommandExecutionResult
{
	
	Status getStatus();
	String getMessage();
	
	public enum Status
	{
		HALT,
		WARN,
		CONTINUE;
	}
}
