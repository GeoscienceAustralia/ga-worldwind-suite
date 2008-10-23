/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package layers.radiometry;

import gov.nasa.worldwind.globes.EllipsoidalGlobe;

/**
 * @author Tom Gaskins
 * @version $Id: Earth.java 5214 2008-04-30 04:34:00Z tgaskins $
 */

public class GAGlobe extends EllipsoidalGlobe
{
    public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    public static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    public static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    public GAGlobe()
    {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES, new GAElevationModel());
    }

    public String toString()
    {
        return "Earth";
    }
}
