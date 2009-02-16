package au.gov.ga.worldwind.panels.places;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;

import java.util.ArrayList;
import java.util.List;

public class PlaceLayer extends AbstractLayer
{
	private final GeographicTextRenderer nameRenderer = new GeographicTextRenderer();
	private List<GeographicText> text = new ArrayList<GeographicText>();

	@Override
	protected void doRender(DrawContext dc)
	{
		synchronized (text)
		{
			nameRenderer.render(dc, text);
		}
	}

	public void addText(GeographicText text)
	{
		synchronized (this.text)
		{
			this.text.add(text);
		}
	}

	public void clearText()
	{
		synchronized (this.text)
		{
			this.text.clear();
		}
	}
	
	@Override
	public void dispose()
	{
		super.dispose();
		nameRenderer.dispose();
	}
}
