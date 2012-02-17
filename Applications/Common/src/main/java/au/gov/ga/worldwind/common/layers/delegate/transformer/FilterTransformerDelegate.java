package au.gov.ga.worldwind.common.layers.delegate.transformer;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.worldwind.common.layers.delegate.IImageTransformerDelegate;
import au.gov.ga.worldwind.common.layers.delegate.filters.TransparentMinimumFilter;
import au.gov.ga.worldwind.common.layers.styled.PropertySetter;
import au.gov.ga.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.worldwind.common.util.XMLUtil;

import com.jhlabs.image.AbstractBufferedImageOp;

public class FilterTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "FilterTransformer";

	private final List<BufferedImageOp> filters;

	public FilterTransformerDelegate()
	{
		this(new ArrayList<BufferedImageOp>());
	}

	public FilterTransformerDelegate(List<BufferedImageOp> filters)
	{
		this.filters = filters;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().equals(DEFINITION_STRING.toLowerCase()))
		{
			List<BufferedImageOp> filters = new ArrayList<BufferedImageOp>();

			XPath xpath = XMLUtil.makeXPath();
			Element[] filterElements = XMLUtil.getElements(layerElement, "Filters/Filter", xpath);
			if (filterElements != null)
			{
				for (Element filterElement : filterElements)
				{
					String name = XMLUtil.getText(filterElement, "@name", xpath);
					try
					{
						Class<?> filterClass = null;
						try
						{
							//first try class name with the local .filter package prefix
							filterClass =
									Class.forName(TransparentMinimumFilter.class.getPackage().getName() + "." + name);
						}
						catch (ClassNotFoundException e)
						{
						}
						if (filterClass == null)
						{
							try
							{
								//next try class name with the com.jhlabs.image package prefix
								filterClass =
										Class.forName(AbstractBufferedImageOp.class.getPackage().getName() + "." + name);
							}
							catch (ClassNotFoundException e)
							{
							}
						}
						if (filterClass == null)
						{
							try
							{
								//if not found, simply try the name as the full class name
								filterClass = Class.forName(name);
							}
							catch (ClassNotFoundException e)
							{
								throw new Exception("Filter not found: " + name, e);
							}
						}

						Object filterObject = filterClass.newInstance();
						if (filterObject instanceof BufferedImageOp)
						{
							BufferedImageOp filter = (BufferedImageOp) filterObject;
							filters.add(filter);

							PropertySetter setter = new PropertySetter();
							StyleAndAttributeFactory.addProperties(filterElement, xpath, setter);
							setter.setPropertiesFromAttributes(null, null, filter);
						}
					}
					catch (Exception e)
					{
						//log exception
						e.printStackTrace();
					}
				}
			}

			return new FilterTransformerDelegate(filters);
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image)
	{
		if (filters != null)
		{
			for (BufferedImageOp filter : filters)
			{
				image = filter.filter(image, null);
			}
		}
		return image;
	}
}
