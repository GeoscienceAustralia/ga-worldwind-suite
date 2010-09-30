package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.AbstractLayer;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;

/**
 * An interface defining accessors for constants used during animation IO.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationIOConstants
{
	
	/**
	 * @return The name of the root element in the file
	 */
	String getRootElementName();
	
	// -------------------------------------
	// Context keys
	// -------------------------------------
	
	/**
	 * @return The context key for the animation in context. Of type {@link Animation}.
	 */
	String getAnimationKey();

	/**
	 * @return The context key for the parameter value owner. Of type {@link Parameter}.
	 */
	String getParameterValueOwnerKey();
	
	/**
	 * @return The context key for the layer associated with a {@link LayerParameter} and {@link AnimatableLayer}. Of type {@link AbstractLayer}
	 */
	String getCurrentLayerKey();
	
	/**
	 * @return The context key for the world window in context. Of type {@link WorldWindow}.
	 */
	String getWorldWindowKey();
	
	// -------------------------------------
	// XML elements and attributes
	// -------------------------------------
	
	/**
	 * @return The root world wind animation element name
	 */
	String getWorldWindAnimationElementName();
	
	/**
	 * @return The world wind animation version attribute name
	 */
	String getWorldWindAnimationAttributeVersion();
	
	/**
	 * @return The name of the 'parameter value' element
	 */
	String getParameterValueElementName();
	
	/**
	 * @return The name of the 'value' attribute on the parameter value element
	 */
	String getParameterValueAttributeValue();
	
	/**
	 * @return The name of the 'frame' attribute on the parameter value element
	 */
	String getParameterValueAttributeFrame();
	
	/**
	 * @return The name of the 'type' attribute on the parameter value element
	 */
	String getParameterValueAttributeType();
	
	/**
	 * @return The name of the 'in value' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeInValue();

	/**
	 * @return The name of the 'in percent' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeInPercent();

	/**
	 * @return The name of the 'out value' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeOutValue();

	/**
	 * @return The name of the 'out percent' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeOutPercent();

	/**
	 * @return The name of the 'locked' attribute on the bezier parameter value element
	 */
	String getBezierValueAttributeLocked();

	/**
	 * @return The name of the 'default value' attribute on the parameter element
	 */
	String getParameterAttributeDefaultValue();
	
	/**
	 * @return The name of the 'name' attribute on the parameter element
	 */
	String getParameterAttributeName();
	
	/**
	 * @return The name of the 'enabled' attribute on the parameter element
	 */
	String getParameterAttributeEnabled();
	
	/**
	 * @return The name of the 'parameter' element
	 */
	String getParameterElementName();
	
	/**
	 * @return The name of the 'eye lat' element of the camera
	 */
	String getCameraEyeLatElementName();
	
	/**
	 * @return The name of the 'eye lon' element of the camera
	 */
	String getCameraEyeLonElementName();
	
	/**
	 * @return The name of the 'eye elevation' element of the camera
	 */
	String getCameraEyeElevationElementName();
	
	/**
	 * @return The name of the 'Lookat lat' element of the camera
	 */
	String getCameraLookatLatElementName();
	
	/**
	 * @return The name of the 'Lookat lon' element of the camera
	 */
	String getCameraLookatLonElementName();
	
	/**
	 * @return The name of the 'Lookat elevation' element of the camera
	 */
	String getCameraLookatElevationElementName();
	
	/**
	 * @return The name of the 'name' attribute of the camera
	 */
	String getCameraAttributeName();
	
	/**
	 * @return The name of the 'camera' element
	 */
	String getCameraElementName();
	
	/**
	 * @return The name of the 'frame count' attribute of the animation element
	 */
	String getAnimationAttributeFrameCount();
	
	/**
	 * @return The name of the 'zoom required' attribute of the animation element 
	 */
	String getAnimationAttributeZoomRequired();
	
	/**
	 * @return The name of the 'animatable objects' element
	 */
	String getAnimatableObjectsElementName();

	/**
	 * @return The name of the 'animation' element
	 */
	String getAnimationElementName();
	
	/**
	 * @return The name of the 'render parameters' element
	 */
	String getRenderParametersElementName();
	
	/**
	 * @return The name of the 'height' element
	 */
	String getHeightElementName();

	/**
	 * @return The name of the 'width' element
	 */
	String getWidthElementName();

	/**
	 * @return The name of the 'frame rate' element
	 */
	String getFrameRateElementName();
	
	/**
	 * @return The name of the 'animatable layer' 
	 */
	String getAnimatableLayerName();
	
	/**
	 * @return The name of the 'name' attribute on the layer element
	 */
	String getAnimatableLayerAttributeName();
	
	/**
	 * @return The name of the 'url' attribute on the layer element
	 */
	String getAnimatableLayerAttributeUrl();
	
	/**
	 * @return The name of the 'enabled' attribute on the layer element
	 */
	String getAnimatableLayerAttributeEnabled();
	
	/**
	 * @return The name of the 'animatable elevation' element
	 */
	String getAnimatableElevationName();
	
	/**
	 * @return The name of the 'name' attribute on the layer element
	 */
	String getAnimatableElevationAttributeName();
	
	/**
	 * @return The name of the 'enabled' attribute on the layer element
	 */
	String getAnimatableElevationAttributeEnabled();
	
	/**
	 * @return The name of the 'elevation models' element
	 */
	String getAnimatableElevationModelContainerName();
	
	/**
	 * @return The name of the 'elevation model' element
	 */
	String getAnimatableElevationModelIdentifierName();
	
	/**
	 * @return The name of the 'name' attribute of the 'elevation model' element
	 */
	String getAnimatableElevationModelIdentifierAttributeName();
	
	/**
	 * @return The name of the 'url' attribute of the 'elevation model' element
	 */
	String getAnimatableElevationModelIdentifierAttributeUrl();
	
	/**
	 * @return The name of the 'elevation exaggeration' element
	 */
	String getElevationExaggerationName();
	
	/**
	 * @return The name of the 'boundary' attribute of the 'elevation exaggeration' element
	 */
	String getElevationExaggerationAttributeBoundary();
	
	/**
	 * Common constants that don't change across versions
	 */
	public static abstract class BaseConstants implements AnimationIOConstants
	{
		public String getParameterValueOwnerKey() {return "parameterValueOwner";}
		public String getAnimationKey() {return "currentAnimation";}
		public String getWorldWindowKey() {return "worldWindow";}
		public String getCurrentLayerKey() {return "currentLayer";}
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
		public String getCameraAttributeName(){return null;}
		public String getCameraElementName(){return null;}
		public String getAnimationAttributeFrameCount(){return null;}
		public String getAnimationAttributeZoomRequired(){return null;}
		public String getAnimatableObjectsElementName(){return null;}
		public String getAnimationElementName(){return null;}
		public String getRenderParametersElementName(){return null;}
		public String getHeightElementName(){return null;}
		public String getWidthElementName(){return null;}
		public String getFrameRateElementName(){return null;}
		public String getAnimatableLayerName() {return null;}
		public String getAnimatableLayerAttributeName() {return null;}
		public String getAnimatableLayerAttributeUrl() {return null;}
		public String getAnimatableLayerAttributeEnabled() {return null;}
		public String getAnimatableElevationName() {return null;}
		public String getAnimatableElevationAttributeName() {return null;}
		public String getAnimatableElevationAttributeEnabled() {return null;}
		public String getAnimatableElevationModelContainerName() {return null;}
		public String getAnimatableElevationModelIdentifierName() {return null;}
		public String getAnimatableElevationModelIdentifierAttributeName() {return null;}
		public String getAnimatableElevationModelIdentifierAttributeUrl() {return null;}
		public String getElevationExaggerationName() {return null;}
		public String getElevationExaggerationAttributeBoundary() {return null;}
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
		public String getCameraAttributeName() {return "name";}
		public String getCameraElementName() {return "camera";}
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
		public String getAnimatableLayerName() {return "layer";}
		public String getAnimatableLayerAttributeName() {return "name";}
		public String getAnimatableLayerAttributeUrl() {return "url";}
		public String getAnimatableLayerAttributeEnabled() {return "enabled";}
		public String getAnimatableElevationName() {return "elevation";}
		public String getAnimatableElevationAttributeName() {return "name";}
		public String getAnimatableElevationAttributeEnabled() {return "enabled";}
		public String getAnimatableElevationModelContainerName() {return "models";}
		public String getAnimatableElevationModelIdentifierName() {return "model";}
		public String getAnimatableElevationModelIdentifierAttributeName() {return "name";}
		public String getAnimatableElevationModelIdentifierAttributeUrl() {return "url";}
		public String getElevationExaggerationName() {return "exaggerator";}
		public String getElevationExaggerationAttributeBoundary() {return "boundary";}
	}
	
	/**
	 * Implementation of the {@link AnimationIOConstants} for V1 files
	 */
	public static class V1 extends BaseConstants implements AnimationIOConstants
	{
		public String getRootElementName() {return "restorableState";}
	}

}
