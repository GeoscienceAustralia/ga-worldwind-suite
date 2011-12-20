package au.gov.ga.worldwind.common.layers.point.providers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.worldwind.common.layers.point.PointLayer;
import au.gov.ga.worldwind.common.layers.point.PointProvider;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * {@link PointProvider} implementation which loads points from an XML element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class XMLPointProvider implements PointProvider
{
	private List<Position> points = new ArrayList<Position>();
	private List<AVList> attributes = new ArrayList<AVList>();
	private Sector sector = null;
	private boolean added = false;

	public XMLPointProvider(Element element)
	{
		XPath xpath = XMLUtil.makeXPath();
		Element[] pointElements = XMLUtil.getElements(element, "Points/Point", xpath);
		if (pointElements != null)
		{
			for (Element pointElement : pointElements)
			{
				Position position = XMLUtil.getPosition(pointElement, null, xpath);
				if (position != null)
				{
					points.add(position);
					sector =
							sector != null ? sector.union(position.latitude, position.longitude) : new Sector(
									position.latitude, position.longitude, position.latitude, position.longitude);

					AVList attributes = new AVListImpl();
					this.attributes.add(attributes);
					NamedNodeMap elementAttributes = pointElement.getAttributes();
					for (int i = 0; i < elementAttributes.getLength(); i++)
					{
						Node child = elementAttributes.item(i);
						attributes.setValue(child.getNodeName(), child.getTextContent());
					}
					NodeList children = pointElement.getChildNodes();
					for (int i = 0; i < children.getLength(); i++)
					{
						Node child = children.item(i);
						attributes.setValue(child.getNodeName(), child.getTextContent());
					}
				}
			}
		}
	}

	@Override
	public Sector getSector()
	{
		return sector;
	}

	@Override
	public void requestData(PointLayer layer)
	{
		if (added)
			return;

		added = true;
		for (int i = 0; i < points.size(); i++)
		{
			layer.addPoint(points.get(i), attributes.get(i));
		}
		layer.loadComplete();
	}

	@Override
	public boolean isLoading()
	{
		return false;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		//do nothing, as this provider is never loading
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		//do nothing, as this provider is never loading
	}
}
