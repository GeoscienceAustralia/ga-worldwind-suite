package au.gov.ga.worldwind.common.layers.delegate.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IRenderDelegate;
import au.gov.ga.worldwind.common.render.ExtendedDrawContext;

public class ElevationOffsetRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "ElevationOffset";
	protected final double elevationOffset;
	protected double oldElevationOffset = 0;

	@SuppressWarnings("unused")
	private ElevationOffsetRenderDelegate()
	{
		this(0);
	}

	public ElevationOffsetRenderDelegate(double elevationOffset)
	{
		this.elevationOffset = elevationOffset;
	}

	@Override
	public void preRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			oldElevationOffset = ((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().getElevationOffset();
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setElevationOffset(elevationOffset);
		}
	}

	@Override
	public void postRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setElevationOffset(oldElevationOffset);
		}
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + elevationOffset + ")";
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\(([\\d.\\-]+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				double elevationOffset = Double.parseDouble(matcher.group(1));
				return new ElevationOffsetRenderDelegate(elevationOffset);
			}
		}
		return null;
	}
}
