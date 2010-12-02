package au.gov.ga.worldwind.common.layers.point.types;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;

import java.net.MalformedURLException;
import java.net.URL;

import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointLayerHelper;
import au.gov.ga.worldwind.common.layers.point.PointProperties;
import au.gov.ga.worldwind.common.util.DefaultLauncher;

/**
 * {@link PointLayer} implementation which extends {@link IconLayer} and uses
 * Icons to represent points.
 * 
 * @author Michael de Hoog
 */
public class IconPointLayer extends IconLayer implements PointLayer, SelectListener
{
	private final PointLayerHelper helper;
	private WWIcon pickedIcon;

	public IconPointLayer(PointLayerHelper helper)
	{
		this.helper = helper;
	}
	
	@Override
	public void render(DrawContext dc)
	{
		if (isEnabled())
		{
			helper.requestPoints(this);
		}
		super.render(dc);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		wwd.addSelectListener(this);
	}

	@Override
	public Sector getSector()
	{
		return helper.getSector();
	}

	@Override
	public void addPoint(Position position, AVList attributeValues)
	{
		PointProperties properties = helper.getStyle(attributeValues);
		UserFacingIcon icon = new UserFacingIcon();
		icon.setPosition(position);
		icon.setToolTipText(properties.text);
		icon.setValue(AVKey.URL, properties.link);
		properties.style.setPropertiesFromAttributes(helper.getContext(), attributeValues, icon);
		this.addIcon(icon);
	}

	@Override
	public void loadComplete()
	{
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return helper.getUrl();
	}

	@Override
	public String getDataCacheName()
	{
		return helper.getDataCacheName();
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

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
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
}
