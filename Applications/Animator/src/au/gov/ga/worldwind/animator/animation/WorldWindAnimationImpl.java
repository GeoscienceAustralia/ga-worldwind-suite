package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.camera.CameraImpl;
import au.gov.ga.worldwind.animator.animation.elevation.AnimatableElevation;
import au.gov.ga.worldwind.animator.animation.elevation.DefaultAnimatableElevation;
import au.gov.ga.worldwind.animator.animation.event.PropagatingChangeableEventListener;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.layer.AnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.DefaultAnimatableLayer;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerOpacityParameter;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoader;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

/**
 * An implementation of the {@link Animation} interface for animations using the
 * WorldWind SDK.
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class WorldWindAnimationImpl extends PropagatingChangeableEventListener implements Animation
{
	/** The default number of frames for an animation */
	private static final int DEFAULT_FRAME_COUNT = 100;
	
	/** Map of <code>frame -> key frame</code>, ordered by frame, for quick lookup of key frames */
	private SortedMap<Integer, KeyFrame> keyFrameMap = new TreeMap<Integer, KeyFrame>();
	
	/** The number of frames in this animation */
	private int frameCount;
	
	/** The render parameters for this animation */
	private RenderParameters renderParameters;
	
	/** The camera to use for rendering the animation */
	private Camera renderCamera;
	
	/** The list of animatable objects in this animation */
	private List<Animatable> animatableObjects = new ArrayList<Animatable>();
	
	/** The list of animatable layers in this animation. A subset of the {@link #animatableObjects} list. */
	private List<AnimatableLayer> animatableLayers = new ArrayList<AnimatableLayer>();
	
	/** The elevation model being used in this animation */
	private AnimatableElevation animatableElevation;
	
	/** Whether or not zoom scaling should be applied */
	private boolean zoomRequired = true;
	
	/** The current frame of the animation */
	private int currentFrame = 0;
	
	/** The worldwind window */
	private WorldWindow worldWindow;

	/** The name of this animation (from {@link Nameable}) */
	private String name;
	
	/**
	 * Constructor. Initialises default values.
	 */
	public WorldWindAnimationImpl(WorldWindow worldWindow)
	{
		Validate.notNull(worldWindow, "A world window is required");
		this.worldWindow = worldWindow;
		this.frameCount = DEFAULT_FRAME_COUNT;
		this.renderParameters = new RenderParameters();
		
		this.renderCamera = new CameraImpl(this);
		this.animatableObjects.add(renderCamera);
		
		this.animatableElevation = new DefaultAnimatableElevation(this);
		this.animatableObjects.add(animatableElevation);
		
		this.name = MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getAnimatorApplicationTitleKey());
	}
	
	@Override
	public int getKeyFrameCount()
	{
		return keyFrameMap.size();
	}
	
	@Override
	public boolean hasKeyFrames()
	{
		return !keyFrameMap.isEmpty();
	}
	
	@Override
	public List<KeyFrame> getKeyFrames()
	{
		return new ArrayList<KeyFrame>(keyFrameMap.values());
	}
	
	@Override
	public List<KeyFrame> getKeyFrames(Parameter p)
	{
		List<KeyFrame> result = new ArrayList<KeyFrame>();
		for (KeyFrame keyFrame : getKeyFrames())
		{
			if (keyFrame.hasValueForParameter(p))
			{
				result.add(keyFrame);
			}
		}
		return result;
	}
	
	@Override
	public KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame)
	{
		// Note: The ArrayList is used to achieve good random access performance so we can iterate backwards.
		// This is not required for the forward-looking version of this method
		List<KeyFrame> candidateKeys = new ArrayList<KeyFrame>(keyFrameMap.headMap(frame).values());
		for (int i = candidateKeys.size()-1; i >= 0; i--)
		{
			KeyFrame candidateKey = candidateKeys.get(i);
			if (candidateKey.hasValueForParameter(p))
			{
				return candidateKey;
			}
		}
		return null;
	}

	@Override
	public KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame)
	{
		Collection<KeyFrame> candidateKeys = keyFrameMap.tailMap(frame + 1).values();
		for (KeyFrame candidateKey : candidateKeys)
		{
			if (candidateKey.hasValueForParameter(p))
			{
				return candidateKey;
			}
		}
		return null;
	}

	@Override
	public KeyFrame getFirstKeyFrame()
	{
		return keyFrameMap.isEmpty() ? null : keyFrameMap.get(keyFrameMap.firstKey());
	}
	
	@Override
	public int getFrameOfFirstKeyFrame()
	{
		return keyFrameMap.isEmpty() ? 0 : keyFrameMap.firstKey();
	}
	
	@Override
	public KeyFrame getLastKeyFrame()
	{
		return keyFrameMap.isEmpty() ? null : keyFrameMap.get(keyFrameMap.lastKey());
	}
	
	@Override
	public int getFrameOfLastKeyFrame()
	{
		return keyFrameMap.isEmpty() ? 0 : keyFrameMap.lastKey();
	}
	
	@Override
	public Collection<Parameter> getAllParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getParameters());
		}
		return result;
	}

	@Override
	public Collection<Parameter> getEnabledParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getEnabledParameters());
		}
		return result;
	}
	
	@Override
	public Collection<Parameter> getArmedParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getArmedParameters());
		}
		return result;
	}

	@Override
	public Camera getCamera()
	{
		return this.renderCamera;
	}
	
	@Override
	public List<Animatable> getAnimatableObjects()
	{
		return Collections.unmodifiableList(this.animatableObjects);
	}
	
	@Override
	public void addAnimatableObject(Animatable object)
	{
		if (object == null || animatableObjects.contains(object))
		{
			return;
		}
		
		animatableObjects.add(object);
		
		if (object instanceof AnimatableLayer)
		{
			animatableLayers.add((AnimatableLayer)object);
		}
		
		object.addChangeListener(this);
		
		fireAddEvent(object);
	}
	
	@Override
	public void addAnimatableObject(int index, Animatable object)
	{
		if (object == null || animatableObjects.contains(object))
		{
			return;
		}
		
		animatableObjects.add(index, object);
		if (object instanceof AnimatableLayer)
		{
			refreshLayersList();
		}
		
		fireAddEvent(object);
	}
	
	@Override
	public void removeAnimatableObject(Animatable object)
	{
		animatableObjects.remove(object);
		if (object instanceof AnimatableLayer)
		{
			animatableLayers.remove((AnimatableLayer)object);
		}
		
		object.removeChangeListener(this);
		
		removeValuesFromKeyFrames(object);
		
		fireRemoveEvent(object);
		
	}

	private void removeValuesFromKeyFrames(Animatable object)
	{
		for (Parameter parameter : object.getParameters())
		{
			for (KeyFrame key : getKeyFrames(parameter))
			{
				key.removeValueForParameter(parameter);
			}
		}
		removeEmptyKeyFrames();
	}

	@Override
	public void changeOrderOfAnimatableObject(Animatable object, int newIndex)
	{
		Validate.isTrue(newIndex >= 0 && newIndex < animatableObjects.size(), "newIndex outside of bounds. Must be in range [0, " + (animatableObjects.size() - 1) + "]");
		if (!animatableObjects.contains(object))
		{
			return;
		}
		
		int oldIndex = animatableObjects.indexOf(object);
		if (oldIndex == newIndex)
		{
			return;
		}
		
		if (oldIndex > newIndex)
		{
			animatableObjects.add(newIndex, object);
			animatableObjects.remove(oldIndex + 1);
		}
		else
		{
			animatableObjects.add(newIndex + 1, object);
			animatableObjects.remove(oldIndex);
		}
		
		if (object instanceof AnimatableLayer)
		{
			refreshLayersList();
		}
		
		fireChangeEvent(object);
	}
	
	private void refreshLayersList()
	{
		animatableLayers.clear();
		for (Animatable object : animatableObjects)
		{
			if (object instanceof AnimatableLayer)
			{
				animatableLayers.add((AnimatableLayer)object);
			}
		}
	}

	@Override
	public int getFrameCount()
	{
		return this.frameCount;
	}

	@Override
	public void setFrameCount(int newCount)
	{
		boolean changed = newCount != frameCount;
		
		// If we are decreasing the frame count, remove any keyframes after the new frame count
		if (newCount < frameCount)
		{
			this.keyFrameMap = this.keyFrameMap.headMap(newCount);
		}
		
		this.frameCount = newCount;
		
		if (changed)
		{
			fireChangeEvent(newCount);
		}
	}

	@Override
	public void applyFrame(int frame)
	{
		setCurrentFrame(frame);
		AnimationContext context = createAnimationContext();
		for (Animatable animatable : animatableObjects)
		{
			animatable.apply(context, frame);
		}
	}

	@Override
	public void recordKeyFrame(int frame)
	{
		recordKeyFrame(frame, getArmedParameters());
		
	}

	@Override
	public void recordKeyFrame(int frame, Collection<Parameter> parameters)
	{
		setCurrentFrame(frame);
		AnimationContext animationContext = createAnimationContext();
		Collection<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
		for (Parameter parameter : parameters)
		{
			parameterValues.add(parameter.getCurrentValue(animationContext));
		}
		
		// If there are no parameter values to record, do nothing
		if (parameterValues.isEmpty())
		{
			return;
		}
		
		insertKeyFrame(new KeyFrameImpl(frame, parameterValues));
		
		Logging.logger().log(Level.FINER, "WorldWindAnimationImpl::recordKeyFrame - Recorded frame " + frame + ": " + this.keyFrameMap.get(frame));
	}
	
	@Override
	public void insertKeyFrame(KeyFrame keyFrame)
	{
		insertKeyFrame(keyFrame, true);
	}
	
	@Override
	public void insertKeyFrame(KeyFrame keyFrame, boolean applySmoothing)
	{
		if (keyFrame == null)
		{
			return;
		}
		
		setCurrentFrame(keyFrame.getFrame());
		
		// If a key frame already exists at this frame, merge the parameter values
		// Otherwise, create a new key frame
		if (this.keyFrameMap.containsKey(keyFrame.getFrame()))
		{
			KeyFrame existingFrame = this.keyFrameMap.get(keyFrame.getFrame());
			existingFrame.addParameterValues(keyFrame.getParameterValues());
		} 
		else
		{
			keyFrame.addChangeListener(this);
			this.keyFrameMap.put(keyFrame.getFrame(), keyFrame);
		}
		
		if (applySmoothing)
		{
			// Smooth the key frames around this one
			smoothKeyFrames(keyFrame);
		}
		
		fireAddEvent(keyFrame);
	}
	
	/**
	 * Smooth the transition into- and out-of the provided key frame
	 * <p/>
	 * Applies value smoothing to the values of the provided key frames, 
	 * as well as those in key frames on either side of the frame.
	 * 
	 * @param keyFrame
	 */
	private void smoothKeyFrames(KeyFrame keyFrame)
	{
		for (ParameterValue parameterValue : keyFrame.getParameterValues())
		{
			parameterValue.smooth();
			
			// Smooth the previous value for this parameter
			KeyFrame previousFrame = getKeyFrameWithParameterBeforeFrame(parameterValue.getOwner(), keyFrame.getFrame());
			if (previousFrame != null)
			{
				ParameterValue previousValue = previousFrame.getValueForParameter(parameterValue.getOwner());
				previousValue.smooth();
			}
			
			// Smooth the next value for this parameter
			KeyFrame nextFrame = getKeyFrameWithParameterAfterFrame(parameterValue.getOwner(), keyFrame.getFrame());
			if (nextFrame != null)
			{
				ParameterValue nextValue = nextFrame.getValueForParameter(parameterValue.getOwner());
				nextValue.smooth();
			}
			
		}
	}
	
	/**
	 * @return An animation context that reflects the current state of the animation
	 */
	private AnimationContext createAnimationContext()
	{
		return new AnimationContextImpl(this);
	}

	@Override
	public RenderParameters getRenderParameters()
	{
		return renderParameters;
	}

	@Override
	public boolean isZoomScalingRequired()
	{
		return zoomRequired;
	}

	@Override
	public void setZoomScalingRequired(boolean zoomScalingRequired)
	{
		// If the zoom scaling is changing, update all of the camera parameters to reflect the change
		if (this.zoomRequired != zoomScalingRequired)
		{
			this.zoomRequired = zoomScalingRequired;
			
			// Apply / Un-apply the zoom scaling to the camera's elevation parameters 
			for (KeyFrame eyeElevationFrame : getKeyFrames(renderCamera.getEyeElevation()))
			{
				applyScalingChangeToValue(eyeElevationFrame.getValueForParameter(renderCamera.getEyeElevation()), zoomScalingRequired);
			}
			for (KeyFrame lookAtElevationFrame : getKeyFrames(renderCamera.getLookAtElevation()))
			{
				applyScalingChangeToValue(lookAtElevationFrame.getValueForParameter(renderCamera.getLookAtElevation()), zoomScalingRequired);
			}
			
			// Smooth the frames we just changed
			for (KeyFrame eyeElevationFrame : getKeyFrames(renderCamera.getEyeElevation()))
			{
				eyeElevationFrame.getValueForParameter(renderCamera.getEyeElevation()).smooth();
			}
			for (KeyFrame lookAtElevationFrame : getKeyFrames(renderCamera.getLookAtElevation()))
			{
				lookAtElevationFrame.getValueForParameter(renderCamera.getLookAtElevation()).smooth();
			}
			
			fireChangeEvent(null);
		}
	}

	/**
	 * Apply the zoom scaling change to the provided parameter value
	 * 
	 * @param value The parameter value to apply the change to
	 * @param scale Whether scaling should be applied or unapplied
	 */
	private void applyScalingChangeToValue(ParameterValue value, boolean scale)
	{
		value.setValue(scale ? applyZoomScaling(value.getValue()) : unapplyZoomScaling(value.getValue()));
		if (value instanceof BezierParameterValue)
		{
			BezierParameterValue bezierValue = (BezierParameterValue)value;
			boolean wasLocked = bezierValue.isLocked();
			bezierValue.setLocked(false);
			bezierValue.setInValue(scale ? doApplyZoomScaling(bezierValue.getInValue(), true) : doUnapplyZoomScaling(bezierValue.getInValue(), true));
			bezierValue.setOutValue(scale ? doApplyZoomScaling(bezierValue.getOutValue(), true) : doUnapplyZoomScaling(bezierValue.getOutValue(), true));
			bezierValue.setLocked(wasLocked);
		}
	}
	
	@Override
	public double applyZoomScaling(double unzoomed)
	{
		return doApplyZoomScaling(unzoomed, false);
	}
	
	/**
	 * Perform zoom scaling.
	 * 
	 * @param unzoomed The value to scale
	 * @param force Whether to override the current 'zoom scaling required' setting
	 */
	private double doApplyZoomScaling(double unzoomed, boolean force)
	{
		if (isZoomScalingRequired() || force)
		{
			return Math.log(Math.max(0, unzoomed) + 1);
		}
		return unzoomed;
	}
	
	@Override
	public double unapplyZoomScaling(double zoomed)
	{
		return doUnapplyZoomScaling(zoomed, false);
	}
	
	/**
	 * Perform zoom un-scaling.
	 * 
	 * @param zoomed The value to scale
	 * @param force Whether to override the current 'zoom scaling required' setting
	 */
	private double doUnapplyZoomScaling(double zoomed, boolean force)
	{
		if (isZoomScalingRequired() || force)
		{
			return Math.pow(Math.E, zoomed) - 1;
		}
		return zoomed;
	}
	
	
	@Override
	public int getCurrentFrame()
	{
		return this.currentFrame;
	}

	@Override
	public void setCurrentFrame(int frame)
	{
		this.currentFrame = frame;
	}

	/**
	 * @return The WorldWindow used in this animation 
	 */
	public WorldWindow getWorldWindow()
	{
		return this.worldWindow;
	}

	@Override
	public boolean hasKeyFrame(int frame)
	{
		return this.keyFrameMap.containsKey(frame);
	}

	@Override
	public boolean hasKeyFrame(Parameter p)
	{
		for (KeyFrame keyFrame : getKeyFrames())
		{
			if (keyFrame.hasValueForParameter(p))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public KeyFrame getKeyFrame(int frame)
	{
		return this.keyFrameMap.get(frame);
	}
	
	@Override
	public void removeKeyFrame(int frame)
	{
		if (this.keyFrameMap.containsKey(frame))
		{
			KeyFrame frameToRemove = this.keyFrameMap.get(frame);
			frameToRemove.removeChangeListener(this);
			this.keyFrameMap.remove(frame);
			fireRemoveEvent(frameToRemove);
		}
	}
	
	@Override
	public void removeKeyFrame(KeyFrame keyFrame)
	{
		if (keyFrame == null)
		{
			return;
		}
		
		if (keyFrameMap.containsValue(keyFrame))
		{
			keyFrame.removeChangeListener(this);
			keyFrameMap.remove(keyFrame.getFrame());
			fireRemoveEvent(keyFrame);
		}
		
	}
	
	@Override
	public void removeEmptyKeyFrames()
	{
		for (KeyFrame keyFrame : getKeyFrames())
		{
			if (!keyFrame.hasParameterValues())
			{
				removeKeyFrame(keyFrame.getFrame());
			}
		}
	}
	
	@Override
	public void scale(double scaleFactor)
	{
		Validate.isTrue(scaleFactor > 0.0, "Scale factor must be greater than 0");
		
		// Don't bother scaling if the scale factor is 1
		if (scaleFactor == 1.0)
		{
			return;
		}
		
		// Expand the frame count if required
		int newFrameCount = (int) Math.ceil(scaleFactor * getFrameOfLastKeyFrame());
		if (getFrameCount() < newFrameCount)
		{
			setFrameCount(newFrameCount);
		}

		List<KeyFrame> oldKeyFrames = getKeyFrames();
		
		// Scale each key frame, and add it to the list
		int[] newFrames = new int[oldKeyFrames.size()];
		for (int i = 0; i < oldKeyFrames.size(); i++)
		{
			// Apply the scale factor
			newFrames[i] = (int) Math.round(oldKeyFrames.get(i).getFrame() * scaleFactor);
			
			// Adjust any key frames that now lie on top of each other
			if (i > 0 && newFrames[i] <= newFrames[i-1])
			{
				newFrames[i] = newFrames[i-1] + 1;
			}
		}

		// Create the new key frames
		this.keyFrameMap.clear();
		for (int i = 0; i < oldKeyFrames.size(); i++)
		{
			insertKeyFrame(new KeyFrameImpl(newFrames[i], oldKeyFrames.get(i).getParameterValues()));
		}
		
	}

	@Override
	public Element toXml(Element parent, AnimationFileVersion version)
	{
		Validate.notNull(parent, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		Element result = WWXML.appendElement(parent, constants.getAnimationElementName());
		
		WWXML.setIntegerAttribute(result, constants.getAnimationAttributeFrameCount(), frameCount);
		WWXML.setBooleanAttribute(result, constants.getAnimationAttributeZoomRequired(), isZoomScalingRequired());
		
		renderParameters.toXml(result, version);
		
		Element animatableContainer = WWXML.appendElement(result, constants.getAnimatableObjectsElementName());
		for (Animatable animatable : animatableObjects)
		{
			animatableContainer.appendChild(animatable.toXml(animatableContainer, version));
		}
		return result;
	}

	@Override
	public Animation fromXml(Element element, AnimationFileVersion version, AVList context)
	{
		Validate.notNull(element, "An XML element is required");
		Validate.notNull(version, "A version ID is required");
		Validate.notNull(context, "A context is required");
		
		AnimationIOConstants constants = version.getConstants();
		
		switch (version)
		{
			case VERSION020:
			{
				WorldWindAnimationImpl result = new WorldWindAnimationImpl((WorldWindow)context.getValue(constants.getWorldWindowKey()));
				
				context.setValue(constants.getAnimationKey(), result);
				
				result.setCurrentFrame(0);
				result.setFrameCount(WWXML.getInteger(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimationAttributeFrameCount(), null));
				result.setZoomScalingRequired(WWXML.getBoolean(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimationAttributeZoomRequired(), null));
				
				// Add the render parameters
				result.renderParameters = new RenderParameters().fromXml(WWXML.getElement(element, constants.getRenderParametersElementName(), null), version, context);
				
				// Add each animatable object
				Element[] animatableObjectElements = WWXML.getElements(element, constants.getAnimatableObjectsElementName() + "/*", null);
				if (animatableObjectElements == null)
				{
					return null;
				}
				result.animatableObjects = new ArrayList<Animatable>();
				for (Element animatableObjectElement : animatableObjectElements)
				{
					Animatable animatable = AnimatableFactory.fromXml(animatableObjectElement, version, context);
					if (animatable == null)
					{
						continue;
					}
					result.addAnimatableObject(animatable);
					
					// If this is a 'special' object, set it to the correct field
					if (animatable instanceof Camera)
					{
						result.renderCamera = (Camera)animatable;
					}
					if (animatable instanceof AnimatableElevation)
					{
						result.animatableElevation = (AnimatableElevation)animatable;
					}
					
				}
				
				return result;
			}
		}
		
		return null;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public List<Layer> getLayers()
	{
		List<Layer> result = new ArrayList<Layer>();
		for (AnimatableLayer animatableLayer : animatableLayers)
		{
			result.add(animatableLayer.getLayer());
		}
		return result;
	}
	
	@Override
	public boolean hasLayer(LayerIdentifier layerIdentifier)
	{
		if (layerIdentifier == null)
		{
			return false;
		}
		for (AnimatableLayer animatableLayer : animatableLayers)
		{
			if (layerIdentifier.equals(animatableLayer.getLayerIdentifier()))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addLayer(LayerIdentifier layerIdentifier)
	{
		Layer loadedLayer = AnimationLayerLoader.loadLayer(layerIdentifier);
		if (loadedLayer == null)
		{
			throw new IllegalArgumentException("Unable to load layer " + layerIdentifier);
		}
		
		DefaultAnimatableLayer animatableLayer = new DefaultAnimatableLayer(loadedLayer);
		animatableLayer.addParameter(new LayerOpacityParameter(this, loadedLayer));
		
		addAnimatableObject(animatableLayer);
	}
	
	@Override
	public AnimatableElevation getAnimatableElevation()
	{
		return animatableElevation;
	}
	
	@Override
	public ElevationModel getRootElevationModel()
	{
		return animatableElevation.getRootElevationModel();
	}
	
	@Override
	public boolean hasElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		if (modelIdentifier == null)
		{
			return false;
		}
		return animatableElevation.hasElevationModel(modelIdentifier);
	}
	
	@Override
	public void addElevationModel(ElevationModelIdentifier modelIdentifier)
	{
		animatableElevation.addElevationModel(modelIdentifier);
	}
}
