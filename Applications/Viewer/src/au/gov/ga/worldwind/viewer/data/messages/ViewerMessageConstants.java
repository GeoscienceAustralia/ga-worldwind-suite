package au.gov.ga.worldwind.viewer.data.messages;

import au.gov.ga.worldwind.common.util.message.CommonMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

public class ViewerMessageConstants extends CommonMessageConstants
{
	static
	{
		MessageSourceAccessor.addBundle("au.gov.ga.worldwind.viewer.data.messages.viewerMessages");
	}

	public static String getApplicationTitleKey() { return "viewer.application.title"; }
	public static String getApplicationTitleSandpitSuffixKey() { return "viewer.application.title.sandpitsuffix"; }
	public static String getHelpUrlKey() { return "viewer.help.url"; }

	public static String getGotoCoordsTitleKey() { return "viewer.dialog.gotocoords.title"; }
	public static String getAboutTitleKey() { return "viewer.dialog.about.title"; }
	public static String getPreferencesTitleKey() { return "viewer.dialog.preferences.title"; }
	public static String getControlsTitleKey() { return "viewer.dialog.controls.title"; }
	public static String getSaveSectorTitleKey() { return "viewer.dialog.savesector.title"; }
	public static String getClipSectorTitleKey() { return "viewer.dialog.clipsector.title"; }

	public static String getSaveImageOverwriteTitleKey() { return "viewer.saveimage.overwrite.title"; }
	public static String getSaveImageOverwriteMessageKey() { return "viewer.saveimage.overwrite.message"; }

	public static String getFileMenuLabelKey() { return "viewer.menu.file.label"; }
	public static String getCreateLayerMenuLabelKey() { return "viewer.menu.createlayer.label"; }
	public static String getViewMenuLabelKey() { return "viewer.menu.view.label"; }
	public static String getOptionsMenuLabelKey() { return "viewer.menu.options.label"; }
	public static String getHelpMenuLabelKey() { return "viewer.menu.help.label"; }

	public static String getOpenLayerActionLabelKey() { return "viewer.action.openlayer.label"; }
	public static String getCreateLayerFromDirectoryLabelKey() { return "viewer.action.createlayerfromdirectory.label"; }
	public static String getWorkOfflineLabelKey() { return "viewer.action.workoffline.label"; }
	public static String getScreenshotLabelKey() { return "viewer.action.screenshot.label"; }
	public static String getExitLabelKey() { return "viewer.action.exit.label"; }
	public static String getDefaultViewLabelKey() { return "viewer.action.defaultview.label"; }
	public static String getGotoCoordsLabelKey() { return "viewer.action.gotocoords.label"; }
	public static String getRenderSkirtsLabelKey() { return "viewer.action.renderskirts.label"; }
	public static String getWireframeLabelKey() { return "viewer.action.wireframe.label"; }
	public static String getWireframeDepthLabelKey() { return "viewer.action.wireframedepth.label"; }
	public static String getFullscreenLabelKey() { return "viewer.action.fullscreen.label"; }
	public static String getPreferencesLabelKey() { return "viewer.action.preferences.label"; }
	public static String getHelpLabelKey() { return "viewer.action.help.label"; }
	public static String getControlsLabelKey() { return "viewer.action.controls.label"; }
	public static String getAboutLabelKey() { return "viewer.action.about.label"; }
	public static String getSaveSectorLabelKey() { return "viewer.action.savesector.label"; }
	public static String getClipSectorLabelKey() { return "viewer.action.clipsector.label"; }
	public static String getClearClipLabelKey() { return "viewer.action.clearclip.label"; }

	public static String getTermImageKey() { return "viewer.term.image"; }

	public static String getTreeLoadingNodeLabelKey() { return "viewer.tree.loadingnode.label"; }
	public static String getTreeErrorNodeLabelKey() { return "viewer.tree.errornode.label"; }
}
