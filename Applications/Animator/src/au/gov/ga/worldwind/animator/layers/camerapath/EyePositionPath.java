package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.animation.camera.Camera;

import com.sun.opengl.util.BufferUtil;

/**
 * A {@link Renderable} that draws the current animation's eye position along with nodes representing key frames.
 */
class EyePositionPath implements Renderable
{
	private static final Color DEFAULT_CAMERA_PATH_COLOUR = Color.WHITE;
	private static final Color DEFAULT_KEYFRAME_NODE_COLOUR = Color.BLUE;
	
	/** The number of frames currently held in the vertex buffers */
	private int frameCount;
	
	/** The 'front' buffer, used to hold vertices ready for drawing */
	private DoubleBuffer drawPathBuffer;
	
	/** The 'back' buffer, used to update vertices when a change is detected */
	private DoubleBuffer updatePathBuffer;
	
	/** A lock used to synchronise access to the path buffers */
	private Object pathBufferLock = new Object();

	/** The centroid used as a reference for the path vertices */
	private Vec4 pathReferenceCenter;
	
	/** A 'front' buffer for key frame markers */
	private List<Marker> drawKeyFrameMarkers = new ArrayList<Marker>();
	
	/** A 'back' buffer for key frame markers */
	private List<Marker> updateKeyFrameMarkers = new ArrayList<Marker>();
	
	/** A lock used to synchronise access to the key frame buffers */
	private Object keyFrameBufferLock = new Object();
	
	private MarkerRenderer markerRenderer = new MarkerRenderer();
	private MarkerAttributes keyFrameMarkerAttributes = new BasicMarkerAttributes();
	
	/** The colour to use to draw the camera path */
	private Color cameraPathColour = DEFAULT_CAMERA_PATH_COLOUR;
	private Color keyFrameNodeColour = DEFAULT_KEYFRAME_NODE_COLOUR;
	
	/** The animation whose camera path is to be displayed on this layer */
	private Animation animation;
	
	public EyePositionPath(Animation animation)
	{
		this.animation = animation;
		
		setupMarkerAttributes();
	}

	public void updateAnimation(Animation animation)
	{
		this.animation = animation;
	}
	
	public void recalulatePath()
	{
		populatePathBuffers();
		populateKeyFrameMarkers();
		swapBuffers();
	}
	
	public void resetPath()
	{
		frameCount = animation.getFrameCount();
		this.drawPathBuffer = BufferUtil.newDoubleBuffer(frameCount * 3);
		this.updatePathBuffer = BufferUtil.newDoubleBuffer(frameCount * 3);
	}
	
	@Override
	public void render(DrawContext dc)
	{
		drawKeyFrameNodes(dc);
		drawPath(dc);
	}

	/**
	 * Draw the camera path
	 */
	private void drawPath(DrawContext dc)
	{
		GL gl = dc.getGL();
		gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
		
		boolean popRefCenter = false;
		if (pathReferenceCenter != null)
		{
			dc.getView().pushReferenceCenter(dc, pathReferenceCenter);
			popRefCenter = true;
		}
		try
		{
			// Points are drawn over the line to prevent gaps forming when 
			// antialiasing and smoothing is applied to the line
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glShadeModel(GL.GL_SMOOTH);
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glEnable(GL.GL_POINT_SMOOTH);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
			gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
			gl.glLineWidth(2.0f);
			gl.glPointSize(2.0f);
			synchronized (pathBufferLock)
			{
				gl.glVertexPointer(3, GL.GL_DOUBLE, 0, drawPathBuffer);

				// Draw a smooth line without depth testing, filling gaps with points
				gl.glDepthMask(false);
				gl.glColor3fv(cameraPathColour.getColorComponents(null), 0);
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, animation.getFrameOfLastKeyFrame());
				gl.glDrawArrays(GL.GL_POINTS, 0, animation.getFrameOfLastKeyFrame());
				gl.glDepthMask(true);
				
				// Now redraw the line with depth testing to ensure line looks correct with markers
				gl.glDrawArrays(GL.GL_LINE_STRIP, 0, animation.getFrameOfLastKeyFrame());
			}
		}
		finally
		{
			if (popRefCenter)
			{
				dc.getView().popReferenceCenter(dc);
			}
			gl.glPopAttrib();
			gl.glPopClientAttrib();
		}
	}
	
	private void setupMarkerAttributes()
	{
		keyFrameMarkerAttributes.setMaterial(new Material(keyFrameNodeColour));
		keyFrameMarkerAttributes.setMaxMarkerSize(200d);
	}
	
	/**
	 * Draw the camera eye position key frame nodes
	 */
	private void drawKeyFrameNodes(DrawContext dc)
	{
		synchronized (keyFrameBufferLock)
		{
			GL gl = dc.getGL();
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			try
			{
				gl.glShadeModel(GL.GL_SMOOTH);
				gl.glColor3fv(keyFrameNodeColour.getColorComponents(null), 0);
				markerRenderer.render(dc, drawKeyFrameMarkers);
			}
			finally
			{
				gl.glPopAttrib();
			}
			
		}
	}
	
	/**
	 * Populate the update vertex buffer with the coordinates of the 
	 * eye location at each frame in the animation.
	 * <p/>
	 * Vertex buffer has size <code>3*animation.getlastFrame()</code>, with <code>[x,y,z]</code> stored at 
	 * <code>[3*frame, 3*frame+1, 3*frame+2]</code>
	 */
	private void populatePathBuffers()
	{
		Camera renderCamera = animation.getCamera();
		AnimationContext context = new AnimationContextImpl(animation);
		pathReferenceCenter = null;
		for (int frame = animation.getFrameOfFirstKeyFrame(); frame < animation.getFrameOfLastKeyFrame(); frame ++)
		{
			Vec4 eyeVector = context.getView().getGlobe().computePointFromPosition(renderCamera.getEyePositionAtFrame(context, frame));
			if (pathReferenceCenter == null)
			{
				pathReferenceCenter  = eyeVector; // Choose the first point in the path to be the reference point
			}
			updatePathBuffer.put(eyeVector.x - pathReferenceCenter.x);
			updatePathBuffer.put(eyeVector.y - pathReferenceCenter.y);
			updatePathBuffer.put(eyeVector.z - pathReferenceCenter.z);
		}
		updatePathBuffer.rewind();
	}
	
	private void populateKeyFrameMarkers()
	{
		updateKeyFrameMarkers.clear();
		AnimationContext context = new AnimationContextImpl(animation);
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			if (isEyePositionFrame(keyFrame))
			{
				Marker marker = new BasicMarker(animation.getCamera().getEyePositionAtFrame(context, keyFrame.getFrame()), keyFrameMarkerAttributes);
				updateKeyFrameMarkers.add(marker);
			}
		}
	}
	
	/**
	 * Swap the two buffers 
	 */
	private void swapBuffers()
	{
		synchronized (pathBufferLock)
		{
			DoubleBuffer tmp = drawPathBuffer;
			drawPathBuffer = updatePathBuffer;
			updatePathBuffer = tmp;
			drawPathBuffer.rewind();
		}
		
		synchronized (keyFrameBufferLock)
		{
			List<Marker> tmp = drawKeyFrameMarkers;
			drawKeyFrameMarkers = updateKeyFrameMarkers;
			updateKeyFrameMarkers = tmp;
		}
	}
	
	private boolean isEyePositionFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(animation.getCamera().getEyeLat()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeLon()) || 
				keyFrame.hasValueForParameter(animation.getCamera().getEyeElevation());
	}

}
