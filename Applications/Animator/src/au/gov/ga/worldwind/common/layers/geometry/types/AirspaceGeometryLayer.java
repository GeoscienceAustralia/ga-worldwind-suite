package au.gov.ga.worldwind.common.layers.geometry.types;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.Curtain;
import gov.nasa.worldwind.render.airspaces.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.layers.geometry.GeometryLayer;
import au.gov.ga.worldwind.common.layers.geometry.Shape;

/**
 * An implementation of the {@link GeometryLayer} interface that renders each
 * shape as a separate {@link Airspace}.
 */
public class AirspaceGeometryLayer extends GeometryLayerBase implements GeometryLayer
{
	private List<AirspaceShape> airspaceShapes = new ArrayList<AirspaceShape>();
	
	public AirspaceGeometryLayer(AVList params)
	{
		super(params);
	}

	@Override
	public Iterable<? extends Shape> getShapes()
	{
		return airspaceShapes;
	}

	@Override
	public void addShape(Shape shape)
	{
		if (shape == null)
		{
			return;
		}
		airspaceShapes.add(new AirspaceShape(shape));
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		for (AirspaceShape airspaceShape :  airspaceShapes)
		{
			airspaceShape.render(dc);
		}
	}

	/**
	 * A container class that associates a {@link Shape} with a renderable {@link Airspace}.
	 */
	private static class AirspaceShape implements Shape, Renderable
	{
		private AtomicBoolean dirty = new AtomicBoolean(true);
		private Airspace airspace;
		private Shape shapeDelegate;
		
		public AirspaceShape(Shape shape)
		{
			Validate.notNull(shape, "A shape is required");
			this.shapeDelegate = shape;
		}
		
		@Override
		public String getId()
		{
			return shapeDelegate.getId();
		}

		@Override
		public List<? extends ShapePoint> getPoints()
		{
			return shapeDelegate.getPoints();
		}

		@Override
		public void addPoint(Position p, AVList attributeValues)
		{
			shapeDelegate.addPoint(p, attributeValues);
			dirty.set(true);
		}
		
		@Override
		public void addPoint(ShapePoint p)
		{
			shapeDelegate.addPoint(p);
			dirty.set(true);
		}
		
		@Override
		public Type getType()
		{
			return shapeDelegate.getType();
		}
		
		/**
		 * Generates the airspace used to render the associated shape
		 */
		private void generateAirspace()
		{
			switch (shapeDelegate.getType())
			{
				case LINE:
				{
					airspace = new Curtain(getPoints());
					break;
				}
				case POLYGON:
				{
					airspace = new Polygon(getPoints());
					break;
				}
			}
			dirty.set(false);
		}
		
		@Override
		public void render(DrawContext dc)
		{
			if (dirty.get())
			{
				generateAirspace();
			}
			airspace.render(dc);
		}
	}
	
}
