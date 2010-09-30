package au.gov.ga.worldwind.common.layers.shapefile.point.icon;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.shapefile.point.ShapefilePointLayer;
import au.gov.ga.worldwind.common.layers.shapefile.point.Style;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.Setupable;

public class ShapefileIconLayer extends ShapefilePointLayer implements SelectListener, Setupable
{
	//TODO think about adding downloading of icons to retrieval system, and caching them

	protected IconLayer iconLayer;
	protected WWIcon pickedIcon;

	public ShapefileIconLayer(Element domElement, AVList params)
	{
		super(domElement, params);
		init();
	}

	protected void init()
	{
		iconLayer = new IconLayer();
		setPickEnabled(true);
	}

	@Override
	public void setPickEnabled(boolean pickable)
	{
		super.setPickEnabled(pickable);
		iconLayer.setPickEnabled(pickable);
	}

	@Override
	protected void addPoint(Position position, AVList attrib, Style style, String text, String link)
	{
		UserFacingIcon icon = new UserFacingIcon();
		icon.setPosition(position);
		icon.setToolTipText(text);
		icon.setValue(AVKey.URL, link);
		style.setPropertiesFromAttributes(context, icon, attrib);
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

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
			return;

		PickedObject topPickedObject = e.getTopPickedObject();
		if (topPickedObject != null && topPickedObject.getObject() instanceof WWIcon)
		{
			if (pickedIcon != null)
			{
				highlight(pickedIcon, false);
			}

			pickedIcon = (WWIcon) topPickedObject.getObject();
			highlight(pickedIcon, true);

			if (e.getEventAction() == SelectEvent.LEFT_PRESS)
			{
				String link = pickedIcon.getStringValue(AVKey.URL);
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else if (pickedIcon != null)
		{
			highlight(pickedIcon, false);
			pickedIcon = null;
		}
	}

	protected void highlight(WWIcon icon, boolean highlight)
	{
		icon.setShowToolTip(highlight);
		icon.setHighlighted(highlight);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}
}
