package au.gov.ga.worldwind.viewer.exporter.command;

import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

public interface SnapshotExportCommand
{
	/**
	 * Execute the command.
	 *
	 * @return The result of executing this command
	 */
	CommandExecutionResult execute(SnapshotExportParameters parameters);
}
