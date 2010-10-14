package au.gov.ga.worldwind.animator.layers.camerapath;

import static au.gov.ga.worldwind.animator.util.GeometryUtil.nearestIntersectionPoint;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.awt.Color;
import java.awt.Point;
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
	// Buffers used to draw the key frame markers
	private List<Marker> eyeMarkersFrontBuffer = new ArrayList<Marker>();
	private List<Marker> eyeMarkersBackBuffer = new ArrayList<Marker>();
	private List<Marker> lookatMarkersFrontBuffer = new ArrayList<Marker>();
	private List<Marker> lookatMarkersBackBuffer = new ArrayList<Marker>();
	private DoubleBuffer joinersFrontBuffer;
	private DoubleBuffer joinersBackBuffer;
	private Object markerBufferLock = new Object();
	
	private MarkerRenderer markerRenderer = new MarkerRenderer();
	private WorldWindow worldWindow;
	
	private Animation animation;
	
	private Click lastClick = null;
	private enum Click
	{
		LEFT, MIDDLE, RIGHT;
	}
	
	public KeyFrameMarkers(WorldWindow wwd, Animation animation)
	{
		this.worldWindow = wwd;
		this.animation = animation;
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
		
		KeyFrameMarker pickedMarker = (KeyFrameMarker)topPickedObject.getObject();
		if (event.isLeftPress())
		{
			lastClick = Click.LEFT;
		}
		else if (event.isRightPress())
		{
			lastClick = Click.RIGHT;
		}
		else if (event.isDrag())
		{
			doDragMarker(pickedMarker, lastClick, event.getPickPoint(), ((DragSelectEvent)event).getPreviousPickPoint());
		} 
		else if (event.isDragEnd())
		{
			doEndDrag(pickedMarker);
		}
	}

	private boolean isKeyFrameMarker(PickedObject pickedObject)
	{
		return pickedObject != null && 
			   (lookatMarkersFrontBuffer.contains(pickedObject.getObject()) || 
			    eyeMarkersFrontBuffer.contains(pickedObject.getObject()));
	}

	private void doDragMarker(KeyFrameMarker pickedMarker, Click clickType, Point pickPoint, Point previousPickPoint)
	{
		pickedMarker.highlight();
		if (clickType == Click.LEFT)
		{
			doDragLatLon(pickedMarker, pickPoint);
		}
	}
	
	private void doDragLatLon(KeyFrameMarker pickedMarker, Point pickPoint)
	{
		// Compute the ray from the screen point
		Line ray = worldWindow.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections = worldWindow.getModel().getGlobe().intersect(ray, pickedMarker.getPosition().elevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = nearestIntersectionPoint(ray, intersections);
		if (intersection == null)
		{
			return;
		}
		
		Position newPosition = worldWindow.getModel().getGlobe().computePositionFromPoint(intersection);
		pickedMarker.setPosition(newPosition);
		
	}

	private void doEndDrag(KeyFrameMarker pickedMarker)
	{
		lastClick = null;
		pickedMarker.applyPositionChangeToAnimation();
		pickedMarker.unhighlight();
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
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineWidth(2.0f);
			gl.glLineStipple(3, (short)0xAAAA);
			
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
		lookatMarkersBackBuffer.clear();
		AnimationContext context = new AnimationContextImpl(animation);
		Vec4 markerCoords;
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			if (isCameraFrame(keyFrame))
			{
				int frame = keyFrame.getFrame();
				KeyFrameMarker marker = new EyeKeyFrameMarker(animation, frame);
				eyeMarkersBackBuffer.add(marker);
				
				markerCoords = context.getView().getGlobe().computePointFromPosition(marker.getPosition());
				joinersBackBuffer.put(markerCoords.x);
				joinersBackBuffer.put(markerCoords.y);
				joinersBackBuffer.put(markerCoords.z);
				
				marker = new LookatKeyFrameMarker(animation, frame);
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
