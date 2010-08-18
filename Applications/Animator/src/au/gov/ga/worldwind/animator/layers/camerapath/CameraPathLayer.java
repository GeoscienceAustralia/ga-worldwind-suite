package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import au.gov.ga.worldwind.animator.animation.OldAnimation;
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
	private OldAnimation animation;

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
	public CameraPathLayer(OldAnimation animation)
	{
		Validate.notNull(animation, "An animation instance is required");
		this.animation = animation;
		this.animation.addChangeListener(this);
		
		this.drawVertexBuffer = DoubleBuffer.allocate(animation.getFrameCount() * 3);
		this.updateVertexBuffer = DoubleBuffer.allocate(animation.getFrameCount() * 3);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glPushAttrib(GL.GL_LINE_BIT);
		gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			gl.glColor3fv(cameraPathColour.getColorComponents(null), 0);
			synchronized (vertexBufferLock)
			{
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, drawVertexBuffer.rewind());
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, animation.getLastFrame());
			}
		}
		finally
		{
			gl.glPopAttrib();
			gl.glPopClientAttrib();
		}
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
		for (int frame = 0; frame < animation.getLastFrame(); frame ++)
		{
			Vec4 eyeVector = dc.getGlobe().computePointFromPosition(animation.getEyeLocationAtFrame(frame));
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
