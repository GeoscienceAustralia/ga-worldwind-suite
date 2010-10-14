package au.gov.ga.worldwind.animator.util;

import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Vec4;

/**
 * Utility methods for dealing with geometry (lines, polygons etc.)
 *
 */
public class GeometryUtil
{

	/**
	 * Calculate the {@link Intersection} point nearest to the provided {@link Line}'s origin
	 */
	public static Vec4 nearestIntersectionPoint(Line line, Intersection[] intersections)
    {
        Vec4 intersectionPoint = null;

        // Find the nearest intersection that's in front of the ray origin.
        double nearestDistance = Double.MAX_VALUE;
        for (Intersection intersection : intersections)
        {
            // Ignore any intersections behind the line origin.
            if (!isPointBehindLineOrigin(line, intersection.getIntersectionPoint()))
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
	
	/**
	 * @return Whether the provided point lies behind the line origin
	 */
	public static boolean isPointBehindLineOrigin(Line line, Vec4 point)
    {
        double dot = point.subtract3(line.getOrigin()).dot3(line.getDirection());
        return dot < 0.0;
    }
	
}
