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
package au.gov.ga.worldwind.animator.animation.camera;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getCameraNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessageOrDefault;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.AnimatableBase;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.KeyFrameImpl;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeElevationParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeLatParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.EyeLonParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.FarClipParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.FieldOfViewParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatElevationParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatLatParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.LookatLonParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.NearClipParameter;
import au.gov.ga.worldwind.animator.animation.camera.CameraParameter.RollParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.math.vector.Vector3;
import au.gov.ga.worldwind.animator.view.ClipConfigurableView;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A default implementation of the {@link Camera} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class CameraImpl extends AnimatableBase implements Camera
{
	private static final long serialVersionUID = 20100819L;

	protected static final String DEFAULT_CAMERA_NAME = "Render Camera";

	private Parameter eyeLat;
	private Parameter eyeLon;
	private Parameter eyeElevation;

	private Parameter lookAtLat;
	private Parameter lookAtLon;
	private Parameter lookAtElevation;

	private Parameter roll;
	private Parameter fieldOfView;

	private boolean clippingParametersActivated = false;
	private Parameter nearClip;
	private Parameter farClip;

	protected Collection<Parameter> parameters;

	/**
	 * Constructor. Initialises the camera parameters.
	 */
	public CameraImpl(Animation animation)
	{
		this(null, animation);
	}

	protected CameraImpl(String name, Animation animation)
	{
		super(name, animation);
		initialiseParameters(animation);
		connectCodependants();
		connectAsListener();
	}

	/**
	 * Constructor used for de-serialising. Not for general use.
	 */
	protected CameraImpl()
	{
	}

	@Override
	protected String getDefaultName()
	{
		return getMessageOrDefault(getCameraNameKey(), DEFAULT_CAMERA_NAME);
	}

	/**
	 * Initialise the camera parameters
	 * 
	 * @param animation
	 */
	protected void initialiseParameters(Animation animation)
	{
		eyeLat = new EyeLatParameter(animation);
		eyeLon = new EyeLonParameter(animation);
		eyeElevation = new EyeElevationParameter(animation);

		lookAtLat = new LookatLatParameter(animation);
		lookAtLon = new LookatLonParameter(animation);
		lookAtElevation = new LookatElevationParameter(animation);

		roll = new RollParameter(animation);
		fieldOfView = new FieldOfViewParameter(animation);

		if (clippingParametersActivated)
		{
			nearClip = new NearClipParameter(animation);
			farClip = new FarClipParameter(animation);
		}
	}

	protected void connectCodependants()
	{
		eyeLat.connectCodependantParameter(eyeLon);
		eyeLat.connectCodependantParameter(eyeElevation);
		eyeLat.connectCodependantParameter(lookAtLat);
		eyeLat.connectCodependantParameter(lookAtLon);
		eyeLat.connectCodependantParameter(lookAtElevation);
	}

	protected void connectAsListener()
	{
		for (Parameter p : getParameters())
		{
			p.addChangeListener(this);
		}
	}

	@Override
	protected void doApply()
	{
		if (!animation.hasKeyFrame(this))
		{
			return;
		}

		int frame = getAnimation().getCurrentFrame();
		Position eye = getEyePositionAtFrame(frame);
		Position center = getLookatPositionAtFrame(frame);
		Angle roll = Angle.fromDegrees(this.roll.getValueAtFrame(frame).getValue());
		Angle fieldOfView = Angle.fromDegrees(this.fieldOfView.getValueAtFrame(frame).getValue());

		View view = animation.getView();
		view.stopMovement();
		if (this.roll.isEnabled())
		{
			view.setRoll(roll);
		}
		if (this.fieldOfView.isEnabled())
		{
			view.setFieldOfView(fieldOfView);
		}
		view.setOrientation(eye, center);

		if (clippingParametersActivated)
		{
			nearClip.applyValueIfEnabled(nearClip.getValueAtFrame(frame).getValue(), frame);
			farClip.applyValueIfEnabled(farClip.getValueAtFrame(frame).getValue(), frame);
		}
	}

	@Override
	public Position[] getEyePositionsBetweenFrames(int startFrame, int endFrame)
	{
		return getPositionsBetweenFrames(startFrame, endFrame, eyeLat, eyeLon, eyeElevation);
	}

	@Override
	public Position[] getLookatPositionsBetweenFrames(int startFrame, int endFrame)
	{
		return getPositionsBetweenFrames(startFrame, endFrame, lookAtLat, lookAtLon, lookAtElevation);
	}

	private Position[] getPositionsBetweenFrames(int startFrame, int endFrame, Parameter lat, Parameter lon,
			Parameter elevation)
	{
		Validate.isTrue(startFrame <= endFrame, "End frame must not be less than start frame");

		ParameterValue[] latValues = lat.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] lonValues = lon.getValuesBetweenFrames(startFrame, endFrame, null);
		ParameterValue[] elevationValues = elevation.getValuesBetweenFrames(startFrame, endFrame, null);

		Position[] result = new Position[latValues.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] =
					Position.fromDegrees(latValues[i].getValue(), lonValues[i].getValue(),
							animation.unapplyZoomScaling(elevationValues[i].getValue()));
		}

		return result;
	}


	@Override
	public Position getEyePositionAtFrame(int frame)
	{
		return Position.fromDegrees(eyeLat.getValueAtFrame(frame).getValue(), eyeLon.getValueAtFrame(frame).getValue(),
				getAnimation().unapplyZoomScaling(eyeElevation.getValueAtFrame(frame).getValue()));
	}

	@Override
	public Position getLookatPositionAtFrame(int frame)
	{
		return Position.fromDegrees(lookAtLat.getValueAtFrame(frame).getValue(), lookAtLon.getValueAtFrame(frame)
				.getValue(), getAnimation().unapplyZoomScaling(lookAtElevation.getValueAtFrame(frame).getValue()));
	}

	@Override
	public Parameter getEyeLat()
	{
		return eyeLat;
	}

	@Override
	public Parameter getEyeLon()
	{
		return eyeLon;
	}

	@Override
	public Parameter getEyeElevation()
	{
		return eyeElevation;
	}

	@Override
	public Parameter getLookAtLat()
	{
		return lookAtLat;
	}

	@Override
	public Parameter getLookAtLon()
	{
		return lookAtLon;
	}

	@Override
	public Parameter getLookAtElevation()
	{
		return lookAtElevation;
	}

	@Override
	public Parameter getRoll()
	{
		return roll;
	}

	@Override
	public Parameter getFieldOfView()
	{
		return fieldOfView;
	}

	@Override
	public boolean isClippingParametersActive()
	{
		return clippingParametersActivated;
	}

	@Override
	public void setClippingParametersActive(boolean active)
	{
		if (active == clippingParametersActivated)
		{
			return;
		}
		this.parameters = null;
		if (!active)
		{
			deactivateClippingParameters();
		}
		else
		{
			activateClippingParameters();
		}
	}

	private void deactivateClippingParameters()
	{
		this.clippingParametersActivated = false;

		// Remove any key frames with the parameters
		this.animation.removeAnimationParameters(nearClip, farClip);

		((ClipConfigurableView) this.animation.getWorldWindow().getView()).setAutoCalculateNearClipDistance(true);
		((ClipConfigurableView) this.animation.getWorldWindow().getView()).setAutoCalculateFarClipDistance(true);

		Parameter oldNearClip = nearClip;
		Parameter oldFarClip = farClip;

		this.nearClip = null;
		this.farClip = null;

		fireRemoveEvent(oldNearClip);
		fireRemoveEvent(oldFarClip);
	}

	private void activateClippingParameters()
	{
		this.clippingParametersActivated = true;
		this.nearClip = new NearClipParameter(animation);
		this.farClip = new FarClipParameter(animation);

		fireAddEvent(nearClip);
		fireAddEvent(farClip);
	}

	@Override
	public Parameter getNearClip()
	{
		return nearClip;
	}

	@Override
	public Parameter getFarClip()
	{
		return farClip;
	}

	@Override
	public Collection<Parameter> getParameters()
	{
		if (parameters == null || parameters.isEmpty())
		{
			parameters = new ArrayList<Parameter>(8);
			parameters.add(eyeLat);
			parameters.add(eyeLon);
			parameters.add(eyeElevation);
			parameters.add(lookAtLat);
			parameters.add(lookAtLon);
			parameters.add(lookAtElevation);
			parameters.add(roll);
			parameters.add(fieldOfView);
			if (clippingParametersActivated)
			{
				parameters.add(nearClip);
				parameters.add(farClip);
			}
		}
		return Collections.unmodifiableCollection(parameters);
	}

	@Override
	public void smoothEyeSpeed()
	{
		List<KeyFrame> eyeKeyFrames = eyeLat.getKeyFramesWithThisParameter();

		// There needs to be at least two key frames for smoothing to apply
		int numberOfKeys = eyeKeyFrames.size();
		if (numberOfKeys <= 1)
		{
			return;
		}

		// Calculate the cumulative distance at each key frame
		double[] cumulativeDistance = new double[numberOfKeys - 1];
		for (int i = 0; i < numberOfKeys - 1; i++)
		{
			int firstFrame = eyeKeyFrames.get(i).getFrame();
			int lastFrame = eyeKeyFrames.get(i + 1).getFrame();

			cumulativeDistance[i] = i == 0 ? 0 : cumulativeDistance[i - 1];

			Vector3 vStart = null;
			for (int frame = firstFrame; frame <= lastFrame; frame++)
			{
				double x = eyeLat.getValueAtFrame(frame).getValue();
				double y = eyeLon.getValueAtFrame(frame).getValue();
				double z = eyeElevation.getValueAtFrame(frame).getValue();

				Vector3 vEnd = new Vector3(x, y, z);
				if (vStart != null)
				{
					cumulativeDistance[i] += vStart.subtract(vEnd).distance();
				}
				vStart = vEnd;
			}
		}

		// Calculate where to put the new frames
		int[] newFrames = new int[numberOfKeys];
		int firstFrame = eyeKeyFrames.get(0).getFrame();
		int lastFrame = eyeKeyFrames.get(numberOfKeys - 1).getFrame();

		newFrames[0] = firstFrame;
		newFrames[numberOfKeys - 1] = lastFrame;

		for (int i = 1; i < numberOfKeys - 1; i++)
		{
			newFrames[i] =
					(int) Math.round(Math.abs((firstFrame - lastFrame + 1) * cumulativeDistance[i - 1]
							/ cumulativeDistance[numberOfKeys - 2]));
		}

		// Fix any frames that have been swapped over by mistake
		for (int i = 0; i < newFrames.length - 1; i++)
		{
			if (newFrames[i] >= newFrames[i + 1])
			{
				newFrames[i + 1] = newFrames[i] + 1;
			}
		}
		// Make sure the last frame is correct. If not, adjust it and re-adjust previous frames.
		if (newFrames[numberOfKeys - 1] != lastFrame)
		{
			newFrames[numberOfKeys - 1] = lastFrame;
			for (int i = newFrames.length - 1; i > 0; i--)
			{
				if (newFrames[i] <= newFrames[i - 1])
				{
					newFrames[i - 1] = newFrames[i] - 1;
				}
			}
		}
		if (newFrames[0] != firstFrame)
		{
			throw new IllegalStateException("Expected new first frame to be '" + firstFrame + "' but is '"
					+ newFrames[0] + "'");
		}

		// Insert new key frames for eye parameters. This may involve creating new key frames so that
		// other animatable objects aren't affected
		for (int i = 0; i < eyeKeyFrames.size(); i++)
		{
			KeyFrame oldKeyFrame = eyeKeyFrames.get(i);

			Collection<ParameterValue> eyeValues = oldKeyFrame.getValuesForParameters(getParameters());
			KeyFrame newKeyFrame = new KeyFrameImpl(newFrames[i], eyeValues);

			oldKeyFrame.removeValuesForParameters(getParameters());

			animation.insertKeyFrame(newKeyFrame);
		}
		animation.removeEmptyKeyFrames();
	}

	@Override
	public void copyStateFrom(Camera camera)
	{
		camera.copyChangeListenersTo(this);
		this.eyeLat = camera.getEyeLat();
		this.eyeLon = camera.getEyeLon();
		this.eyeElevation = camera.getEyeElevation();
		this.lookAtLat = camera.getLookAtLat();
		this.lookAtLon = camera.getLookAtLon();
		this.lookAtElevation = camera.getLookAtElevation();
		this.roll = camera.getRoll();
		this.fieldOfView = camera.getFieldOfView();
		this.clippingParametersActivated = camera.isClippingParametersActive();
		this.nearClip = camera.getNearClip();
		this.farClip = camera.getFarClip();
		this.parameters = null;
		this.setEnabled(camera.isEnabled(), false);
		this.setArmed(camera.isArmed());
	}

	@Override
	protected String getXmlElementName(AnimationIOConstants constants)
	{
		return constants.getCameraElementName();
	}

	@Override
	protected AnimatableBase createAnimatableFromXml(String name, Animation animation, boolean enabled,
			Element element, AnimationFileVersion version, AVList context)
	{
		CameraImpl camera = createCamera(name, animation);
		setupCameraFromXml(camera, element, version, context);
		return camera;
	}

	protected CameraImpl createCamera(String name, Animation animation)
	{
		return new CameraImpl(name, animation);
	}

	protected void setupCameraFromXml(CameraImpl camera, Element element, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		XPath xpath = XPathFactory.newInstance().newXPath();

		camera.parameters = null;
		camera.eyeLat =
				new EyeLatParameter().fromXml(WWXML.getElement(element, constants.getCameraEyeLatElementName(), xpath),
						version, context);
		camera.eyeLon =
				new EyeLonParameter().fromXml(WWXML.getElement(element, constants.getCameraEyeLonElementName(), xpath),
						version, context);
		camera.eyeElevation =
				new EyeElevationParameter().fromXml(
						WWXML.getElement(element, constants.getCameraEyeElevationElementName(), xpath), version,
						context);

		camera.lookAtLat =
				new LookatLatParameter().fromXml(
						WWXML.getElement(element, constants.getCameraLookatLatElementName(), xpath), version, context);
		camera.lookAtLon =
				new LookatLonParameter().fromXml(
						WWXML.getElement(element, constants.getCameraLookatLonElementName(), xpath), version, context);
		camera.lookAtElevation =
				new LookatElevationParameter().fromXml(
						WWXML.getElement(element, constants.getCameraLookatElevationElementName(), xpath), version,
						context);

		Element rollElement = WWXML.getElement(element, constants.getCameraRollElementName(), xpath);
		if (rollElement != null)
		{
			camera.roll = new RollParameter().fromXml(rollElement, version, context);
		}
		else
		{
			camera.roll = new RollParameter(camera.getAnimation());
		}

		Element fieldOfViewElement = WWXML.getElement(element, constants.getCameraFieldOfViewElementName(), xpath);
		if (fieldOfViewElement != null)
		{
			camera.fieldOfView = new FieldOfViewParameter().fromXml(fieldOfViewElement, version, context);
		}
		else
		{
			camera.fieldOfView = new FieldOfViewParameter(camera.getAnimation());
		}

		// Near and far clipping are optional.
		Element nearClipElement = WWXML.getElement(element, constants.getCameraNearClipElementName(), xpath);
		Element farClipElement = WWXML.getElement(element, constants.getCameraFarClipElementName(), xpath);
		if (nearClipElement != null || farClipElement != null)
		{
			camera.nearClip =
					nearClipElement != null ? new NearClipParameter().fromXml(nearClipElement, version, context)
							: new NearClipParameter(animation);
			camera.farClip =
					farClipElement != null ? new FarClipParameter().fromXml(farClipElement, version, context)
							: new FarClipParameter(animation);
			camera.clippingParametersActivated = true;
		}

		camera.connectCodependants();
		camera.connectAsListener();
	}
}
