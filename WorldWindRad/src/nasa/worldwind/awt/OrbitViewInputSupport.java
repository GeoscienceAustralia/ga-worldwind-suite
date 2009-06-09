/*
Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.awt;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.*;

/**
 * @author dcollins
 * @version $Id: OrbitViewInputSupport.java 9587 2009-03-20 20:58:58Z dcollins $
 */
class OrbitViewInputSupport
{
    // OrbitView that will receive value changes.
    private boolean viewChanged;
    private boolean viewOutOfFocus;
    private boolean targetStopped;
    // Target OrbitView values.
    private Position centerTarget; // Initially, do not change OrbitView's center position.
    private Angle headingTarget; // Initially, do not change OrbitView's heading.
    private Angle pitchTarget; // Initially, do not change OrbitView's pitch.
    private double zoomTarget = -1; // Initially, do not change OrbitView's zoom.
    // OrbitView value error thresholds.
    private double centerMinEpsilon;
    private double headingMinEpsilon;
    private double pitchMinEpsilon;
    private double zoomMinEpsilon;
    // OrbitView value smoothing coefficients.
    private double centerSmoothing;
    private double headingSmoothing;
    private double pitchSmoothing;
    private double zoomSmoothing;

    OrbitViewInputSupport()
    {
        loadConfigurationValues();
        // We don't know what state the view is in, so we assume it is "out of focus".
        setViewOutOfFocus(true);
    }

    private void loadConfigurationValues()
    {
        // No configuration keys exist for these values yet (they may never).
        /*setCenterMinEpsilon(1e-9);
        setHeadingMinEpsilon(1e-4);
        setPitchMinEpsilon(1e-4);
        setZoomMinEpsilon(1e-3);*/
        setCenterMinEpsilon(1e-10);
        setHeadingMinEpsilon(1e-2);
        setPitchMinEpsilon(1e-2);
        setZoomMinEpsilon(1e-2);

        // No configuration keys exist for these values yet (they may never).
        setCenterSmoothing(0.4);
        setHeadingSmoothing(0.7);
        setPitchSmoothing(0.7);
        setZoomSmoothing(0.9);
    }

    public void onViewStopped()
    {
        this.clearTargets();
    }

    public void onViewCenterStopped()
    {
        this.centerTarget = null;
        this.setViewOutOfFocus(false);
    }

    private boolean isViewChanged()
    {
        boolean result = this.viewChanged;
        this.viewChanged = false;
        return result;
    }

    private void flagViewChanged()
    {
        this.viewChanged = true;
    }

    private void refreshView(OrbitView view)
    {
        if (isViewChanged())
            view.firePropertyChange(AVKey.VIEW, null, this);
    }

    private boolean isViewOutOfFocus()
    {
        return this.viewOutOfFocus;
    }

    private void setViewOutOfFocus(boolean b)
    {
        this.viewOutOfFocus = b;
    }

    private void focusView(OrbitView view)
    {
        if (view == null)
            return;
        
        try
        {
            // Update the View's focus.
            if (view.canFocusOnViewportCenter())
            {
                view.focusOnViewportCenter();
                setViewOutOfFocus(false);
                flagViewChanged();
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            // If updating the View's focus failed, raise the flag again.
            setViewOutOfFocus(true);
        }
    }

    private boolean isTargetStopped()
    {
        boolean result = this.targetStopped;
        this.targetStopped = false;
        return result;
    }

    private void flagTargetStopped()
    {
        this.targetStopped = true;
    }

    public Position getCenterTarget()
    {
        return this.centerTarget;
    }

    public void setCenterTarget(OrbitView view, Position centerTarget)
    {
        OrbitViewLimits viewLimits = (view != null) ? view.getOrbitViewLimits() : null;

        Position newTarget = limitCenterPosition(centerTarget, viewLimits);

        // If smoothing is disabled, and centerTarget != null, then set center position directly.
        if (this.centerSmoothing == 0 && newTarget != null && view != null)
        {
            this.centerTarget = null;
            view.setCenterPosition(newTarget);
            flagViewChanged();
            setViewOutOfFocus(true);
        }
        // Otherwise, just set the target.
        else
        {
            this.centerTarget = newTarget;
        }

        // Cancel heading, pitch, and zoom targets.
        if (newTarget != null)
        {
            this.headingTarget = null;
            this.pitchTarget = null;
            this.zoomTarget = -1;
        }

        refreshView(view);
    }

    public Angle getHeadingTarget()
    {
        return this.headingTarget;
    }

    public void setHeadingTarget(OrbitView view, Angle headingTarget)
    {
        OrbitViewLimits viewLimits = (view != null) ? view.getOrbitViewLimits() : null;

        Angle newTarget = limitHeading(headingTarget, viewLimits);

        if (isViewOutOfFocus())
            focusView(view);

        // If smoothing is disabled, and headingTarget != null, then set heading directly.
        if (this.headingSmoothing == 0 && newTarget != null && view != null)
        {
            this.headingTarget = null;
            view.setHeading(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.headingTarget = newTarget;
        }

        // Cancel center and zoom targets.
        if (newTarget != null)
        {
            this.centerTarget = null;
            this.zoomTarget = -1;
        }

        refreshView(view);
    }

    public Angle getPitchTarget()
    {
        return this.pitchTarget;
    }

    public void setPitchTarget(OrbitView view, Angle pitchTarget)
    {
        OrbitViewLimits viewLimits = (view != null) ? view.getOrbitViewLimits() : null;

        Angle newTarget = limitPitch(pitchTarget, viewLimits);

        if (isViewOutOfFocus())
            focusView(view);

        // If smoothing is disabled, and pitchTarget != null, then set pitch directly.
        if (this.pitchSmoothing == 0 && newTarget != null && view != null)
        {
            this.pitchTarget = null;
            view.setPitch(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.pitchTarget = newTarget;
        }

        // Cancel center and zoom targets,
        if (newTarget != null)
        {
            this.centerTarget = null;
            this.zoomTarget = -1;
        }

        refreshView(view);
    }

    public double getZoomTarget()
    {
        return this.zoomTarget;
    }

    public void setZoomTarget(OrbitView view, double zoomTarget)
    {
        OrbitViewLimits viewLimits = (view != null) ? view.getOrbitViewLimits() : null;

        double newTarget = limitZoom(zoomTarget, viewLimits);

        if (isViewOutOfFocus() && view != null)
        {
            double beforeFocus, afterFocus;
            beforeFocus = view.getZoom();
            focusView(view);
            afterFocus = view.getZoom();
            newTarget = limitZoom(newTarget + (afterFocus - beforeFocus), viewLimits);
        }

        // If smoothing is disabled, and zoomTarget >= 0, then set zoom directly.
        if (this.zoomSmoothing == 0 && newTarget >= 0 && view != null)
        {
            this.zoomTarget = -1;
            view.setZoom(newTarget);
            flagViewChanged();
        }
        // Otherwise, just set the target.
        else
        {
            this.zoomTarget = newTarget;
        }

        // Cancel center, heading, and pitch targets.
        if (newTarget >= 0)
        {
            this.centerTarget = null;
            this.headingTarget = null;
            this.pitchTarget = null;
        }

        refreshView(view);
    }

    public boolean hasTargets()
    {
        return this.centerTarget != null
            || this.headingTarget != null
            || this.pitchTarget != null
            || this.zoomTarget >= 0;
    }

    public void clearTargets()
    {
        this.centerTarget = null;
        this.headingTarget = null;
        this.pitchTarget = null;
        this.zoomTarget = -1;
        // Clear viewing flags.
        this.viewChanged = false;
        setViewOutOfFocus(false);
        this.targetStopped = false;
    }

    private static Position limitCenterPosition(Position position, OrbitViewLimits viewLimits)
    {
        if (position == null)
            return null;

        position = BasicOrbitView.normalizedCenterPosition(position);
        position = BasicOrbitViewLimits.limitCenterPosition(position, viewLimits);
        return position;
    }

    private static Angle limitHeading(Angle heading, OrbitViewLimits viewLimits)
    {
        if (heading == null)
            return null;

        heading = BasicOrbitView.normalizedHeading(heading);
        heading = BasicOrbitViewLimits.limitHeading(heading, viewLimits);
        return heading;
    }

    private static Angle limitPitch(Angle pitch, OrbitViewLimits viewLimits)
    {
        if (pitch == null)
            return null;

        pitch = BasicOrbitView.normalizedPitch(pitch);
        pitch = BasicOrbitViewLimits.limitPitch(pitch, viewLimits);
        return pitch;
    }

    private static double limitZoom(double zoom, OrbitViewLimits viewLimits)
    {
        zoom = normalizedZoom(zoom);
        zoom = BasicOrbitViewLimits.limitZoom(zoom, viewLimits);
        return zoom;
    }

    private static double normalizedZoom(double unnormalizedZoom)
    {
        return unnormalizedZoom < 0 ? -1 : unnormalizedZoom;
    }

    public double getCenterMinEpsilon()
    {
        return this.centerMinEpsilon;
    }

    public void setCenterMinEpsilon(double centerMinEpsilon)
    {
        if (centerMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", centerMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerMinEpsilon = centerMinEpsilon;
    }

    public double getHeadingMinEpsilon()
    {
        return headingMinEpsilon;
    }

    public void setHeadingMinEpsilon(double headingMinEpsilon)
    {
        if (headingMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", headingMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingMinEpsilon = headingMinEpsilon;
    }

    public double getPitchMinEpsilon()
    {
        return this.pitchMinEpsilon;
    }

    public void setPitchMinEpsilon(double pitchMinEpsilon)
    {
        if (pitchMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", pitchMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pitchMinEpsilon = pitchMinEpsilon;
    }

    public double getZoomMinEpsilon()
    {
        return this.zoomMinEpsilon;
    }

    public void setZoomMinEpsilon(double zoomMinEpsilon)
    {
        if (zoomMinEpsilon < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", zoomMinEpsilon);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zoomMinEpsilon = zoomMinEpsilon;
    }

    public double getCenterSmoothing()
    {
        return this.centerSmoothing;
    }

    public void setCenterSmoothing(double centerSmoothing)
    {
        if (centerSmoothing < 0 || centerSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", centerSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centerSmoothing = centerSmoothing;
    }

    public double getHeadingSmoothing()
    {
        return this.headingSmoothing;
    }

    public void setHeadingSmoothing(double headingSmoothing)
    {
        if (headingSmoothing < 0 || headingSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", headingSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.headingSmoothing = headingSmoothing;
    }

    public double getPitchSmoothing()
    {
        return this.pitchSmoothing;
    }

    public void setPitchSmoothing(double pitchSmoothing)
    {
        if (pitchSmoothing < 0 || pitchSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", pitchSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pitchSmoothing = pitchSmoothing;
    }

    public double getZoomSmoothing()
    {
        return this.zoomSmoothing;
    }

    public void setZoomSmoothing(double zoomSmoothing)
    {
        if (zoomSmoothing < 0 || zoomSmoothing > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", zoomSmoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zoomSmoothing = zoomSmoothing;
    }

    public void moveViewTowardTargets(OrbitView view)
    {
        if (view == null)
            return;

        // Clear any locally tracked view state.
        isViewChanged();
        isTargetStopped();

        if (this.centerTarget != null)
            moveTowardCenterTarget(view);
        if (this.headingTarget != null)
            moveTowardHeadingTarget(view);
        if (this.pitchTarget != null)
            moveTowardPitchTarget(view);
        if (this.zoomTarget >= 0)
            moveTowardZoomTarget(view);

        refreshView(view);

        if (isTargetStopped() && !hasTargets())
        {
            if (isViewOutOfFocus())
                focusView(view);
            
            clearTargets();
            view.firePropertyChange(AVKey.VIEW_QUIET, null, view);
        }
    }

    private void moveTowardCenterTarget(OrbitView view)
    {
        Position nextCenter = this.centerTarget;
        Position curCenter = view.getCenterPosition();

        double latlonDifference = LatLon.greatCircleDistance(nextCenter, curCenter).degrees;
        double elevDifference = Math.abs(nextCenter.getElevation() - curCenter.getElevation());
        boolean stopMoving = Math.max(latlonDifference, elevDifference) < this.centerMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.centerSmoothing;
            nextCenter = new Position(
                Angle.mix(interpolant, curCenter.getLatitude(), this.centerTarget.getLatitude()),
                Angle.mix(interpolant, curCenter.getLongitude(), this.centerTarget.getLongitude()),
                (1 - interpolant) * curCenter.getElevation() + interpolant * this.centerTarget.getElevation());
        }

        try
        {
            // Clear any previous collision state the view may have.
            view.hadCollisions();
            view.setCenterPosition(nextCenter);
            // If the change caused a collision, update the target center position with the
            // elevation that resolved the collision.
            if (view.hadCollisions())
                this.centerTarget = new Position(
                        this.centerTarget, view.getCenterPosition().getElevation());
            flagViewChanged();
            setViewOutOfFocus(true);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.centerTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardHeadingTarget(OrbitView view)
    {
        Angle nextHeading = this.headingTarget;
        Angle curHeading = view.getHeading();

        double difference = Math.abs(nextHeading.subtract(curHeading).degrees);
        boolean stopMoving = difference < this.headingMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.headingSmoothing;
            nextHeading = Angle.mix(interpolant, curHeading, this.headingTarget);
        }

        try
        {
            view.setHeading(nextHeading);
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.headingTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardPitchTarget(OrbitView view)
    {
        Angle nextPitch = this.pitchTarget;
        Angle curPitch = view.getPitch();

        double difference = Math.abs(nextPitch.subtract(curPitch).degrees);
        boolean stopMoving = difference < this.pitchMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.pitchSmoothing;
            nextPitch = Angle.mix(interpolant, curPitch, this.pitchTarget);
        }

        try
        {
            // Clear any previous collision state the view may have.
            view.hadCollisions();
            view.setPitch(nextPitch);
            // If the change caused a collision, cancel future pitch changes.
            if (view.hadCollisions())
                stopMoving = true;
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.pitchTarget = null;
            flagTargetStopped();
        }
    }

    private void moveTowardZoomTarget(OrbitView view)
    {
        double nextZoom = this.zoomTarget;
        double curZoom = view.getZoom();

        double difference = Math.abs(nextZoom - curZoom);
        boolean stopMoving = difference < this.zoomMinEpsilon;

        if (!stopMoving)
        {
            double interpolant = 1 - this.zoomSmoothing;
            nextZoom = (1 - interpolant) * curZoom + interpolant * this.zoomTarget;
        }

        try
        {
            view.setZoom(nextZoom);
            flagViewChanged();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileChangingView");
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            stopMoving = true;
        }

        // If target is close, cancel future value changes.
        if (stopMoving)
        {
            this.zoomTarget = -1;
            flagTargetStopped();
        }
    }
}
