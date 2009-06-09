/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.awt;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.KeyEventState;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitViewLimits;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.ScheduledOrbitViewStateIterator;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * @author dcollins
 * @version $Id: ViewInputHandler.java 9881 2009-04-02 16:43:28Z dcollins $
 */
public class ViewInputHandler
    implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener, RenderingListener,
    java.beans.PropertyChangeListener
{
    private WorldWindow wwd;
    private ViewInputAttributes attributes;
    // Optional behaviors.
    private boolean enableSmoothing;
    private boolean lockHeading;
    private boolean stopOnFocusLost;
    private Position selectedPosition;
    // AWT event support.
    private boolean wwdFocusOwner;
    private Point lastMousePoint;
    private Point mousePoint;
    private KeyEventState keyEventState = new KeyEventState();
    private OrbitViewInputSupport orbitViewInputSupport = new OrbitViewInputSupport();
    // Input transformation coefficients.
    private double dragSlopeFactor = DEFAULT_DRAG_SLOPE_FACTOR;
    // Per-frame input event timing support.
    private long lastPerFrameInputTime;

    private static final double DEFAULT_DRAG_SLOPE_FACTOR = 0.002;
    private static final long DEFAULT_PER_FRAME_INPUT_DELAY = 35L;
    
    // These constants are used by the device input handling routines to determine whether or not to
    // (1) generate view change events based on the current device state, or
    // (2) query whether or not events would be generated from the current device state.
    protected static final String GENERATE_EVENTS = "GenerateEvents";
    protected static final String QUERY_EVENTS = "QueryEvents";

    // These constants define scaling functions for transforming raw input into a range of values. The scale functions
    // are interpreted as follows:
    // EYE_ALTITUDE: distance from eye to ground, divided by 3 * globe's radius and clamped to range [0, 1]
    // ZOOM: distance from eye to view center point, divided by 3 * globe's radius and clamped to range [0, 1]
    // EYE_ALTITUDE_EXP or ZOOM_EXP: function placed in an exponential function in the range [0, 1]
    protected static final String SCALE_FUNC_EYE_ALTITUDE = "ScaleFuncEyeAltitude";
    protected static final String SCALE_FUNC_EYE_ALTITUDE_EXP = "ScaleFuncEyeAltitudeExp";
    protected static final String SCALE_FUNC_ZOOM = "ScaleFuncZoom";
    protected static final String SCALE_FUNC_ZOOM_EXP = "ScaleFuncZoomExp";

    public ViewInputHandler()
    {
        this.enableSmoothing = true;
        this.lockHeading = true;
        this.stopOnFocusLost = true;
        this.attributes = new ViewInputAttributes();
    }

    /**
     * Return the <code>WorldWindow</code> this ViewInputHandler is listening to for input events, and will modify in
     * response to those events
     *
     * @return the <code>WorldWindow</code> this ViewInputHandler is listening to, and will modify in response to
     * events.
     */
    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    /**
     * Sets the <code>WorldWindow</code> this ViewInputHandler should listen to for input events, and should modify in
     * response to those events. If the parameter <code>newWorldWindow</code> is null, then this ViewInputHandler
     * will do nothing.
     *
     * @param newWorldWindow the <code>WorldWindow</code> to listen on, and modify in response to events.
     */
    public void setWorldWindow(WorldWindow newWorldWindow)
    {
        if (newWorldWindow == this.wwd)
            return;

        if (this.wwd != null)
        {
            this.wwd.removeRenderingListener(this);
            this.wwd.getSceneController().removePropertyChangeListener(this);
        }

        this.wwd = newWorldWindow;

        if (this.wwd != null)
        {
            this.wwd.addRenderingListener(this);
            this.wwd.getSceneController().addPropertyChangeListener(this);
        }
    }

    /**
     * Returns the values that are used to transform raw input events into view movments.
     *
     * @return values that are be used to transform raw input into view movement.
     */
    public ViewInputAttributes getAttributes()
    {
        return this.attributes;
    }

    /**
     * Sets the values that will be used to transform raw input events into view movements. ViewInputAttributes
     * define a calibration value for each combination of device and action, and a general sensitivity value
     * for each device.
     *
     * @param attributes values that will be used to transform raw input into view movement.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     *
     * @see @ViewInputAttributes
     */
    public void setAttributes(ViewInputAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attributes = attributes;
    }

    /**
     * Returns whether the ViewInputHandler will smooth view movements in response to input events.
     *
     * @return true if the view will movements are smoothed; false otherwise.
     */
    public boolean isEnableSmoothing()
    {
        return this.enableSmoothing;
    }

    /**
     * Sets whether the ViewInputHandler should smooth view movements in response to input events. A value of true
     * will cause the ViewInputHandler to delegate decisions about whether to smooth a certain input event to its
     * {@link ViewInputAttributes}. A value of false will disable all smoothing.
     *
     * @param enable true to smooth view movements; false otherwise.
     */
    public void setEnableSmoothing(boolean enable)
    {
        this.enableSmoothing = enable;
    }

    /**
     * Returns whether the view's heading should stay the same unless explicitly changed.
     *
     * @return true if the view's heading will stay the same unless explicity changed; false otherwise.
     */
    public boolean isLockHeading()
    {
        return this.lockHeading;
    }

    /**
     * Sets whether the view's heading should stay the same unless explicitly changed. For example, moving forward
     * along a great arc would suggest a change in position and heading. If the heading had been locked, the
     * ViewInputHandler will move forward in a way that doesn't change the heading.
     *
     * @param lock true if the view's heading should stay the same unless explicity changed; false otherwise.
     */
    public void setLockHeading(boolean lock)
    {
        this.lockHeading = lock;
    }

    /**
     * Returns whether the view will stop when the WorldWindow looses focus.
     *
     * @return true if the view will stop when the WorldWindow looses focus; false otherwise.
     */
    public boolean isStopOnFocusLost()
    {
        return this.stopOnFocusLost;
    }

    /**
     * Sets whether the view should stop when the WorldWindow looses focus.
     *
     * @param stop true if the view should stop when the WorldWindow looses focus; false otherwise.
     */
    public void setStopOnFocusLost(boolean stop)
    {
        this.stopOnFocusLost = stop;
    }

    /**
     * Returns the <code>factor</code> that dampens view movement when the user pans drags the cursor in a way that could
     * cause an abrupt transition.
     *
     * @return factor dampening view movement when a mouse drag event would cause an abrupt transition.
     * @see #setDragSlopeFactor
     */
    public double getDragSlopeFactor()
    {
        return this.dragSlopeFactor;
    }

    /**
     * Sets the <code>factor</code> that dampens view movement when a mouse drag event would cause an abrupt
     * transition. The drag slope is the ratio of screen pixels to Cartesian distance moved, measured by the previous
     * and current mouse points. As drag slope gets larger, it becomes more difficult to operate the view. This
     * typically happens while dragging over and around the horizon, where movement of a few pixels can cause the view
     * to move many kilometers. This <code>factor</code> is the amount of damping applied to the view movement in such
     * cases. Setting <code>factor</code> to zero will disable this behavior, while setting <code>factor</code> to a
     * positive value may dampen the effects of mouse dragging.
     *
     * @param factor dampening view movement when a mouse drag event would cause an abrupt transition. Must be greater
     * than or equal to zero.
     *
     * @throws IllegalArgumentException if <code>factor</code> is less than zero.
     */
    public void setDragSlopeFactor(double factor)
    {
        if (factor < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "factor < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.dragSlopeFactor = factor;
    }

    protected View getView()
    {
        return (this.wwd != null) ? this.wwd.getView() : null;
    }

    //**************************************************************//
    //********************  AWT Event Support  *********************//
    //**************************************************************//

    protected boolean isWorldWindowFocusOwner()
    {
        return this.wwdFocusOwner;
    }

    protected void setWorldWindowFocusOwner(boolean focusOwner)
    {
        this.wwdFocusOwner = focusOwner;
    }

    protected Point getMousePoint()
    {
        return this.mousePoint;
    }

    protected Point getLastMousePoint()
    {
        return this.lastMousePoint;
    }

    protected void updateMousePoint(MouseEvent e)
    {
        this.lastMousePoint = this.mousePoint;
        this.mousePoint = new Point(e.getPoint());
    }

    protected Position getSelectedPosition()
    {
        return this.selectedPosition;
    }

    protected void setSelectedPosition(Position position)
    {
        this.selectedPosition = position;
    }

    protected Position computeSelectedPosition()
    {
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null)
        {
            PickedObject top =  pickedObjects.getTopPickedObject();
            if (top != null && top.isTerrain())
            {
                return top.getPosition();
            }
        }
        return null;
    }

    //**************************************************************//
    //********************  View Change Events  ********************//
    //**************************************************************//

    protected void onFocusView(Position focalPosition, ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            if (view.hasStateIterator())
                view.stopStateIterators();

            // We're treating a speed parameter as smoothing here. A greater speed results in greater smoothing and
            // slower response. Therefore the min speed used at lower altitudes ought to be *greater* than the max
            // speed used at higher altitudes.
            double[] values = actionAttribs.getValues();
            double smoothing = this.getScaledValue(values[0], values[1], SCALE_FUNC_ZOOM);
            if (!actionAttribs.isEnableSmoothing())
                smoothing = 0.0;

            this.orbitViewInputSupport.setCenterTarget((OrbitView) view, null);
            this.orbitViewInputSupport.setCenterSmoothing(smoothing);
            this.orbitViewInputSupport.setCenterTarget((OrbitView) view, focalPosition);
            view.firePropertyChange(AVKey.VIEW, null, view);
        }
    }

    protected void onPanViewAbsolute(Angle latitudeChange, Angle longitudeChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (latitudeChange.equals(Angle.ZERO) && longitudeChange.equals(Angle.ZERO))
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            Position newPosition = ((OrbitView) view).getCenterPosition().add(new Position(
                latitudeChange, longitudeChange, 0.0));
            this.setCenterPosition((OrbitView) view, newPosition, actionAttribs);
        }
    }

    protected void onPanViewRelative(Angle forwardChange, Angle sideChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (forwardChange.equals(Angle.ZERO) && sideChange.equals(Angle.ZERO))
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            double sinHeading = ((OrbitView) view).getHeading().sin();
            double cosHeading = ((OrbitView) view).getHeading().cos();
            double latChange = cosHeading * forwardChange.getDegrees() - sinHeading * sideChange.getDegrees();
            double lonChange = sinHeading * forwardChange.getDegrees() + cosHeading * sideChange.getDegrees();
            Position newPosition = ((OrbitView) view).getCenterPosition().add(
                Position.fromDegrees(latChange, lonChange, 0.0));
            this.setCenterPosition((OrbitView) view, newPosition, actionAttribs);
        }
    }

    protected void onResetHeading()
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            Angle newHeading = BasicOrbitViewLimits.limitHeading(Angle.ZERO, ((OrbitView) view).getOrbitViewLimits());
            view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingIterator(
                ((OrbitView) view).getHeading(), newHeading));
        }
    }

    protected void onResetHeadingAndPitch()
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            Angle newHeading = BasicOrbitViewLimits.limitHeading(Angle.ZERO, ((OrbitView) view).getOrbitViewLimits());
            Angle newPitch = BasicOrbitViewLimits.limitPitch(Angle.ZERO, ((OrbitView) view).getOrbitViewLimits());
            view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingPitchIterator(
                ((OrbitView) view).getHeading(), newHeading, ((OrbitView) view).getPitch(), newPitch));
        }
    }

    protected void onRotateView(Angle headingChange, Angle pitchChange,
        ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            if (!headingChange.equals(Angle.ZERO))
                this.changeHeading((OrbitView) view, headingChange, actionAttribs);

            if (!pitchChange.equals(Angle.ZERO))
                this.changePitch((OrbitView) view, pitchChange, actionAttribs);
        }
    }

    protected void onStopView()
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        view.stopMovement();
    }

    protected void onZoomView(double zoomChange, ViewInputAttributes.ActionAttributes actionAttribs)
    {
        View view = this.getView();
        if (view == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (zoomChange == 0)
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            this.changeZoom((OrbitView) view, zoomChange, actionAttribs);
        }
    }

    //**************************************************************//
    //********************  Key Events  ****************************//
    //**************************************************************//

    public void keyTyped(KeyEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.keyEventState.keyTyped(e);
    }

    public void keyPressed(KeyEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.keyEventState.keyPressed(e);
        this.handleKeyPressed(e);
    }

    public void keyReleased(KeyEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.keyEventState.keyReleased(e);
    }

    protected void handleKeyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_N)
        {
            this.onResetHeading();
        }
        else if (e.getKeyCode() == KeyEvent.VK_R)
        {
            this.onResetHeadingAndPitch();
        }
        else if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            this.onStopView();
        }
        else
        {
            // Determine whether or not the current key state would have generated a view change event.
            // If so, issue a repaint event to give the per-frame input a chance to run.
            if (this.handlePerFrameKeyState(this.keyEventState, QUERY_EVENTS))
            {
                View view = this.getView();
                if (view != null)
                {
                    view.firePropertyChange(AVKey.VIEW, null, view);
                }
            }
        }
    }

    //**************************************************************//
    //********************  Mouse Events  **************************//
    //**************************************************************//

    public void mouseClicked(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.handleMouseClicked(e);
    }

    public void mousePressed(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.updateMousePoint(e);
        this.setSelectedPosition(this.computeSelectedPosition());
    }

    public void mouseReleased(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.updateMousePoint(e);
        this.setSelectedPosition(null);
    }

    public void mouseEntered(MouseEvent e)
    {
       if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    public void mouseExited(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    protected void handleMouseClicked(MouseEvent e)
    {
        if (MouseEvent.BUTTON1 == e.getButton())
        {
            this.handleMouseFocus(e);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleMouseFocus(MouseEvent e)
    {
        Position pos = this.computeSelectedPosition();
        if (pos == null)
            return;

        ViewInputAttributes.ActionAttributes actionAttributes = this.attributes.getActionAttributes(
            ViewInputAttributes.DEVICE_MOUSE, ViewInputAttributes.VIEW_FOCUS);

        this.onFocusView(pos, actionAttributes);
    }

    //**************************************************************//
    //********************  Mouse Motion Events  *******************//
    //**************************************************************//

    public void mouseDragged(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.updateMousePoint(e);
        this.handleMouseDragged(e);
    }

    public void mouseMoved(MouseEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }

        this.updateMousePoint(e);
    }
    
    protected void handleMouseDragged(MouseEvent e)
    {
        // If the rotate modifier is down, or if the rotate button is down, then invoke rotate commands.
        if ((e.getModifiersEx() & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.META_DOWN_MASK)) != 0
            || (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0)
        {
            if ((e.getModifiersEx() & (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) != 0)
            {
                this.handleMouseRotate(e);
            }
        }
        // Otherwise, if the zoom button is down, then invoke zoom commands.
        else if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0)
        {
            this.handleMouseZoom(e);
        }
        // Otherwise, if the pan button is down, then invoke pan commands.
        else if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
        {
            this.handleMousePan(e);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleMousePan(MouseEvent e)
    {
        Point point = constrainToSourceBounds(this.getMousePoint(), this.wwd);
        Point lastPoint = constrainToSourceBounds(this.getLastMousePoint(), this.wwd);
        if (point == null || lastPoint == null)
        {
            return;
        }

        if (this.getSelectedPosition() == null)
        {
            // Compute the current selected position if none exists. This happens if the user starts dragging when
            // the cursor is off the globe, then drags the cursor onto the globe.
            this.setSelectedPosition(this.computeSelectedPosition());
        }
        else if (this.computeSelectedPosition() == null)
        {
            // User dragged the cursor off the globe. Clear the selected position to ensure a new one will be
            // computed if the user drags the cursor back to the globe.
            this.setSelectedPosition(null);
        }
        else if (this.computeSelectedPointAt(point) == null || this.computeSelectedPointAt(lastPoint) == null)
        {
            // User selected a position that is won't work for dragging. Probably the selected elevation is above the
            // eye elevation, in which case dragging becomes unpredictable. Clear the selected position to ensure
            // a new one will be computed if the user drags the cursor to a valid position.
            this.setSelectedPosition(null);
        }

        Vec4 vec = this.computeSelectedPointAt(point);
        Vec4 lastVec = this.computeSelectedPointAt(lastPoint);

        // Cursor is on the globe, pan between the two positions.
        if (vec != null && lastVec != null)
        {
            ViewInputAttributes.ActionAttributes actionAttributes =
                this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_MOUSE,
                    ViewInputAttributes.VIEW_PAN);

            // Compute the change in view location given two screen points and corresponding world vectors.
            LatLon latlon = this.getChangeInLocation(lastPoint, point, lastVec, vec);
            this.onPanViewAbsolute(latlon.getLatitude(), latlon.getLongitude(),  actionAttributes);
        }
        // Cursor is off the globe, we potentially want to simulate globe dragging.
        else
        {
            Point movement = subtract(point, lastPoint);
            int forwardInput = movement.y;
            int sideInput = -movement.x;

            ViewInputAttributes.DeviceAttributes deviceAttributes =
                this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_MOUSE);
            ViewInputAttributes.ActionAttributes actionAttributes =
                this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_MOUSE,
                    ViewInputAttributes.VIEW_PAN);

            Angle forwardChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(forwardInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));
            Angle sideChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(sideInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));

            this.onPanViewRelative(forwardChange, sideChange, actionAttributes);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleMouseRotate(MouseEvent e)
    {
        Point point = constrainToSourceBounds(this.getMousePoint(), this.wwd);
        Point lastPoint = constrainToSourceBounds(this.getLastMousePoint(), this.wwd);
        if (point == null || lastPoint == null)
        {
            return;
        }

        Point movement = subtract(point, lastPoint);
        int headingInput = movement.x;
        int pitchInput = movement.y;

        // Switch the direction of heading change depending on whether the cursor is above or below
        // the center of the screen.
        if (this.wwd instanceof Component)
        {
            if (this.getMousePoint().y < ((Component) this.wwd).getHeight() / 2)
            {
                headingInput = -headingInput;
            }
        }

        ViewInputAttributes.DeviceAttributes deviceAttributes =
            this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_MOUSE);
        ViewInputAttributes.ActionAttributes actionAttributes =
            this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_MOUSE,
                ViewInputAttributes.VIEW_ROTATE);

        Angle headingChange = Angle.fromDegrees(
            this.rawInputToChangeInValue(headingInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));
        Angle pitchChange = Angle.fromDegrees(
            this.rawInputToChangeInValue(pitchInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));

        this.onRotateView(headingChange, pitchChange, actionAttributes);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleMouseZoom(MouseEvent e)
    {
        Point point = constrainToSourceBounds(this.getMousePoint(), this.wwd);
        Point lastPoint = constrainToSourceBounds(this.getLastMousePoint(), this.wwd);
        if (point == null || lastPoint == null)
        {
            return;
        }

        Point movement = subtract(point, lastPoint);
        int zoomInput = movement.y;

        ViewInputAttributes.DeviceAttributes deviceAttributes =
            this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_MOUSE);
        ViewInputAttributes.ActionAttributes actionAttributes =
            this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_MOUSE,
                ViewInputAttributes.VIEW_ZOOM);

        double zoomChange = this.rawInputToChangeInValue(zoomInput, deviceAttributes, actionAttributes,
            SCALE_FUNC_ZOOM);

        this.onZoomView(zoomChange, actionAttributes);
    }

    protected Vec4 computeSelectedPointAt(Point point)
    {
        if (this.getSelectedPosition() == null)
        {
            return null;
        }
        
        View view = this.getView();
        if (view == null)
        {
            return null;
        }

        // Reject a selected position if its elevation is above the eye elevation. When that happens, the user is
        // essentially dragging along the inside of a sphere, and the effects of dragging are reversed. To the user
        // this behavior appears unpredictable.
        double elevation = this.getSelectedPosition().getElevation();
        if (view.getEyePosition().getElevation() <= elevation)
        {
            return null;
        }

        // Intersect with a somewhat larger or smaller Globe which will pass through the selected point, but has the
        // same proportions as the actual Globe. This will simulate dragging the selected position more accurately.
        Line ray = view.computeRayFromScreenPoint(point.getX(), point.getY());
        Intersection[] intersections = this.wwd.getModel().getGlobe().intersect(ray, elevation);
        if (intersections == null || intersections.length == 0)
        {
            return null;
        }

        return this.nearestIntersectionPoint(ray, intersections);
    }

    protected Vec4 nearestIntersectionPoint(Line line, Intersection[] intersections)
    {
        Vec4 intersectionPoint = null;

        // Find the nearest intersection that's in front of the ray origin.
        double nearestDistance = Double.MAX_VALUE;
        for (Intersection intersection : intersections)
        {
            // Ignore any intersections behind the line origin.
            if (!this.isPointBehindLineOrigin(line, intersection.getIntersectionPoint()))
            {
                double d = intersection.getIntersectionPoint().distanceTo3(line.getOrigin());
                if (d < nearestDistance)
                {
                    intersectionPoint = intersection.getIntersectionPoint();
                    nearestDistance = d;
                }
            }
        }

        return intersectionPoint;
    }

    public boolean isPointBehindLineOrigin(Line line, Vec4 point)
    {
        double dot = point.subtract3(line.getOrigin()).dot3(line.getDirection());
        return dot < 0.0;
    }

    protected LatLon getChangeInLocation(Point point1, Point point2, Vec4 vec1, Vec4 vec2)
    {
        // Modify the distance we'll actually travel based on the slope of world distance travelled to screen
        // distance travelled . A large slope means the user made a small change in screen space which resulted
        // in a large change in world space. We want to reduce the impact of that change to something reasonable.

        double dragSlope = this.computeDragSlope(point1, point2, vec1, vec2);
        double dragSlopeFactor = this.getDragSlopeFactor();
        double scale = 1.0 / (1.0 + dragSlopeFactor * dragSlope * dragSlope);

        Position pos1 = this.wwd.getModel().getGlobe().computePositionFromPoint(vec1);
        Position pos2 = this.wwd.getModel().getGlobe().computePositionFromPoint(vec2);
        Angle azimuth = LatLon.greatCircleAzimuth(pos1, pos2);
        Angle distance = LatLon.greatCircleDistance(pos1, pos2);
        Angle adjustedDistance = Angle.fromDegrees(distance.getDegrees() * scale);
        LatLon adjustedPos2 = LatLon.greatCircleEndPosition(pos1, azimuth, adjustedDistance);

        // Return the distance to travel in angular degrees.
        return pos1.subtract(adjustedPos2);
    }

    protected double computeDragSlope(Point point1, Point point2, Vec4 vec1, Vec4 vec2)
    {
        View view = this.getView();
        if (view == null)
        {
            return 0.0;
        }

        // Compute the screen space distance between point1 and point2.
        double dx = point2.getX() - point1.getX();
        double dy = point2.getY() - point1.getY();
        double pixelDistance = Math.sqrt(dx * dx + dy * dy);

        // Determine the distance from the eye to the point on the forward vector closest to vec1 and vec2
        double d = view.getEyePoint().distanceTo3(vec1);
        // Compute the size of a screen pixel at the nearest of the two distances.
        double pixelSize = view.computePixelSizeAtDistance(d);

        // Return the ratio of world distance to screen distance.
        double slope = vec1.distanceTo3(vec2) / (pixelDistance * pixelSize);
        if (slope < 1.0)
            slope = 1.0;

        return slope - 1.0;
    }

    protected static Point constrainToSourceBounds(Point point, Object source)
    {
        if (point == null)
            return null;

        if (!(source instanceof Component))
            return point;

        Component c = (Component) source;

        int x = (int) point.getX();
        if (x < 0)
            x = 0;
        if (x > c.getWidth())
            x = c.getWidth();

        int y = (int) point.getY();
        if (y < 0)
            y = 0;
        if (y > c.getHeight())
            y = c.getHeight();

        return new Point(x, y);
    }

    protected static Point subtract(Point a, Point b)
    {
        if (a == null || b == null)
            return null;
        return new Point((int)(a.getX() - b.getX()), (int)(a.getY() - b.getY()));
    }

    //**************************************************************//
    //********************  Mouse Wheel Events  ********************//
    //**************************************************************//

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.handleMouseWheelMoved(e);
    }

    protected void handleMouseWheelMoved(MouseWheelEvent e)
    {
        this.handleMouseWheelZoom(e);    
    }

    protected void handleMouseWheelZoom(MouseWheelEvent e)
    {
        double zoomInput = e.getWheelRotation();

        ViewInputAttributes.DeviceAttributes deviceAttributes =
            this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_MOUSE_WHEEL);
        ViewInputAttributes.ActionAttributes actionAttributes =
            this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_MOUSE_WHEEL,
                ViewInputAttributes.VIEW_ZOOM);

        double zoomChange = this.rawInputToChangeInValue(zoomInput, deviceAttributes, actionAttributes,
            SCALE_FUNC_ZOOM);

        this.onZoomView(zoomChange, actionAttributes);
    }

    //**************************************************************//
    //********************  Focus Events  **************************//
    //**************************************************************//

    public void focusGained(FocusEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.setWorldWindowFocusOwner(true);
    }

    public void focusLost(FocusEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        this.setWorldWindowFocusOwner(false);

        if (this.isStopOnFocusLost())
            this.onStopView();

        this.keyEventState.clearKeyState();
    }

    //**************************************************************//
    //********************  Rendering Events  **********************//
    //**************************************************************//

    public void stageChanged(RenderingEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        //noinspection StringEquality
        if (RenderingEvent.BEFORE_RENDERING == e.getStage())
        {
            this.handleBeforeRendering(e);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void handleBeforeRendering(RenderingEvent e)
    {
        this.handlePerFrameInput();

        View view = this.getView();
        if (view == null)
        {
            return;
        }

        if (view instanceof OrbitView)
        {
            // Cancel any of our view changes if someone else has set a view state iterator.
            if (view.hasStateIterator())
                this.orbitViewInputSupport.clearTargets();

            if (this.orbitViewInputSupport.hasTargets())
                this.orbitViewInputSupport.moveViewTowardTargets((OrbitView) view);
        }
    }

    //**************************************************************//
    //********************  Rendering Events  **********************//
    //**************************************************************//

    protected void handlePerFrameInput()
    {
        // Process per-frame input only when the World Window is the focus owner.
        if (!this.isWorldWindowFocusOwner())
        {
            return;
        }

        // Throttle the frequency at which we process per-frame input, which is usually invoked each frame. This helps
        // balance the input response of high and low framerate applications.
        long time = System.currentTimeMillis();
        if (time - this.lastPerFrameInputTime > DEFAULT_PER_FRAME_INPUT_DELAY)
        {
            this.handlePerFrameKeyState(this.keyEventState, GENERATE_EVENTS);
            this.lastPerFrameInputTime = time;
        }
        else
        {
            // Determine whether or not the current key state would have generated a view change event. If so, issue
            // a repaint event to give the per-frame input a chance to run again.
            if (this.handlePerFrameKeyState(this.keyEventState, QUERY_EVENTS))
            {
                View view = this.getView();
                if (view != null)
                {
                    view.firePropertyChange(AVKey.VIEW, null, view);
                }
            }
        }
    }

    // Interpret the current key state according to the specified target. If the target is KEY_POLL_GENERATE_EVENTS,
    // then the the key state will generate any appropriate view change events. If the target is KEY_POLL_QUERY_EVENTS,
    // then the key state will not generate events, and this will return whether or not any view change events would
    // have been generated.
    protected boolean handlePerFrameKeyState(KeyEventState keys, String target)
    {
        boolean isKeyEventTrigger;

        // If the rotate modifier is down, then invoke rotate commands.
        if ((keys.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)
        {
            isKeyEventTrigger = this.handleKeyRotate(keys, target);
        }
        // If the zoom modifier is down, then invoke zoom commands.
        else if ((keys.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0)
        {
            isKeyEventTrigger = this.handleKeyZoom(keys, target);
        }
        else
        {
            // Otherwise, invoke commands in priority order: zoom, rotate, then pan. If any succeeds the remaining
            // commands will not be invoked.
            if (!(isKeyEventTrigger = this.handleKeyRotate(keys, target)))
            {
                if (!(isKeyEventTrigger = this.handleKeyZoom(keys, target)))
                {
                    isKeyEventTrigger = this.handleKeyPan(keys, target);
                }
            }
        }

        return isKeyEventTrigger;
    }

    protected boolean handleKeyPan(KeyEventState keys, String target)
    {
        double forwardInput = 0;
        double sideInput = 0;

        if (keys.isKeyDown(KeyEvent.VK_LEFT))
            sideInput += -1;
        if (keys.isKeyDown(KeyEvent.VK_RIGHT))
            sideInput += 1;
        if (keys.isKeyDown(KeyEvent.VK_UP))
            forwardInput += 1;
        if (keys.isKeyDown(KeyEvent.VK_DOWN))
            forwardInput += -1;

        if (forwardInput == 0 && sideInput == 0)
        {
            return false;
        }

        //noinspection StringEquality
        if (target == GENERATE_EVENTS)
        {
            // Normalize the forward and right magnitudes.
            double length = Math.sqrt(forwardInput * forwardInput + sideInput * sideInput);
            if (length > 0.0)
            {
                forwardInput /= length;
                sideInput /= length;
            }

            boolean isSlowEnabled = (keys.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;

            ViewInputAttributes.DeviceAttributes deviceAttributes =
                this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_KEYBOARD);
            ViewInputAttributes.ActionAttributes actionAttributes =
                this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_KEYBOARD,
                    isSlowEnabled? ViewInputAttributes.VIEW_PAN_SLOW : ViewInputAttributes.VIEW_PAN);

            Angle forwardChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(forwardInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM_EXP));
            Angle sideChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(sideInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM_EXP));

            this.onPanViewRelative(forwardChange, sideChange, actionAttributes);
        }

        return true;
    }

    protected boolean handleKeyRotate(KeyEventState keys, String target)
    {
        double headingInput = 0;
        double pitchInput = 0;

        if ((keys.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0)
        {
            if (keys.isKeyDown(KeyEvent.VK_LEFT))
                headingInput += -1;
            if (keys.isKeyDown(KeyEvent.VK_RIGHT))
                headingInput += 1;
            if (keys.isKeyDown(KeyEvent.VK_UP))
                pitchInput += -1;
            if (keys.isKeyDown(KeyEvent.VK_DOWN))
                pitchInput += 1;
        }
        else
        {
            if (keys.isKeyDown(KeyEvent.VK_PAGE_DOWN))
                pitchInput += 1;
            if (keys.isKeyDown(KeyEvent.VK_PAGE_UP))
                pitchInput += -1;
        }

        if (headingInput == 0 && pitchInput == 0)
        {
            return false;
        }

        //noinspection StringEquality
        if (target == GENERATE_EVENTS)
        {
            // Normalize the heading and pitch magnitudes.
            double length = Math.sqrt(headingInput * headingInput + pitchInput * pitchInput);
            if (length > 0.0)
            {
                headingInput /= length;
                pitchInput /= length;
            }

            boolean isSlowEnabled = (keys.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;

            ViewInputAttributes.DeviceAttributes deviceAttributes =
                this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_KEYBOARD);
            ViewInputAttributes.ActionAttributes actionAttributes =
                this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_KEYBOARD,
                    isSlowEnabled ? ViewInputAttributes.VIEW_ROTATE_SLOW : ViewInputAttributes.VIEW_ROTATE);

            Angle headingChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(headingInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));
            Angle pitchChange = Angle.fromDegrees(
                this.rawInputToChangeInValue(pitchInput, deviceAttributes, actionAttributes, SCALE_FUNC_ZOOM));

            this.onRotateView(headingChange, pitchChange, actionAttributes);
        }

        return true;
    }

    protected boolean handleKeyZoom(KeyEventState keys, String target)
    {
        double zoomInput = 0;

        if ((keys.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0)
        {
            if (keys.isKeyDown(KeyEvent.VK_UP))
                zoomInput += -1;
            if (keys.isKeyDown(KeyEvent.VK_DOWN))
                zoomInput += 1;
        }
        else
        {
            if (keys.isKeyDown(KeyEvent.VK_ADD) || keys.isKeyDown(KeyEvent.VK_EQUALS))
                zoomInput += -1;
            if (keys.isKeyDown(KeyEvent.VK_SUBTRACT) || keys.isKeyDown(KeyEvent.VK_MINUS))
                zoomInput += 1;
        }

        if (zoomInput == 0)
        {
            return false;
        }

        //noinspection StringEquality
        if (target == GENERATE_EVENTS)
        {
            boolean isSlowEnabled = (keys.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0;

            ViewInputAttributes.DeviceAttributes deviceAttributes =
                this.attributes.getDeviceAttributes(ViewInputAttributes.DEVICE_KEYBOARD);
            ViewInputAttributes.ActionAttributes actionAttributes =
                this.attributes.getActionAttributes(ViewInputAttributes.DEVICE_KEYBOARD,
                    isSlowEnabled ? ViewInputAttributes.VIEW_ZOOM_SLOW : ViewInputAttributes.VIEW_ZOOM);

            double zoomChange = this.rawInputToChangeInValue(zoomInput, deviceAttributes, actionAttributes,
                SCALE_FUNC_ZOOM);

            this.onZoomView(zoomChange, actionAttributes);
        }

        return true;
    }

    //**************************************************************//
    //********************  Property Change Events  ****************//
    //**************************************************************//

    public void propertyChange(java.beans.PropertyChangeEvent e)
    {
        if (this.wwd == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        if (e == null) // include this test to ensure any derived implementation performs it
        {
            return;
        }

        //noinspection StringEquality
        if (e.getPropertyName() == View.VIEW_STOPPED)
        {
            this.handleViewStopped();
        }
        else //noinspection StringEquality
            if (e.getPropertyName() == OrbitView.CENTER_STOPPED)
        {
            this.handleOrbitViewCenterStopped();
        }
    }

    protected void handleViewStopped()
    {
        this.orbitViewInputSupport.onViewStopped();
    }

    protected void handleOrbitViewCenterStopped()
    {
        this.orbitViewInputSupport.onViewCenterStopped();
    }

    //**************************************************************//
    //********************  Raw Input Transformation  **************//
    //**************************************************************//

    // Translates raw user input into a change in value, according to the specified device and action attributes.
    // The input is scaled by the action attribute range (depending on eye position), then scaled by the device
    // sensitivity.
    protected double rawInputToChangeInValue(double rawInput,
        ViewInputAttributes.DeviceAttributes deviceAttributes, ViewInputAttributes.ActionAttributes actionAttributes,
        String scaleFunc)
    {
        double value = rawInput;

        double[] range = actionAttributes.getValues();
        value *= this.getScaledValue(range[0], range[1], scaleFunc);
        value *= deviceAttributes.getSensitivity();

        return value;
    }

    protected double getScaledValue(double minValue, double maxValue, String scaleFunc)
    {
        if (scaleFunc == null)
        {
            return minValue;
        }

        double t = 0.0;
        if (scaleFunc.startsWith(SCALE_FUNC_EYE_ALTITUDE))
        {
            t = this.evaluateScaleFuncEyeAltitude();
        }
        else if (scaleFunc.startsWith(SCALE_FUNC_ZOOM))
        {
            t = this.evaluateScaleFuncZoom();
        }

        if (scaleFunc.toLowerCase().endsWith("exp"))
        {
            t = Math.pow(2.0, t) - 1.0;
        }
        
        return minValue * (1.0 - t) + maxValue * t;
    }

    protected double evaluateScaleFuncEyeAltitude()
    {
        View view = this.getView();
        if (view == null)
        {
            return 0.0;
        }

        Position eyePos = view.getEyePosition();
        double radius = this.wwd.getModel().getGlobe().getRadius();
        double surfaceElevation = this.wwd.getModel().getGlobe().getElevation(eyePos.getLatitude(), eyePos.getLongitude());
        double t = (eyePos.getElevation() - surfaceElevation) / (3.0 * radius);
        return (t < 0 ? 0 : (t > 1 ? 1 : t));
    }

    protected double evaluateScaleFuncZoom()
    {
        View view = this.getView();
        if (view == null)
        {
            return 0.0;
        }

        double radius = this.wwd.getModel().getGlobe().getRadius();
        double t = ((OrbitView) view).getZoom() / (3.0 * radius);
        return (t < 0 ? 0 : (t > 1 ? 1 : t));
    }

    //**************************************************************//
    //********************  View State Change Utilities  ***********//
    //**************************************************************//

    protected void setCenterPosition(OrbitView view, Position position, ViewInputAttributes.ActionAttributes attrib)
    {
        // Stop ViewStateIterators, so we are the only one affecting the view.
        if (view.hasStateIterator())
            view.stopStateIterators();

        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && this.isEnableSmoothing()))
            smoothing = 0.0;

        if (smoothing != 0.0 && this.orbitViewInputSupport.getCenterTarget() != null)
        {
            Position target = this.orbitViewInputSupport.getCenterTarget();
            Position cur = view.getCenterPosition();
            position = new Position(
                target.getLatitude().add(position.getLatitude()).subtract(cur.getLatitude()),
                target.getLongitude().add(position.getLongitude()).subtract(cur.getLongitude()),
                target.getElevation() + position.getElevation() - cur.getElevation());
        }

        this.orbitViewInputSupport.setCenterSmoothing(smoothing);
        this.orbitViewInputSupport.setCenterTarget(view, position);
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void changeHeading(OrbitView view, Angle change, ViewInputAttributes.ActionAttributes attrib)
    {
        // Stop ViewStateIterators, so we are the only one affecting the view.
        if (view.hasStateIterator())
            view.stopStateIterators();

        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && this.isEnableSmoothing()))
            smoothing = 0.0;

        Angle newHeading;
        if (smoothing != 0.0 && this.orbitViewInputSupport.getHeadingTarget() != null)
        {
            newHeading = this.orbitViewInputSupport.getHeadingTarget().add(change);
        }
        else
        {
            newHeading = view.getHeading().add(change);
        }

        this.orbitViewInputSupport.setHeadingSmoothing(smoothing);
        this.orbitViewInputSupport.setHeadingTarget(view, newHeading);
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void changePitch(OrbitView view, Angle change, ViewInputAttributes.ActionAttributes attrib)
    {
        // Stop ViewStateIterators, so we are the only one affecting the view.
        if (view.hasStateIterator())
            view.stopStateIterators();

        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && this.isEnableSmoothing()))
            smoothing = 0.0;

        Angle newPitch;
        if (smoothing != 0.0 && this.orbitViewInputSupport.getPitchTarget() != null)
        {
            newPitch = this.orbitViewInputSupport.getPitchTarget().add(change);
        }
        else
        {
            newPitch = view.getPitch().add(change);
        }

        this.orbitViewInputSupport.setPitchSmoothing(smoothing);
        this.orbitViewInputSupport.setPitchTarget(view, newPitch);
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected void changeZoom(OrbitView view, double change, ViewInputAttributes.ActionAttributes attrib)
    {
        // Stop ViewStateIterators, so we are the only one affecting the view.
        if (view.hasStateIterator())
            view.stopStateIterators();

        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && this.isEnableSmoothing()))
            smoothing = 0.0;
        
        double newZoom;
        if (smoothing != 0.0 && this.orbitViewInputSupport.getZoomTarget() >= 0)
        {
            newZoom = computeNewZoomTarget(this.orbitViewInputSupport.getZoomTarget(), change);
        }
        else
        {
            newZoom = computeNewZoomTarget(view.getZoom(), change);
        }

        this.orbitViewInputSupport.setZoomSmoothing(smoothing);
        this.orbitViewInputSupport.setZoomTarget(view, newZoom);
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected static double computeNewZoomTarget(double curZoom, double change)
    {
        double logCurZoom = curZoom != 0 ? Math.log(curZoom) : 0;
        return Math.exp(logCurZoom + change);
    }
}
