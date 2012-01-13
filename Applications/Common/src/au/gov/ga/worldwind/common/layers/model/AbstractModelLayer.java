package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.WWTexture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

public abstract class AbstractModelLayer extends AbstractLayer implements ModelLayer
{
	protected final List<FastShape> shapes;
	protected WWTexture pointTexture;
	protected WWTexture blankTexture;

	protected boolean sectorDirty = true;
	protected Sector sector;

	protected Color color;
	protected Double lineWidth;
	protected Double pointSize;
	protected boolean wireframe = false;

	protected boolean pointSprite = false;
	protected Double pointMinSize = 2d;
	protected Double pointMaxSize = 1000d;
	protected Double pointConstantAttenuation = 0d;
	protected Double pointLinearAttenuation = 0d;
	protected Double pointQuadraticAttenuation = 6E-12d;

	protected final HierarchicalListenerList hierarchicalListenerList = new HierarchicalListenerList();
	protected final ModelLayerTreeNode treeNode = new ModelLayerTreeNode(this);

	public AbstractModelLayer(List<FastShape> shapes)
	{
		Validate.notNull(shapes, "Shapes cannot be null");
		this.shapes = shapes;

		for (FastShape shape : shapes)
		{
			treeNode.addChild(shape);
			shape.setWireframe(isWireframe());
		}
	}

	protected abstract void requestData();

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		requestData();

		synchronized (shapes)
		{
			if (shapes.isEmpty())
			{
				return;
			}

			GL gl = dc.getGL();
			try
			{
				gl.glPushAttrib(GL.GL_POINT_BIT | GL.GL_LINE_BIT);

				if (lineWidth != null)
				{
					gl.glLineWidth(lineWidth.floatValue());
				}
				if (pointSize != null)
				{
					gl.glPointSize(pointSize.floatValue());
				}
				if (pointMinSize != null)
				{
					gl.glPointParameterf(GL.GL_POINT_SIZE_MIN, pointMinSize.floatValue());
				}
				if (pointMaxSize != null)
				{
					gl.glPointParameterf(GL.GL_POINT_SIZE_MAX, pointMaxSize.floatValue());
				}
				if (pointConstantAttenuation != null || pointLinearAttenuation != null
						|| pointQuadraticAttenuation != null)
				{
					float ca = pointConstantAttenuation != null ? pointConstantAttenuation.floatValue() : 1f;
					float la = pointLinearAttenuation != null ? pointLinearAttenuation.floatValue() : 0f;
					float qa = pointQuadraticAttenuation != null ? pointQuadraticAttenuation.floatValue() : 0f;
					gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, new float[] { ca, la, qa }, 0);
				}

				//if rendering points, render them as sprites
				gl.glEnable(GL.GL_POINT_SMOOTH);
				gl.glEnable(GL.GL_POINT_SPRITE);

				for (FastShape shape : shapes)
				{
					if (color != null)
					{
						shape.setColor(color);
					}
					if (shape.getMode() == GL.GL_POINTS && pointSprite)
					{
						//shape will be rendered as points; setup point sprite texture
						try
						{
							gl.glPushAttrib(GL.GL_TEXTURE_BIT);

							if (pointTexture == null)
							{
								BufferedImage image = ImageIO.read(this.getClass().getResourceAsStream("sprite.png"));
								pointTexture = new BasicWWTexture(image, true);
								blankTexture = new BasicWWTexture(image, false);
							}

							float r = 1, g = 1, b = 1, alpha = (float) (getOpacity() * shape.getOpacity());
							if (shape.getColor() != null)
							{
								Color color = shape.getColor();
								r = color.getRed() / 255f;
								g = color.getGreen() / 255f;
								b = color.getBlue() / 255f;
							}

							//texture stage 0 (previous * texture)
							gl.glActiveTexture(GL.GL_TEXTURE0);
							gl.glEnable(GL.GL_TEXTURE_2D);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
							gl.glTexEnvi(GL.GL_POINT_SPRITE, GL.GL_COORD_REPLACE, GL.GL_TRUE);

							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);

							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_ALPHA, GL.GL_PREVIOUS);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC1_ALPHA, GL.GL_TEXTURE);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GL.GL_MODULATE);

							/*gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_ALPHA, GL.GL_TEXTURE);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC1_ALPHA, GL.GL_PREVIOUS);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_OPERAND1_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GL.GL_SUBTRACT);*/

							pointTexture.bind(dc);

							//texture stage 1 (previous * texture envionment color)
							gl.glActiveTexture(GL.GL_TEXTURE1);
							gl.glEnable(GL.GL_TEXTURE_2D);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);

							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC1_RGB, GL.GL_CONSTANT);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_MODULATE);

							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_ALPHA, GL.GL_PREVIOUS);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC1_ALPHA, GL.GL_CONSTANT);
							gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GL.GL_MODULATE);

							gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, new float[] { r, g, b, alpha }, 0);
							blankTexture.bind(dc);

							shape.render(dc);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							gl.glPopAttrib();
						}
					}
					else
					{
						shape.render(dc);
					}
				}
			}
			finally
			{
				gl.glPopAttrib();
			}
		}
	}

	@Override
	public Sector getSector()
	{
		synchronized (shapes)
		{
			if (sectorDirty)
			{
				sector = null;
				for (FastShape shape : shapes)
				{
					sector = Sector.union(sector, shape.getSector());
				}
				sectorDirty = false;
			}
		}
		return sector;
	}

	@Override
	public boolean isWireframe()
	{
		return wireframe;
	}

	@Override
	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
		synchronized (shapes)
		{
			for (FastShape shape : shapes)
			{
				shape.setWireframe(wireframe);
			}
		}
	}

	public boolean isPointSprite()
	{
		return pointSprite;
	}

	public void setPointSprite(boolean pointSprite)
	{
		this.pointSprite = pointSprite;
	}

	@Override
	public void setup(WorldWindow wwd)
	{
	}

	@Override
	public void addShape(FastShape shape)
	{
		synchronized (shapes)
		{
			shapes.add(shape);
		}
		shape.setWireframe(isWireframe());
		sectorDirty = true;
		treeNode.addChild(shape);
		hierarchicalListenerList.notifyListeners(this, treeNode);
	}

	@Override
	public void removeShape(FastShape shape)
	{
		synchronized (shapes)
		{
			shapes.remove(shape);
		}
		sectorDirty = true;
		treeNode.removeChild(shape);
		hierarchicalListenerList.notifyListeners(this, treeNode);
	}

	@Override
	public void addHierarchicalListener(HierarchicalListener listener)
	{
		hierarchicalListenerList.add(listener);
		hierarchicalListenerList.notifyListeners(this, treeNode);
	}

	@Override
	public void removeHierarchicalListener(HierarchicalListener listener)
	{
		hierarchicalListenerList.remove(listener);
	}
}
