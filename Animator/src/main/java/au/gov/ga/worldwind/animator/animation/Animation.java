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
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.util.Collection;
import java.util.List;

import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.elevation.AnimatableElevation;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.Changeable;
import au.gov.ga.worldwind.animator.animation.io.XmlSerializable;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.application.effects.AnimatableEffect;
import au.gov.ga.worldwind.animator.layers.LayerIdentifier;
import au.gov.ga.worldwind.animator.terrain.ElevationModelIdentifier;
import au.gov.ga.worldwind.animator.util.Nameable;

/**
 * An animation.
 * <p/>
 * An animation is the primary structure in the Animator application. It contains a collection of {@link Animatable} objects,
 * each of which contain animation {@link Parameter}s.
 * <p/>
 * It also contains a list of {@link KeyFrame}s, which can have multiple {@link Parameter}s associated with them.
 * <p/>
 * This interface defines a number of convenience methods which make it easy to access and manipulate components of an animation. 
 * 
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Animation extends AnimationObject, XmlSerializable<Animation>, AnimationEventListener, Changeable, Nameable
{

	/**
	 * Returns the list of all key frames associated with this animation, ordered by
	 * frame.
	 * 
	 * @return All key frames, ordered by frame.
	 */
	List<KeyFrame> getKeyFrames();
	
	/**
	 * Returns the ordered list of all key frames that contain a value for the provided parameter.
	 * 
	 * @param p The parameter in question
	 * 
	 * @return The list of key frames with a value for the provided parameter, ordered by frame
	 */
	List<KeyFrame> getKeyFrames(Parameter p);
	
	/**
	 * @return <code>true</code> if there are key frames recorded for this animation, 
	 * <code>false</code> otherwise
	 */
	boolean hasKeyFrames();
	
	/**
	 * @return <code>true</code> if there is a key frame at the given frame, <code>false</code> otherwise
	 */
	boolean hasKeyFrame(int frame);
	
	/**
	 * @return <code>true</code> if there is a key frame with a value for the given parameter, <code>false</code> otherwise
	 */
	boolean hasKeyFrame(Parameter p);
	
	/**
	 * @return <code>true</code> if there is a key frame with a value for a parameter of the given animatable object, <code>false</code> otherwise
	 */
	boolean hasKeyFrame(Animatable o);
	
	/**
	 * @return The number of key frames associated with this animation
	 */
	int getKeyFrameCount();
	
	/**
	 * @return The key frame at the given frame, or <code>null</code> if no key frame is recorded at that frame
	 */
	KeyFrame getKeyFrame(int frame);
	
	/**
	 * Same as
	 * {@link Animation#getKeyFrameWithParameterBeforeFrame(Parameter, int, boolean)}
	 * with <code>inclusive=false</code>.
	 */
	KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame);

	/**
	 * Returns the last {@link KeyFrame} <em>before</em> (or, if inclusive,
	 * <em>at</em>) the provided frame that contains a value for the provided
	 * {@link Parameter}.
	 * <p/>
	 * If there are no {@link KeyFrame}s with a value recorded for the provided
	 * parameter before the provided frame, returns <code>null</code>.
	 * 
	 * @return the first {@link KeyFrame} <em>before</em> or <em>at</em> the
	 *         provided frame that contains a value for the provided
	 *         {@link Parameter}, or <code>null</code> if one cannot be found
	 */
	KeyFrame getKeyFrameWithParameterBeforeFrame(Parameter p, int frame, boolean inclusive);
	
	/**
	 * Same as
	 * {@link Animation#getKeyFrameWithParameterAfterFrame(Parameter, int, boolean)}
	 * with <code>inclusive=false</code>.
	 */
	KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame);
	
	/**
	 * Returns the first {@link KeyFrame} <em>after</em> (or, if inclusive,
	 * <em>at</em>) the provided frame that contains a value for the provided
	 * {@link Parameter}.
	 * <p/>
	 * If there are no {@link KeyFrame}s with a value recorded for the provided
	 * parameter after the provided frame, returns <code>null</code>.
	 * 
	 * @return the first {@link KeyFrame} <em>after</em> or <em>at</em> the
	 *         provided frame that contains a value for the provided
	 *         {@link Parameter}, or <code>null</code> if one cannot be found
	 */
	KeyFrame getKeyFrameWithParameterAfterFrame(Parameter p, int frame, boolean inclusive);
	
	/**
	 * @return The first key frame in the animation, or <code>null</code> if no key frames are recorded
	 */
	KeyFrame getFirstKeyFrame();
	
	/**
	 * Convenience method.
	 * 
	 * @return The frame of the first key frame in the animation, or '0' if no key frames are recorded
	 */
	int getFrameOfFirstKeyFrame();
	
	/**
	 * @return The last key frame in the animation, or <code>null</code> if no key frames are recorded
	 */
	KeyFrame getLastKeyFrame();
	
	/**
	 * Convenience method.
	 * 
	 * @return The frame of the last key frame in the animation, or '0' if no key frames are recorded
	 */
	int getFrameOfLastKeyFrame();
	
	/**
	 * @return A collection of all parameters associated with this animation
	 */
	Collection<Parameter> getAllParameters();
	
	/**
	 * @return A collection of all parameters associated with this animation that are currently enabled
	 */
	Collection<Parameter> getEnabledParameters();
	
	/**
	 * @return A collection of all parameters associated with this animation that are currently armed
	 */
	Collection<Parameter> getArmedParameters();
	
	/**
	 * @return A collection of all parameters associated with this animation that are currently both enabled and armed
	 */
	Collection<Parameter> getEnabledArmedParameters();
	
	/**
	 * @return The camera used in this animation
	 */
	Camera getCamera();
	
	/**
	 * Set this animation's camera.
	 * 
	 * @param camera Camera to use in this animation
	 */
	void setCamera(Camera camera);
	
	/**
	 * @return A list of all objects being animated in this animation. Ordering is in draw order (if applicable).
	 */
	List<Animatable> getAnimatableObjects();
	
	/**
	 * Add the provided animatable object to this animation.
	 * 
	 * @param object The object to add.
	 */
	void addAnimatableObject(Animatable object);
	
	/**
	 * Add the provided animatable object to this animation at the provided index
	 * 
	 * @param index The index to add the object at
	 * @param object The object to add.
	 */
	void addAnimatableObject(int index, Animatable object);
	
	/**
	 * Remove the provided animatable object from this animation.
	 * <p/>
	 * All associated key frame values will also be removed.
	 * 
	 * @param object The object to remove.
	 * @return Index the object was at before removal.
	 */
	int removeAnimatableObject(Animatable object);
	
	/**
	 * Remove the provided animation parameters from this animation
	 * <p/>
	 * If any key frames exists that only have this parameter, they will be removed
	 */
	void removeAnimationParameters(Parameter... parameters);
	
	/**
	 * Change the order of the provided animatable object, moving it from it's current index in the list of objects to the new index.
	 * <p/>
	 * If the provided object is not in this animation's list of objects, this method will have no effect.
	 */
	void moveAnimatableObject(Animatable object, int newIndex);
	
	/**
	 * @return The number of frames in this animation
	 */
	int getFrameCount();
	
	/**
	 * @return The 0-based index of the last frame in this animation (equivalent to frameCount - 1)
	 */
	int getLastFrame();
	
	/**
	 * Set the frame count of this animation.
	 * <p/>
	 * If this will result in a decrease in the frame count of the animation, any key frames at a 
	 * frame > <code>newCount</code> will be lost.
	 * 
	 * @param newCount The new frame count to set.
	 */
	void setFrameCount(int newCount);
	
	/**
	 * Apply this animation's state at the given frame to the 'world'
	 * 
	 * @param frame The frame at which the state of the animation is to be applied
	 * 
	 * @throws IllegalArgumentException if <code>frame</code> is less than 0 or greater than <code>frameCount</code>
	 */
	void applyFrame(int frame);
	
	/**
	 * Record a key frame at the given frame with the current value of all <em>enabled</em> parameters.
	 * <p/>
	 * If there are no enabled parameters, no key frame will be created.
	 * <p/>
	 * Equivalent to the call <code>recordKeyFrame(frame, getEnabledParameters());</code>
	 * 
	 * @param frame The frame to record the key frame at
	 */
	void recordKeyFrame(int frame);
	
	/**
	 * Record a key frame at the given frame with the current value of the parameters 
	 * provided in the given collection.
	 * <p/>
	 * If the provided list of parameters is empty, no key frame will be created.
	 * 
	 * @param frame The frame to record the key frame at
	 * @param parameters The collection of parameters to record on the key frame
	 */
	void recordKeyFrame(int frame, Collection<Parameter> parameters);
	
	/**
	 * Insert the provided key frame into the animation
	 * <p/>
	 * If a key frame already exists at the frame at which the key frame is inserted, the parameter values of the two key frames will be merged,
	 * with those in the new key frame taking precedence.
	 * <p/>
	 * The inserted key frame, and those around it, will be smoothed.
	 * 
	 * @param keyFrame The key frame to insert.
	 */
	void insertKeyFrame(KeyFrame keyFrame);
	
	/**
	 * Insert the provided key frame into the animation
	 * <p/>
	 * If a key frame already exists at the frame at which the key frame is inserted, the parameter values of the two key frames will be merged,
	 * with those in the new key frame taking precedence.
	 * <p/>
	 * If <code>applySmoothing</code> is true, smoothing will be applied to the inserted key frame, and those around it to ensure
	 * a smooth transition through the inserted key frame.
	 * 
	 * @param keyFrame The key frame to insert.
	 */
	void insertKeyFrame(KeyFrame keyFrame, boolean applySmoothing);
	
	/**
	 * Remove the key frame at the given frame from the animation
	 * <p/>
	 * If there is no key frame at the frame, this will have no effect
	 * 
	 * @param frame The frame of the key frame to remove
	 */
	void removeKeyFrame(int frame);
	
	/**
	 * Remove the key frame at the given frame
	 * 
	 * @param keyFrame The key frame to remove from the animation
	 */
	void removeKeyFrame(KeyFrame keyFrame);
	
	/**
	 * Remove all key frames that have no associated {@link ParameterValue}s
	 */
	void removeEmptyKeyFrames();
	
	/**
	 * @return The render parameters for this animation.
	 */
	RenderParameters getRenderParameters();
	
	/**
	 * @return Whether zoom scaling should be applied in this animation
	 */
	boolean isZoomScalingRequired();
	
	/**
	 * Set whether or not zoom scaling should be applied in this animation.
	 * 
	 * @param zoomScalingRequired Whether zoom scaling should be applied in this animation
	 */
	void setZoomScalingRequired(boolean zoomScalingRequired);
	
	/**
	 * @return The current frame in the animation
	 */
	int getCurrentFrame();
	
	/**
	 * Sets the current frame of the animation
	 * 
	 * @param frame The current frame to set
	 */
	void setCurrentFrame(int frame);
	
	/**
	 * Scales the animation by the provided scaling factor.
	 * <p/>
	 * If the scaling factor is in the interval <code>(0,1)</code>, the animation's key frames will be 
	 * compressed together by the scale amount.
	 * <p/>
	 * If the scaling factor is in the interval <code>(1,)</code>, the animation's key frames will be
	 * expanded by the scale amount.
	 * 
	 * @param scaleFactor The factor by which to scale the animation
	 */
	void scale(double scaleFactor);

	/**
	 * Helper method. Applies zoom scaling to the provided unzoomed value if zoom scaling is enabled.
	 * 
	 * @param unzoomed The unzoomed value to scale
	 * 
	 * @return The unzoomed value, scaled appropriately.
	 */
	double applyZoomScaling(double unzoomed);

	/**
	 * Helper method. Removed zoom scaling from the provided zoomed value if zoom scaling is enabled.
	 * 
	 * @param zoomed The zoomed value to scale
	 * 
	 * @return The zoomed value, scaled appropriately.
	 */
	double unapplyZoomScaling(double zoomed);
	
	/**
	 * Gets the list of layers (in order) associated with this animation.
	 * 
	 * @return The list of layers associated with this animation. If no layers are associated, will return the empty list.
	 */
	List<Layer> getLayers();
	
	/**
	 * @return The list of effects associated with this animation. If no effects are associated, will return the empty list.
	 */
	List<AnimatableEffect> getEffects();
	
	/**
	 * @return Whether or not this animation contains the layer identified by the provided layer identifier
	 */
	boolean hasLayer(LayerIdentifier layerIdentifier);
	
	/**
	 * Add the layer identified in the identifier to this animation, if it isn't already.
	 */
	void addLayer(LayerIdentifier layerIdentifier);
	
	/**
	 * @return The root elevation model associated with this {@link Animation}
	 */
	ElevationModel getRootElevationModel();
	
	/**
	 * @return The {@link AnimatableElevation} associated with this Animation
	 */
	AnimatableElevation getAnimatableElevation();
	
	/**
	 * Add the elevation model identified by the provided identifier to this model's {@link AnimatableElevation}, if it isn't already.
	 */
	void addElevationModel(ElevationModelIdentifier modelIdentifier);
	
	/**
	 * @return Whether or not this animation contains the elevation model identified by the provided model identifier
	 */
	boolean hasElevationModel(ElevationModelIdentifier modelIdentifier);

	/**
	 * @return The WorldWindow used in this animation
	 */
	WorldWindow getWorldWindow();
	
	/**
	 * @return The current View used by the animator
	 */
	View getView();
}
