package gov.nasa.worldwind.custom.render;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.formats.models.ModelFactory;
import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;

import org.csiro.examples.model.Adjustable;

/**
 * @author R.Wathelet, most of the code is from RodgersGB Model3DLayer class
 * see https://joglutils.dev.java.net/
 * modified by eterna2
 * modified by R.Wathelet adding the Adjustable
 */
public class Movable3DModel implements Renderable, Movable, Adjustable {

    private Position position;
    private Model model;
    private double yaw = 0.0;
    private double roll = 0.0;
    private double pitch = 0.0;
    private boolean keepConstantSize = true;
    private Vec4 referenceCenterPoint;
    private Globe globe;
    private double size = 1;
    private boolean useArdor = false;

    public boolean isUseArdor() {
		return useArdor;
	}

	public void setUseArdor(boolean useArdor) {
		this.useArdor = useArdor;
	}

	public Movable3DModel(Model model, Position pos) {
        this.model = model;
        this.setPosition(pos);
        this.model.setUseLighting(false);
        this.model.setUseTexture(false);
    }

    public Movable3DModel(String path, Position pos) {
        try {
            this.model = ModelFactory.createModel(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setPosition(pos);
        this.model.setUseLighting(true);
        this.model.setUseTexture(true);
    }

    public Movable3DModel(String path, Position pos, double size) {
        this(path, pos);
        this.setSize(size);
    }

    public void render(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        try {
            beginDraw(dc);
            if (dc.isPickingMode()) {
                this.model.setRenderPicker(true);
            } else {
                this.model.setRenderPicker(false);
            }
            draw(dc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            endDraw(dc);
        }
    }

    protected void draw(DrawContext dc) {
        GL gl = dc.getGL();
        this.globe = dc.getGlobe();
        Vec4 loc = dc.getGlobe().computePointFromPosition(this.getPosition());
        double localSize = this.computeSize(dc, loc);
        this.computeReferenceCenter(dc);
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            dc.getView().pushReferenceCenter(dc, loc);
            gl.glRotated(position.getLongitude().degrees, 0, 1, 0);
            gl.glRotated(-position.getLatitude().degrees, 1, 0, 0);
            gl.glRotated(yaw, 0, 0, 1);
            gl.glRotated(pitch, 1, 0, 0);
            gl.glRotated(roll, 0, 1, 0);
            gl.glScaled(localSize, localSize, localSize);
            
            if(useArdor)
            {
            	//TODO: Make the Ardor Renderer work
                ArdorModelRenderer.getInstance().render(dc, this.getModel());
            }
            else
            {
            	DisplayListRenderer.getInstance().render(gl, this.getModel());
            }
            dc.getView().popReferenceCenter(dc);
        }
    }

    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL gl = dc.getGL();
        Vec4 cameraPosition = dc.getView().getEyePoint();
        gl.glPushAttrib(
                GL.GL_TEXTURE_BIT |
                GL.GL_COLOR_BUFFER_BIT |
                GL.GL_DEPTH_BUFFER_BIT |
                GL.GL_HINT_BIT |
                GL.GL_POLYGON_BIT |
                GL.GL_ENABLE_BIT |
                GL.GL_CURRENT_BIT |
                GL.GL_LIGHTING_BIT |
                GL.GL_TRANSFORM_BIT);
        //float[] lightPosition = {0F, 100000000f, 0f, 0f};
        float[] lightPosition = {(float) (cameraPosition.x + 1000), (float) (cameraPosition.y + 1000), (float) (cameraPosition.z + 1000), 1.0f};
        /** Ambient light array */
        float[] lightAmbient = {0.4f, 0.4f, 0.4f, 0.4f};
        /** Diffuse light array */
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        /** Specular light array */
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] model_ambient = {0.5f, 0.5f, 0.5f, 1.0f};
        gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, lightSpecular, 0);
        gl.glDisable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_LIGHT1);
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_NORMALIZE);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL gl = dc.getGL();
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }

    public Position getReferencePosition() {
        return this.getPosition();
    }

    public void move(Position delta) {
        if (delta == null) {
            String msg = Logging.getMessage("nullValue.PositionDeltaIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.moveTo(this.getReferencePosition().add(delta));
    }

    public void moveTo(Position position) {
        if (position == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Vec4 newRef = this.globe.computePointFromPosition(position);
        Angle distance = LatLon.greatCircleDistance(this.getPosition(), position);
        Vec4 axis = this.referenceCenterPoint.cross3(newRef).normalize3();
        Vec4 p = this.globe.computePointFromPosition(this.getPosition());
        p = p.transformBy3(Quaternion.fromAxisAngle(distance, axis));
        this.position = this.globe.computePositionFromPoint(p);
    }

    private void computeReferenceCenter(DrawContext dc) {
        this.referenceCenterPoint = this.computeTerrainPoint(dc,
                this.getPosition().getLatitude(), this.getPosition().getLongitude());
    }

    private Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon) {
        Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon);
        if (p == null) {
            p = dc.getGlobe().computePointFromPosition(lat, lon,
                    dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
        }
        return p;
    }

    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.keepConstantSize) {
            return size;
        }
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double newSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (newSize < 2) {
            newSize = 2;
        }
        return newSize;
    }

    public boolean isConstantSize() {
        return keepConstantSize;
    }

    public void setKeepConstantSize(boolean val) {
        this.keepConstantSize = val;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Model getModel() {
        return model;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double val) {
        this.yaw = val;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double val) {
        this.roll = val;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double val) {
        this.pitch = val;
    }
}
