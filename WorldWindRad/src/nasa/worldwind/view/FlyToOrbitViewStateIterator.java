/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.BasicOrbitViewAnimator;
import gov.nasa.worldwind.view.BasicOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;
import gov.nasa.worldwind.view.OrbitViewAnimator;
import gov.nasa.worldwind.view.OrbitViewPropertyAccessor;
import gov.nasa.worldwind.view.ScheduledOrbitViewInterpolator;
import gov.nasa.worldwind.view.BasicOrbitViewAnimator.AngleAnimator;
import gov.nasa.worldwind.view.BasicOrbitViewAnimator.DoubleAnimator;
import gov.nasa.worldwind.view.BasicOrbitViewAnimator.PositionAnimator;

/**
 * @author dcollins
 * @version $Id: FlyToOrbitViewStateIterator.java 5897 2008-08-09 05:20:21Z tgaskins $
 */
public class FlyToOrbitViewStateIterator extends BasicOrbitViewStateIterator
{
    protected FlyToOrbitViewStateIterator(long lengthMillis, OrbitViewAnimator animator)
    {
        super(false, new ScheduledOrbitViewInterpolator(lengthMillis), animator);
    }

    // ============== "Pan To" ======================= //
    // ============== "Pan To" ======================= //
    // ============== "Pan To" ======================= //

    private static class PanAnimator extends BasicOrbitViewAnimator
    {
        private final Globe globe;
        private final OrbitViewAnimator centerAnimator;
        private final OrbitViewAnimator zoomAnimator;
        private final OrbitViewAnimator headingAnimator;
        private final OrbitViewAnimator pitchAnimator;
        private final OrbitViewAnimator beginToMidZoomAnimator, endToMidZoomAnimator;
        private final boolean useMidZoom;
        private final boolean endCenterOnSurface;

        private PanAnimator(
            Globe globe,
            Position beginCenter, Position endCenter,
            Angle beginHeading, Angle endHeading,
            Angle beginPitch, Angle endPitch,
            double beginZoom, double endZoom)
        {
            this(globe,
                beginCenter, endCenter,
                beginHeading, endHeading,
                beginPitch, endPitch,
                beginZoom, endZoom,
                false);
        }

        private PanAnimator(
            Globe globe,
            Position beginCenter, Position endCenter,
            Angle beginHeading, Angle endHeading,
            Angle beginPitch, Angle endPitch,
            double beginZoom, double endZoom,
            boolean endCenterOnSurface)
        {
            this.globe  = globe;
            this.endCenterOnSurface = endCenterOnSurface;

            // Center position.
            this.centerAnimator = createPositionAnimator(beginCenter, endCenter);
            // Zoom.
            this.zoomAnimator = new DoubleAnimator(
                beginZoom, endZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
            // Heading.
            this.headingAnimator = new AngleAnimator(
                beginHeading, endHeading,
                OrbitViewPropertyAccessor.createHeadingAccessor());
            // Pitch.
            this.pitchAnimator = new AngleAnimator(
                beginPitch, endPitch,
                OrbitViewPropertyAccessor.createPitchAccessor());

            // Mid-zoom logic.
            double midZoom = computeMidZoom(
                this.globe,
                beginCenter, endCenter,
                beginZoom, endZoom);
            this.useMidZoom = useMidZoom(
                beginZoom, endZoom, midZoom);
            this.beginToMidZoomAnimator = new DoubleAnimator(
                beginZoom, midZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
            this.endToMidZoomAnimator = new DoubleAnimator(
                endZoom, midZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
        }

        private PositionAnimator createPositionAnimator(Position beginCenter, Position endCenter)
        {
            return new PositionAnimator(
                    beginCenter, endCenter,
                    OrbitViewPropertyAccessor.createCenterPositionAccessor())
            {
                public Position nextPosition(double interpolant, OrbitView orbitView)
                {
                    // Invoke the standard next position functionality.
                    Position pos = super.nextPosition(interpolant, orbitView);

                    // If the caller has flagged endCenterOnSurface, then we override endPosition's elevation with
                    // the surface elevation.
                    if (endCenterOnSurface)
                    {
                        // Use interpolated lat/lon.
                        LatLon ll = pos;
                        // Override end position elevation with surface elevation at end lat/lon.
                        double e1 = getBegin().getElevation();
                        double e2 = globe.getElevation(getEnd().getLatitude(), getEnd().getLongitude());
                        pos = new Position(ll, (1 - interpolant) * e1 + interpolant * e2);
                    }

                    return pos;
                }
            };
        }

        private static double computeMidZoom(
            Globe globe,
            LatLon beginLatLon, LatLon endLatLon,
            double beginZoom, double endZoom)
        {
            // Scale factor is angular distance over 180 degrees.
            Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
            double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);

            // Mid-point zoom is interpolated value between minimum and maximum zoom.
            final double MIN_ZOOM = Math.min(beginZoom, endZoom);
            final double MAX_ZOOM = 3.0 * globe.getRadius();
            return mixDouble(scaleFactor, MIN_ZOOM, MAX_ZOOM);
        }

        private static boolean useMidZoom(double beginZoom, double endZoom, double midZoom)
        {
            double a = Math.abs(endZoom - beginZoom);
            double b = Math.abs(midZoom - Math.max(beginZoom, endZoom));
            return a < b;
        }

        protected void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (stateIterator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIteratorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nextCenterState(interpolant, orbitView, stateIterator);
            this.nextZoomState(interpolant, orbitView, stateIterator);
            this.nextHeadingState(interpolant, orbitView, stateIterator);
            this.nextPitchState(interpolant, orbitView, stateIterator); 
        }

        private void nextCenterState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double CENTER_START = this.useMidZoom ? 0.2 : 0.0;
            final double CENTER_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = basicInterpolant(interpolant, CENTER_START, CENTER_STOP, MAX_SMOOTHING);
            this.centerAnimator.doNextState(latLonInterpolant, orbitView, stateIterator);
        }

        private void nextHeadingState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            //final double HEADING_START = this.useMidZoom ? 0.0 : 0.6;
            final double HEADING_START = 0.1;
            //final double HEADING_STOP = 1.0;
            final double HEADING_STOP = 0.9;
            double headingInterpolant = basicInterpolant(interpolant, HEADING_START, HEADING_STOP, MAX_SMOOTHING);
            this.headingAnimator.doNextState(headingInterpolant, orbitView, stateIterator);
        }

        private void nextPitchState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            //final double PITCH_START = 0.0;
            final double PITCH_START = 0.1;
            //final double PITCH_STOP = 0.8;
            final double PITCH_STOP = 0.9;
            double pitchInterpolant = basicInterpolant(interpolant, PITCH_START, PITCH_STOP, MAX_SMOOTHING);
            this.pitchAnimator.doNextState(pitchInterpolant, orbitView, stateIterator);
        }

        private void nextZoomState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            if (this.useMidZoom)
            {
                final double ZOOM_START = 0.0;
                final double ZOOM_STOP = 1.0;
                double zoomInterpolant = this.zoomInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
                if (interpolant <= 0.5)
                    this.beginToMidZoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
                else
                    this.endToMidZoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
            }
            else
            {
                final double ZOOM_START = 0.0;
                final double ZOOM_STOP = 1.0;
                double zoomInterpolant = basicInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
                this.zoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
            }
        }

        private double zoomInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
            int maxSmoothing)
        {
            // Map interpolant in to range [start, stop].
            double normalizedInterpolant = interpolantNormalized(interpolant, startInterpolant, stopInterpolant);

            // During first half of iteration, zoom increases from begin to mid,
            // and decreases from mid to end during second half.
            if (normalizedInterpolant <= 0.5)
            {
                normalizedInterpolant = 2.0 * normalizedInterpolant;
            }
            else
            {
                normalizedInterpolant = 1.0 - (2.0 * normalizedInterpolant - 1.0);
            }

            return interpolantSmoothed(normalizedInterpolant, maxSmoothing);
        }
    }

    // ============== "Zoom To" ======================= //
    // ============== "Zoom To" ======================= //
    // ============== "Zoom To" ======================= //
    
    private static class ZoomAnimator extends BasicOrbitViewAnimator
    {
        private final OrbitViewAnimator headingAnimator;
        private final OrbitViewAnimator pitchAnimator;
        private final OrbitViewAnimator zoomAnimator;

        private ZoomAnimator(
            Angle beginHeading, Angle endHeading,
            Angle beginPitch, Angle endPitch,
            double beginZoom, double endZoom)
        {
            // Heading.
            this.headingAnimator = new AngleAnimator(
                beginHeading, endHeading,
                OrbitViewPropertyAccessor.createHeadingAccessor());
            // Pitch.
            this.pitchAnimator = new AngleAnimator(
                beginPitch, endPitch,
                OrbitViewPropertyAccessor.createPitchAccessor());
            // Zoom.
            this.zoomAnimator = new DoubleAnimator(
                beginZoom, endZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
        }

        protected void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (stateIterator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIteratorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nextZoomState(interpolant, orbitView, stateIterator);
            this.nextHeadingState(interpolant, orbitView, stateIterator);
            this.nextPitchState(interpolant, orbitView, stateIterator);
        }

        private void nextHeadingState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double HEADING_START = 0.0;
            final double HEADING_STOP = 0.6;
            double headingInterpolant = basicInterpolant(interpolant, HEADING_START, HEADING_STOP, MAX_SMOOTHING);
            this.headingAnimator.doNextState(headingInterpolant, orbitView, stateIterator);
        }

        private void nextPitchState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double PITCH_START = 0.0;
            final double PITCH_STOP = 0.6;
            double pitchInterpolant = basicInterpolant(interpolant, PITCH_START, PITCH_STOP, MAX_SMOOTHING);
            this.pitchAnimator.doNextState(pitchInterpolant, orbitView, stateIterator);
        }

        private void nextZoomState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double ZOOM_START = 0.0;
            final double ZOOM_STOP = 1.0;
            double zoomInterpolant = basicInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
            this.zoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
        }
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static FlyToOrbitViewStateIterator createPanToIterator(
        OrbitView orbitView, Globe globe,
        Position center,
        Angle heading,
        Angle pitch,
        double zoom)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position beginCenter = orbitView.getCenterPosition();
        Angle beginHeading = orbitView.getHeading();
        Angle beginPitch = orbitView.getPitch();
        double beginZoom = orbitView.getZoom();
        return createPanToIterator(
            globe,
            beginCenter, center,
            beginHeading, heading,
            beginPitch, pitch,
            beginZoom, zoom);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        OrbitView orbitView, Globe globe,
        Position center,
        Angle heading,
        Angle pitch,
        double zoom,
        boolean endCenterOnSurface)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position beginCenter = orbitView.getCenterPosition();
        Angle beginHeading = orbitView.getHeading();
        Angle beginPitch = orbitView.getPitch();
        double beginZoom = orbitView.getZoom();
        return createPanToIterator(
            globe,
            beginCenter, center,
            beginHeading, heading,
            beginPitch, pitch,
            beginZoom, zoom,
            endCenterOnSurface);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        Position beginCenter, Position endCenter,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginCenter == null || endCenter == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: scale on mid-altitude?
        final long MIN_LENGTH_MILLIS = 4000;
        final long MAX_LENGTH_MILLIS = 16000;
        long lengthMillis = getScaledLengthMillis(
            beginCenter, endCenter,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        return createPanToIterator(
            globe,
            beginCenter, endCenter,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            lengthMillis);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        Position beginCenter, Position endCenter,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        boolean endCenterOnSurface)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginCenter == null || endCenter == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: scale on mid-altitude?
        final long MIN_LENGTH_MILLIS = 4000;
        final long MAX_LENGTH_MILLIS = 16000;
        long lengthMillis = getScaledLengthMillis(
            beginCenter, endCenter,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        return createPanToIterator(
            globe,
            beginCenter, endCenter,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            lengthMillis,
            endCenterOnSurface);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        Position beginCenter, Position endCenter,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        long lengthMillis)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginCenter == null || endCenter == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator animator = new PanAnimator(
            globe,
            beginCenter, endCenter,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom);
        return new FlyToOrbitViewStateIterator(lengthMillis, animator);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        Position beginCenter, Position endCenter,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        long lengthMillis,
        boolean endCenterOnSurface)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginCenter == null || endCenter == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator animator = new PanAnimator(
            globe,
            beginCenter, endCenter,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            endCenterOnSurface);
        return new FlyToOrbitViewStateIterator(lengthMillis, animator);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        OrbitView orbitView,
        Angle heading, Angle pitch,
        double zoom)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle beginHeading = orbitView.getHeading();
        Angle beginPitch = orbitView.getPitch();
        double beginZoom = orbitView.getZoom();
        return createZoomToIterator(
            beginHeading, heading,
            beginPitch, pitch,
            beginZoom, zoom);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom)
    {
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final long MIN_LENGTH_MILLIS = 1000;
        final long MAX_LENGTH_MILLIS = 8000;
        long lengthMillis = getScaledLengthMillis(
            beginZoom, endZoom,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        return createZoomToIterator(
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            lengthMillis);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        long lengthMillis)
    {
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator animator = new ZoomAnimator(
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom);
        return new FlyToOrbitViewStateIterator(lengthMillis, animator);
    }

    private static long getScaledLengthMillis(
        double beginZoom, double endZoom,
        long minLengthMillis, long maxLengthMillis)
    {
        double scaleFactor = Math.abs(endZoom - beginZoom) / Math.max(endZoom, beginZoom);
        // Clamp scaleFactor to range [0, 1].
        scaleFactor = clampDouble(scaleFactor, 0.0, 1.0);
        // Iteration time is interpolated value between minumum and maximum lengths.
        return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
    }

    private static long getScaledLengthMillis(
            LatLon beginLatLon, LatLon endLatLon,
            long minLengthMillis, long maxLengthMillis)
    {
        Angle sphericalDistance = LatLon.greatCircleDistance(beginLatLon, endLatLon);
        double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
        return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    // Map amount range [startAmount, stopAmount] to [0, 1] when amount is inside range.
    private static double interpolantNormalized(double amount, double startAmount, double stopAmount)
    {
        if (amount < startAmount)
            return 0.0;
        else if (amount > stopAmount)
            return 1.0;
        return (amount - startAmount) / (stopAmount - startAmount);
    }

    private static double interpolantSmoothed(double interpolant, int smoothingIterations)
    {
        // Apply iterative hermite smoothing.
        double smoothed = interpolant;
        for (int i = 0; i < smoothingIterations; i++)
        {
            smoothed = smoothed * smoothed * (3.0 - 2.0 * smoothed);
        }
        return smoothed;
    }

    private static double basicInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
        int maxSmoothing)
    {
        double normalizedInterpolant = interpolantNormalized(interpolant, startInterpolant, stopInterpolant);
        return interpolantSmoothed(normalizedInterpolant, maxSmoothing);
    }

    private static double angularRatio(Angle x, Angle y)
    {
        if (x == null || y == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double unclampedRatio = x.divide(y);
        return clampDouble(unclampedRatio, 0, 1);
    }

    private static double clampDouble(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    private static double mixDouble(double amount, double value1, double value2)
    {
        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;
        return value1 * (1.0 - amount) + value2 * amount;
    }
}
