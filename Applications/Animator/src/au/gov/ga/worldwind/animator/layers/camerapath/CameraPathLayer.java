package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.animation.event.AnimationEvent;
import au.gov.ga.worldwind.animator.animation.event.AnimationEventListener;
import au.gov.ga.worldwind.animator.animation.event.KeyFrameEvent;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;

import com.sun.opengl.util.BufferUtil;

/**
 * A WorldWind Layer that displays the camera path of a given {@link Animation}
 * in the 3D world.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraPathLayer extends AbstractLayer implements AnimationEventListener
{
	/** The default colour to use for painting the camera path */
	private static final Color DEFAULT_CAMERA_PATH_COLOUR = Color.WHITE;
	
	/** The number of frames currently held in the vertex buffers */
	private int frameCount;
	
	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;

	/** The 'front' buffer, used to hold vertices ready for drawing */
	private DoubleBuffer drawVertexBuffer;
	
	/** The 'back' buffer, used to update vertices when a change is detected */
	private DoubleBuffer updateVertexBuffer;
	
	/** A lock used to synchronise access to the vertex buffers */
	private Object vertexBufferLock = new Object();
	
	/** A thread used to update the vertex buffers outside of the render thread */
	private ExecutorService updater = Executors.newFixedThreadPool(1, new DaemonThreadFactory());
	
	/** The colour to use to draw the camera path */
	private Color cameraPathColour = DEFAULT_CAMERA_PATH_COLOUR;
	
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
		this.animation = animation;
		
		resetVertexBuffers();
		updateVertexBuffers();
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		drawPath(dc);
		drawEyePositionNodes(dc);
	}

	@Override
	public void receiveAnimationEvent(AnimationEvent event)
	{
		if (frameCountHasChanged())
		{
			resetVertexBuffers();
			updateVertexBuffers();
		}
		else if (cameraPathHasChanged(event))
		{
			updateVertexBuffers();
		}
	}
	
	public void updateAnimation(Animation animation)
	{
		this.animation.removeChangeListener(this);
		this.animation = animation;
		this.animation.addChangeListener(this);
		
		resetVertexBuffers();
		updateVertexBuffers();
	}

	/**
	 * Draw the camera path
	 */
	private void drawPath(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glColor3fv(cameraPathColour.getColorComponents(null), 0);
			synchronized (vertexBufferLock)
			{
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, drawVertexBuffer);
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, animation.getFrameOfLastKeyFrame());
			}
		}
		finally
		{
			gl.glPopClientAttrib();
		}
	}
	
	/**
	 * Draw the camera eye position nodes
	 */
	private void drawEyePositionNodes(DrawContext dc)
	{
		AnimationContext context = new AnimationContextImpl(animation);
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			if (isEyePositionFrame(keyFrame))
			{
				Vec4 eyeVector = dc.getGlobe().computePointFromPosition(animation.getCamera().getEyePositionAtFrame(context, keyFrame.getFrame()));
				Sphere marker = new Sphere(eyeVector, 100);
				marker.render(dc);
			}
		}
		
	}

	/**
	 * Re-create the vertex buffers to the current animation size
	 */
	private void resetVertexBuffers()
	{
		frameCount = animation.getFrameCount();
		this.drawVertexBuffer = BufferUtil.newDoubleBuffer(frameCount * 3);
		this.updateVertexBuffer = BufferUtil.newDoubleBuffer(frameCount * 3);
	}
	
	private void recalulateVertexBuffers()
	{
		populateVertexBuffer();
		swapBuffers();
	}
	
	private void updateVertexBuffers()
	{
		updater.execute(new Runnable()
		{
			@Override
			public void run()
			{
				recalulateVertexBuffers();
			}
		});
	}
	
	/**
	 * Populate the update vertex buffer with the coordinates of the 
	 * eye location at each frame in the animation.
	 * <p/>
	 * Vertex buffer has size <code>3*animation.getlastFrame()</code>, with <code>[x,y,z]</code> stored at 
	 * <code>[3*frame, 3*frame+1, 3*frame+2]</code>
	 */
	private void populateVertexBuffer()
	{
		Camera renderCamera = animation.getCamera();
		AnimationContext context = new AnimationContextImpl(animation);
		for (int frame = animation.getFrameOfFirstKeyFrame(); frame < animation.getFrameOfLastKeyFrame(); frame ++)
		{
			Vec4 eyeVector = context.getView().getGlobe().computePointFromPosition(renderCamera.getEyePositionAtFrame(context, frame));
			updateVertexBuffer.put(eyeVector.x);
			updateVertexBuffer.put(eyeVector.y);
			updateVertexBuffer.put(eyeVector.z);
		}
		updateVertexBuffer.rewind();
	}
	
	/**
	 * Swap the two buffers 
	 */
	private void swapBuffers()
	{
		synchronized (vertexBufferLock)
		{
			DoubleBuffer tmp = drawVertexBuffer;
			drawVertexBuffer = updateVertexBuffer;
			updateVertexBuffer = tmp;
			drawVertexBuffer.rewind();
		}
	}
	
	private boolean isEyePositionFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(animation.getCamera().getEyeLat()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeLon()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeElevation());
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
		return isEyePositionFrame(keyFrameEvent.getKeyFrame());
	}

	private boolean frameCountHasChanged()
	{
		return animation.getFrameCount() != frameCount;
	}
}
