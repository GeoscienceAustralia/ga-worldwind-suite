package au.gov.ga.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.worldwind.common.util.Validate;

/**
 * A default implementation of the {@link Shape} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicShapeImpl implements Shape
{
	private String id;
	private Type type;
	private List<ShapePoint> points = new ArrayList<ShapePoint>();
	
	/**
	 * Create a new shape of the provided type
	 */
	public BasicShapeImpl(String id, Type type)
	{
		Validate.notBlank(id, "An ID must be provided");
		Validate.notNull(type, "A type must be provided");
		
		this.type = type;
		this.id = id;
	}
	
	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public List<? extends ShapePoint> getPoints()
	{
		return points;
	}

	@Override
	public void addPoint(ShapePoint p)
	{
		if (p == null)
		{
			return;
		}
		points.add(p);
	}
	
	@Override
	public void addPoint(Position p, AVList attributeValues)
	{
		if (p == null)
		{
			return;
		}
		points.add(new ShapePoint(p, attributeValues));
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
