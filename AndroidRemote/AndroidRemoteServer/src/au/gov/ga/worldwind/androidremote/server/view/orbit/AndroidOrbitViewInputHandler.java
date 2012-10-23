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
package au.gov.ga.worldwind.androidremote.server.view.orbit;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import au.gov.ga.worldwind.androidremote.server.ServerCommunicator;
import au.gov.ga.worldwind.androidremote.shared.Communicator.State;
import au.gov.ga.worldwind.androidremote.shared.CommunicatorListener;
import au.gov.ga.worldwind.androidremote.shared.Message;
import au.gov.ga.worldwind.androidremote.shared.messages.ShakeMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.DownMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.Finger;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.FingerMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.MoveMessage;
import au.gov.ga.worldwind.androidremote.shared.messages.finger.UpMessage;

/**
 * Custom {@link ViewInputHandler} that converts Android finger gestures into
 * globe camera movements. Supports dragging, double tapping, pinch-to-zoom,
 * rotation, pitch, and spinning the globe with infinite momentum.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AndroidOrbitViewInputHandler extends FreeRotateOrbitViewInputHandler implements CommunicatorListener
{
	public static enum DoubleState
	{
		NONE,
		ZOOM,
		PITCH,
		HEADING_MOVE,
		HEADING_ROTATE
	}

	private final static double HALF_PI = Math.PI * 0.5;
	private final static double DEG_TO_RAD = Math.PI / 180d;
	private final static double RAD_TO_DEG = 180d / Math.PI;

	private final static double doubleToSingleDelay = 0.2; //seconds
	private final static double doubleTapDelay = 0.5; //seconds

	private final static ActionAttributes horizontalMovementAttributes = new ActionAttributes(4e-7, 4e-2, true, 0.4);
	private final static ActionAttributes horizontalGestureAttributes = new ActionAttributes(2e-4, 2e1, false, 0.4);
	private final static ActionAttributes verticalMovementAttributes = new ActionAttributes(1e-2, 1e-2, true, 0.85);
	private final static ActionAttributes verticalGestureAttributes = new ActionAttributes(5e-1, 5e-1, false, 0.85);
	private final static ActionAttributes headingMovementAttributes = new ActionAttributes(1, 1, false, 0.85);
	private final static ActionAttributes headingGestureAttributes = new ActionAttributes(1e1, 1e1, false, 0.85);
	private final static ActionAttributes pitchMovementAttributes = new ActionAttributes(5e-1, 1, true, 0.85);
	private final static ActionAttributes pitchGestureAttributes = new ActionAttributes(1e1, 2e1, false, 0.85);
	private final static ActionAttributes resetAttributes = new ActionAttributes(1, 1, true, 0.85);
	private final static DeviceAttributes deviceAttributes = new DeviceAttributes(1.0);

	private final static double beginZoomChange = 40d;
	private final static double beginHeadingChange = 5d * DEG_TO_RAD;
	private final static double beginPitchChange = 50d;
	private final static double minGestureVelocity = 0.5d;
	private final static double sameGestureDirectionDelta = 45d * DEG_TO_RAD;

	private final Map<Integer, Finger> lastFingers = new HashMap<Integer, Finger>();
	private final Map<Integer, Finger> secondLastFingers = new HashMap<Integer, Finger>();
	private final Map<Integer, Long> lastUpTime = new HashMap<Integer, Long>();

	private Long lastDoubleTime;
	private DoubleFinger startDoubleFinger;
	private DoubleFinger lastDoubleFinger;
	private DoubleState doubleState = DoubleState.NONE;
	private double lastDoubleStateInput;

	private double gestureAngle = 0;
	private double gestureSpeed = 0;
	private double lastGestureAngle = 0;
	private double lastGestureSpeed = 0;
	private Long gestureLastNanos;
	private Long lastGestureTime;
	private int gestureCount = 0;
	private Long doubleGestureLastNanos;
	private Finger doubleGestureLastFinger;
	private DoubleState gestureDoubleState = DoubleState.NONE;

	public AndroidOrbitViewInputHandler()
	{
		ServerCommunicator.INSTANCE.addListener(this);
	}

	@Override
	public void stateChanged(State newState)
	{
		lastFingers.clear();
		secondLastFingers.clear();
		stopGesture();
		doubleState = DoubleState.NONE;
		gestureDoubleState = DoubleState.NONE;
	}

	public void stopGesture()
	{
		gestureSpeed = 0;
		lastGestureSpeed = 0;
		lastGestureTime = 0L;
	}

	@Override
	public void receivedMessage(Message<?> message)
	{
		if (message instanceof FingerMessage)
		{
			handleFingerMessage((FingerMessage<?>) message);
		}
		else if (message instanceof ShakeMessage)
		{
			onResetHeadingPitchRoll(resetAttributes);
		}
	}

	public DoubleState getBeginDoubleState(DoubleFingerDelta delta)
	{
		if (Math.abs(delta.distanceDelta) > beginZoomChange)
		{
			return DoubleState.ZOOM;
		}
		if (Math.abs(delta.yDelta) > beginPitchChange)
		{
			return DoubleState.PITCH;
		}
		if (Math.abs(delta.xDelta) > beginPitchChange)
		{
			return DoubleState.HEADING_MOVE;
		}
		if (Math.abs(delta.angleDelta) > beginHeadingChange)
		{
			return DoubleState.HEADING_ROTATE;
		}
		return DoubleState.NONE;
	}

	public double getDoubleStateInput(DoubleFingerDelta delta, DoubleState state)
	{
		switch (state)
		{
		case ZOOM:
			return delta.distanceDelta;
		case PITCH:
			return delta.yDelta;
		case HEADING_MOVE:
			return delta.xDelta * 1e-1;
		case HEADING_ROTATE:
			return delta.angleDelta * RAD_TO_DEG;
		default:
			return 0;
		}
	}

	public DoubleState getBestDoubleState(DoubleFingerDelta delta)
	{
		double distanceRatio = Math.abs(delta.distanceDelta) / beginZoomChange;
		double angleRatio = Math.abs(delta.angleDelta) / beginHeadingChange;
		double xRatio = Math.abs(delta.xDelta) / beginPitchChange;
		double yRatio = Math.abs(delta.yDelta) / beginPitchChange;
		double max = Math.max(distanceRatio, Math.max(angleRatio, Math.max(xRatio, yRatio)));
		if (max == distanceRatio)
		{
			return DoubleState.ZOOM;
		}
		if (max == angleRatio)
		{
			return DoubleState.HEADING_ROTATE;
		}
		if (max == xRatio)
		{
			return DoubleState.HEADING_MOVE;
		}
		if (max == yRatio)
		{
			return DoubleState.PITCH;
		}
		return DoubleState.NONE;
	}

	private void handleFingerMessage(FingerMessage<?> message)
	{
		double forwardInput = 0, sideInput = 0, zoomInput = 0, headingInput = 0, pitchInput = 0;

		Finger[] fingers = message.fingers;
		if (message instanceof DownMessage)
		{
			gestureSpeed = 0;

			for (Finger finger : fingers)
			{
				if (!lastFingers.containsKey(finger.id) && finger.down)
				{
					lastFingers.put(finger.id, finger);
				}
			}

			if (fingers.length == 2)
			{
				startDoubleFinger = new DoubleFinger(fingers[0], fingers[1]);
				lastDoubleFinger = null;
				doubleState = DoubleState.NONE;
				lastDoubleStateInput = 0;

				gestureCount = 0;
				lastGestureSpeed = 0;
			}
		}
		else if (message instanceof MoveMessage)
		{
			for (Finger finger : fingers)
			{
				//this will never happen:
				if (!lastFingers.containsKey(finger.id))
				{
					lastFingers.put(finger.id, finger);
				}
			}

			if (fingers.length == 1)
			{
				boolean singleAllowed =
						lastDoubleTime == null || (System.nanoTime() - lastDoubleTime) / 1e9d > doubleToSingleDelay;
				if (singleAllowed)
				{
					Finger finger = fingers[0];
					Finger lastFinger = lastFingers.get(finger.id);
					forwardInput = finger.y - lastFinger.y;
					sideInput = lastFinger.x - finger.x;

					double speed = Math.sqrt(finger.xVelocity * finger.xVelocity + finger.yVelocity * finger.yVelocity);
					if (speed < minGestureVelocity / 4d)
					{
						gestureCount = 0;
						lastGestureSpeed = 0;
					}
				}
			}
			else if (fingers.length == 2)
			{
				DoubleFinger doubleFinger = new DoubleFinger(fingers[0], fingers[1]);
				if (startDoubleFinger == null)
				{
					startDoubleFinger = doubleFinger;
				}
				if (lastDoubleFinger == null)
				{
					doubleState = DoubleState.NONE;
				}
				DoubleFinger otherDoubleFinger = doubleState == DoubleState.NONE ? startDoubleFinger : lastDoubleFinger;
				DoubleFingerDelta delta = new DoubleFingerDelta(otherDoubleFinger, doubleFinger);
				double input = getDoubleStateInput(delta, doubleState);
				lastDoubleStateInput = input;

				switch (doubleState)
				{
				case NONE:
					doubleState = getBeginDoubleState(delta);
					break;
				case ZOOM:
					zoomInput = input;
					break;
				case PITCH:
					pitchInput = input;
					break;
				case HEADING_MOVE:
				case HEADING_ROTATE:
					headingInput = input;
					break;
				}

				lastDoubleTime = System.nanoTime();
				lastDoubleFinger = doubleFinger;
			}

			for (Finger finger : fingers)
			{
				secondLastFingers.put(finger.id, lastFingers.get(finger.id));
				lastFingers.put(finger.id, finger);
			}
		}
		else if (message instanceof UpMessage)
		{
			Finger upFinger = null;
			for (Finger finger : fingers)
			{
				if (!finger.down)
				{
					lastFingers.remove(finger.id);
					upFinger = finger;
				}
			}
			if (upFinger != null)
			{
				if (fingers.length == 2)
				{
					doubleGestureLastNanos = System.nanoTime();
					doubleGestureLastFinger = upFinger;
				}
				else if (fingers.length == 1)
				{
					//last finger up, treat it as a fling

					double speed =
							Math.sqrt(upFinger.xVelocity * upFinger.xVelocity + upFinger.yVelocity * upFinger.yVelocity);

					gestureLastNanos = System.nanoTime();
					if (doubleGestureLastNanos != null
							&& (gestureLastNanos - doubleGestureLastNanos) / 1e9d < doubleToSingleDelay)
					{
						double lastSpeed =
								Math.sqrt(doubleGestureLastFinger.xVelocity * doubleGestureLastFinger.xVelocity
										+ doubleGestureLastFinger.yVelocity * doubleGestureLastFinger.yVelocity);
						if (speed > minGestureVelocity || lastSpeed > minGestureVelocity)
						{
							//double gesture

							Finger finger1 = upFinger;
							Finger finger2 = doubleGestureLastFinger;

							DoubleState newGestureDoubleState = doubleState;
							double signum = lastDoubleStateInput;
							if (newGestureDoubleState == DoubleState.NONE)
							{
								if (startDoubleFinger != null)
								{
									DoubleFinger doubleFinger =
											startDoubleFinger.finger1.id == finger2.id ? new DoubleFinger(finger2,
													finger1) : new DoubleFinger(finger1, finger2);
									DoubleFingerDelta delta = new DoubleFingerDelta(startDoubleFinger, doubleFinger);
									newGestureDoubleState = getBestDoubleState(delta);
									signum = getDoubleStateInput(delta, newGestureDoubleState);
								}
							}
							gestureDoubleState = newGestureDoubleState;

							if (gestureDoubleState == DoubleState.PITCH
									|| gestureDoubleState == DoubleState.HEADING_MOVE
									|| gestureDoubleState == DoubleState.HEADING_ROTATE)
							{
								gestureDoubleState = doubleState;
								gestureSpeed = Math.max(speed, lastSpeed) * Math.signum(signum);
							}
							else if (gestureDoubleState == DoubleState.ZOOM)
							{
								Finger lastFinger1 = estimateLastFinger(finger1);
								Finger lastFinger2 = estimateLastFinger(finger2);
								DoubleFinger doubleFinger = new DoubleFinger(finger1, finger2);
								DoubleFinger lastDoubleFinger = new DoubleFinger(lastFinger1, lastFinger2);

								double zoomChange = lastDoubleFinger.distance - doubleFinger.distance;
								gestureSpeed = zoomChange;
							}
							lastGestureTime = System.nanoTime();
						}
					}
					else if (speed > minGestureVelocity)
					{
						//single gesture
						gestureDoubleState = DoubleState.NONE;
						gestureCount++;
						gestureAngle = Math.atan2(upFinger.yVelocity, -upFinger.xVelocity) + HALF_PI;
						boolean similarAngle =
								DoubleFingerDelta.absAngleBetween(gestureAngle, lastGestureAngle) < sameGestureDirectionDelta;
						gestureSpeed = similarAngle ? (lastGestureSpeed + speed / gestureCount) : (speed);

						lastGestureSpeed = gestureSpeed;
						lastGestureAngle = gestureAngle;
						lastGestureTime = System.nanoTime();
					}
					getView().firePropertyChange(AVKey.VIEW, null, getView());
				}

				if (gestureSpeed == 0)
				{
					//no gesture, check for double tap

					long time = System.nanoTime();
					if (lastGestureTime == null || time - lastGestureTime > doubleToSingleDelay)
					{
						Long lastTime = lastUpTime.get(upFinger.id);
						if (lastTime != null && (time - lastTime) / 1e9d < doubleTapDelay)
						{
							doubleTapZoom(fingers.length < 2);
							lastUpTime.clear();
						}
						else
						{
							lastUpTime.put(upFinger.id, time);
						}
					}
				}
			}
		}

		move(forwardInput, sideInput, zoomInput, headingInput, pitchInput);
	}

	private void doubleTapZoom(boolean zoomIn)
	{
		View v = getView();
		if (!(v instanceof OrbitView))
			return;
		OrbitView view = (OrbitView) v;

		double minElevation = 1000d;

		double zoom = view.getZoom();
		if (zoom > minElevation)
		{
			zoom = Math.max(minElevation, zoomIn ? zoom / 3 : zoom * 3);
		}
		Position beginCenter = view.getCenterPosition();
		Position endCenter = new Position(beginCenter.latitude, beginCenter.longitude, beginCenter.getElevation());

		view.stopMovement();
		view.stopAnimations();
		view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter, endCenter,
				view.getHeading(), view.getHeading(), view.getPitch(), view.getPitch(), view.getZoom(), zoom, 1000,
				WorldWind.ABSOLUTE));
		wwd.redraw();
	}

	private void move(final double forwardInput, final double sideInput, final double zoomInput,
			final double headingInput, final double pitchInput)
	{
		if (forwardInput != 0 || sideInput != 0 || zoomInput != 0 || headingInput != 0 || pitchInput != 0)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					getView().stopAnimations();

					if (forwardInput != 0 || sideInput != 0)
					{
						Angle angle = Angle.fromRadians(Math.atan2(-forwardInput, -sideInput) - HALF_PI);
						double distance = Math.sqrt(forwardInput * forwardInput + sideInput * sideInput);
						rotateFree(angle, Angle.fromDegrees(distance), deviceAttributes, horizontalMovementAttributes);
					}
					if (zoomInput != 0)
					{
						onVerticalTranslate(zoomInput, zoomInput, deviceAttributes, verticalMovementAttributes);
					}
					if (headingInput != 0)
					{
						Angle headingChange =
								Angle.fromDegrees(headingInput * getScaleValueRotate(headingMovementAttributes));
						onRotateView(headingChange, Angle.ZERO, headingMovementAttributes);
					}
					if (pitchInput != 0)
					{
						Angle pitchChange =
								Angle.fromDegrees(pitchInput * getScaleValueRotate(pitchMovementAttributes));
						onRotateView(Angle.ZERO, pitchChange, pitchMovementAttributes);
					}
				}
			});
		}
	}

	private Finger estimateLastFinger(Finger finger)
	{
		return new Finger(finger.id, finger.x - finger.xVelocity, finger.y - finger.yVelocity, finger.xVelocity,
				finger.yVelocity, finger.down);
	}

	@Override
	public void apply()
	{
		super.apply();

		if (gestureLastNanos != null)
		{
			long currentNanos = System.nanoTime();
			double time = (currentNanos - gestureLastNanos) / 1e9d;
			gestureLastNanos = currentNanos;

			if (gestureSpeed != 0)
			{
				switch (gestureDoubleState)
				{
				case NONE:
					rotateFree(Angle.fromRadians(gestureAngle), Angle.fromDegrees(time * gestureSpeed),
							deviceAttributes, horizontalGestureAttributes);
					break;
				case PITCH:
					Angle pitchChange =
							Angle.fromDegrees(time * gestureSpeed * getScaleValueRotate(pitchGestureAttributes));
					onRotateView(Angle.ZERO, pitchChange, pitchGestureAttributes);
					break;
				case HEADING_MOVE:
				case HEADING_ROTATE:
					Angle headingMoveChange =
							Angle.fromDegrees(time * gestureSpeed * getScaleValueRotate(headingGestureAttributes));
					onRotateView(headingMoveChange, Angle.ZERO, headingGestureAttributes);
					break;
				case ZOOM:
					double zoomChange = time * gestureSpeed;
					onVerticalTranslate(zoomChange, zoomChange, deviceAttributes, verticalGestureAttributes);
					break;
				}
				getView().firePropertyChange(AVKey.VIEW, null, getView());
			}
		}
	}
}
