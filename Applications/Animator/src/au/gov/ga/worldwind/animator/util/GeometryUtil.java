package au.gov.ga.worldwind.animator.util;

import au.gov.ga.worldwind.common.util.Validate;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;

/**
 * Utility methods for working with geometry
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GeometryUtil
{

	/**
	 * Compute the 3D Plane that contains the two provided lines
	 */
	public static Plane createPlaneContainingLines(Line line1, Line line2)
	{
		Validate.isTrue(line1 != null && line2 != null, "Two lines are required to create a plane in 3D space");
		
		// Choose three arbitrary points that lie on the two lines
		Vec4 point1 = line1.getPointAt(0);
		Vec4 point2 = line1.getPointAt(1000);
		Vec4 point3 = line2.getPointAt(500);
		
		return createPlaneFromThreePoints(point1, point2, point3);
	}

	/**
	 * Compute the 3D Plane that contains the three provided points
	 */
	public static Plane createPlaneFromThreePoints(Vec4 point1, Vec4 point2, Vec4 point3)
	{
		Validate.isTrue(point1 != null && point2 != null && point3 != null, "Three points are required to define a plane in 3D space");
		
		// Using the Plane equation ax+by+cz+d = 0
		// Expansion taken from http://local.wasp.uwa.edu.au/~pbourke/geometry/planeeq/
		double a = (point1.y * (point2.z - point3.z)) + (point2.y * (point3.z - point1.z)) + (point3.y * (point1.z - point2.z));
		double b = (point1.z * (point2.x - point3.x)) + (point2.z * (point3.x - point1.x)) + (point3.z * (point1.x - point2.x));
		double c = (point1.x * (point2.y - point3.y)) + (point2.x * (point3.y - point1.y)) + (point3.x * (point1.y - point2.y));
		double d = -((point1.x * ((point2.y * point3.z) - (point3.y * point2.z))) + (point2.x * ((point3.y * point1.z) - (point1.y * point3.z))) + (point3.x * ((point1.y * point2.z) - (point2.y * point1.z))));
		
		Vec4 normal = new Vec4(a, b, c).normalize3();
		
		// Distance calculation from http://mathworld.wolfram.com/Plane.html
		double distance = d / (Math.sqrt(a*a + b*b + c*c));
		
		return new Plane(normal.x, normal.y, normal.z, distance);
	}
	
}
