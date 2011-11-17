package gov.nasa.worldwind.custom.render;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.formats.models.ModelFactory;
import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.formats.models.loader.ArdorColladaLoader;
import gov.nasa.worldwind.formats.models.loader.SimpleNamespaceContext;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicReference;

import javax.media.opengl.GL;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csiro.examples.model.Adjustable;
import org.xml.sax.InputSource;

import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.record.TextureStateRecord;
import com.ardor3d.renderer.state.record.TextureUnitRecord;
import com.ardor3d.scenegraph.Node;

public class Ardor3DModel 
	implements Renderable, Scene, Adjustable
{
	private static final Log LOG = LogFactory.getLog(Ardor3DModel.class);
	
	private Position position;
    protected Model model;
    private double yaw = 0.0;
    private double roll = 0.0;
    private double pitch = 0.0;
    private boolean keepConstantSize = true;
    private Vec4 referenceCenterPoint;
    private Globe globe;
    private double size = 1;
    
	private double scalex = 1.0;
	private double scaley = 1.0;
	private double scalez = 1.0;
    
    protected final AtomicReference<Node> nodeRef = new AtomicReference<Node>();
    private JoglCanvasRenderer renderer;
    
    private boolean visible = true;
    
	private double unitScale = 1.0;

	private String path;

    public Ardor3DModel(String path, Position pos) {
    	this.path = path;
        try {
            this.model = ModelFactory.createModel(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setPosition(pos);
        this.model.setUseLighting(true);
        this.model.setUseTexture(true);
    }
    
	@Override
	public void render(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        
        if (!this.isVisible())
            return;
        
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
	
	private void parseUnitScale() {
		XPathFactory xpFactory = XPathFactory.newInstance();
		XPath xpath = xpFactory.newXPath();

		SimpleNamespaceContext nsc = new SimpleNamespaceContext();

		xpath.setNamespaceContext(nsc);

		InputSource source;
		try {
			System.out.println("3D model file : " + path);
			source = new InputSource(new FileReader(path));

			String result = (String) xpath.evaluate(
					"//col:asset/col:unit/@meter", source,
					XPathConstants.STRING);
			if (result != null && result.length()>0) {
				System.out.println("3D model unitScale : " + result);
				unitScale = Double.parseDouble(result);
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    protected void draw(DrawContext dc) {
        GL gl = dc.getGL();
        this.globe = dc.getGlobe();
        this.referenceCenterPoint = this.computeReferenceCenter(dc);
        Vec4 loc = referenceCenterPoint;
        	//dc.getGlobe().computePointFromPosition(this.getPosition());
        double localSize = this.computeSize(dc, loc) * unitScale;
        
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
            dc.getView().pushReferenceCenter(dc, loc);
            gl.glRotated(position.getLongitude().degrees, 0, 1, 0);
            gl.glRotated(-position.getLatitude().degrees, 1, 0, 0);
    		gl.glRotated(pitch, 1, 0, 0);
    		gl.glRotated(roll, 0, 1, 0);
    		gl.glRotated(-yaw, 0, 0, 1);
    		gl.glScaled(localSize*scalex, localSize*scaley, localSize*scalez);
            
            drawArdor(dc);
            dc.getView().popReferenceCenter(dc);
        }
    }
    
    private boolean requestedLoad = false;
    
    private void drawArdor(DrawContext dc) {
    	ArdorColladaLoader.initializeArdorSystem(dc);
    	
    	Node node = this.nodeRef.get();
    	
    	if (node == null && !requestedLoad) {
            if (!WorldWind.getTaskService().isFull()) {
            	initialize(dc, model);  //set the local variable node
            	WorldWind.getTaskService().addTask(new LoadModelTask(model.getSource()));
            	requestedLoad = true;
            } else {
            	LOG.warn("Task queue is full, delay model load");
            }            
        } 
    	
    	if (node != null) {

            GL gl = dc.getGL();

            gl.glMatrixMode(GL.GL_MODELVIEW);

            gl.glPushMatrix();

            gl.glPushAttrib(
                    GL.GL_TEXTURE_BIT |
                    GL.GL_LIGHTING_BIT);

			if (model.isUsingTexture()) {
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glEnable(GL.GL_BLEND);
                gl.glEnable(GL.GL_RESCALE_NORMAL);
            } else {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glDisable(GL.GL_BLEND);
            }
			
	        final RenderContext context = ContextManager.getCurrentContext();
	        final ContextCapabilities caps = context.getCapabilities();
	        
	        {
		        final TextureStateRecord record = (TextureStateRecord) context.getStateRecord(StateType.Texture);
		        
		        for (int i = 0; i < caps.getNumberOfTotalTextureUnits(); i++) {
	                TextureUnitRecord unitRecord = record.units[i];
	                unitRecord.invalidate();
		        }
	        }
	        
            node.draw(renderer.getRenderer());
           	renderer.getRenderer().renderBuckets();

            gl.glPopAttrib();
            gl.glPopMatrix();
           
        }
	}
    
    protected void loadModel() {
    	try {
			Node node = ArdorColladaLoader.loadColladaModel(model.getSource());
			nodeRef.set(node);
		} catch (Exception e) {
			LOG.error("Failed to load model", e);
		}
    }
    
    private class LoadModelTask 
    	implements Runnable {
    	
    	private final String source;
    	
    	LoadModelTask(String source) {
    		this.source = source;
    	}
    	
    	@Override
    	public void run() {
    		loadModel();
    	}    	
    }
    	
    
	private void initialize(DrawContext dc, Model model) {
        try {
            renderer = new JoglCanvasRenderer(this) {
                JoglRenderer joglRenderer = new JoglRenderer();
                @Override
                public Renderer getRenderer() {
                    return joglRenderer;
                }
            };

            TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
        } catch (Exception e) {
            LOG.error("Failed to load model", e);
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
                GL.GL_TRANSFORM_BIT |
                GL.GL_CLIENT_VERTEX_ARRAY_BIT);
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
    
    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.keepConstantSize) {
            return size;
        }
        if (loc == null) {
            LOG.error("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double newSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (newSize < 2) {
            newSize = 2;
        }
        return newSize;
    }
    
    protected Vec4 computeReferenceCenter(DrawContext dc) {
    	return dc.getGlobe().computePointFromPosition(position);
        //return this.computeTerrainPoint(dc,
        //        this.getPosition().getLatitude(), this.getPosition().getLongitude());
    }

    protected final Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon) {
        Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon);
        if (p == null) {
            p = dc.getGlobe().computePointFromPosition(lat, lon,
                    dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
        }
        return p;
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
    
    public boolean isVisible() {
    	return this.visible;
    }
    
    public void setVisible(boolean v) {
    	this.visible = v;;
    }

	@Override
	public boolean renderUnto(Renderer renderer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PickResults doPick(Ray3 pickRay) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getScalex() {
		return scalex;
	}

	public void setScalex(double scalex) {
		this.scalex = scalex;
	}

	public double getScaley() {
		return scaley;
	}

	public void setScaley(double scaley) {
		this.scaley = scaley;
	}

	public double getScalez() {
		return scalez;
	}

	public void setScalez(double scalez) {
		this.scalez = scalez;
	}
}
