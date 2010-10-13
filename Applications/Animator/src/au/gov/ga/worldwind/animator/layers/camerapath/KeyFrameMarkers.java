package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;

import com.sun.opengl.util.BufferUtil;

/**
 * A {@link Renderable} that draws a {@link Marker} for each camera key frame
 */
class KeyFrameMarkers implements Renderable, SelectListener
{
	private static final Color DEFAULT_EYE_NODE_COLOUR = Color.BLUE;
	private static final Color DEFAULT_LOOKAT_NODE_COLOUR = Color.MAGENTA;
	private static final Color DEFAULT_HIGHLIGHTED_NODE_COLOUR = Color.YELLOW;
	
	// Buffers used to draw the key frame markers
	private List<Marker> eyeMarkersFrontBuffer = new ArrayList<Marker>();
	private List<Marker> eyeMarkersBackBuffer = new ArrayList<Marker>();
	private List<Marker> lookatMarkersFrontBuffer = new ArrayList<Marker>();
	private List<Marker> lookatMarkersBackBuffer = new ArrayList<Marker>();
	private DoubleBuffer joinersFrontBuffer;
	private DoubleBuffer joinersBackBuffer;
	private Object markerBufferLock = new Object();
	
	private MarkerRenderer markerRenderer = new MarkerRenderer();
	private BasicMarkerAttributes eyeMarkerAttributes = new BasicMarkerAttributes();
	private BasicMarkerAttributes lookatMarkerAttributes = new BasicMarkerAttributes();
	private BasicMarkerAttributes highlighedMarkerAttributes = new BasicMarkerAttributes();
	
	private Animation animation;
	
	private boolean dragging;
	
	public KeyFrameMarkers(Animation animation)
	{
		this.animation = animation;
		setupMarkerAttributes();
	}

	@Override
	public void render(DrawContext dc)
	{
		drawKeyFrameMarkers(dc);
	}

	@Override
	public void selected(SelectEvent event)
	{
		if (event == null)
		{
			return;
		}
		
		PickedObject topPickedObject = event.getTopPickedObject();
		if (!isKeyFrameMarker(topPickedObject))
		{
			return;
		}
		
		Marker pickedMarker = (Marker)topPickedObject.getObject();
		
		if (event.getMouseEvent() != null && event.getMouseEvent().getID() == MouseEvent.MOUSE_PRESSED)
		{
			highlight(pickedMarker);
		}
		else if (event.getMouseEvent() != null && event.getMouseEvent().getID() == MouseEvent.MOUSE_RELEASED)
		{
			unhighlight(pickedMarker);
		}
	}

	private boolean isKeyFrameMarker(PickedObject pickedObject)
	{
		return pickedObject != null && 
			   (lookatMarkersFrontBuffer.contains(pickedObject.getObject()) || 
			    eyeMarkersFrontBuffer.contains(pickedObject.getObject()));
	}

	private void highlight(Marker pickedMarker)
	{
		pickedMarker.setAttributes(highlighedMarkerAttributes);
	}
	
	private void unhighlight(Marker pickedMarker)
	{
		if (lookatMarkersFrontBuffer.contains(pickedMarker))
		{
			pickedMarker.setAttributes(lookatMarkerAttributes);
		}
		else
		{
			pickedMarker.setAttributes(eyeMarkerAttributes);
		}
	}

	public void updateAnimation(Animation animation)
	{
		this.animation = animation;
	}
	
	public void recalulateKeyFrameMarkers()
	{
		populateKeyFrameMarkers();
		swapBuffers();
	}
	
	public void resetKeyFrameMarkers()
	{
		eyeMarkersFrontBuffer.clear();
		eyeMarkersBackBuffer.clear();
		lookatMarkersFrontBuffer.clear();
		lookatMarkersBackBuffer.clear();
		joinersFrontBuffer = BufferUtil.newDoubleBuffer(animation.getKeyFrameCount() * 3 * 2);
		joinersBackBuffer = BufferUtil.newDoubleBuffer(animation.getKeyFrameCount() * 3 * 2);
	}
	
	private void setupMarkerAttributes()
	{
		eyeMarkerAttributes.setMaterial(new Material(DEFAULT_EYE_NODE_COLOUR));
		eyeMarkerAttributes.setMaxMarkerSize(2000d);
		
		lookatMarkerAttributes = new BasicMarkerAttributes(eyeMarkerAttributes);
		lookatMarkerAttributes.setMaterial(new Material(DEFAULT_LOOKAT_NODE_COLOUR));
		
		highlighedMarkerAttributes = new BasicMarkerAttributes(eyeMarkerAttributes);
		highlighedMarkerAttributes.setMaterial(new Material(DEFAULT_HIGHLIGHTED_NODE_COLOUR));
	}
	
	/**
	 * Draw the camera eye position key frame markers
	 */
	private void drawKeyFrameMarkers(DrawContext dc)
	{
		synchronized (markerBufferLock)
		{
			markerRenderer.render(dc, eyeMarkersFrontBuffer);
			markerRenderer.render(dc, lookatMarkersFrontBuffer);
			drawJoiners(dc);
		}
	}

	private void drawJoiners(DrawContext dc)
	{
		if (animation.getKeyFrameCount() <= 0 || dc.isPickingMode())
		{
			return;
		}
		
		GL gl = dc.getGL();
		gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
		try
		{
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glShadeModel(GL.GL_SMOOTH);
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
			gl.glLineWidth(2.0f);
			
			gl.glColor3fv(Color.WHITE.getColorComponents(null), 0);
			gl.glVertexPointer(3, GL.GL_DOUBLE, 0, joinersFrontBuffer);
			
			gl.glDrawArrays(GL.GL_LINES, 0, animation.getKeyFrameCount() * 2);
		}
		finally
		{
			gl.glPopAttrib();
			gl.glPopClientAttrib();
		}
	}
	
	/**
	 * Populate the back buffers with key frame marker data.
	 * <p/>
	 * The joiner buffer is populated with the eye and lookat positions next to each other for use with the GL_LINES
	 * drawing mode. <code>[eyex, eyey, eyez][lookatx, lookaty, lookatz]</code> are stored at 
	 * <code>[6*frame, 6*frame+1, 6*frame+2][6*frame+3, 6*frame+4, 6*frame+5]</code>
	 */
	private void populateKeyFrameMarkers()
	{
		eyeMarkersBackBuffer.clear();
		AnimationContext context = new AnimationContextImpl(animation);
		Vec4 markerCoords;
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			if (isCameraFrame(keyFrame))
			{
				Marker marker = new BasicMarker(animation.getCamera().getEyePositionAtFrame(context, keyFrame.getFrame()), eyeMarkerAttributes);
				eyeMarkersBackBuffer.add(marker);
				
				markerCoords = context.getView().getGlobe().computePointFromPosition(marker.getPosition());
				joinersBackBuffer.put(markerCoords.x);
				joinersBackBuffer.put(markerCoords.y);
				joinersBackBuffer.put(markerCoords.z);
				
				marker = new BasicMarker(animation.getCamera().getLookatPositionAtFrame(context, keyFrame.getFrame()), lookatMarkerAttributes);
				lookatMarkersBackBuffer.add(marker);
				
				markerCoords = context.getView().getGlobe().computePointFromPosition(marker.getPosition());
				joinersBackBuffer.put(markerCoords.x);
				joinersBackBuffer.put(markerCoords.y);
				joinersBackBuffer.put(markerCoords.z);
			}
		}
		joinersBackBuffer.rewind();
	}
	
	private void swapBuffers()
	{
		synchronized (markerBufferLock)
		{
			List<Marker> tmp = eyeMarkersFrontBuffer;
			eyeMarkersFrontBuffer = eyeMarkersBackBuffer;
			eyeMarkersBackBuffer = tmp;
			
			tmp = lookatMarkersFrontBuffer;
			lookatMarkersFrontBuffer = lookatMarkersBackBuffer;
			lookatMarkersBackBuffer = tmp;
			
			DoubleBuffer tmpBuffer = joinersFrontBuffer;
			joinersFrontBuffer = joinersBackBuffer;
			joinersBackBuffer = tmpBuffer;
		}
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

}
