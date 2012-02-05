package au.gov.ga.worldwind.viewer.exporter.command;

import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

import static au.gov.ga.worldwind.viewer.exporter.ExporterMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;

/**
 * A base implementation of the {@link SnapshotExportCommand} that provides
 * some useful utility methods
 */
public abstract class AbstractSnapshotExportCommand implements SnapshotExportCommand
{
	@Override
	final public CommandExecutionResult execute(SnapshotExportParameters parameters)
	{
		if (parameters == null)
		{
			return halt(getMessage(getExporterMissingParametersErrorMsgKey()));
		}
		return doExecute(parameters);
	}

	/**
	 * @return A halt result with the provided message.
	 */
	final protected CommandExecutionResult halt(String message)
	{
		return CommandExecutionResultFactory.getHaltResult(message);
	}
	
	/**
	 * @return A warn result with the provided message.
	 */
	final protected CommandExecutionResult warn(String message)
	{
		return CommandExecutionResultFactory.getWarnResult(message);
	}
	
	/**
	 * @return A proceed result with the provided message.
	 */
	final protected CommandExecutionResult proceed()
	{
		return CommandExecutionResultFactory.getContinueResult();
	}
	
	/**
	 * Perform sub-class specific execution logic
	 */
	protected abstract CommandExecutionResult doExecute(SnapshotExportParameters parameters);
}
