package au.gov.ga.worldwind.viewer.layers.screenoverlay;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;

import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import javax.swing.JLabel;

import au.gov.ga.worldwind.common.util.Util;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * A layer that can display html formatted text overlayed on the screen.
 */
public class ScreenOverlayLayer extends AbstractLayer
{
	
	private String text;
	
	private ScreenOverlayAttributes attributes;
	
	private TextOverlay overlay = new TextOverlay();
	
	protected PickSupport pickSupport = new PickSupport();
	
	/**
	 * The ordered renderable with eye distance 0 that will render the layer on top of
	 * most other layers
	 */
	protected class TextOverlay implements OrderedRenderable
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
	
	public void setText(String text)
	{
		this.text = text;
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
		if (Util.isBlank(text))
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
                | GL.GL_CURRENT_BIT);
            attribsPushed = true;

            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = 500;
            double height = 500;

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = Math.max(1, Math.max(width, height));
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            //gl.glTranslated(locationSW.x, locationSW.y, locationSW.z);
            //gl.glScaled(scale, scale, 1);

            if (!dc.isPickingMode())
            {
            	// TODO: Cache the texture
//                Texture iconTexture = dc.getTextureCache().get(this.getIconFilePath());
//                if (iconTexture == null)
//                {
//                    this.initializeTexture(dc);
//                    iconTexture = dc.getTextureCache().get(this.getIconFilePath());
//                    if (iconTexture == null)
//                    {
//                        // TODO: log warning
//                    }
//                }

            	Texture overlayTexture = TextureIO.newTexture(createImageFromText(text, (int)width, (int)height), false);
            	
                if (overlayTexture != null)
                {
                    gl.glEnable(GL.GL_TEXTURE_2D);
                    overlayTexture.bind();

                    gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                    gl.glEnable(GL.GL_BLEND);
                    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                    TextureCoords texCoords = overlayTexture.getImageTexCoords();
                    gl.glScaled(width, height, 1d);
                    dc.drawUnitQuad(texCoords);
                }
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
                gl.glPopAttrib();
        }
    }

	private BufferedImage createImageFromText(String text, int width, int height)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(JLabel.TOP);
		label.setVerticalTextPosition(JLabel.TOP);
		label.setSize(width, height);
		label.paint(image.getGraphics());
		
		return image;
	}
}
