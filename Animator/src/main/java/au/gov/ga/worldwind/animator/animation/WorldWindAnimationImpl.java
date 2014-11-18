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
package au.gov.ga.worldwind.animator.animation;

import gov.nasa.worldwind.View;
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
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;

import javax.xml.xpath.XPath;

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
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameter;
import au.gov.ga.worldwind.animator.animation.layer.parameter.LayerParameterFactory;
import au.gov.ga.worldwind.animator.animation.parameter.BezierParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffect;
import au.gov.ga.worldwind.animator.layers.AnimationLayerLoaderFactory;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.util.Nameable;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.LenientReadWriteLock;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;
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
	private static final int DEFAULT_FRAME_COUNT = 101;

	/**
	 * Map of <code>frame -> key frame</code>, ordered by frame, for quick
	 * lookup of key frames
	 */
	private NavigableMap<Integer, KeyFrame> keyFrameMap = new TreeMap<Integer, KeyFrame>();
	private ReadWriteLock keyFrameMapLock = new LenientReadWriteLock();

	/** The number of frames in this animation */
	private int frameCount;

	/** The render parameters for this animation */
	private RenderParameters renderParameters;

	/** The camera to use for rendering the animation */
	private Camera renderCamera;

	/** The list of animatable objects in this animation */
	private final List<Animatable> animatableObjects = new ArrayList<Animatable>();

	/**
	 * The list of animatable layers in this animation. A subset of the
	 * {@link #animatableObjects} list.
	 */
	private final List<AnimatableLayer> animatableLayers = new ArrayList<AnimatableLayer>();

	/**
	 * The list of effects in this animation. A subset of the
	 * {@link #animatableObjects} list.
	 */
	private final List<AnimatableEffect> effects = new ArrayList<AnimatableEffect>();

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
		this.renderCamera.addChangeListener(this);
		this.animatableObjects.add(renderCamera);

		this.animatableElevation = new DefaultAnimatableElevation(this);
		this.animatableObjects.add(animatableElevation);

		this.name = MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getAnimatorApplicationTitleKey());
	}

	@Override
	public int getKeyFrameCount()
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return keyFrameMap.size();
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public boolean hasKeyFrames()
	{
		return getKeyFrameCount() > 0;
	}

	@Override
	public List<KeyFrame> getKeyFrames()
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return new ArrayList<KeyFrame>(keyFrameMap.values());
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public List<KeyFrame> getKeyFrames(Parameter p)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			List<KeyFrame> result = new ArrayList<KeyFrame>();
			for (KeyFrame keyFrame : keyFrameMap.values())
			{
				if (keyFrame.hasValueForParameter(p))
				{
					result.add(keyFrame);
				}
			}
			return result;
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame)
	{
		return getKeyFrameWithParameterBeforeFrame(p, frame, false);
	}

	@Override
	public KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame, boolean inclusive)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			Collection<KeyFrame> candidateKeys = keyFrameMap.headMap(frame, inclusive).descendingMap().values();
			for (KeyFrame candidateKey : candidateKeys)
			{
				if (candidateKey.hasValueForParameter(p))
				{
					return candidateKey;
				}
			}
			return null;
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame)
	{
		return getKeyFrameWithParameterAfterFrame(p, frame, false);
	}

	@Override
	public KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame, boolean inclusive)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			Collection<KeyFrame> candidateKeys = keyFrameMap.tailMap(frame, inclusive).values();
			for (KeyFrame candidateKey : candidateKeys)
			{
				if (candidateKey.hasValueForParameter(p))
				{
					return candidateKey;
				}
			}
			return null;
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public KeyFrame getFirstKeyFrame()
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return keyFrameMap.isEmpty() ? null : keyFrameMap.get(keyFrameMap.firstKey());
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public int getFrameOfFirstKeyFrame()
	{
		KeyFrame first = getFirstKeyFrame();
		return first == null ? 0 : first.getFrame();
	}

	@Override
	public KeyFrame getLastKeyFrame()
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return keyFrameMap.isEmpty() ? null : keyFrameMap.get(keyFrameMap.lastKey());
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public int getFrameOfLastKeyFrame()
	{
		KeyFrame last = getLastKeyFrame();
		return last == null ? 0 : last.getFrame();
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
	public Collection<Parameter> getEnabledArmedParameters()
	{
		List<Parameter> result = new ArrayList<Parameter>();
		for (Animatable a : this.animatableObjects)
		{
			result.addAll(a.getEnabledArmedParameters());
		}
		return result;
	}

	@Override
	public Camera getCamera()
	{
		return this.renderCamera;
	}

	@Override
	public void setCamera(Camera camera)
	{
		if (this.renderCamera != camera)
		{
			int index = 0;

			//if a camera already existed, copy the data to the new camera
			if (this.renderCamera != null)
			{
				camera.copyStateFrom(this.renderCamera);
				this.renderCamera.clearChangeListeners();

				//remove the object, but don't remove the KeyFrames
				index = Math.max(index, removeAnimatableObject(this.renderCamera, false));
			}

			this.renderCamera = camera;
			addAnimatableObject(index, this.renderCamera);
		}
	}

	@Override
	public List<Animatable> getAnimatableObjects()
	{
		return Collections.unmodifiableList(this.animatableObjects);
	}

	@Override
	public void addAnimatableObject(Animatable object)
	{
		addAnimatableObject(animatableObjects.size(), object);
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
			if (index >= animatableObjects.size())
			{
				//if we are adding at the end, then simply add the layer to the list
				animatableLayers.add((AnimatableLayer) object);
			}
			else
			{
				//otherwise we don't know where in the list we added the layer, so just refresh it
				refreshLayersList();
			}
		}
		if (object instanceof AnimatableEffect)
		{
			if (index >= animatableObjects.size())
			{
				effects.add((AnimatableEffect) object);
			}
			else
			{
				refreshEffectsList();
			}
		}

		object.addChangeListener(this);

		fireAddEvent(object);
	}

	@Override
	public int removeAnimatableObject(Animatable object)
	{
		return removeAnimatableObject(object, true);
	}

	protected int removeAnimatableObject(Animatable object, boolean removeValuesFromKeyFrames)
	{
		int index = animatableObjects.indexOf(object);
		if (index >= 0)
		{
			animatableObjects.remove(index);
			if (object instanceof AnimatableLayer)
			{
				animatableLayers.remove(object);
			}
			if (object instanceof AnimatableEffect)
			{
				effects.remove(object);
			}

			object.removeChangeListener(this);

			if (removeValuesFromKeyFrames)
			{
				removeValuesFromKeyFrames(object);
			}

			fireRemoveEvent(object);
		}
		return index;
	}

	protected void clearAnimatableObjects()
	{
		for (int i = animatableObjects.size() - 1; i>= 0; i--)
		{
			removeAnimatableObject(animatableObjects.get(i));
		}
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
	public void removeAnimationParameters(Parameter... parameters)
	{
		for (Parameter parameter : parameters)
		{
			for (KeyFrame key : getKeyFrames(parameter))
			{
				key.removeValueForParameter(parameter);
			}
		}
		removeEmptyKeyFrames();
	}

	@Override
	public void moveAnimatableObject(Animatable object, int newIndex)
	{
		Validate.isTrue(newIndex >= 0 && newIndex < animatableObjects.size(),
				"newIndex outside of bounds. Must be in range [0, " + (animatableObjects.size() - 1) + "]");

		int oldIndex = animatableObjects.indexOf(object);
		if (oldIndex < 0 || oldIndex == newIndex)
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
				animatableLayers.add((AnimatableLayer) object);
			}
		}
	}

	private void refreshEffectsList()
	{
		effects.clear();
		for (Animatable object : animatableObjects)
		{
			if (object instanceof AnimatableEffect)
			{
				effects.add((AnimatableEffect) object);
			}
		}
	}

	@Override
	public int getFrameCount()
	{
		return this.frameCount;
	}

	@Override
	public int getLastFrame()
	{
		return frameCount - 1;
	}

	@Override
	public void setFrameCount(int newCount)
	{
		boolean changed = newCount != frameCount;

		// If we are decreasing the frame count, remove any keyframes after the new frame count
		if (newCount < frameCount)
		{
			try
			{
				keyFrameMapLock.writeLock().lock();

				// the headMap() operation returns a map that has a restriced range - copy into a new map.
				NavigableMap<Integer, KeyFrame> headMap = this.keyFrameMap.headMap(newCount, false);

				this.keyFrameMap = new TreeMap<Integer, KeyFrame>();
				this.keyFrameMap.putAll(headMap);
			}
			finally
			{
				keyFrameMapLock.writeLock().unlock();
			}
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
		for (Animatable animatable : animatableObjects)
		{
			animatable.apply();
		}
	}

	@Override
	public void recordKeyFrame(int frame)
	{
		recordKeyFrame(frame, getEnabledArmedParameters());

	}

	@Override
	public void recordKeyFrame(int frame, Collection<Parameter> parameters)
	{
		setCurrentFrame(frame);
		Collection<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
		for (Parameter parameter : parameters)
		{
			parameterValues.add(parameter.getCurrentValue());
		}

		// If there are no parameter values to record, do nothing
		if (parameterValues.isEmpty())
		{
			return;
		}

		insertKeyFrame(new KeyFrameImpl(frame, parameterValues));

		Logging.logger().log(Level.FINER,
				"WorldWindAnimationImpl::recordKeyFrame - Recorded frame " + frame + ": " + getKeyFrame(frame));
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
		KeyFrame existingFrame = getKeyFrame(keyFrame.getFrame());
		if (existingFrame != null)
		{
			existingFrame.addParameterValues(keyFrame.getParameterValues());
			for (ParameterValue pv : keyFrame.getParameterValues())
			{
				pv.removeChangeListener(keyFrame);
			}
		}
		else
		{
			keyFrame.addChangeListener(this);
			try
			{
				keyFrameMapLock.writeLock().lock();
				this.keyFrameMap.put(keyFrame.getFrame(), keyFrame);
			}
			finally
			{
				keyFrameMapLock.writeLock().unlock();
			}
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
	 * Applies value smoothing to the values of the provided key frames, as well
	 * as those in key frames on either side of the frame.
	 * 
	 * @param keyFrame
	 */
	private void smoothKeyFrames(KeyFrame keyFrame)
	{
		for (ParameterValue parameterValue : keyFrame.getParameterValues())
		{
			parameterValue.smooth();

			// Smooth the previous value for this parameter
			KeyFrame previousFrame =
					getKeyFrameWithParameterBeforeFrame(parameterValue.getOwner(), keyFrame.getFrame());
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
				applyScalingChangeToValue(eyeElevationFrame.getValueForParameter(renderCamera.getEyeElevation()),
						zoomScalingRequired);
			}
			for (KeyFrame lookAtElevationFrame : getKeyFrames(renderCamera.getLookAtElevation()))
			{
				applyScalingChangeToValue(lookAtElevationFrame.getValueForParameter(renderCamera.getLookAtElevation()),
						zoomScalingRequired);
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
	 * @param value
	 *            The parameter value to apply the change to
	 * @param scale
	 *            Whether scaling should be applied or unapplied
	 */
	private void applyScalingChangeToValue(ParameterValue value, boolean scale)
	{
		value.setValue(scale ? applyZoomScaling(value.getValue()) : unapplyZoomScaling(value.getValue()));
		if (value instanceof BezierParameterValue)
		{
			BezierParameterValue bezierValue = (BezierParameterValue) value;
			boolean wasLocked = bezierValue.isLocked();
			bezierValue.setLocked(false);
			bezierValue.setInValue(scale ? doApplyZoomScaling(bezierValue.getInValue(), true) : doUnapplyZoomScaling(
					bezierValue.getInValue(), true));
			bezierValue.setOutValue(scale ? doApplyZoomScaling(bezierValue.getOutValue(), true) : doUnapplyZoomScaling(
					bezierValue.getOutValue(), true));
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
	 * @param unzoomed
	 *            The value to scale
	 * @param force
	 *            Whether to override the current 'zoom scaling required'
	 *            setting
	 */
	private double doApplyZoomScaling(double unzoomed, boolean force)
	{
		if (isZoomScalingRequired() || force)
		{
			if (unzoomed < 0.0)
			{
				return unzoomed;
			}
			return Math.log(unzoomed + 1);
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
	 * @param zoomed
	 *            The value to scale
	 * @param force
	 *            Whether to override the current 'zoom scaling required'
	 *            setting
	 */
	private double doUnapplyZoomScaling(double zoomed, boolean force)
	{
		if (isZoomScalingRequired() || force)
		{
			if (zoomed < 0.0)
			{
				return zoomed;
			}
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

	@Override
	public WorldWindow getWorldWindow()
	{
		return this.worldWindow;
	}

	@Override
	public boolean hasKeyFrame(int frame)
	{
		return getKeyFrame(frame) != null;
	}

	public boolean hasKeyFrame(KeyFrame keyFrame)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return keyFrameMap.containsValue(keyFrame);
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public boolean hasKeyFrame(Parameter p)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			for (KeyFrame keyFrame : keyFrameMap.values())
			{
				if (keyFrame.hasValueForParameter(p))
				{
					return true;
				}
			}
			return false;
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public boolean hasKeyFrame(Animatable o)
	{
		if (o == null || !animatableObjects.contains(o))
		{
			return false;
		}
		for (Parameter p : o.getParameters())
		{
			if (hasKeyFrame(p))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public KeyFrame getKeyFrame(int frame)
	{
		try
		{
			keyFrameMapLock.readLock().lock();
			return this.keyFrameMap.get(frame);
		}
		finally
		{
			keyFrameMapLock.readLock().unlock();
		}
	}

	@Override
	public void removeKeyFrame(int frame)
	{
		KeyFrame frameToRemove = getKeyFrame(frame);
		removeKeyFrame(frameToRemove);
	}

	@Override
	public void removeKeyFrame(KeyFrame keyFrame)
	{
		if (keyFrame == null)
		{
			return;
		}

		if (hasKeyFrame(keyFrame))
		{
			keyFrame.removeChangeListener(this);
			try
			{
				keyFrameMapLock.writeLock().lock();
				keyFrameMap.remove(keyFrame.getFrame());
			}
			finally
			{
				keyFrameMapLock.writeLock().unlock();
			}
			fireRemoveEvent(keyFrame);
		}
	}

	@Override
	public void removeEmptyKeyFrames()
	{
		try
		{
			keyFrameMapLock.writeLock().lock();
			//have to use getKeyFrames() which returns a list copy, because we are modifying the map contents
			for (KeyFrame keyFrame : getKeyFrames())
			{
				if (!keyFrame.hasParameterValues())
				{
					removeKeyFrame(keyFrame.getFrame());
				}
			}
		}
		finally
		{
			keyFrameMapLock.writeLock().unlock();
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
			if (i > 0 && newFrames[i] <= newFrames[i - 1])
			{
				newFrames[i] = newFrames[i - 1] + 1;
			}
		}

		// Create the new key frames
		try
		{
			keyFrameMapLock.writeLock().lock();
			this.keyFrameMap.clear();
			for (int i = 0; i < oldKeyFrames.size(); i++)
			{
				insertKeyFrame(new KeyFrameImpl(newFrames[i], oldKeyFrames.get(i).getParameterValues()));
			}
		}
		finally
		{
			keyFrameMapLock.writeLock().unlock();
		}

		fireChangeEvent(newFrameCount);
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
			WorldWindAnimationImpl result =
					new WorldWindAnimationImpl((WorldWindow) context.getValue(constants.getWorldWindowKey()));

			context.setValue(constants.getAnimationKey(), result);

			XPath xpath = WWXML.makeXPath();

			result.setCurrentFrame(0);
			Integer frameCount =
					WWXML.getInteger(element, ATTRIBUTE_PATH_PREFIX + constants.getAnimationAttributeFrameCount(),
							xpath);
			result.setZoomScalingRequired(XMLUtil.getBoolean(element,
					ATTRIBUTE_PATH_PREFIX + constants.getAnimationAttributeZoomRequired(), true, xpath));

			// Add the render parameters
			result.renderParameters =
					new RenderParameters().fromXml(
							WWXML.getElement(element, constants.getRenderParametersElementName(), xpath), version,
							context);

			// Add each animatable object
			Element[] animatableObjectElements =
					WWXML.getElements(element, constants.getAnimatableObjectsElementName() + "/*", xpath);
			if (animatableObjectElements != null)
			{
				result.clearAnimatableObjects();
				for (Element animatableObjectElement : animatableObjectElements)
				{
					Animatable animatable =
							AnimatableFactoryRegistry.instance.fromXml(animatableObjectElement, version, context);
					if (animatable == null)
					{
						continue;
					}
					result.addAnimatableObject(animatable);

					// If this is a 'special' object, set it to the correct field
					if (animatable instanceof Camera)
					{
						result.renderCamera = (Camera) animatable;
					}
					if (animatable instanceof AnimatableElevation)
					{
						result.animatableElevation = (AnimatableElevation) animatable;
					}
				}
			}

			//if the frame count didn't exist in the XML, or is too small, calculate a smart default value
			int lastFrame = result.getFrameOfLastKeyFrame();
			if (frameCount == null || lastFrame >= frameCount)
			{
				if (lastFrame > 0)
				{
					frameCount = lastFrame + 1;
				}
				else
				{
					frameCount = DEFAULT_FRAME_COUNT;
				}
			}
			result.setFrameCount(frameCount);

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
	public List<AnimatableEffect> getEffects()
	{
		return Collections.unmodifiableList(effects);
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
		Layer loadedLayer = AnimationLayerLoaderFactory.loadLayer(layerIdentifier);
		if (loadedLayer == null)
		{
			throw new IllegalArgumentException("Unable to load layer " + layerIdentifier);
		}

		DefaultAnimatableLayer animatableLayer = new DefaultAnimatableLayer(this, loadedLayer);
		for (LayerParameter parameter : LayerParameterFactory.createDefaultParametersForLayer(this, loadedLayer))
		{
			animatableLayer.addParameter(parameter);
		}

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

	@Override
	public View getView()
	{
		return getWorldWindow().getView();
	}
}
