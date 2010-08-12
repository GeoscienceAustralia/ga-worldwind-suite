package au.gov.ga.worldwind.animator.view.roll;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;

public class RollOrbitViewInputSupport extends OrbitViewInputSupport
{
    public static Matrix computeTransformMatrix(Globe globe, Position center, Angle heading, Angle pitch, Angle roll, double zoom)
    {
    	//from OrbitViewInputSupport.computeTransformMatrix, with roll added
    	
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (pitch == null)
        {
            String message = Logging.getMessage("nullValue.PitchIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Construct the model-view transform matrix for the specified coordinates.
        // Because this is a model-view transform, matrices are applied in reverse order.
        Matrix transform;
        // Zoom, heading, pitch.
        transform = computeHeadingPitchRollZoomTransform(heading, pitch, roll, zoom);
        // Center position.
        transform = transform.multiply(OrbitViewInputSupport.computeCenterTransform(globe, center));

        return transform;
    }
	
	protected static Matrix computeHeadingPitchRollZoomTransform(Angle heading, Angle pitch, Angle roll, double zoom)
    {
		//from OrbitViewInputSupport.computeHeadingPitchZoomTransform, with roll added
		
        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (pitch == null)
        {
            String message = Logging.getMessage("nullValue.PitchIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix transform;
        // Zoom.
        transform = Matrix.fromTranslation(0, 0, -zoom);
        //Roll
		transform = transform.multiply(Matrix.fromRotationZ(roll));
        // Pitch is treated clockwise as rotation about the X-axis. We flip the pitch value so that a positive
        // rotation produces a clockwise rotation (when facing the axis).
        transform = transform.multiply(Matrix.fromRotationX(pitch.multiply(-1.0)));
        // Heading.
        transform = transform.multiply(Matrix.fromRotationZ(heading));
        return transform;
    }
}
