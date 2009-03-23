/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.layers;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

/**
 * Renders an atmosphere around the globe and a sky dome at low altitude.
 * <p>
 * Ported from my WW plugin SkyGradient and from WW2DPlusOne.
 * </p>
 * Note : based on a spherical globe.<br />
 * Issue : Ellipsoidal globe doesnt match the spherical atmosphere everywhere.
 *
 * @author Patrick Murris
 * @version $Id: SkyGradientLayer.java 5897 2008-08-09 05:20:21Z tgaskins $
 */
public class SkyGradientLayer extends AbstractLayer
{
    private final static int STACKS = 12;
    private final static int SLICES = 64;

    protected int glListId = -1;        // GL list id
    // TODO: make configurable
    protected double thickness = 100e3; // Atmosphere thickness
    protected float[] horizonColor = new float[] { 0.66f, 0.70f, 0.81f, 1.0f }; // horizon color (same as fog)
    protected float[] zenithColor = new float[]{0.26f, 0.47f, 0.83f, 1.0f}; // zenith color
    protected double lastRebuildHorizon = 0;

    /**
     * Renders an atmosphere around the globe
     */
    public SkyGradientLayer() {
    }

    /**
     * Get the atmosphere thickness in meter
     * @return the atmosphere thickness in meter
     */
    public double getAtmosphereThickness()
    {
        return this.thickness;
    }

    /**
     * Set the atmosphere thickness in meter
     * @param thickness the atmosphere thickness in meter
     */
    public void setAtmosphereThickness(double thickness)
    {
        if (thickness < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.thickness = thickness;
    }

    /**
     * Get the horizon color
     * @return the horizon color
     */
    public Color getHorizonColor()
    {
        return new Color(this.horizonColor[0], this.horizonColor[1], this.horizonColor[2], this.horizonColor[3]);
    }

    /**
     * Set the horizon color
     * @param color the horizon color
     */
    public void setHorizonColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        color.getComponents(this.horizonColor);
    }

    /**
     * Get the zenith color
     * @return the zenith color
     */
    public Color getZenithColor()
    {
        return new Color(this.zenithColor[0], this.zenithColor[1], this.zenithColor[2], this.zenithColor[3]);
    }

    /**
     * Set the zenith color
     * @param color the zenith color
     */
    public void setZenithColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        color.getComponents(this.zenithColor);
    }

    @Override
    public void doRender(DrawContext dc)
    {
        GL gl = dc.getGL();
        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try {
            View view = dc.getView();
            Position camPos = dc.getGlobe().computePositionFromPoint(view.getEyePoint());
            double worldRadius = dc.getGlobe().getRadiusAt(camPos);
            double distToCenterOfPlanet = view.getEyePoint().getLength3();
            double camAlt = camPos.getElevation();
            double tangentalDistance = view.computeHorizonDistance();
            // Dome radius
            double domeRadius = tangentalDistance;

            // horizon latitude degrees
            double horizonLat = (-Math.PI / 2 + Math.acos(tangentalDistance / distToCenterOfPlanet))
                    * 180 / Math.PI;
            // zenith latitude degrees
            double zenithLat = 90;
            if (camAlt >= thickness) {
                double tangentalDistanceZenith = Math.sqrt(distToCenterOfPlanet * distToCenterOfPlanet
                        - (worldRadius + thickness) * (worldRadius + thickness));
                zenithLat = (-Math.PI / 2 + Math.acos(tangentalDistanceZenith / distToCenterOfPlanet)) * 180 / Math.PI;
            }
            if (camAlt < thickness && camAlt > thickness * 0.7) {
                zenithLat = (thickness - camAlt) / (thickness - thickness * 0.7) * 90;
            }

            // Build or rebuild sky dome if horizon distance changed more then 100m
            // Note: increasing this threshold may produce artefacts like far clipping at very low altitude
             if (this.glListId == -1 || Math.abs(this.lastRebuildHorizon - tangentalDistance) > 100)
             {
                 if (this.glListId != -1)
                     gl.glDeleteLists(this.glListId, 1);

                 this.makeSkyDome(dc, (float) (domeRadius), horizonLat, zenithLat, SLICES, STACKS);
                 this.lastRebuildHorizon = tangentalDistance;
             }

            // GL set up
            gl.glPushAttrib(GL.GL_POLYGON_BIT); // Temporary hack around aliased sky.
            gl.glPopAttrib();
            
            gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_TRANSFORM_BIT
                    | GL.GL_POLYGON_BIT | GL.GL_TEXTURE_BIT | GL.GL_ENABLE_BIT
                    | GL.GL_CURRENT_BIT);
            attribsPushed = true;
            gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);

            Matrix projection = Matrix.fromPerspective(view.getFieldOfView(),
                            view.getViewport().getWidth(), view.getViewport().getHeight(),
                            100, domeRadius + 10e3);
            double[] matrixArray = new double[16];
            projection.toArray(matrixArray, 0, false);
            gl.glMatrixMode(GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadMatrixd(matrixArray, 0);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            //gl.glLoadIdentity();
            //view.pushReferenceCenter(dc, Vec4.ZERO);
            // Place sky - TODO: find another ellipsoid friendlier way (the sky dome is not exactly normal...
            // to the ground at higher latitude)
            Vec4 camPoint = view.getEyePoint();
            Vec4 camPosFromPoint = CartesianToSpherical(camPoint.x, camPoint.y, camPoint.z);
            gl.glRotatef((float) (Angle.fromRadians(camPosFromPoint.z).degrees), 0.0f, 1.0f, 0.0f);
            gl.glRotatef((float) (-Angle.fromRadians(camPosFromPoint.y).degrees + 90), 1.0f, 0.0f, 0.0f);
            // Place dome 4km below normal level to better fill below sea landscape background.
            gl.glTranslatef(0.0f, (float) (distToCenterOfPlanet - 4e3f), 0.0f);

             // Draw sky
             if (this.glListId != -1)
                 gl.glCallList(this.glListId);

            //view.popReferenceCenter(dc);
        }
        finally {
            // Restore GL state
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    /**
     * Build sky dome and draw into a glList
     *
     * @param dc the current DrawContext
     * @param radius the sky dome radius in meters.
     * @param startLat the horizon latitude in decimal degrees.
     * @param endLat the zenith latitude in decimal degrees.
     * @param slices the number of longitude divisions used for the dome geometry.
     * @param stacks the number of latitude divisions used for the dome geometry.
     */
    private void makeSkyDome(DrawContext dc, float radius, double startLat, double endLat,
                                int slices, int stacks)
    {
        GL gl = dc.getGL();
        this.glListId = gl.glGenLists(1);
        gl.glNewList(this.glListId, GL.GL_COMPILE);
        this.drawSkyGradient(dc, radius, startLat, endLat, slices, stacks);
        gl.glEndList();
    }

    /**
     * Draws the sky dome
     *
     * @param dc       the current DrawContext
     * @param radius   the sky dome radius
     * @param startLat the horizon latitude
     * @param endLat   the zenith latitude
     * @param slices   the number of slices - vertical divisions
     * @param stacks   the nuber os stacks - horizontal divisions
     */
    private void drawSkyGradient(DrawContext dc, float radius, double startLat, double endLat,
                                int slices, int stacks) {
        double latitude, longitude, latitudeTop = endLat;

        // GL setup
        GL gl = dc.getGL();
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);
        gl.glDisable(GL.GL_TEXTURE_2D);
        //gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);    // wireframe

        // TODO: Simplify code
        double linear, linearTop, k, kTop, colorFactorZ, colorFactorZTop = 0;
        double colorFactorH, colorFactorHTop = 0, alphaFactor, alphaFactorTop = 0;

        // bottom fade
        latitude = startLat - Math.max((endLat - startLat) / 4, 2);
        gl.glBegin(GL.GL_QUAD_STRIP);
        for (int slice = 0; slice <= slices; slice++) {
            longitude = 180 - ((float) slice / slices * (float) 360);
            Vec4 v = SphericalToCartesian(latitude, longitude, radius);
            gl.glColor4d(zenithColor[0], zenithColor[1], zenithColor[2], 0);
            gl.glVertex3d(v.getX(), v.getY(), v.getZ());
            v = SphericalToCartesian(startLat, longitude, radius);
            gl.glColor4d(horizonColor[0], horizonColor[1], horizonColor[2], .9 * horizonColor[3]);
            gl.glVertex3d(v.getX(), v.getY(), v.getZ());
        }
        gl.glEnd();

        // stacks and slices
        for (int stack = 1; stack < stacks - 1; stack++) {
            // bottom vertex
            linear = (float) (stack - 1) / (stacks - 1f);
            k = 1 - Math.cos(linear * Math.PI / 2);
            latitude = startLat + Math.pow(k, 3) * (endLat - startLat);
            colorFactorZ = linear;                        // coef zenith color
            colorFactorH = 1 - colorFactorZ;              // coef horizon color
            alphaFactor = 1 - Math.pow(linear, 4);        // coef alpha transparency
            if (alphaFactor > .9) alphaFactor = .9f;
            // top vertex
            linearTop = (float) (stack) / (stacks - 1f);
            kTop = 1 - Math.cos(linearTop * Math.PI / 2);
            latitudeTop = startLat + Math.pow(kTop, 3) * (endLat - startLat);
            colorFactorZTop = linearTop;                      // coef zenith color
            colorFactorHTop = 1 - colorFactorZTop;            // coef horizon color
            alphaFactorTop = 1 - Math.pow(linearTop, 4);      // coef alpha transparency
            if (alphaFactorTop > .9) alphaFactorTop = .9f;
            // Draw stack
            gl.glBegin(GL.GL_QUAD_STRIP);
            for (int slice = 0; slice <= slices; slice++) {
                longitude = 180 - ((float) slice / slices * (float) 360);
                Vec4 v = SphericalToCartesian(latitude, longitude, radius);
                gl.glColor4d(
                        (horizonColor[0] * colorFactorH + zenithColor[0] * colorFactorZ),
                        (horizonColor[1] * colorFactorH + zenithColor[1] * colorFactorZ),
                        (horizonColor[2] * colorFactorH + zenithColor[2] * colorFactorZ),
                        (horizonColor[3] * colorFactorH + zenithColor[3] * colorFactorZ) * alphaFactor);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                v = SphericalToCartesian(latitudeTop, longitude, radius);
                gl.glColor4d(
                        (horizonColor[0] * colorFactorHTop + zenithColor[0] * colorFactorZTop),
                        (horizonColor[1] * colorFactorHTop + zenithColor[1] * colorFactorZTop),
                        (horizonColor[2] * colorFactorHTop + zenithColor[2] * colorFactorZTop),
                        (horizonColor[3] * colorFactorHTop + zenithColor[3] * colorFactorZTop) * alphaFactorTop);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
            }
            gl.glEnd();
        }

        // Top fade
        if (endLat < 90) {
            gl.glBegin(GL.GL_QUAD_STRIP);
            for (int slice = 0; slice <= slices; slice++) {
                longitude = 180 - ((float) slice / slices * (float) 360);
                Vec4 v = SphericalToCartesian(latitudeTop, longitude, radius);
                gl.glColor4d(
                        (horizonColor[0] * colorFactorHTop + zenithColor[0] * colorFactorZTop),
                        (horizonColor[1] * colorFactorHTop + zenithColor[1] * colorFactorZTop),
                        (horizonColor[2] * colorFactorHTop + zenithColor[2] * colorFactorZTop),
                        (horizonColor[3] * colorFactorHTop + zenithColor[3] * colorFactorZTop) * alphaFactorTop);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
                v = SphericalToCartesian(endLat, longitude, radius);
                gl.glColor4d(zenithColor[0], zenithColor[1], zenithColor[2], 0);
                gl.glVertex3d(v.getX(), v.getY(), v.getZ());
            }
            gl.glEnd();
        }

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_BLEND);
    }

    /**
     * Draws the positive three axes - x is red, y is green and z is blue
     *
     * @param dc     the current DrawContext
     * @param length the lenght of the axes lines
     */
//    private static void DrawAxis(DrawContext dc, float length) {
//        GL gl = dc.getGL();
//        gl.glBegin(GL.GL_LINES);
//
//        // Draw 3 axis
//        gl.glColor3f(0f, 0f, 1f);  // Z Blue
//        gl.glVertex3d(0d, 0d, 0d);
//        gl.glVertex3d(0d, 0d, length);
//        gl.glColor3f(0f, 1f, 0f);  // Y Green
//        gl.glVertex3d(0d, 0d, 0d);
//        gl.glVertex3d(0d, length, 0d);
//        gl.glColor3f(1f, 0f, 0f);  // X Red
//        gl.glVertex3d(0d, 0d, 0d);
//        gl.glVertex3d(length, 0d, 0d);
//
//        gl.glEnd();
//    }

    /**
     * Converts position in spherical coordinates (lat/lon/altitude)
     * to cartesian (XYZ) coordinates.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param radius    Radius
     * @return the corresponding Point
     */
    private static Vec4 SphericalToCartesian(double latitude, double longitude, double radius) {
        latitude *= Math.PI / 180.0f;
        longitude *= Math.PI / 180.0f;

        double radCosLat = radius * Math.cos(latitude);

        return new Vec4(
                radCosLat * Math.sin(longitude),
                radius * Math.sin(latitude),
                radCosLat * Math.cos(longitude));
    }

    /**
     * Converts position in cartesian coordinates (XYZ)
     * to spherical (radius, lat, lon) coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return a <code>Vec4</code> point for the spherical coordinates {radius, lat, lon}
     */
    private static Vec4 CartesianToSpherical(double x, double y, double z) {
        double rho = Math.sqrt(x * x + y * y + z * z);
        double longitude = Math.atan2(x, z);
        double latitude = Math.asin(y / rho);

        return new Vec4(rho, latitude, longitude);
    }

    public void dispose()
    {
        if (this.glListId < 0)
            return;

        GLContext glc = GLContext.getCurrent();
        if (glc == null)
            return;

        glc.getGL().glDeleteLists(this.glListId, 1);
        this.glListId = -1;
    }

    @Override
    public String toString() {
        return Logging.getMessage("layers.Earth.SkyGradientLayer.Name");
    }
}