package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.KeyFrameEvent;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A WorldWind Layer that displays the camera path of a given {@link Animation}
 * in the 3D world.
 */
public class CameraPathLayer extends AbstractLayer implements AnimationEventListener, SelectListener, ChangeOfAnimationListener
{
	private EyePositionPath eyePositionPath;
	private LookatPositionPath lookatPositionPath;
	private KeyFrameMarkers keyFrameMarkers;

	/** The number of frames currently held in the vertex buffers */
	private int frameCount;

	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;
	private boolean animationChanged = true;

	/** A thread used to update the vertex buffers outside of the render thread */
	private ExecutorService updater = Executors.newFixedThreadPool(1, new DaemonThreadFactory("Camera Path Updater"));
	private Future<UpdateTask> currentTask;
	private Future<UpdateTask> nextTask;
	private WorldWindow worldWindow;

	/**
	 * Constructor.
	 * <p/>
	 * Initialises the mandatory animation instance.
	 * 
	 * @param wwd
	 *            The current WorldWindow
	 * @param animation
	 *            The animation instance whose camera path will be displayed by
	 *            this layer
	 */
	public CameraPathLayer(WorldWindow wwd, Animation animation)
	{
		Validate.notNull(animation, "An animation instance is required");
		Validate.notNull(wwd, "A world window is required");
		setPickEnabled(true);

		eyePositionPath = new EyePositionPath(animation);
		lookatPositionPath = new LookatPositionPath(animation);
		lookatPositionPath.setEnableDepthTesting(false);
		keyFrameMarkers = new KeyFrameMarkers(wwd, animation);
		worldWindow = wwd;

		updateAnimation(animation);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (animationChanged)
		{
			reset();
			update();
			animationChanged = false;
		}

		eyePositionPath.render(dc);
		lookatPositionPath.render(dc);
		keyFrameMarkers.render(dc);
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		keyFrameMarkers.render(dc);
	}

	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (frameCountHasChanged())
		{
			reset();
			update();
		}
		else if (cameraPathHasChanged(event))
		{
			update();
		}
	}

	public void updateAnimation(Animation animation)
	{
		if (this.animation != null)
		{
			this.animation.removeChangeListener(this);
		}
		this.animation = animation;
		this.animation.addChangeListener(this);

		eyePositionPath.setAnimation(animation);
		lookatPositionPath.setAnimation(animation);
		keyFrameMarkers.setAnimation(animation);

		animationChanged = true;
	}

	@Override
	public void selected(SelectEvent event)
	{
		// Propagate the event to the key frame markers
		keyFrameMarkers.selected(event);
	}

	private void reset()
	{
		this.frameCount = animation.getFrameCount();
		eyePositionPath.resetPath();
		lookatPositionPath.resetPath();
		keyFrameMarkers.resetKeyFrameMarkers();
	}

	@SuppressWarnings("unchecked")
	private void update()
	{
		if (currentTask == null || currentTask.isDone())
		{
			currentTask = (Future<UpdateTask>) updater.submit(new UpdateTask());
		}
		else
		{
			if (nextTask != null)
			{
				nextTask.cancel(true);
			}
			nextTask = (Future<UpdateTask>) updater.submit(new UpdateTask());
		}
	}

	private boolean cameraPathHasChanged(AnimationEvent event)
	{
		return eventIsRelatedToCamera(event) || isKeyFrameEventContainingCameraParameter(event);
	}

	private boolean isKeyFrameEventContainingCameraParameter(AnimationEvent event)
	{
		if (eventValueIsKeyFrameContainingCameraParameter(event))
		{
			return true;
		}

		KeyFrameEvent keyFrameEvent = (KeyFrameEvent) event.getCauseOfClass(KeyFrameEvent.class);
		if (keyFrameEvent == null)
		{
			return false;
		}
		return isCameraFrame(keyFrameEvent.getKeyFrame());
	}

	private boolean eventValueIsKeyFrameContainingCameraParameter(AnimationEvent event)
	{
		Object rootValue = event.getRootCause().getValue();
		if (rootValue instanceof KeyFrame)
		{
			return isCameraFrame((KeyFrame) rootValue);
		}
		return false;
	}

	private boolean eventIsRelatedToCamera(AnimationEvent event)
	{
		return event.hasOwnerInChainOfType(Camera.class);
	}

	private boolean isCameraFrame(KeyFrame keyFrame)
	{
		if (keyFrame == null)
		{
			return false;
		}

		return keyFrame.hasValueForParameter(animation.getCamera().getEyeLat())
				|| keyFrame.hasValueForParameter(animation.getCamera().getEyeLon())
				|| keyFrame.hasValueForParameter(animation.getCamera().getEyeElevation())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtLat())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtLon())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtElevation());
	}

	private boolean frameCountHasChanged()
	{
		return animation.getFrameCount() != frameCount;
	}

	private class UpdateTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				eyePositionPath.recalulatePath();
				lookatPositionPath.recalulatePath();
				keyFrameMarkers.recalulateKeyFrameMarkers();
				worldWindow.redraw();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
