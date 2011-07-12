package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.Logging;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.Validate;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * A layer that can display html formatted text and images overlayed on the screen.
 */
public class ScreenOverlayLayer extends AbstractLayer
{
	private ScreenOverlayAttributes attributes;
	
	private ScreenOverlay overlay = new ScreenOverlay();
	
	/**
	 * Create a new {@link ScreenOverlayLayer} with the given source data and the
	 * default attribute values
	 */
	public ScreenOverlayLayer(URL sourceUrl)
	{
		Validate.notNull(sourceUrl, "Source data URL is required");
		this.attributes = new MutableScreenOverlayAttributesImpl(sourceUrl);
	}
	
	/**
	 * Create a new {@link ScreenOverlayLayer} with the given overlay attributes
	 */
	public ScreenOverlayLayer(ScreenOverlayAttributes attributes)
	{
		Validate.notNull(attributes, "Overlay attributes are required");
		this.attributes = attributes;
	}
	
	/**
	 * Create a new {@link ScreenOverlayLayer} with parameters provided in the given {@link AVList}
	 */
	public ScreenOverlayLayer(AVList params)
	{
		Validate.notNull(params, "Initialisation parameters are required");
		this.attributes = new MutableScreenOverlayAttributesImpl(params);
	}
	
	/**
	 * The ordered renderable with eye distance 0 that will render the layer on top of
	 * most other layers
	 */
	protected class ScreenOverlay implements OrderedRenderable
	{
		@Override
		public double getDistanceFromEye()
		{
			return 0;
		}

		@Override
		public void render(DrawContext dc)
		{
			ScreenOverlayLayer.this.draw(dc);
		}
		
		@Override
		public void pick(DrawContext dc, Point pickPoint)
		{
			ScreenOverlayLayer.this.draw(dc);
		}
		
	}
	
	public void setAttributes(ScreenOverlayAttributes attributes)
	{
		Validate.notNull(attributes, "Attributes are required");
		this.attributes = attributes;
	}
	
	public ScreenOverlayAttributes getAttributes()
	{
		return this.attributes;
	}
	
	@Override
	protected void doRender(DrawContext dc)
    {
        dc.addOrderedRenderable(this.overlay);
    }

	@Override
    protected void doPick(DrawContext dc, Point pickPoint)
    {
        dc.addOrderedRenderable(this.overlay);
    }
	
	protected void draw(DrawContext dc)
    {
		if (attributes == null)
		{
			return;
		}
		
		GL gl = dc.getGL();

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_COLOR_BUFFER_BIT
                | GL.GL_ENABLE_BIT
                | GL.GL_TEXTURE_BIT
                | GL.GL_TRANSFORM_BIT
                | GL.GL_VIEWPORT_BIT
                | GL.GL_CURRENT_BIT
                | GL.GL_LINE_BIT);
            attribsPushed = true;

            gl.glDisable(GL.GL_DEPTH_TEST);

            Rectangle viewport = dc.getView().getViewport();
            Rectangle overlay = new Rectangle((int)attributes.getWidth(viewport.width), (int)attributes.getHeight(viewport.height));

            // Parallel projection 
            gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = Math.max(1, Math.max(overlay.width, overlay.height));
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            // Translate to the correct position
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();
            Vec4 location = computeLocation(viewport, overlay);
            gl.glTranslated(location.x, location.y, location.z);

            if (!dc.isPickingMode())
            {
            	if (attributes.isDrawBorder())
            	{
            		drawBorder(dc, overlay);
            	}
            	
            	drawOverlay(dc, overlay);
            }
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
            {
                gl.glPopAttrib();
            }
        }
    }

	private void drawBorder(DrawContext dc, Rectangle overlay)
	{
		DoubleBuffer buffer = null; 
		buffer = FrameFactory.createShapeBuffer(FrameFactory.SHAPE_RECTANGLE, 
									  			(overlay.width + attributes.getBorderWidth() * 2), 
									  			(overlay.height + attributes.getBorderWidth() * 2), 
									  			0, buffer);
		GL gl = dc.getGL();
		
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		
		gl.glLineWidth((float)attributes.getBorderWidth());
		
		gl.glEnable(GL.GL_BLEND);
		float[] compArray = new float[4];
		attributes.getBorderColor().getRGBComponents(compArray);
		gl.glColor4fv(compArray, 0);
		
		gl.glTranslated(-attributes.getBorderWidth()/2, -attributes.getBorderWidth()/2, 0);
		FrameFactory.drawBuffer(dc, GL.GL_LINE_STRIP, buffer.remaining() / 2, buffer);
		gl.glTranslated(attributes.getBorderWidth(), attributes.getBorderWidth(), 0);
	}
	
	private void drawOverlay(DrawContext dc, Rectangle overlay)
	{
		Texture overlayTexture = getTexture(dc, overlay);
		if (overlayTexture != null)
		{
			GL gl = dc.getGL();
		    gl.glEnable(GL.GL_TEXTURE_2D);
		    overlayTexture.bind();

		    gl.glColor4d(1d, 1d, 1d, this.getOpacity());
		    gl.glEnable(GL.GL_BLEND);
		    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		    TextureCoords texCoords = overlayTexture.getImageTexCoords();
		    gl.glScaled(overlay.width, overlay.height, 1d);
		    dc.drawUnitQuad(texCoords);
		}
	}

	private Texture getTexture(DrawContext dc, Rectangle overlay)
	{
		try
		{
			Texture texture = dc.getTextureCache().get(attributes.getSourceId());
			
			if (texture == null || texture.getImageHeight() != overlay.height || texture.getImageWidth() != overlay.width)
			{
				BufferedImage htmlImage = HtmlToImage.createImageFromHtml(attributes.getSourceUrl(), overlay.width, overlay.height);
				texture = TextureIO.newTexture(htmlImage, false);
				dc.getTextureCache().put(attributes.getSourceId(), texture);
				
				GL gl = dc.getGL();
		        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		        int[] maxAnisotropy = new int[1];
		        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
		        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
			}
			
			return texture;
		}
		catch (Exception e)
		{
			String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg, e);
		}
	}
	
	private Vec4 computeLocation(Rectangle viewport, Rectangle overlay)
	{
		double x = 0d;
        double y = 0d;
        
        switch (attributes.getPosition())
        {
        	case CENTRE:
        	{
        		x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
        		y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
        		break;
        	}
        	
        	case NORTH:
        	{
        		x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
        		y = viewport.height - overlay.height - attributes.getBorderWidth();
        		break;
        	}
        	
        	case NORTHEAST:
        	{
        		x = viewport.width - overlay.width - attributes.getBorderWidth();
        		y = viewport.height - overlay.height - attributes.getBorderWidth();
        		break;
        	}
        	
        	case EAST:
        	{
        		x = viewport.width - overlay.width - attributes.getBorderWidth();
        		y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
        		break;
        	}
        	
        	case SOUTHEAST:
        	{
        		x = viewport.width - overlay.width - attributes.getBorderWidth();
        		y = attributes.getBorderWidth();
        		break;
        	}
        	
        	case SOUTH:
        	{
        		x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
        		y = attributes.getBorderWidth();
        		break;
        	}
        	
        	case SOUTHWEST:
        	{
        		x = attributes.getBorderWidth();
        		y = attributes.getBorderWidth();
        		break;
        	}
        	
        	case WEST:
        	{
        		x = attributes.getBorderWidth();
        		y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
        		break;
        	}
        	
        	case NORTHWEST:
        	{
        		x = attributes.getBorderWidth();
        		y = viewport.height - overlay.height - attributes.getBorderWidth();
        		break;
        	}
        }
		
        return new Vec4(x, y, 0);
	}
}
