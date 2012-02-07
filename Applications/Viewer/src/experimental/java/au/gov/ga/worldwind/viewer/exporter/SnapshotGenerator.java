package au.gov.ga.worldwind.viewer.exporter;

import au.gov.ga.worldwind.viewer.exporter.parameters.SnapshotExportParameters;

/**
 * An interface for snapshot generators
 */
public interface SnapshotGenerator
{
	/**
	 * Export a snapshot of the current state of the system as an
	 * offline standalone version.
	 */
	void exportSnapshot(SnapshotExportParameters parameters);

}
