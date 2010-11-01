package au.gov.ga.worldwind.animator.layers.camerapath;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.AnimationContextImpl;
import au.gov.ga.worldwind.animator.animation.KeyFrame;
import au.gov.ga.worldwind.animator.util.GeometryUtil;

import com.sun.opengl.util.BufferUtil;

/**
 * A {@link Renderable} that draws a {@link Marker} for each camera key frame
 * <p/>
 * When registered as a {@link SelectListener} on the current world window,
 * allows the user to drag the markers to alter their lat/lon/elevation
 * position.
 * 
 * @see WorldWindow#addSelectListener(SelectListener)
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

	private ReentrantReadWriteLock frontLock = new ReentrantReadWriteLock(false);
	private Object backLock = new Object();

	private MarkerRenderer markerRenderer = new MarkerRenderer();

	private WorldWindow worldWindow;

	private Animation animation;

	private Click lastClick = null;

	private enum Click
	{
		LEFT,
		MIDDLE,
		RIGHT;
	}

	private KeyFrameMarker lastPickedMarker = null;

	public KeyFrameMarkers(WorldWindow wwd, Animation animation)
	{
		this.worldWindow = wwd;
		setAnimation(animation);
		markerRenderer.setKeepSeparated(false);
	}

	public void setAnimation(Animation animation)
	{
		this.animation = animation;
	}

	@Override
	public void render(DrawContext dc)
	{
		drawKeyFrameMarkers(dc);
		if (lastClick == Click.RIGHT && lastPickedMarker != null)
		{
			drawMarkerElevationRay(dc);
		}
	}

	@Override
	public void selected(SelectEvent event)
	{
		if (event == null)
		{
			return;
		}

		KeyFrameMarker pickedMarker = getPickedMarker(event);
		if (pickedMarker == null)
		{
			return;
		}

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
			doDragMarker(pickedMarker, lastClick, event.getPickPoint(),
					((DragSelectEvent) event).getPreviousPickPoint());
		}
		else if (event.isDragEnd())
		{
			doEndDrag(pickedMarker);
		}
	}

	/**
	 * @return The actual picked object, corrected for buffer swaps
	 */
	private KeyFrameMarker getPickedMarker(SelectEvent event)
	{
		try
		{
			frontLock.readLock().lock();

			PickedObject pickedObject = event.getTopPickedObject();
			if (pickedObject == null)
				return null;

			// In case the buffers have been swapped, we need to find the actual picked object so 
			// that highlighting etc. will work correctly
			int index = lookatMarkersFrontBuffer.indexOf(pickedObject.getObject());
			if (index >= 0)
			{
				return (KeyFrameMarker) lookatMarkersFrontBuffer.get(index);
			}
			index = eyeMarkersFrontBuffer.indexOf(pickedObject.getObject());
			if (index >= 0)
			{
				return (KeyFrameMarker) eyeMarkersFrontBuffer.get(index);
			}
			return null;
		}
		finally
		{
			frontLock.readLock().unlock();
		}
	}

	private void doDragMarker(KeyFrameMarker pickedMarker, Click clickType, Point pickPoint,
			Point previousPickPoint)
	{
		lastPickedMarker = pickedMarker;
		pickedMarker.highlight();
		if (clickType == Click.LEFT)
		{
			doDragLatLon(pickedMarker, pickPoint);
		}
		else if (clickType == Click.RIGHT)
		{
			doDragElevation(pickedMarker, pickPoint, previousPickPoint);
		}
	}

	/**
	 * Apply dragging in the lat-lon plane to the selected marker.
	 * <p/>
	 * Uses a ray-intersection test with a sphere at the elevation of the marker
	 * to determine the lat/lon coordinates of the mouse cursor.
	 */
	private void doDragLatLon(KeyFrameMarker pickedMarker, Point pickPoint)
	{
		// Compute the ray from the screen point
		Line ray = worldWindow.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections =
				worldWindow.getModel().getGlobe()
						.intersect(ray, pickedMarker.getPosition().elevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = ray.nearestIntersectionPoint(intersections);
		if (intersection == null)
		{
			return;
		}

		Position newPosition =
				worldWindow.getModel().getGlobe().computePositionFromPoint(intersection);
		pickedMarker.setPosition(newPosition);

	}

	/**
	 * Apply dragging along the elevation axis to the selected marker.
	 * <p/>
	 * Only change in the mouse y-coordinates will affect the elevation change.
	 * <p/>
	 * Performs a ray-plane intersection test using a plane projected from the
	 * screen <code>y = pickPoint.y</code> intersected with the origin-marker
	 * ray.
	 */
	private void doDragElevation(KeyFrameMarker pickedMarker, Point pickPoint,
			Point previousPickPoint)
	{
		// Calculate the plane projected from screen y=pickPoint.y
		Line screenLeftRay =
				worldWindow.getView().computeRayFromScreenPoint(pickPoint.x - 100, pickPoint.y);
		Line screenRightRay =
				worldWindow.getView().computeRayFromScreenPoint(pickPoint.x + 100, pickPoint.y);

		// As the two lines are very close to parallel, use an arbitrary line joining them rather than the two lines to avoid precision problems
		Line joiner =
				Line.fromSegment(screenLeftRay.getPointAt(500), screenRightRay.getPointAt(500));

		Plane screenPlane = GeometryUtil.createPlaneContainingLines(screenLeftRay, joiner);
		if (screenPlane == null)
		{
			return;
		}

		// Calculate the origin-marker ray
		Line markerEarhCentreRay =
				Line.fromSegment(worldWindow.getModel().getGlobe().getCenter(), worldWindow
						.getModel().getGlobe().computePointFromPosition(pickedMarker.getPosition()));

		Vec4 intersection = screenPlane.intersect(markerEarhCentreRay);
		if (intersection == null)
		{
			return;
		}

		// Set the elevation of the marker to the elevation at the point of intersection
		Position intersectionPosition =
				worldWindow.getModel().getGlobe().computePositionFromPoint(intersection);

		Position newMarkerPosition =
				new Position(pickedMarker.getPosition().latitude,
						pickedMarker.getPosition().longitude, intersectionPosition.elevation);

		pickedMarker.setPosition(newMarkerPosition);
	}

	private void doEndDrag(KeyFrameMarker pickedMarker)
	{
		lastPickedMarker = null;
		lastClick = null;
		pickedMarker.applyPositionChangeToAnimation();
		pickedMarker.unhighlight();
		worldWindow.redrawNow();
	}

	public void recalulateKeyFrameMarkers()
	{
		//don't allow multiple threads calling this function at the same time
		synchronized (backLock)
		{
			populateKeyFrameMarkers();
			swapBuffers();
		}
	}

	public void resetKeyFrameMarkers()
	{
		synchronized (backLock)
		{
			try
			{
				frontLock.writeLock().lock();
				eyeMarkersFrontBuffer.clear();
				eyeMarkersBackBuffer.clear();
				lookatMarkersFrontBuffer.clear();
				lookatMarkersBackBuffer.clear();
			}
			finally
			{
				frontLock.writeLock().unlock();
			}
		}
	}

	/**
	 * Draw the camera eye position key frame markers
	 */
	private void drawKeyFrameMarkers(DrawContext dc)
	{
		try
		{
			frontLock.readLock().lock();
			
			// The marker renderer caches marker points - if two calls are used the second buffer may not be drawn correctly
			// To fix, we combine the two buffers and execute a single call
			ArrayList<Marker> combinedBuffer = new ArrayList<Marker>();
			combinedBuffer.addAll(eyeMarkersFrontBuffer);
			combinedBuffer.addAll(lookatMarkersFrontBuffer);
			markerRenderer.render(dc, combinedBuffer);

			drawJoiners(dc);
		}
		finally
		{
			frontLock.readLock().unlock();
		}
	}

	private void drawJoiners(DrawContext dc)
	{
		if (animation.getKeyFrameCount() <= 0 || dc.isPickingMode()
				|| joinersFrontBuffer.limit() <= 0)
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
			gl.glLineStipple(3, (short) 0xAAAA);

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
	 * Draw a ray passing from the earth centre through the
	 * {@link #lastPickedMarker}.
	 * <p/>
	 * Used as a guide as to where the marker will move when adjusting it's
	 * elevation.
	 */
	private void drawMarkerElevationRay(DrawContext dc)
	{
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

			gl.glColor3fv(Color.GREEN.getColorComponents(null), 0);

			Line markerEarhCentreRay =
					Line.fromSegment(
							worldWindow.getModel().getGlobe().getCenter(),
							worldWindow.getModel().getGlobe()
									.computePointFromPosition(lastPickedMarker.getPosition()));
			gl.glBegin(GL.GL_LINE_STRIP);

			Vec4 linePoint = markerEarhCentreRay.getPointAt(0);
			gl.glVertex3d(linePoint.x, linePoint.y, linePoint.z);

			linePoint = markerEarhCentreRay.getPointAt(1000000000);
			gl.glVertex3d(linePoint.x, linePoint.y, linePoint.z);

			gl.glEnd();
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
	 * The joiner buffer is populated with the eye and lookat positions next to
	 * each other for use with the GL_LINES drawing mode.
	 * <code>[eyex, eyey, eyez][lookatx, lookaty, lookatz]</code> are stored at
	 * <code>[6*frame, 6*frame+1, 6*frame+2][6*frame+3, 6*frame+4, 6*frame+5]</code>
	 * 
	 * Don't call unless you hold the backLock lock.
	 */
	private void populateKeyFrameMarkers()
	{
		eyeMarkersBackBuffer.clear();
		lookatMarkersBackBuffer.clear();
		joinersBackBuffer = BufferUtil.newDoubleBuffer(animation.getKeyFrameCount() * 3 * 2);

		AnimationContext context = new AnimationContextImpl(animation);
		Vec4 markerCoords;
		for (KeyFrame keyFrame : animation.getKeyFrames())
		{
			if (isCameraFrame(keyFrame))
			{
				int frame = keyFrame.getFrame();
				KeyFrameMarker marker = new EyeKeyFrameMarker(animation, frame);
				eyeMarkersBackBuffer.add(marker);

				markerCoords =
						context.getView().getGlobe().computePointFromPosition(marker.getPosition());
				joinersBackBuffer.put(markerCoords.x);
				joinersBackBuffer.put(markerCoords.y);
				joinersBackBuffer.put(markerCoords.z);

				marker = new LookatKeyFrameMarker(animation, frame);
				lookatMarkersBackBuffer.add(marker);

				markerCoords =
						context.getView().getGlobe().computePointFromPosition(marker.getPosition());
				joinersBackBuffer.put(markerCoords.x);
				joinersBackBuffer.put(markerCoords.y);
				joinersBackBuffer.put(markerCoords.z);
			}
		}
		joinersBackBuffer.rewind();
	}

	/**
	 * Don't call unless you hold the backLock lock.
	 */
	private void swapBuffers()
	{
		try
		{
			frontLock.writeLock().lock();

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
		finally
		{
			frontLock.writeLock().unlock();
		}
	}

	private boolean isCameraFrame(KeyFrame keyFrame)
	{
		return keyFrame.hasValueForParameter(animation.getCamera().getEyeLat())
				|| keyFrame.hasValueForParameter(animation.getCamera().getEyeLon())
				|| keyFrame.hasValueForParameter(animation.getCamera().getEyeElevation())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtLat())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtLon())
				|| keyFrame.hasValueForParameter(animation.getCamera().getLookAtElevation());
	}

}
