package gov.nasa.worldwind.custom.render;

import gov.nasa.worldwind.formats.models.iModel3DRenderer;
import gov.nasa.worldwind.formats.models.geometry.Model;
import gov.nasa.worldwind.formats.models.loader.ArdorColladaLoader;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.scenegraph.Node;

public class ArdorModelRenderer implements iModel3DRenderer, Scene{

	private JoglCanvasRenderer renderer;
	private Node node;
	private static ArdorModelRenderer instance = new ArdorModelRenderer();

	@Override
	public void debug(boolean value) {
		// TODO Auto-generated method stub
		
	}
	
	public static ArdorModelRenderer getInstance()
	{
		return instance;
	}

	@Override
	public void render(Object context, Model model) {
		// TODO Auto-generated method stub
		if (context instanceof DrawContext)
		{
			if (model != null)
			{
				renderArdorModel((DrawContext)context, model);
			}	
		}
	}
	
	private void initialize(DrawContext dc, Model model) {
        try {
            node = ArdorColladaLoader.loadColladaModel(model.getSource());
            renderer = new JoglCanvasRenderer(this) {
                JoglRenderer joglRenderer = new JoglRenderer();
                @Override
                public Renderer getRenderer() {
                    return joglRenderer;
                }
            };

            TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void renderArdorModel(DrawContext dc, Model model)
	{
        ArdorColladaLoader.initializeArdorSystem(dc);

        if (node == null) {
            initialize(dc, model);  //set the local variable node
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

            node.draw(renderer.getRenderer());
            renderer.getRenderer().renderBuckets();

            gl.glPopAttrib();
            gl.glPopMatrix();
        }
	}

	@Override
	public PickResults doPick(Ray3 arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean renderUnto(Renderer arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
