package au.gov.ga.worldwind.viewer.exporter.command;

import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

import static au.gov.ga.worldwind.viewer.exporter.ExporterMessageConstants.*;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.*;

/**
 * A {@link SnapshotExportCommand} that creates the output location if needed
 */
public class CreateOutputLocationCommand extends AbstractSnapshotExportCommand
{
	@Override
	protected CommandExecutionResult doExecute(SnapshotExportParameters parameters)
	{
		if (!parameters.outputLocation.exists())
		{
			boolean creationSuccessful = parameters.outputLocation.mkdirs();
			if (!creationSuccessful)
			{
				return halt(getMessage(getExporterCreateOutputLocationErrorMsgKey(), parameters.outputLocation.getAbsolutePath()));
			}
		}
		return proceed();
	}
}