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

	// Dialogs
	public static String getGotoCoordsTitleKey() { return "viewer.dialog.gotocoords.title"; }
	public static String getAboutTitleKey() { return "viewer.dialog.about.title"; }
	public static String getPreferencesTitleKey() { return "viewer.dialog.preferences.title"; }
	public static String getControlsTitleKey() { return "viewer.dialog.controls.title"; }
	public static String getSaveSectorTitleKey() { return "viewer.dialog.savesector.title"; }
	public static String getClipSectorTitleKey() { return "viewer.dialog.clipsector.title"; }
	public static String getNewLayerDialogTitleKey() { return "viewer.dialog.newlayer.title"; }
	public static String getEditLayerDialogTitleKey() { return "viewer.dialog.editlayer.title"; }
	public static String getOpenLayerDialogTitleKey() { return "viewer.dialog.openlayer.title"; }
	public static String getDeleteLayerDialogTitleKey() { return "viewer.dialog.deletelayer.title"; }

	// Messages
	public static String getSaveImageOverwriteTitleKey() { return "viewer.saveimage.overwrite.title"; }
	public static String getSaveImageOverwriteMessageKey() { return "viewer.saveimage.overwrite.message"; }
	public static String getRefreshLayerConfirmationMessageKey() { return "viewer.refreshlayer.message"; }
	public static String getOpenLayerErrorMessageKey() { return "viewer.openlayer.error.message"; }
	public static String getOpenLayerErrorTitleKey() { return "viewer.openlayer.error.title"; }
	public static String getConfirmDeleteLayerMultipleMessageKey() { return "viewer.deletelayer.confirm.multiple.message"; }
	public static String getConfirmDeleteLayerSingleMessageKey() { return "viewer.deletelayer.confirm.single.message"; }
	public static String getGotoLocationSupportsMessageKey() { return "viewer.gotolocation.supports.message"; }
	public static String getGotoLocationInvalidMessageKey() { return "viewer.gotolocation.invalid.message"; }
	
	// Field labels
	public static String getGotoLocationPromptLabelKey() { return "viewer.gotolocation.prompt.label"; }
	public static String getGotoLocationPromptTooltipKey() { return "viewer.gotolocation.prompt.tooltip"; }
	public static String getGotoLocationGotoTooltipKey() { return "viewer.gotolocation.go.tooltip"; }
	
	// Panels
	public static String getLayersPanelTitleKey() { return "viewer.panel.layers.title"; }
	public static String getDatasetsPanelTitleKey() { return "viewer.panel.datasets.title"; }
	
	// Menu items
	public static String getFileMenuLabelKey() { return "viewer.menu.file.label"; }
	public static String getCreateLayerMenuLabelKey() { return "viewer.menu.createlayer.label"; }
	public static String getViewMenuLabelKey() { return "viewer.menu.view.label"; }
	public static String getOptionsMenuLabelKey() { return "viewer.menu.options.label"; }
	public static String getHelpMenuLabelKey() { return "viewer.menu.help.label"; }

	// Actions
	public static String getOpenLayerActionLabelKey() { return "viewer.action.openlayer.label"; }
	public static String getCreateLayerFromDirectoryLabelKey() { return "viewer.action.createlayerfromdirectory.label"; }
	public static String getWorkOfflineLabelKey() { return "viewer.action.workoffline.label"; }
	public static String getScreenshotLabelKey() { return "viewer.action.screenshot.label"; }
	public static String getExitLabelKey() { return "viewer.action.exit.label"; }
	public static String getDefaultViewLabelKey() { return "viewer.action.defaultview.label"; }
	public static String getGotoCoordsLabelKey() { return "viewer.action.gotocoords.label"; }
	public static String getRenderSkirtsLabelKey() { return "viewer.action.renderskirts.label"; }
	public static String getWireframeLabelKey() { return "viewer.action.wireframe.label"; }
	public static String getWireframeElevationLabelKey() { return "viewer.action.wireframeelevation.label"; }
	public static String getWireframeDepthLabelKey() { return "viewer.action.wireframedepth.label"; }
	public static String getFullscreenLabelKey() { return "viewer.action.fullscreen.label"; }
	public static String getPreferencesLabelKey() { return "viewer.action.preferences.label"; }
	public static String getHelpLabelKey() { return "viewer.action.help.label"; }
	public static String getControlsLabelKey() { return "viewer.action.controls.label"; }
	public static String getAboutLabelKey() { return "viewer.action.about.label"; }
	public static String getSaveSectorLabelKey() { return "viewer.action.savesector.label"; }
	public static String getClipSectorLabelKey() { return "viewer.action.clipsector.label"; }
	public static String getClearClipLabelKey() { return "viewer.action.clearclip.label"; }
	public static String getLaunchWmsBrowserLabelKey() { return "viewer.action.wmsbrowser.label"; }
	
	public static String getLayersNewFolderLabelKey() { return "viewer.action.layers.newfolder.label"; }
	public static String getLayersNewFolderTooltipKey() { return "viewer.action.layers.newfolder.tooltip"; }
	public static String getLayersNewLayerLabelKey() { return "viewer.action.layers.newlayer.label"; }
	public static String getLayersNewLayerTooltipKey() { return "viewer.action.layers.newlayer.tooltip"; }
	public static String getLayersOpenLayerLabelKey() { return "viewer.action.layers.openlayer.label"; }
	public static String getLayersOpenLayerTooltipKey() { return "viewer.action.layers.openlayer.tooltip"; }
	public static String getLayersRenameLabelKey() { return "viewer.action.layers.rename.label"; }
	public static String getLayersRenameTooltipKey() { return "viewer.action.layers.rename.tooltip"; }
	public static String getLayersEditLabelKey() { return "viewer.action.layers.edit.label"; }
	public static String getLayersEditTooltipKey() { return "viewer.action.layers.edit.tooltip"; }
	public static String getLayersDeleteLabelKey() { return "viewer.action.layers.delete.label"; }
	public static String getLayersDeleteTooltipKey() { return "viewer.action.layers.delete.tooltip"; }
	public static String getLayersExpandAllLabelKey() { return "viewer.action.layers.expandall.label"; }
	public static String getLayersExpandAllTooltipKey() { return "viewer.action.layers.expandall.tooltip"; }
	public static String getLayersCollapseAllLabelKey() { return "viewer.action.layers.collapseall.label"; }
	public static String getLayersCollapseAllTooltipKey() { return "viewer.action.layers.collapseall.tooltip"; }
	public static String getLayersRefreshLayerLabelKey() { return "viewer.action.layers.refresh.label"; }
	public static String getLayersRefreshLayerTooltipKey() { return "viewer.action.layers.refresh.tooltip"; }
	public static String getLayersReloadLayerLabelKey() { return "viewer.action.layers.reload.label"; }
	public static String getLayersReloadLayerTooltipKey() { return "viewer.action.layers.reload.tooltip"; }
	public static String getLayersEnableLayerLabelKey() { return "viewer.action.layers.enable.label"; }
	public static String getLayersEnableLayerTooltipKey() { return "viewer.action.layers.enable.tooltip"; }
	public static String getLayersDisableLayerLabelKey() { return "viewer.action.layers.disable.label"; }
	public static String getLayersDisableLayerTooltipKey() { return "viewer.action.layers.disable.tooltip"; }
	public static String getLayersOpacityTooltipKey() { return "viewer.action.layers.opacity.tooltip"; }
	
	public static String getPlacesAddLabelKey() { return "viewer.action.places.add.label";}
	public static String getPlacesAddTooltipKey() { return "viewer.action.places.add.tooltip";}
	public static String getPlacesEditLabelKey() { return "viewer.action.places.edit.label";}
	public static String getPlacesEditTooltipKey() { return "viewer.action.places.edit.tooltip";}
	public static String getPlacesDeleteLabelKey() { return "viewer.action.places.delete.label";}
	public static String getPlacesDeleteTooltipKey() { return "viewer.action.places.delete.tooltip";}
	public static String getPlacesPlayLabelKey() { return "viewer.action.places.play.label";}
	public static String getPlacesPlayTooltipKey() { return "viewer.action.places.play.tooltip";}
	public static String getPlacesStopTooltipKey() { return "viewer.action.places.stop.tooltip";}
	public static String getPlacesImportLabelKey() { return "viewer.action.places.import.label";}
	public static String getPlacesImportTooltipKey() { return "viewer.action.places.import.tooltip";}
	public static String getPlacesExportLabelKey() { return "viewer.action.places.export.label";}
	public static String getPlacesExportTooltipKey() { return "viewer.action.places.export.tooltip";}
	public static String getPlacesNextLabelKey() { return "viewer.action.places.next.label";}
	public static String getPlacesNextTooltipKey() { return "viewer.action.places.next.tooltip";}
	public static String getPlacesPreviousLabelKey() { return "viewer.action.places.previous.label";}
	public static String getPlacesPreviousTooltipKey() { return "viewer.action.places.previous.tooltip";}
	
	// Terms
	public static String getTermImageKey() { return "viewer.term.image"; }
	public static String getTermLayerKey() { return "viewer.term.layer"; }
	public static String getTermFolderKey() { return "viewer.term.folder"; }

	public static String getTreeNewFolderLabelKey() { return "viewer.tree.newfolder.label"; }
	public static String getTreeWmsRootNodeLabel() { return "viewer.tree.wmsrootnode.label"; }
	public static String getTreeLoadingNodeLabelKey() { return "viewer.tree.loadingnode.label"; }
	public static String getTreeErrorNodeLabelKey() { return "viewer.tree.errornode.label"; }
	
	
}
