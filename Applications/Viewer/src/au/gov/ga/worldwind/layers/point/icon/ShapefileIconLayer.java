package au.gov.ga.worldwind.layers.point.icon;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;

import java.awt.Point;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.layers.point.ShapefilePointLayer;
import au.gov.ga.worldwind.layers.point.Style;

public class ShapefileIconLayer extends ShapefilePointLayer
{
	//TODO think about adding downloading of icons to retrieval system, and caching them

	protected IconLayer iconLayer;

	public ShapefileIconLayer(AVList params)
	{
		super(params);
		init();
	}

	public ShapefileIconLayer(Element domElement, AVList params)
	{
		super(domElement, params);
		init();
	}

	protected void init()
	{
		iconLayer = new IconLayer();
		iconLayer.setPickEnabled(true);
		setPickEnabled(true);
	}

	@Override
	protected void addPoint(Position position, AVList attrib, Style style, String text, String link)
	{
		UserFacingIcon icon = new UserFacingIcon();
		icon.setShowToolTip(true);
		icon.setPosition(position);
		//icon.setToolTipText(text);
		style.setPropertiesFromAttributes(icon, attrib);
		iconLayer.addIcon(icon);
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		iconLayer.pick(dc, point);
	}

	@Override
	protected void doPreRender(DrawContext dc)
	{
		iconLayer.preRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		iconLayer.render(dc);
	}
}
