package au.gov.ga.worldwind.viewer.layers.point;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Style
{
	protected String name;
	protected boolean defalt;
	protected Map<String, String> properties = new HashMap<String, String>();

	public Style(String name, boolean defalt)
	{
		setName(name);
		setDefault(defalt);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isDefault()
	{
		return defalt;
	}

	public void setDefault(boolean defalt)
	{
		this.defalt = defalt;
	}

	public void addProperty(String property, String value)
	{
		properties.put(property, value);
	}

	public void setPropertiesFromAttributes(Object object, AVList attributes)
	{
		Map<String, Method> methods = new HashMap<String, Method>();
		for (Method method : object.getClass().getMethods())
			methods.put(method.getName(), method);

		for (Entry<String, String> entry : properties.entrySet())
		{
			String property = entry.getKey();
			String methodName = "set" + property;
			if (!methods.containsKey(methodName))
			{
				String message =
						"Could not find setter method '" + methodName + "' in class "
								+ object.getClass();
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			Method setter = methods.get(methodName);
			Class<?>[] parameters = setter.getParameterTypes();
			if (parameters.length != 1)
			{
				String message =
						"Setter method '" + methodName + "' in class " + object.getClass()
								+ " doesn't take 1 parameter";
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			String stringValue = entry.getValue();
			stringValue = replaceVariablesWithAttributeValues(stringValue, attributes);

			Class<?> type = parameters[0];
			Object value = null;
			try
			{
				value = convertStringToType(stringValue, type);
			}
			catch (Exception e)
			{
			}

			if (value == null)
			{
				String message = "Error converting '" + stringValue + " to type " + type;
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			try
			{
				setter.invoke(object, value);
			}
			catch (Exception e)
			{
				String message =
						"Error invoking '" + methodName + "' in class " + object.getClass() + ": "
								+ e;
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message, e);
			}
		}
	}

	protected static String replaceVariablesWithAttributeValues(String string, AVList attributes)
	{
		Pattern pattern = Pattern.compile("%[^%]+%");
		Matcher matcher = pattern.matcher(string);
		StringBuffer replacement = new StringBuffer();
		int start = 0;
		while (matcher.find(start))
		{
			replacement.append(string.substring(start, matcher.start()));

			String attribute = matcher.group();
			attribute = attribute.substring(1, attribute.length() - 1);

			if (!attributes.hasKey(attribute))
			{
				String message = "Could not find attribute '" + attribute + "'";
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			String value = attributes.getValue(attribute).toString();
			replacement.append(value);

			start = matcher.end();
		}

		replacement.append(string.substring(start));
		return replacement.toString();
	}

	protected static Object convertStringToType(String string, Class<?> type)
	{
		if (type.isAssignableFrom(String.class))
		{
			return string;
		}
		else if (type.isAssignableFrom(Double.class))
		{
			return Double.valueOf(string);
		}
		else if (type.isAssignableFrom(Integer.class))
		{
			return Integer.valueOf(string);
		}
		else if (type.isAssignableFrom(Color.class))
		{
			int[] ints = splitInts(string);
			if (ints.length == 1)
				return new Color(ints[0]);
			else if (ints.length == 3)
				return new Color(ints[0], ints[1], ints[2]);
			else if (ints.length == 4)
				return new Color(ints[0], ints[1], ints[2], ints[3]);
		}
		else if (type.isAssignableFrom(Dimension.class))
		{
			int[] ints = splitInts(string);
			if (ints.length == 1)
				return new Dimension(ints[0], ints[0]);
			else if (ints.length == 2)
				return new Dimension(ints[0], ints[1]);
		}
		else if (type.isAssignableFrom(Font.class))
		{
			return Font.decode(string);
		}
		return null;
	}

	protected static int[] splitInts(String string)
	{
		String[] split = string.trim().split(",");
		List<Integer> ints = new ArrayList<Integer>(split.length);
		for (String s : split)
		{
			try
			{
				ints.add(Integer.valueOf(s));
			}
			catch (Exception e)
			{
			}
		}

		int[] is = new int[ints.size()];
		for (int i = 0; i < is.length; i++)
			is[i] = ints.get(i);
		return is;
	}
}
