/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.util.message;

import au.gov.ga.worldwind.common.util.message.CommonMessageConstants;

/**
 * Constants class that contains the keys into the animation message bundle
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnimationMessageConstants extends CommonMessageConstants
{
	public static String getAnimatorApplicationTitleKey() { return "animator.application.title"; }
	public static String getNewAnimationNameKey() { return "animator.animation.newanimationtitle"; }
	public static String getFileMenuLabelKey() { return "common.gui.menu.file.label"; }
	public static String getNewMenuLabelKey() { return "common.gui.menu.new.label"; }
	public static String getOpenMenuLabelKey() { return "common.gui.menu.open.label"; }
	public static String getSaveMenuLabelKey() { return "common.gui.menu.save.label"; }
	public static String getSaveAsMenuLabelKey() { return "common.gui.menu.saveas.label"; }
	public static String getExitMenuLabelKey() { return "common.gui.menu.exit.label"; }
	public static String getFrameMenuLabelKey() { return "animator.gui.menu.frame.label"; }
	public static String getAddKeyMenuLabelKey() { return "animator.gui.menu.addkey.label"; }
	public static String getDeleteKeyMenuLabelKey() { return "animator.gui.menu.deletekey.label"; }
	public static String getAutoKeyMenuLabelKey() { return "animator.gui.menu.autokey.label"; }
	public static String getSetFrameCountMenuLabelKey() { return "animator.gui.menu.framecount.label"; }
	public static String getPreviousFrameMenuLabelKey() { return "animator.gui.menu.previousframe.label"; }
	public static String getNextFrameMenuLabelKey() { return "animator.gui.menu.nextframe.label"; }
	public static String getPrevious10FrameMenuLabelKey() { return "animator.gui.menu.previous10frame.label"; }
	public static String getNext10FrameMenuLabelKey() { return "animator.gui.menu.next10frame.label"; }
	public static String getFirstFrameMenuLabelKey() { return "animator.gui.menu.firstframe.label"; }
	public static String getLastFrameMenuLabelKey() { return "animator.gui.menu.lastframe.label"; }
	public static String getAnimationMenuLabelKey() { return "animator.gui.menu.animation.label"; }
	public static String getUseZoomScalingMenuLabelKey() { return "animator.gui.menu.scalezoom.label"; }
	public static String getScaleAnimationMenuLabelKey() { return "animator.gui.menu.scaleanimation.label"; }
	public static String getScaleHeightMenuLabelKey() { return "animator.gui.menu.scaleheight.label"; }
	public static String getSmoothEyeSpeedMenuLabelKey() { return "animator.gui.menu.smootheyespeed.label"; }
	public static String getShowWireframeMenuLabelKey() { return "animator.gui.menu.showwireframe.label"; }
	public static String getTargetModeMenuLabelKey() { return "animator.gui.menu.targetmode.label"; }
	public static String getPreviewMenuLabelKey() { return "animator.gui.menu.preview.label"; }
	public static String getPreviewX2MenuLabelKey() { return "animator.gui.menu.previewx2.label"; }
	public static String getPreviewX10MenuLabelKey() { return "animator.gui.menu.previewx10.label"; }
	public static String getRenderMenuLabelKey() { return "animator.gui.menu.render.label"; }
	public static String getRenderMenuTooltipKey() { return "animator.gui.menu.render.tooltip"; }
	public static String getRenderHighResMenuLabelKey() { return "animator.gui.menu.renderhires.label"; }
	public static String getRenderStandardResMenuLabelKey() { return "animator.gui.menu.renderstandardres.label"; }
	public static String getResizeToRenderDimensionsLabelKey() { return "animator.gui.menu.resizetorenderdimensions.label"; }
	public static String getDebugMenuLabelKey() { return "animator.gui.menu.debug.label"; }
	public static String getKeyValuesMenuLabelKey() { return "animator.gui.menu.getkeyvalues.label"; }
	public static String getParameterValuesMenuLabelKey() { return "animator.gui.menu.getparametervalues.label"; }
	public static String getCameraNameKey() { return "animator.animation.camera.name"; }
	public static String getStereoCameraNameKey() { return "animator.animation.stereo.camera.name"; }
	public static String getCameraEyeLatNameKey() { return "animator.animation.camera.eye.lat.name"; }
	public static String getCameraEyeLonNameKey() { return "animator.animation.camera.eye.lon.name"; }
	public static String getCameraEyeZoomNameKey() { return "animator.animation.camera.eye.zoom.name"; }
	public static String getCameraLookatLatNameKey() { return "animator.animation.camera.lookat.lat.name"; }
	public static String getCameraLookatLonNameKey() { return "animator.animation.camera.lookat.lon.name"; }
	public static String getCameraLookatZoomNameKey() { return "animator.animation.camera.lookat.zoom.name"; }
	public static String getCameraRollNameKey() { return "animator.animation.camera.roll.name"; }
	public static String getCameraFieldOfViewNameKey() { return "animator.animation.camera.fieldofview.name"; }
	public static String getCameraFocalLengthNameKey() { return "animator.animation.camera.focal.length.name"; }
	public static String getCameraEyeSeparationNameKey() { return "animator.animation.camera.eye.separation.name"; }
	public static String getCameraNearClipNameKey() { return "animator.animation.camera.nearclip.name"; }
	public static String getCameraFarClipNameKey() { return "animator.animation.camera.farclip.name"; }
	public static String getOpenFailedMessageKey() { return "animator.message.openfailed.message"; }
	public static String getOpenFailedCaptionKey() { return "animator.message.openfailed.caption"; }
	public static String getSaveFailedMessageKey() { return "animator.message.savefailed.message"; }
	public static String getSaveFailedCaptionKey() { return "animator.message.savefailed.caption"; }
	public static String getOpenV1FileMessageKey() { return "animator.message.openv1file.message"; }
	public static String getOpenV1FileCaptionKey() { return "animator.message.openv1file.caption"; }
	public static String getOpenDialogTitleKey() { return "animator.gui.dialog.open.title"; }
	public static String getAboutDialogTitleKey() { return "animator.gui.dialog.about.title"; }
	public static String getSaveAsDialogTitleKey() { return "animator.gui.dialog.saveas.title"; }
	public static String getConfirmOverwriteMessageKey() { return "animator.message.overwriteconfirm.message"; }
	public static String getConfirmOverwriteCaptionKey() { return "animator.message.overwriteconfirm.caption"; }
	public static String getQuerySaveMessageKey() { return "animator.message.querysave.message"; }
	public static String getQuerySaveCaptionKey() { return "animator.message.querysave.caption"; }
	public static String getQuerySmoothEyeSpeedMessageKey() { return "animator.message.smootheyespeed.message"; }
	public static String getQuerySmoothEyeSpeedCaptionKey() { return "animator.message.smootheyespeed.caption"; }
	public static String getScaleAnimationMessageKey() { return "animator.message.scaleanimation.message"; }
	public static String getScaleAnimationCaptionKey() { return "animator.message.scaleanimation.caption"; }
	public static String getSetFrameCountMessageKey() { return "animator.message.setframecount.message"; }
	public static String getSetFrameCountCaptionKey() { return "animator.message.setframecount.caption"; }
	public static String getSetDimensionFailedMessageKey() { return "animator.message.setdimensionfailed.message"; }
	public static String getSetDimensionFailedCaptionKey() { return "animator.message.setdimensionfailed.caption"; }
	public static String getSaveRenderDialogTitleKey() { return "animator.gui.dialog.saverender.title"; }
	public static String getConfirmRenderOverwriteMessageKey() { return "animator.message.renderoverwriteconfirm.message"; }
	public static String getConfirmRenderOverwriteCaptionKey() { return "animator.message.renderoverwriteconfirm.caption"; }
	public static String getNoRecentFileMessageKey() { return "animator.gui.menu.norecentfiles.label"; }
	public static String getAnimationBrowserPanelNameKey() { return "animator.gui.panel.animationbrowser.title"; }
	public static String getAnimationBrowserRemoveObjectLabelKey() { return "animator.gui.menu.removeanimationobject.label"; }
	public static String getOpacityParameterNameKey() { return "animator.layer.opacity.name"; }
	public static String getLayerPalettePanelNameKey() { return "animator.gui.panel.layerpalette.title"; }
	public static String getAddLayerToAnimationLabelKey() { return "animator.gui.menu.addlayertoanimation.label"; }
	public static String getAddLayerToListLabelKey() { return "animator.gui.menu.addlayertolist.label"; }
	public static String getRemoveLayerFromListLabelKey() { return "animator.gui.menu.removelayerfromlist.label"; }
	public static String getQueryRemoveLayersFromListMessageKey() { return "animator.message.queryremovelayersfromlist.message"; }
	public static String getQueryRemoveLayersFromListCaptionKey() { return "animator.message.queryremovelayersfromlist.caption"; }
	public static String getObjectPropertiesPanelNameKey() { return "animator.gui.panel.objectproperties.title"; }
	public static String getObjectPropertiesPanelNoSelectionMessageKey() { return "animator.message.objectproperties.noselected"; }
	public static String getObjectPropertiesPanelNoEditableMessageKey() { return "animator.message.objectproperties.noeditable"; }
	public static String getObjectPropertiesPanelSelectionTitleKey() { return "animator.message.objectproperties.selected"; }
	public static String getObjectPropertiesPanelValueCaptionKey() { return "animator.gui.panel.objectproperties.valuecaption.label"; }
	public static String getQueryRemoveObjectFromAnimationMessageKey() {return "animator.message.queryremoveobjectfromanimation.message"; }
	public static String getQueryRemoveObjectFromAnimationCaptionKey() { return "animator.message.queryremoveobjectfromanimation.caption"; }
	public static String getAnimationBrowserMoveUpLabelKey() { return "animator.gui.menu.moveobjectup.label"; }
	public static String getAnimationBrowserMoveDownLabelKey() { return "animator.gui.menu.moveobjectdown.label"; }
	public static String getElevationNameKey() { return "animator.elevation.name"; }
	public static String getElevationExaggerationNameKey() { return "animator.elevation.exaggeration.name"; }
	public static String getAddElevationModelLabelKey() { return "animator.gui.menu.addelevationmodel.label"; }
	public static String getOpenElevationModelFailedCaptionKey() { return "animator.message.openelevationmodelfailed.caption";}
	public static String getOpenElevationModelFailedMessageKey() { return "animator.message.openelevationmodelfailed.message";}
	public static String getAddExaggeratorLabelKey() { return "animator.gui.menu.addexaggerator.label"; }
	public static String getAddExaggeratorDialogTitleKey() { return "animator.gui.dialog.newexaggerator.title"; }
	public static String getExaggeratorDialogExaggerationLabelKey() { return "animator.gui.dialog.newexaggerator.exaggeration.label"; }
	public static String getExaggeratorDialogBoundaryLabelKey() { return "animator.gui.dialog.newexaggerator.boundary.label"; }
	public static String getAddEffectLabelKey() { return "animator.gui.menu.addeffect.label"; }
	public static String getAddEffectDialogTitleKey() { return "animator.gui.dialog.addeffect.title"; }
	public static String getSetProxyLabelKey() { return "animator.gui.dialog.setproxy"; }
	public static String getProxyDialogTitleKey() { return "animator.gui.dialog.proxy.title"; }
	public static String getProxyEnabledLabelKey() { return "animator.gui.dialog.proxy.enabled"; }
	public static String getProxyUseSystemLabelKey() { return "animator.gui.dialog.proxy.usesystem"; }
	public static String getProxyHostLabelKey() { return "animator.gui.dialog.proxy.host"; }
	public static String getProxyPortLabelKey() { return "animator.gui.dialog.proxy.port"; }
	public static String getProxyTypeLabelKey() { return "animator.gui.dialog.proxy.type"; }
	public static String getProxyNonProxyHostsLabelKey() { return "animator.gui.dialog.proxy.nonproxyhosts"; }
	public static String getAnimationBrowserEnableAllLabelKey() { return "animator.gui.menu.enableall.label"; }
	public static String getAnimationBrowserDisableAllLabelKey() { return "animator.gui.menu.disableall.label"; }
	public static String getAnimationBrowserArmAllLabelKey() { return "animator.gui.menu.armall.label"; }
	public static String getAnimationBrowserDisarmAllLabelKey() { return "animator.gui.menu.disarmall.label"; }
	public static String getCopyKeyFrameLabelKey() { return "animator.gui.menu.copykey.label"; }
	public static String getCutKeyFrameLabelKey() { return "animator.gui.menu.cutkey.label"; }
	public static String getPasteKeyFrameLabelKey() { return "animator.gui.menu.pastekey.label"; }
	public static String getFogNearParameterNameKey() { return "animator.layer.fognear.name"; }
	public static String getFogFarParameterNameKey() { return "animator.layer.fogfar.name"; }
	public static String getOutlineOpacityParameterNameKey() { return "animator.layer.outlineopacity.name"; }
	public static String getShowCameraPathLabelKey() { return "animator.gui.menu.showcamerapath.label"; }
	public static String getShowGridLabelKey() { return "animator.gui.menu.showgrid.label"; }
	public static String getShowRuleOfThirdsLabelKey() { return "animator.gui.menu.showruleofthirds.label"; }
	public static String getShowCrosshairsLabelKey() { return "animator.gui.menu.showcrosshairs.label"; }
	public static String getAnimateClippingLabelKey() { return "animator.gui.menu.animateClipping.label"; }
	public static String getLogEventsLabelKey() { return "animator.gui.menu.logevents.label"; }
	public static String getRenderProgressDialogTitleKey() { return "animator.gui.dialog.renderprogress.title"; }
	public static String getRenderProgressFrameMessageKey() { return "animator.gui.dialog.renderprogress.framemessage.label"; }
	public static String getRenderProgressStartingMessageKey() { return "animator.gui.dialog.renderprogress.startingmessage.label"; }
	public static String getParameterEditorWindowLabelKey() { return "animator.gui.parametereditor.window.title"; }
	public static String getWindowMenuLabelKey() { return "animator.gui.menu.window.label"; }
	public static String getHelpMenuLabelKey() { return "animator.gui.menu.help.label"; }
	public static String getShowParameterEditorMenuLabelKey() { return "animator.gui.menu.showparametereditor.label"; }
	public static String getStereoCameraMenuLabelKey() { return "animator.gui.menu.stereo.camera.label"; }
	public static String getDynamicStereoMenuLabelKey() { return "animator.gui.menu.dynamic.stereo.label"; }
	public static String getParameterEditorLockedBezierMenuLabelKey() { return "animator.gui.parametereditor.lockedbezier.label"; }
	public static String getParameterEditorUnlockedBezierMenuLabelKey() { return "animator.gui.parametereditor.unlockedbezier.label"; }
	public static String getParameterEditorLinearMenuLabelKey() { return "animator.gui.parametereditor.linear.label"; }
	public static String getUnselectAllMenuLabelKey() { return "animator.gui.parametereditor.unselectall.label"; }
	public static String getZoomAllMenuLabelKey() { return "animator.gui.parametereditor.zoomall.label"; }
	public static String getZoomFrameMenuLabelKey() { return "animator.gui.parametereditor.zoomframe.label"; }
	public static String getZoomValueMenuLabelKey() { return "animator.gui.parametereditor.zoomvalue.label"; }
	public static String getShowWmsBrowserMenuLabelKey() { return "animator.gui.menu.showwmsbrowser.label"; }
	public static String getShowUserGuideMenuLabelKey() { return "animator.gui.menu.showuserguide.label"; }
	public static String getShowTutorialMenuLabelKey() { return "animator.gui.menu.showtutorial.label"; }
	public static String getShowAboutMenuLabelKey() { return "animator.gui.menu.showabout.label"; }
	public static String getRenderDialogTitleKey() { return "animator.gui.dialog.render.title"; }
	public static String getLicenceDialogTitleKey() { return "animator.gui.dialog.licence.title"; }
	public static String getRenderDialogRenderLabelKey() { return "animator.gui.dialog.render.renderbutton.label"; }
	public static String getRenderDialogRenderTooltipKey() { return "animator.gui.dialog.render.renderbutton.tooltip"; }
	public static String getRenderDialogApplyLabelKey() { return "animator.gui.dialog.render.applybutton.label"; }
	public static String getRenderDialogApplyTooltipKey() { return "animator.gui.dialog.render.applybutton.tooltip"; }
	public static String getRenderDialogWidthLabelKey() { return "animator.gui.dialog.render.widthfield.label"; }
	public static String getRenderDialogWidthTooltipKey() { return "animator.gui.dialog.render.widthfield.tooltip"; }
	public static String getRenderDialogHeightLabelKey() { return "animator.gui.dialog.render.heightfield.label"; }
	public static String getRenderDialogHeightTooltipKey() { return "animator.gui.dialog.render.heightfield.tooltip"; }
	public static String getRenderDialogDimensionsLabelKey() { return "animator.gui.dialog.render.dimensions.label"; }
	public static String getRenderDialogScaleLabelKey() { return "animator.gui.dialog.render.scalefield.label";}
	public static String getRenderDialogScaleTooltipKey() { return "animator.gui.dialog.render.scalefield.tooltip";}
	public static String getRenderDialogRenderSizeLabelKey() { return "animator.gui.dialog.render.rendersize.label"; }
	public static String getRenderDialogDetailLabelKey() { return "animator.gui.dialog.render.detail.label"; }
	public static String getRenderDialogDetailLevelLabelKey() { return "animator.gui.dialog.render.lodfield.label"; }
	public static String getRenderDialogDetailLevelTooltipKey() { return "animator.gui.dialog.render.lodfield.tooltip"; }
	public static String getRenderDialogDestinationLabelKey() { return "animator.gui.dialog.render.destination.label"; }
	public static String getRenderDialogOutputFieldLabelKey() { return "animator.gui.dialog.render.output.label"; }
	public static String getRenderDialogOutputFieldTooltipKey() { return "animator.gui.dialog.render.output.tooltip"; }
	public static String getRenderDialogOutputExampleLabelKey() {  return "animator.gui.dialog.render.outputexample.label"; }
	public static String getRenderDialogFrameRangeLabelKey() { return "animator.gui.dialog.render.framerange.label"; }
	public static String getRenderDialogFrameStartLabelKey() { return "animator.gui.dialog.render.framestart.label"; }
	public static String getRenderDialogFrameStartTooltipKey() { return "animator.gui.dialog.render.framestart.tooltip"; }
	public static String getRenderDialogFrameEndLabelKey() { return "animator.gui.dialog.render.frameend.label"; }
	public static String getRenderDialogFrameEndTooltipKey() { return "animator.gui.dialog.render.frameend.tooltip"; }
	public static String getRenderDialogResetLabelKey() { return "animator.gui.dialog.render.reset.label"; }
	public static String getRenderDialogResetTooltipKey() { return "animator.gui.dialog.render.reset.tooltip"; }
	public static String getShowLicenceLabelKey() { return "animator.gui.dialog.about.licence.label"; }
	public static String getCantOpenUserGuideMessageKey() { return "animator.message.openuserguidefailed.message"; }
	public static String getCantOpenUserGuideCaptionKey() { return "animator.message.openuserguidefailed.caption"; }
	public static String getCantOpenTutorialsMessageKey() { return "animator.message.opentutorialfailed.message"; }
	public static String getCantOpenTutorialsCaptionKey() { return "animator.message.opentutorialfailed.caption"; }
	public static String getDepthOfFieldNameKey() { return "animator.effect.depthoffield.name"; }
	public static String getDepthOfFieldFarParameterNameKey() { return "animator.effect.depthoffieldfar.name"; }
	public static String getDepthOfFieldNearParameterNameKey() { return "animator.effect.depthoffieldnear.name"; }
	public static String getDepthOfFieldFocusParameterNameKey() { return "animator.effect.depthoffieldfocus.name"; }
	public static String getEdgeDetectionNameKey() { return "animator.effect.edgedetection.name"; }
	public static String getClipSectorTitleKey() { return "animator.gui.menu.clipsector.title"; }
	public static String getClipSectorLabelKey() { return "animator.gui.menu.clipsector.label"; }
	public static String getClearClipLabelKey() { return "animator.gui.menu.clearclip.label"; }
	public static String getAddSunPositionLabelKey() { return "animator.gui.menu.addsunposition.label"; }
	public static String getSunPositionAnimatableNameKey() { return "animator.sunposition.name"; }
	public static String getSunPositionTypeParameterNameKey() { return "animator.sunposition.type.name"; }
	public static String getSunPositionLongitudeParameterNameKey() { return "animator.sunposition.longitude.name"; }
	public static String getSunPositionLatitudeParameterNameKey() { return "animator.sunposition.latitude.name"; }
	public static String getSunPositionTimeParameterNameKey() { return "animator.sunposition.time.name"; }
	public static String getAddHeadLabelKey() { return "animator.gui.menu.addhead.label"; }
	public static String getHeadAnimatableNameKey() { return "animator.head.name"; }
	public static String getHeadRotationXParameterNameKey() { return "animator.head.rotationx.name"; }
	public static String getHeadRotationYParameterNameKey() { return "animator.head.rotationy.name"; }
	public static String getHeadRotationZParameterNameKey() { return "animator.head.rotationz.name"; }
	public static String getHeadRotationWParameterNameKey() { return "animator.head.rotationw.name"; }
	public static String getHeadPositionXParameterNameKey() { return "animator.head.positionx.name"; }
	public static String getHeadPositionYParameterNameKey() { return "animator.head.positiony.name"; }
	public static String getHeadPositionZParameterNameKey() { return "animator.head.positionz.name"; }
}
