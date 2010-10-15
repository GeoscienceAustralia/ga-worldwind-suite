package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.KeyFrameEvent;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.animator.view.orbit.BasicOrbitView;

/**
 * A WorldWind Layer that displays the camera path of a given {@link Animation}
 * in the 3D world.
 */
public class CameraPathLayer extends AbstractLayer implements AnimationEventListener, SelectListener
{
	private EyePositionPath eyePositionPath;
	private LookatPositionPath lookatPositionPath;
	private KeyFrameMarkers keyFrameMarkers;
	
	/** The number of frames currently held in the vertex buffers */
	private int frameCount;
	
	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;

	/** A thread used to update the vertex buffers outside of the render thread */
	private ExecutorService updater = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
	
	/**
	 * Constructor.
	 * <p/>
	 * Initialises the mandatory animation instance.
	 * 
	 * @param wwd The current WorldWindow
	 * @param animation The animation instance whose camera path will be displayed by this layer
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
		
		updateAnimation(animation);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		double nearClipDistance = setNearClipDistance(dc, 1.0);
		
		eyePositionPath.render(dc);
		lookatPositionPath.render(dc);
		keyFrameMarkers.render(dc);
		
		setNearClipDistance(dc, nearClipDistance);
	}
	
	/**
	 * Set the near clip distance if it is settable, returning the original clip distance before the set operation.
	 */
	private double setNearClipDistance(DrawContext dc, double nearClipDistance)
	{
		double currentClipDistance = dc.getView().getNearClipDistance();
		
		if (dc.getView() instanceof BasicOrbitView)
		{
			((BasicOrbitView)dc.getView()).setNearClipDistance(nearClipDistance);
		}
		
		return currentClipDistance;
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
		
		eyePositionPath.updateAnimation(animation);
		lookatPositionPath.updateAnimation(animation);
		keyFrameMarkers.updateAnimation(animation);
		
		reset();
		update();
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
	
	private void update()
	{
		updater.execute(new Runnable()
		{
			@Override
			public void run()
			{
				eyePositionPath.recalulatePath();
				lookatPositionPath.recalulatePath();
				keyFrameMarkers.recalulateKeyFrameMarkers();
			}
		});
	}
	
	private boolean cameraPathHasChanged(AnimationEvent event)
	{
		// Any event attached to the Camera counts
		if (event.hasOwnerInChainOfType(Camera.class))
		{
			return true;
		}
		
		// Otherwise, any key frame event that includes a camera parameter
		KeyFrameEvent keyFrameEvent = (KeyFrameEvent)event.getCauseOfClass(KeyFrameEvent.class);
		if (keyFrameEvent == null)
		{
			return false;
		}
		return isCameraFrame(keyFrameEvent.getKeyFrame());
	}

	private boolean isCameraFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(animation.getCamera().getEyeLat()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeLon()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeElevation()) ||
				keyFrame.hasValueForParameter(animation.getCamera().getLookAtLat()) ||
				keyFrame.hasValueForParameter(animation.getCamera().getLookAtLon()) ||
				keyFrame.hasValueForParameter(animation.getCamera().getLookAtElevation());
	}
	
	private boolean frameCountHasChanged()
	{
		return animation.getFrameCount() != frameCount;
	}
}
