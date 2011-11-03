package org.csiro.examples.model;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.RayCastingSupport;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * original code from tag of BasicDragger2.java 4909 2008-04-03 22:40:05Z patrickmurris $
 *
 * modified by R.Wathelet 2008-06-11.
 * Added the roll, yaw, pitch, altitude and size functions for an Adjutable object.
 * That is, press a key then start dragging with the left mouse button staring on the object.
 * Press p or P and drag the mouse in the y direction to adjust the pitch.
 * Press a or A and drag the mouse in the y direction to adjust the altitude.
 * Press s or S and drag the mouse in the x direction to adjust the size.
 * Any other key adjusts the roll (x direction) and yaw (y direction).
 *
 * Normal dragging is done as in BasicDragger2 by dragging a Movable object
 * with the left mouse button.
 */
public class GenericDraggerAdjuster implements SelectListener {

    private final WorldWindowGLCanvas wwd;
    private boolean dragging = false;
    private Point dragRefCursorPoint;
    private Point prevCursorPoint;
    private Vec4 dragRefObjectPoint;
    private double dragRefAltitude;
    private char keyPressed;
    private boolean isKeyPressed = false;
    private double minSize = 100.0;     // the minimum size for the object
    private double sizeInc = 100.0;     // the size increment as the mouse is moved
    private double altitudeInc = 100.0; // the altitude increment
    private double sensitivity = 0.1;   // sensitivity factor for roll, yaw and pitch, should not be zero

    public GenericDraggerAdjuster(WorldWindowGLCanvas wwd) {
        if (wwd == null) {
            String msg = Logging.getMessage("nullValue.WorldWindowGLCanvas");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.wwd = wwd;
        this.wwd.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                keyPressed = e.getKeyChar();
                isKeyPressed = true;
            }

            public void keyReleased(KeyEvent e) {
                isKeyPressed = false;
            }
        });
    }

    @Override
    public void selected(SelectEvent event) {
        if (event == null) {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (isKeyPressed) {
            this.doAdjust(event);
        } else {
            this.dragMovable(event);
        }
    }

    public void doAdjust(SelectEvent event) {
        if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
            this.dragging = false;
        } else if (event.getEventAction().equals(SelectEvent.DRAG)) {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null) {
                return;
            }

            if (!(topObject instanceof Adjustable)) {
                return;
            }

            Adjustable theObject = (Adjustable) topObject;
            if (!this.isDragging()) {
                this.dragRefCursorPoint = dragEvent.getPreviousPickPoint();
                this.prevCursorPoint = dragEvent.getPreviousPickPoint();
            }

            switch (keyPressed) {
                case 'p':
                case 'P':
                    double pitchValue = Math.abs(this.dragRefCursorPoint.y - dragEvent.getPickPoint().y) * sensitivity;
                    if (this.prevCursorPoint.y < dragEvent.getPickPoint().y) {
                        theObject.setPitch((theObject.getPitch() - pitchValue) % 360.0);
                    } else {
                        theObject.setPitch((theObject.getPitch() + pitchValue) % 360.0);
                    }
                    break;

                case 's':
                case 'S':
                    double localSize = sizeInc * Math.abs(this.dragRefCursorPoint.x - dragEvent.getPickPoint().x);
                    if (this.prevCursorPoint.x > dragEvent.getPickPoint().x) {
                        localSize = theObject.getSize() - localSize;
                    } else {
                        localSize = theObject.getSize() + localSize;
                    }
                    if (localSize <= 0) {
                        localSize = minSize;
                    }
                    theObject.setSize(localSize);
                    break;

                case 'a':
                case 'A':
                    double altitude = altitudeInc * Math.abs(this.dragRefCursorPoint.y - dragEvent.getPickPoint().y);
                    if (this.prevCursorPoint.y < dragEvent.getPickPoint().y) {
                        altitude = theObject.getPosition().getElevation() - altitude;
                    } else {
                        altitude = theObject.getPosition().getElevation() + altitude;
                    }
                    theObject.setPosition(new Position(theObject.getPosition(), altitude));
                    break;

                default:

                    double roll = Math.abs(dragEvent.getPickPoint().x - this.dragRefCursorPoint.x) * sensitivity;
                    if (this.prevCursorPoint.x > dragEvent.getPickPoint().x) {
                        theObject.setRoll((theObject.getRoll() - roll) % 360.0);
                    } else {
                        theObject.setRoll((theObject.getRoll() + roll) % 360.0);
                    }

                    double yaw = Math.abs(dragEvent.getPickPoint().y - this.dragRefCursorPoint.y) * sensitivity;
                    if (this.prevCursorPoint.y > dragEvent.getPickPoint().y) {
                        theObject.setYaw((theObject.getYaw() - yaw) % 360.0);
                    } else {
                        theObject.setYaw((theObject.getYaw() + yaw) % 360.0);
                    }
                    break;
            }
            this.prevCursorPoint = dragEvent.getPreviousPickPoint();
            this.dragging = true;
        }
    }

    public void dragMovable(SelectEvent event) {
        if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
            this.dragging = false;
        } else if (event.getEventAction().equals(SelectEvent.DRAG)) {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null) {
                return;
            }

            if (!(topObject instanceof Movable)) {
                return;
            }

            Movable dragObject = (Movable) topObject;
            View view = wwd.getView();
            Globe globe = wwd.getModel().getGlobe();

            // Compute dragged object ref-point in model coordinates.
            // Use the Icon and Annotation logic of elevation as offset above ground when below max elevation.
            Position refPos = dragObject.getReferencePosition();
            Vec4 refPoint = null;
            if (refPos.getElevation() < globe.getMaxElevation()) {
                refPoint = wwd.getSceneController().getTerrain().getSurfacePoint(refPos);
            }

            if (refPoint == null) {
                refPoint = globe.computePointFromPosition(refPos);
            }

            if (!this.isDragging()) // Dragging started
            {
                // Save initial reference points for object and cursor in screen coordinates
                // Note: y is inverted for the object point.
                this.dragRefObjectPoint = view.project(refPoint);
                // Save cursor position
                this.dragRefCursorPoint = dragEvent.getPreviousPickPoint();
                // Save start altitude
                this.dragRefAltitude = globe.computePositionFromPoint(refPoint).getElevation();
                // used when dragging then pressing a key
                this.prevCursorPoint = dragEvent.getPreviousPickPoint();
            }

            // Compute screen-coord delta since drag started.
            int dx = dragEvent.getPickPoint().x - this.dragRefCursorPoint.x;
            int dy = dragEvent.getPickPoint().y - this.dragRefCursorPoint.y;

            // Find intersection of screen coord (refObjectPoint + delta) with globe.
            double x = this.dragRefObjectPoint.x + dx;
            double y = event.getMouseEvent().getComponent().getSize().height - this.dragRefObjectPoint.y + dy - 1;
            Line ray = view.computeRayFromScreenPoint(x, y);
            Position pickPos = null;
            if (view.getEyePosition().getElevation() < globe.getMaxElevation() * 10) {
                // Use ray casting below some altitude
                pickPos = RayCastingSupport.intersectRayWithTerrain(globe,
                        ray.getOrigin(), ray.getDirection(), 200, 20);
            } else {
                // Use intersection with sphere at reference altitude.
                Intersection inters[] = globe.intersect(ray, this.dragRefAltitude);
                if (inters != null) {
                    pickPos = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
                }
            }
            if (pickPos != null) {
                // Intersection with globe. Move reference point to the intersection point,
                // but maintain current altitude.
                Position p = new Position(pickPos, dragObject.getReferencePosition().getElevation());
                dragObject.moveTo(p);
            }
            this.dragging = true;
        }
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public void setSensitivity(double val) {
        this.sensitivity = val;
    }

    public void setSizeIncrement(double val) {
        this.sizeInc = val;
    }

    public void setMinimumSize(double val) {
        this.minSize = val;
    }

    public void setAltitudeIncrement(double val) {
        this.altitudeInc = val;
    }

    public double getSensitivity() {
        return this.sensitivity;
    }

    public double getAltitudeIncrement() {
        return this.altitudeInc;
    }

    public double getMinimumSize() {
        return this.minSize;
    }

    public double getSizeIncrement() {
        return this.sizeInc;
    }
}
