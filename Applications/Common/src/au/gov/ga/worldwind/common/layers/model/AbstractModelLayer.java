package au.gov.ga.worldwind.common.layers.model;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Color;
import java.util.List;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.FastShape;
import au.gov.ga.worldwind.common.util.Validate;

public abstract class AbstractModelLayer extends AbstractLayer implements ModelLayer
{
	protected final List<FastShape> shapes;

	protected boolean sectorDirty = true;
	protected Sector sector;

	protected Color color;
	protected Double lineWidth;
	protected Double pointSize;
	protected boolean wireframe = false;

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

				if (pointSize != null)
				{
					gl.glPointSize(pointSize.floatValue());
				}
				if (lineWidth != null)
				{
					gl.glLineWidth(lineWidth.floatValue());
				}
				for (FastShape shape : shapes)
				{
					if (color != null)
					{
						shape.setColor(color);
					}
					shape.render(dc);
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
