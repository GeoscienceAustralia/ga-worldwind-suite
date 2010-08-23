package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWBufferUtil;

import java.awt.Color;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.opengl.util.BufferUtil;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.camera.Camera;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A WorldWind Layer that displays the camera path of a given {@link Animation}
 * in the 3D world.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class CameraPathLayer extends AbstractLayer implements ChangeListener
{
	/** The default colour to use for painting the camera path */
	private static final Color DEFAULT_CAMERA_PATH_COLOUR = Color.WHITE;
	
	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;

	/** The 'front' buffer, used to hold vertices ready for drawing */
	private DoubleBuffer drawVertexBuffer;
	
	/** The 'back' buffer, used to update vertices when a change is detected */
	private DoubleBuffer updateVertexBuffer;
	
	/** A lock used to synchronise access to the vertex buffers */
	private Object vertexBufferLock = new Object();
	
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
		
		this.drawVertexBuffer = BufferUtil.newDoubleBuffer(animation.getFrameCount() * 3);
		this.updateVertexBuffer = BufferUtil.newDoubleBuffer(animation.getFrameCount() * 3);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		populateVertexBuffer(dc);
		swapBuffers();
		
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
	 * Swap the two buffers 
	 */
	private synchronized void swapBuffers()
	{
		DoubleBuffer tmp = drawVertexBuffer;
		drawVertexBuffer = updateVertexBuffer;
		updateVertexBuffer = tmp;
		drawVertexBuffer.rewind();
	}

	/**
	 * Populate the update vertex buffer with the coordinates of the 
	 * eye location at each frame in the animation.
	 * <p/>
	 * Vertex buffer has size <code>3*animation.getlastFrame()</code>, with <code>[x,y,z]</code> stored at 
	 * <code>[3*frame, 3*frame+1, 3*frame+2]</code>
	 */
	private synchronized void populateVertexBuffer(DrawContext dc)
	{
		Camera renderCamera = animation.getCamera();
		AnimationContext context = new AnimationContextImpl(animation);
		for (int frame = animation.getFrameOfFirstKeyFrame(); frame < animation.getFrameOfLastKeyFrame(); frame ++)
		{
			Vec4 eyeVector = dc.getGlobe().computePointFromPosition(renderCamera.getEyePositionAtFrame(context, frame));
			updateVertexBuffer.put(eyeVector.x);
			updateVertexBuffer.put(eyeVector.y);
			updateVertexBuffer.put(eyeVector.z);
		}
		updateVertexBuffer.rewind();
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		// TODO Auto-generated method stub
		
	}

}
