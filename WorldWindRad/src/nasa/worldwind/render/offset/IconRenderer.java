/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.render.offset;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.Pedestal;
import gov.nasa.worldwind.render.ToolTipRenderer;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.util.Logging;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import javax.media.opengl.GL;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * @author tag
 * @version $Id: IconRenderer.java 5168 2008-04-24 21:32:51Z dcollins $
 */
public class IconRenderer
{
	private Pedestal pedestal;
	private PickSupport pickSupport = new PickSupport();
	private HashMap<Font, ToolTipRenderer> toolTipRenderers = new HashMap<Font, ToolTipRenderer>();

	public IconRenderer()
	{
	}

	public Pedestal getPedestal()
	{
		return pedestal;
	}

	public void setPedestal(Pedestal pedestal)
	{
		this.pedestal = pedestal;
	}

	private static boolean isIconValid(WWIcon icon, boolean checkPosition)
	{
		if (icon == null || icon.getImageSource() == null)
			return false;

		//noinspection RedundantIfStatement
		if (checkPosition && icon.getPosition() == null)
			return false;

		return true;
	}

	public void pick(DrawContext dc, Iterable<WWIcon> icons,
			java.awt.Point pickPoint, Layer layer)
	{
		this.drawMany(dc, icons);
	}

	public void pick(DrawContext dc, WWIcon icon, Vec4 iconPoint,
			java.awt.Point pickPoint, Layer layer)
	{
		if (!isIconValid(icon, false))
			return;

		this.drawOne(dc, icon, iconPoint);
	}

	public void render(DrawContext dc, Iterable<WWIcon> icons)
	{
		this.drawMany(dc, icons);
	}

	public void render(DrawContext dc, WWIcon icon, Vec4 iconPoint)
	{
		if (!isIconValid(icon, false))
			return;

		this.drawOne(dc, icon, iconPoint);
	}

	private void drawMany(DrawContext dc, Iterable<WWIcon> icons)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (dc.getVisibleSector() == null)
			return;

		SectorGeometryList geos = dc.getSurfaceGeometry();
		//noinspection RedundantIfStatement
		if (geos == null)
			return;

		if (icons == null)
		{
			String msg = Logging.getMessage("nullValue.IconIterator");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Iterator<WWIcon> iterator = icons.iterator();

		if (!iterator.hasNext())
			return;

		while (iterator.hasNext())
		{
			WWIcon icon = iterator.next();
			if (!isIconValid(icon, true))
				continue;

			if (!icon.isVisible())
				continue;

			// Determine Cartesian position from the surface geometry if the icon is near the surface,
			// otherwise draw it from the globe.
			Position pos = icon.getPosition();
			Vec4 iconPoint = null;
			if (pos.getElevation() < dc.getGlobe().getMaxElevation())
				iconPoint = dc.getSurfaceGeometry().getSurfacePoint(
						icon.getPosition());
			if (iconPoint == null)
				iconPoint = dc.getGlobe().computePointFromPosition(
						icon.getPosition());

			// The icons aren't drawn here, but added to the ordered queue to be drawn back-to-front.
			double eyeDistance = icon.isAlwaysOnTop() ? 0 : dc.getView()
					.getEyePoint().distanceTo3(iconPoint);
			dc.addOrderedRenderable(new OrderedIcon(icon, iconPoint,
					eyeDistance));

			if (icon.isShowToolTip())
				this.addToolTip(dc, icon, iconPoint);
		}
	}

	private void drawOne(DrawContext dc, WWIcon icon, Vec4 iconPoint)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		if (dc.getVisibleSector() == null)
			return;

		SectorGeometryList geos = dc.getSurfaceGeometry();
		//noinspection RedundantIfStatement
		if (geos == null)
			return;

		if (!icon.isVisible())
			return;

		if (iconPoint == null)
		{
			Angle lat = icon.getPosition().getLatitude();
			Angle lon = icon.getPosition().getLongitude();

			if (!dc.getVisibleSector().contains(lat, lon))
				return;

			iconPoint = dc.getSurfaceGeometry().getSurfacePoint(lat, lon,
					icon.getPosition().getElevation());
			if (iconPoint == null)
				return;
		}

		if (!dc.getView().getFrustumInModelCoordinates().contains(iconPoint))
			return;

		double horizon = dc.getView().computeHorizonDistance();
		double eyeDistance = icon.isAlwaysOnTop() ? 0 : dc.getView()
				.getEyePoint().distanceTo3(iconPoint);
		if (eyeDistance > horizon)
			return;

		// The icon isn't drawn here, but added to the ordered queue to be drawn back-to-front.
		dc.addOrderedRenderable(new OrderedIcon(icon, iconPoint, eyeDistance));

		if (icon.isShowToolTip())
			this.addToolTip(dc, icon, iconPoint);
	}

	private void addToolTip(DrawContext dc, WWIcon icon, Vec4 iconPoint)
	{
		if (icon.getToolTipFont() == null && icon.getToolTipText() == null)
			return;

		final Vec4 screenPoint = dc.getView().project(iconPoint);
		if (screenPoint == null)
			return;

		OrderedText tip = new OrderedText(icon.getToolTipText(), icon
				.getToolTipFont(), screenPoint, icon.getToolTipTextColor(), 0d);
		dc.addOrderedRenderable(tip);
	}

	private class OrderedText implements OrderedRenderable
	{
		Font font;
		String text;
		Vec4 point;
		double eyeDistance;
		java.awt.Point pickPoint;
		Layer layer;
		java.awt.Color color;

		OrderedText(String text, Font font, Vec4 point, java.awt.Color color,
				double eyeDistance)
		{
			this.text = text;
			this.font = font;
			this.point = point;
			this.eyeDistance = eyeDistance;
			this.color = color;
		}

		OrderedText(String text, Font font, Vec4 point,
				java.awt.Point pickPoint, Layer layer, double eyeDistance)
		{
			this.text = text;
			this.font = font;
			this.point = point;
			this.eyeDistance = eyeDistance;
			this.pickPoint = pickPoint;
			this.layer = layer;
		}

		public double getDistanceFromEye()
		{
			return this.eyeDistance;
		}

		public void render(DrawContext dc)
		{
			ToolTipRenderer tr = IconRenderer.this.toolTipRenderers
					.get(this.font);
			if (tr == null)
			{
				if (this.font != null)
				{
					TextRenderer textRenderer = new TextRenderer(this.font,
							true, true);
					textRenderer.setUseVertexArrays(false);
					tr = new ToolTipRenderer(textRenderer);
				}
				else
				{
					tr = new ToolTipRenderer();
				}
				IconRenderer.this.toolTipRenderers.put(this.font, tr);
			}

			Rectangle vp = dc.getView().getViewport();
			tr.setForeground(this.color);
			tr.setUseSystemLookAndFeel(this.color == null);
			tr.beginRendering(vp.width, vp.height);
			tr.draw(this.text, (int) point.x, (int) point.y);
			tr.endRendering();
		}

		public void pick(DrawContext dc, java.awt.Point pickPoint)
		{
		}
	}

	private class OrderedIcon implements OrderedRenderable, Locatable
	{
		WWIcon icon;
		Vec4 point;
		double eyeDistance;
		java.awt.Point pickPoint;
		Layer layer;

		OrderedIcon(WWIcon icon, Vec4 point, double eyeDistance)
		{
			this.icon = icon;
			this.point = point;
			this.eyeDistance = eyeDistance;
		}

		OrderedIcon(WWIcon icon, Vec4 point, java.awt.Point pickPoint,
				Layer layer, double eyeDistance)
		{
			this.icon = icon;
			this.point = point;
			this.eyeDistance = eyeDistance;
			this.pickPoint = pickPoint;
			this.layer = layer;
		}

		public double getDistanceFromEye()
		{
			return this.eyeDistance;
		}

		public Position getPosition()
		{
			return this.icon.getPosition();
		}

		public void render(DrawContext dc)
		{
			IconRenderer.this.beginDrawIcons(dc);

			try
			{
				IconRenderer.this.drawIcon(dc, this);
				// Draw as many as we can in a batch to save ogl state switching.
				while (dc.getOrderedRenderables().peek() instanceof OrderedIcon)
				{
					OrderedIcon oi = (OrderedIcon) dc.getOrderedRenderables()
							.poll();
					IconRenderer.this.drawIcon(dc, oi);
				}
			}
			catch (WWRuntimeException e)
			{
				Logging.logger().log(Level.SEVERE,
						"generic.ExceptionWhileRenderingIcon", e);
			}
			catch (Exception e)
			{
				Logging.logger().log(Level.SEVERE,
						"generic.ExceptionWhileRenderingIcon", e);
			}
			finally
			{
				IconRenderer.this.endDrawIcons(dc);
			}
		}

		public void pick(DrawContext dc, java.awt.Point pickPoint)
		{
			IconRenderer.this.pickSupport.clearPickList();
			IconRenderer.this.beginDrawIcons(dc);
			try
			{
				IconRenderer.this.drawIcon(dc, this);
				// TODO: Restore the batching below, but ensure that icons are individually selectable
				//                // Draw as many as we can in a batch to save ogl state switching.
				//                while (dc.getOrderedRenderables().peek() instanceof OrderedIcon)
				//                {
				//                    IconRenderer.this.drawIcon(dc, (OrderedIcon) dc.getOrderedRenderables().poll());
				//                }
			}
			catch (WWRuntimeException e)
			{
				Logging.logger().log(Level.SEVERE,
						"generic.ExceptionWhileRenderingIcon", e);
			}
			catch (Exception e)
			{
				Logging.logger().log(Level.SEVERE,
						"generic.ExceptionWhilePickingIcon", e);
			}
			finally
			{
				IconRenderer.this.endDrawIcons(dc);
				IconRenderer.this.pickSupport.resolvePick(dc, pickPoint, layer);
				IconRenderer.this.pickSupport.clearPickList(); // to ensure entries can be garbage collected
			}
		}
	}

	private void beginDrawIcons(DrawContext dc)
	{
		GL gl = dc.getGL();

		int attributeMask = GL.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
				| GL.GL_TRANSFORM_BIT // for modelview and perspective
				| GL.GL_VIEWPORT_BIT // for depth range
				| GL.GL_CURRENT_BIT // for current color
				| GL.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
				| GL.GL_TEXTURE_BIT // for texture env
				| GL.GL_DEPTH_BUFFER_BIT // for depth func
				| GL.GL_ENABLE_BIT; // for enable/disable changes
		gl.glPushAttrib(attributeMask);

		// Apply the depth buffer but don't change it.
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthMask(false);

		// Suppress any fully transparent image pixels
		gl.glEnable(GL.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL.GL_GREATER, 0.001f);

		// Load a parallel projection with dimensions (viewportWidth, viewportHeight)
		int[] viewport = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrtho(0d, viewport[2], 0d, viewport[3], -1d, 1d);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();

		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPushMatrix();

		if (dc.isPickingMode())
		{
			this.pickSupport.beginPicking(dc);

			// Set up to replace the non-transparent texture colors with the single pick color.
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
					GL.GL_COMBINE);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);
		}
		else
		{
			gl.glEnable(GL.GL_TEXTURE_2D);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
		}
	}

	private void endDrawIcons(DrawContext dc)
	{
		if (dc.isPickingMode())
			this.pickSupport.endPicking(dc);

		GL gl = dc.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glPopMatrix();

		gl.glPopAttrib();
	}

	private Vec4 drawIcon(DrawContext dc, OrderedIcon uIcon)
	{
		if (uIcon.point == null)
		{
			String msg = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().severe(msg);
			return null;
		}

		WWIcon icon = uIcon.icon;

		final Vec4 screenPoint = dc.getView().project(uIcon.point);
		if (screenPoint == null)
			return null;

		Texture iconTexture = dc.getTextureCache().get(icon.getImageSource());
		if (iconTexture == null)
			iconTexture = this.initializeTexture(dc, icon.getImageSource());

		double pedestalScale;
		double pedestalSpacing;
		Texture pedestalTexture = null;
		if (pedestal != null)
		{
			pedestalScale = this.pedestal.getScale();
			pedestalSpacing = pedestal.getSpacingPixels();

			pedestalTexture = dc.getTextureCache().get(pedestal.getPath()); // TODO: copy 'n paste bug?
			if (pedestalTexture == null)
				pedestalTexture = this.initializeTexture(dc, pedestal
						.getImageSource());
		}
		else
		{
			pedestalScale = 0d;
			pedestalSpacing = 0d;
		}

		javax.media.opengl.GL gl = dc.getGL();

		this.setDepthFunc(dc, uIcon, screenPoint);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		Dimension size = icon.getSize();
		double width = size != null ? size.getWidth() : iconTexture.getWidth();
		double height = size != null ? size.getHeight() : iconTexture
				.getHeight();
		gl.glTranslated(screenPoint.x, screenPoint.y - height
				+ (pedestalScale * height) + pedestalSpacing, 0d);

		if (icon.isHighlighted())
		{
			double heightDelta = this.pedestal != null ? 0 : height / 2; // expand only above the pedestal
			gl.glTranslated(width / 2, heightDelta, 0);
			gl.glScaled(icon.getHighlightScale(), icon.getHighlightScale(),
					icon.getHighlightScale());
			gl.glTranslated(-width / 2, -heightDelta, 0);
		}

		if (dc.isPickingMode())
		{
			java.awt.Color color = dc.getUniquePickColor();
			int colorCode = color.getRGB();
			this.pickSupport.addPickableObject(colorCode, icon, uIcon
					.getPosition(), false);
			gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(),
					(byte) color.getBlue());
		}

		if (icon.getBackgroundImage() != null)
			this.applyBackground(dc, icon, screenPoint, width, height,
					pedestalSpacing, pedestalScale);

		iconTexture.bind();
		TextureCoords texCoords = iconTexture.getImageTexCoords();
		gl.glScaled(width, height, 1d);
		dc.drawUnitQuad(texCoords);

		if (pedestalTexture != null)
		{
			gl.glLoadIdentity();
			gl.glTranslated(screenPoint.x - (pedestalScale * (width / 2)),
					screenPoint.y, 0d);
			gl.glScaled(width * pedestalScale, height * pedestalScale, 1d);

			pedestalTexture.bind();
			texCoords = pedestalTexture.getImageTexCoords();
			dc.drawUnitQuad(texCoords);
		}

		return screenPoint;
	}

	private void applyBackground(DrawContext dc, WWIcon icon, Vec4 screenPoint,
			double width, double height, double pedestalSpacing,
			double pedestalScale)
	{
		javax.media.opengl.GL gl = dc.getGL();

		Object backgroundImage;
		Texture backgroundTexture;
		double backgroundScale;
		backgroundImage = icon.getBackgroundImage();
		backgroundScale = icon.getBackgroundScale();
		backgroundTexture = dc.getTextureCache().get(backgroundImage);
		if (backgroundTexture == null)
			backgroundTexture = this.initializeTexture(dc, backgroundImage);

		if (backgroundTexture != null)
		{
			backgroundTexture.bind();
			TextureCoords texCoords = backgroundTexture.getImageTexCoords();
			gl.glPushMatrix();
			gl.glLoadIdentity();
			double bgwidth = backgroundScale * width;
			double bgheight = backgroundScale * height;
			// Offset the background for the highlighted scale.
			//if (icon.isHighlighted())
			//{
			//    gl.glTranslated(0d, height * (icon.getHighlightScale() - 1) / 2, 0d);
			//}
			// Offset the background for the pedestal height.
			gl.glTranslated(0d, (pedestalScale * height) + pedestalSpacing, 0d);
			// Place the background centered behind the icon.
			gl.glTranslated(screenPoint.x - bgwidth / 2, screenPoint.y
					- (bgheight - height) / 2, 0d);
			// Scale to the background image dimension.
			gl.glScaled(bgwidth, bgheight, 1d);
			dc.drawUnitQuad(texCoords);
			gl.glPopMatrix();
		}
	}

	private void setDepthFunc(DrawContext dc, OrderedIcon uIcon,
			Vec4 screenPoint)
	{
		GL gl = dc.getGL();

		if (uIcon.icon.isAlwaysOnTop())
		{
			gl.glDepthFunc(GL.GL_ALWAYS);
			return;
		}

		Position eyePos = dc.getView().getEyePosition();
		if (eyePos == null)
		{
			gl.glDepthFunc(GL.GL_ALWAYS);
			return;
		}

		double altitude = eyePos.getElevation();
		if (altitude < (dc.getGlobe().getMaxElevation() * dc
				.getVerticalExaggeration()))
		{
			double depth = screenPoint.z - (8d * 0.00048875809d);
			depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
			gl.glDepthFunc(GL.GL_LESS);
			gl.glDepthRange(depth, depth);
		}
		else if (screenPoint.z >= 1d)
		{
			gl.glDepthFunc(GL.GL_EQUAL);
			gl.glDepthRange(1d, 1d);
		}
		else
		{
			gl.glDepthFunc(GL.GL_ALWAYS);
		}
	}

	private Texture initializeTexture(DrawContext dc, Object imageSource)
	{
		try
		{
			Texture iconTexture = null;

			if (imageSource instanceof String)
			{
				String path = (String) imageSource;
				java.io.InputStream iconStream = this.getClass()
						.getResourceAsStream("/" + path);
				if (iconStream == null)
				{
					java.io.File iconFile = new java.io.File(path);
					if (iconFile.exists())
					{
						iconStream = new java.io.FileInputStream(iconFile);
					}
				}
				iconTexture = TextureIO.newTexture(iconStream, true, null);
			}
			else if (imageSource instanceof BufferedImage)
			{
				iconTexture = TextureIO.newTexture((BufferedImage) imageSource,
						true);
			}
			else
			{
				// TODO: Log case of unknown image-source type.
			}

			if (iconTexture == null)
			{
				// TODO: Log case.
				return null;
			}

			// Icons with the same path are assumed to be identical textures, so key the texture id off the path.
			dc.getTextureCache().put(imageSource, iconTexture);
			iconTexture.bind();

			GL gl = dc.getGL();
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
					GL.GL_MODULATE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER,
					GL.GL_LINEAR_MIPMAP_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER,
					GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S,
					GL.GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,
					GL.GL_CLAMP_TO_EDGE);

			return iconTexture;
		}
		catch (java.io.IOException e)
		{
			String msg = Logging
					.getMessage("generic.IOExceptionDuringTextureInitialization");
			Logging.logger().log(Level.SEVERE, msg, e);
			throw new WWRuntimeException(msg, e);
		}
	}

	@Override
	public String toString()
	{
		return Logging.getMessage("layers.IconLayer.Name");
	}
}
