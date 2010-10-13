package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

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

/**
 * A WorldWind Layer that displays the camera path of a given {@link Animation}
 * in the 3D world.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraPathLayer extends AbstractLayer implements AnimationEventListener
{
	private EyePositionPath eyePositionPath;
	private LookatPositionPath lookatPositionPath;
	
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
	 * @param animation The animation instance whose camera path will be displayed by this layer
	 */
	public CameraPathLayer(Animation animation)
	{
		Validate.notNull(animation, "An animation instance is required");
		eyePositionPath = new EyePositionPath(animation);
		lookatPositionPath = new LookatPositionPath(animation);
		
		updateAnimation(animation);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		eyePositionPath.render(dc);
		lookatPositionPath.render(dc);
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
		
		reset();
		update();
	}

	private void reset()
	{
		this.frameCount = animation.getFrameCount();
		eyePositionPath.resetPath();
		lookatPositionPath.resetPath();
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
