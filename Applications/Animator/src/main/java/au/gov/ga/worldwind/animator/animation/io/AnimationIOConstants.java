package au.gov.ga.worldwind.animator.animation.io;


/**
 * An interface defining accessors for constants used during animation IO.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface AnimationIOConstants
{
	
	String getRootElementName();
	
	// -------------------------------------
	// Context keys
	// -------------------------------------
	
	String getAnimationKey();
	String getParameterValueOwnerKey();
	String getCurrentLayerKey();
	String getCurrentEffectKey();
	String getWorldWindowKey();
	
	// -------------------------------------
	// XML elements and attributes
	// -------------------------------------
	
	String getWorldWindAnimationElementName();
	String getWorldWindAnimationAttributeVersion();
	String getParameterValueElementName();
	String getParameterValueAttributeValue();
	String getParameterValueAttributeFrame();
	String getParameterValueAttributeType();
	String getBezierValueAttributeInValue();
	String getBezierValueAttributeInPercent();
	String getBezierValueAttributeOutValue();
	String getBezierValueAttributeOutPercent();
	String getBezierValueAttributeLocked();
	String getParameterAttributeDefaultValue();
	String getParameterAttributeName();
	String getParameterAttributeEnabled();
	String getParameterElementName();
	String getCameraEyeLatElementName();
	String getCameraEyeLonElementName();
	String getCameraEyeElevationElementName();
	String getCameraLookatLatElementName();
	String getCameraLookatLonElementName();
	String getCameraLookatElevationElementName();
	String getCameraFocalLengthElementName();
	String getCameraEyeSeparationElementName();
	String getCameraDynamicStereoElementName();
	String getCameraNearClipElementName();
	String getCameraFarClipElementName();
	String getCameraElementName();
	String getStereoCameraElementName();
	String getAnimationAttributeFrameCount();
	String getAnimationAttributeZoomRequired();
	String getAnimatableObjectsElementName();
	String getAnimationElementName();
	String getRenderParametersElementName();
	String getHeightElementName();
	String getWidthElementName();
	String getFrameRateElementName();
	String getAnimatableLayerElementName();
	String getAnimatableAttributeName();
	String getAnimatableAttributeUrl();
	String getAnimatableAttributeEnabled();
	String getAnimatableElevationElementName();
	String getAnimatableElevationModelContainerName();
	String getAnimatableElevationModelIdentifierName();
	String getElevationExaggerationElementName();
	String getElevationExaggerationBoundaryAttributeName();
	String getLockedDimensionsElementName();
	String getRenderDestinationElementName();
	String getFrameStartElementName();
	String getFrameEndElementName();
	String getDetailLevelElementName();
	String getRenderAlphaElementName();
	String getImageScalePercentElementName();
	String getDepthOfFieldEffectElementName();
	String getDepthOfFieldNearElementName();
	String getDepthOfFieldFarElementName();
	String getDepthOfFieldFocusElementName();
	String getEdgeDetectionEffectElementName();

	/**
	 * Common constants that don't change across versions
	 */
	public static abstract class BaseConstants implements AnimationIOConstants
	{
		public String getParameterValueOwnerKey() {return "parameterValueOwner";}
		public String getAnimationKey() {return "currentAnimation";}
		public String getWorldWindowKey() {return "worldWindow";}
		public String getCurrentLayerKey() {return "currentLayer";}
		public String getCurrentEffectKey() {return "currentEffect";}
		public String getRootElementName() {return "restorableState";}
		public String getWorldWindAnimationElementName(){return null;}
		public String getWorldWindAnimationAttributeVersion(){return null;}
		public String getParameterValueElementName(){return null;}
		public String getParameterValueAttributeValue(){return null;}
		public String getParameterValueAttributeFrame(){return null;}
		public String getParameterValueAttributeType(){return null;}
		public String getBezierValueAttributeInValue(){return null;}
		public String getBezierValueAttributeInPercent(){return null;}
		public String getBezierValueAttributeOutValue(){return null;}
		public String getBezierValueAttributeOutPercent(){return null;}
		public String getBezierValueAttributeLocked(){return null;}
		public String getParameterAttributeDefaultValue(){return null;}
		public String getParameterAttributeName(){return null;}
		public String getParameterAttributeEnabled(){return null;}
		public String getParameterElementName(){return null;}
		public String getCameraEyeLatElementName(){return null;}
		public String getCameraEyeLonElementName(){return null;}
		public String getCameraEyeElevationElementName(){return null;}
		public String getCameraLookatLatElementName(){return null;}
		public String getCameraLookatLonElementName(){return null;}
		public String getCameraLookatElevationElementName(){return null;}
		public String getCameraFocalLengthElementName() {return null;}
		public String getCameraEyeSeparationElementName() {return null;}
		public String getCameraDynamicStereoElementName() {return null;}
		public String getCameraNearClipElementName() {return null;}
		public String getCameraFarClipElementName() {return null;}
		public String getCameraElementName(){return null;}
		public String getStereoCameraElementName(){return null;}
		public String getAnimationAttributeFrameCount(){return null;}
		public String getAnimationAttributeZoomRequired(){return null;}
		public String getAnimatableObjectsElementName(){return null;}
		public String getAnimationElementName(){return null;}
		public String getRenderParametersElementName(){return null;}
		public String getHeightElementName(){return null;}
		public String getWidthElementName(){return null;}
		public String getFrameRateElementName(){return null;}
		public String getAnimatableLayerElementName() {return null;}
		public String getAnimatableAttributeName() {return null;}
		public String getAnimatableAttributeUrl() {return null;}
		public String getAnimatableAttributeEnabled() {return null;}
		public String getAnimatableElevationElementName() {return null;}
		public String getAnimatableElevationModelContainerName() {return null;}
		public String getAnimatableElevationModelIdentifierName() {return null;}
		public String getElevationExaggerationElementName() {return null;}
		public String getElevationExaggerationBoundaryAttributeName() {return null;}
		public String getLockedDimensionsElementName() {return null;}
		public String getRenderDestinationElementName() {return null;}
		public String getFrameStartElementName() {return null;}
		public String getFrameEndElementName() {return null;}
		public String getDetailLevelElementName() {return null;}
		public String getRenderAlphaElementName() {return null;}
		public String getImageScalePercentElementName() {return null;}
		public String getDepthOfFieldEffectElementName() {return null;}
		public String getDepthOfFieldNearElementName() {return null;}
		public String getDepthOfFieldFarElementName() {return null;}
		public String getDepthOfFieldFocusElementName() {return null;}
		public String getEdgeDetectionEffectElementName() {return null;}
	}
	
	/**
	 * Implementation of the {@link AnimationIOConstants} for V2 files
	 */
	public static class V2 extends BaseConstants implements AnimationIOConstants
	{
		public String getRootElementName() {return getWorldWindAnimationElementName();}
		public String getParameterValueElementName() {return "parameterValue";}
		public String getParameterValueAttributeValue() {return "value";}
		public String getParameterValueAttributeFrame() {return "frame";}
		public String getParameterValueAttributeType() {return "type";}
		public String getBezierValueAttributeInValue() {return "inValue";}
		public String getBezierValueAttributeInPercent() {return "inPercent";}
		public String getBezierValueAttributeOutValue() {return "outValue";}
		public String getBezierValueAttributeOutPercent() {return "outPercent";}
		public String getBezierValueAttributeLocked() {return "locked";}
		public String getParameterAttributeDefaultValue() {return "defaultValue";}
		public String getParameterAttributeName() {return "name";}
		public String getParameterElementName() {return "parameter";}
		public String getParameterAttributeEnabled() {return "enabled";}
		public String getCameraEyeLatElementName() {return "eyeLat";}
		public String getCameraEyeLonElementName() {return "eyeLon";}
		public String getCameraEyeElevationElementName() {return "eyeElevation";}
		public String getCameraLookatLatElementName() {return "lookAtLat";}
		public String getCameraLookatLonElementName() {return "lookAtLon";}
		public String getCameraLookatElevationElementName() {return "lookAtElevation";}
		public String getCameraFocalLengthElementName() {return "focalLength";}
		public String getCameraEyeSeparationElementName() {return "eyeSeparation";}
		public String getCameraDynamicStereoElementName() {return "dynamicStereo";}
		public String getCameraNearClipElementName() {return "nearClip";}
		public String getCameraFarClipElementName() {return "farClip";}
		public String getCameraElementName() {return "camera";}
		public String getStereoCameraElementName(){return "stereoCamera";}
		public String getAnimationAttributeFrameCount() {return "frameCount";}
		public String getAnimationAttributeZoomRequired() {return "zoomRequired";}
		public String getAnimationElementName() {return "animation";}
		public String getAnimatableObjectsElementName() {return "animatableObjects";}
		public String getRenderParametersElementName() {return "renderParameters";}
		public String getFrameRateElementName() {return "frameRate";}
		public String getWidthElementName() {return "width";}
		public String getHeightElementName() {return "height";}
		public String getWorldWindAnimationElementName() {return "worldWindAnimation";}
		public String getWorldWindAnimationAttributeVersion() {return "version";}
		public String getAnimatableLayerElementName() {return "layer";}
		public String getAnimatableAttributeName() {return "name";}
		public String getAnimatableAttributeUrl() {return "url";}
		public String getAnimatableAttributeEnabled() {return "enabled";}
		public String getAnimatableElevationElementName() {return "elevation";}
		public String getAnimatableElevationModelContainerName() {return "models";}
		public String getAnimatableElevationModelIdentifierName() {return "model";}
		public String getElevationExaggerationElementName() {return "exaggerator";}
		public String getElevationExaggerationBoundaryAttributeName() {return "boundary";}
		public String getLockedDimensionsElementName() { return "locked";}
		public String getRenderDestinationElementName() { return "renderDestination"; }
		public String getFrameStartElementName() {return "startFrame";}
		public String getFrameEndElementName() {return "endFrame";}
		public String getDetailLevelElementName() {return "detailLevel";}
		public String getRenderAlphaElementName() {return "renderAlpha";}
		public String getImageScalePercentElementName() {return "imageScalePercent";}
		public String getDepthOfFieldEffectElementName() {return "depthoffield";}
		public String getDepthOfFieldNearElementName() {return "near";}
		public String getDepthOfFieldFarElementName() {return "far";}
		public String getDepthOfFieldFocusElementName() {return "focus";}
		public String getEdgeDetectionEffectElementName() {return "edgeeffect";}
	}
	
	/**
	 * Implementation of the {@link AnimationIOConstants} for V1 files
	 */
	public static class V1 extends BaseConstants implements AnimationIOConstants
	{
		public String getRootElementName() {return "restorableState";}
	}

}
