package au.gov.ga.worldwind.viewer.exporter.parameters;

import java.io.File;

/**
 * Parameters used to control what occurs when a standalone snapshot is generated.
 */
public class SnapshotExportParameters
{
	/** The display name to use for the theme */
	public String displayName;
	
	/** An identifier used when naming configuration files etc. */
	public String themeId;
	
	/** The root folder for the snapshot output */
	public File outputLocation;
	
	/** Whether to copy the user's cache as an offline cache source */
	public boolean copyUserCache = true;
	
	/** Whether to export the current places list */
	public boolean exportPlaces = true;
	
	/** Whether to create a run.bat file for the distro */
	public boolean createExecutables = true;
	
	// Theme configuration items
	public boolean includeCompass = true;
	public boolean includeScalebar = true;
	public boolean includeWorldMap = true;
	public boolean includeNavControls = true;
	
	public boolean includeToolBar = true;
	public boolean includeStatusBar = true;
	public boolean includeMenuBar = true;
	public boolean includeWmsBrowser = true;
	
	public boolean includeDatasetPanel = true;
	public boolean includeLayersPanel = true;
	public boolean includePlacesPanel = true;
	public boolean includeExaggerationPanel = true;
	public boolean includeSearchPanel = true;
}
