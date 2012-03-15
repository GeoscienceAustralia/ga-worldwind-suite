package au.gov.ga.worldwind.viewer.exporter;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.viewer.exporter.command.CommandExecutionResult;
import au.gov.ga.worldwind.viewer.exporter.command.CommandExecutionResult.Status;
import au.gov.ga.worldwind.viewer.exporter.command.CreateOutputLocationCommand;
import au.gov.ga.worldwind.viewer.exporter.command.SnapshotExportCommand;
import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

/**
 * Default implementation of the {@link SnapshotGenerator} interface.
 * <p/>
 * Uses an ordered list of registered {@link SnapshotExportCommand}s to
 * control the generation process.
 */
public class DefaultSnapshotGenerator implements SnapshotGenerator
{
	private List<SnapshotExportCommand> commands;
	
	public DefaultSnapshotGenerator()
	{
		commands = new ArrayList<SnapshotExportCommand>();
		commands.add(new CreateOutputLocationCommand());
	}

	@Override
	public void exportSnapshot(SnapshotExportParameters parameters)
	{
		if (parameters == null)
		{
			return;
		}
		
		for (SnapshotExportCommand command : commands)
		{
			CommandExecutionResult executionResult = command.execute(parameters);
			
			if (executionResult.getStatus() == Status.CONTINUE)
			{
				continue;
			}
			
			notifyUser(executionResult.getMessage());
			
			if (executionResult.getStatus() == Status.HALT)
			{
				break;
			}
		}
	}

	private void notifyUser(String message)
	{
		// TODO
	}
	
}
