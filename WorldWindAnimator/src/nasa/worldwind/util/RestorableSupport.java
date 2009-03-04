/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package nasa.worldwind.util;

import javax.xml.parsers.ParserConfigurationException;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.util.Logging;

/**
 * RestorableSupport provides convenient read and write access to restorable
 * state located in a simple XML document format. This document is rooted by the
 * <code>restorableState</code> element. State is stored in
 * <code>stateObject</code> elements. Each <code>stateObject</code> element is
 * identified by its <code>name</code> attribute. The value of a
 * <code>stateObject</code> can either be simple text content, or nested
 * <code>stateObject</code> elements.
 * <p>
 * For example, this document stores four states: the string "Hello World!", the
 * largest value an unsigned byte can hold, the value of PI to six digits, and a
 * boolean "true". <code>
 * <pre>
 * {@literal <?xml version=”1.0” encoding=”UTF-8”?>}
 * {@literal <restorableState>}
 *   {@literal <stateObject name=”helloWorldString”>Hello World!</stateObject>}
 *   {@literal <stateObject name=”maxUnsignedByteValue”>255</stateObject>}
 *   {@literal <stateObject name=”pi”>3.141592</stateObject>}
 *   {@literal <stateObject name=”booleanTrue”>true</stateObject>}
 * {@literal </restorableState>}
 * </pre>
 * </code> Callers can create a new RestorableSupport with no state content, or
 * create a RestorableSupport from an existing XML document string. Callers can
 * then add state by name and value, and query state by name. RestorableSupport
 * provides convenience methods for addding and querying state values as
 * Strings, Integers, Doubles, and Booleans.
 * 
 * @author dcollins
 * @version $Id: RestorableSupport.java 4707 2008-03-15 07:56:52Z dcollins $
 * @see gov.nasa.worldwind.Restorable
 */
public class RestorableSupport
{
	private static final String DEFAULT_DOCUMENT_ELEMENT_TAG_NAME = "restorableState";
	private static final String DEFAULT_STATE_OBJECT_TAG_NAME = "stateObject";

	private org.w3c.dom.Document doc;
	private javax.xml.xpath.XPath xpath;
	private String stateObjectTagName;

	private RestorableSupport(org.w3c.dom.Document doc)
	{
		if (doc == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.doc = doc;
		javax.xml.xpath.XPathFactory pathFactory = javax.xml.xpath.XPathFactory
				.newInstance();
		this.xpath = pathFactory.newXPath();
		this.stateObjectTagName = DEFAULT_STATE_OBJECT_TAG_NAME;
	}

	/**
	 * Creates a new RestorableSupport with no contents.
	 * 
	 * @return a new, empty RestorableSupport instance.
	 */
	public static RestorableSupport newRestorableSupport()
	{
		javax.xml.parsers.DocumentBuilderFactory docBuilderFactory = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();

		try
		{
			javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory
					.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder.newDocument();
			// Create the "restorableState" document root element.
			createDocumentElement(doc, DEFAULT_DOCUMENT_ELEMENT_TAG_NAME);
			return new RestorableSupport(doc);
		}
		catch (javax.xml.parsers.ParserConfigurationException e)
		{
			String message = Logging
					.getMessage("RestorableSupport.ExceptionCreatingParser");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
	}

	/**
	 * Creates a new RestorableSupport with the contents of the specified state
	 * document.
	 * 
	 * @param stateInXml
	 *            the XML document to parse for state.
	 * @return a new RestorableSupport instance with the specified state.
	 * @throws IllegalArgumentException
	 *             If <code>stateInXml</code> is null, or the its contents are
	 *             not a well formed XML document.
	 */
	public static RestorableSupport parse(String stateInXml)
	{
		if (stateInXml == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		javax.xml.parsers.DocumentBuilderFactory docBuilderFactory = javax.xml.parsers.DocumentBuilderFactory
				.newInstance();

		try
		{
			javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory
					.newDocumentBuilder();
			org.w3c.dom.Document doc = docBuilder
					.parse(new org.xml.sax.InputSource(
							new java.io.StringReader(stateInXml)));
			return new RestorableSupport(doc);
		}
		catch (java.io.IOException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ExceptionParsingXml", stateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}
		catch (org.xml.sax.SAXException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ExceptionParsingXml", stateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}
		catch (javax.xml.parsers.ParserConfigurationException e)
		{
			String message = Logging
					.getMessage("RestorableSupport.ExceptionCreatingParser");
			Logging.logger().severe(message);
			throw new IllegalStateException(message, e);
		}
	}

	private org.w3c.dom.Element getDocumentElement()
	{
		return this.doc.getDocumentElement();
	}

	private static void createDocumentElement(org.w3c.dom.Document doc,
			String tagName)
	{
		if (doc == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (tagName == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Document already has a root element.
		if (doc.getDocumentElement() != null)
			return;

		org.w3c.dom.Element elem = doc.createElement(tagName);
		doc.appendChild(elem);
	}

	/**
	 * Returns an XML document string describing this RestorableSupport's
	 * current set of state objects. If this RestorableSupport cannot be
	 * converted, this will return null.
	 * 
	 * @return an XML state document string.
	 */
	public String getStateAsXml()
	{
		javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory
				.newInstance();
		try
		{
			// The StringWriter will receive the document xml.
			java.io.StringWriter stringWriter = new java.io.StringWriter();
			// Attempt to write the Document to the StringWriter.
			javax.xml.transform.Transformer transformer = transformerFactory
					.newTransformer();
			transformer.transform(new javax.xml.transform.dom.DOMSource(
					this.doc), new javax.xml.transform.stream.StreamResult(
					stringWriter));
			// If successful, return the StringWriter contents as a String.
			return stringWriter.toString();
		}
		catch (javax.xml.transform.TransformerConfigurationException e)
		{
			String message = Logging
					.getMessage("RestorableSupport.ExceptionWritingXml");
			Logging.logger().severe(message);
			return null;
		}
		catch (javax.xml.transform.TransformerException e)
		{
			String message = Logging
					.getMessage("RestorableSupport.ExceptionWritingXml");
			Logging.logger().severe(message);
			return null;
		}
	}

	/**
	 * Returns an XML document string describing this RestorableSupport's
	 * current set of state objects. Calling <code>toString</code> is equivalent
	 * to calling <code>getStateAsXml</code>.
	 * 
	 * @return an XML state document string.
	 */
	public String toString()
	{
		return getStateAsXml();
	}

	/**
	 * An interface to the <code>stateObject</code> elements in an XML state
	 * document, as defined by {@link gov.nasa.worldwind.util.RestorableSupport}
	 * . The <code>name</code> and simple String <code>value</code> of a
	 * <code>stateObject</code> can be queried or set through StateObject. This
	 * also serves as a context through which nested <code>stateObjects</code>
	 * can be found or created.
	 */
	public static class StateObject
	{
		final org.w3c.dom.Element elem;

		StateObject(org.w3c.dom.Element element)
		{
			if (element == null)
			{
				String message = Logging.getMessage("nullValue.ElementIsNull");
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			this.elem = element;
		}

		/**
		 * Returns the name of this StateObject as a String, or null if this
		 * StateObject has no name.
		 * 
		 * @return this StateObject's name.
		 */
		public String getName()
		{
			return this.elem.getAttribute("name");
		}

		/**
		 * Sets the name of this StateObject to the specified String.
		 * 
		 * @param name
		 *            the new name of this StateObject.
		 * @throws IllegalArgumentException
		 *             If <code>name</code> is null.
		 */
		public void setName(String name)
		{
			if (name == null)
			{
				String message = Logging.getMessage("nullValue.StringIsNull");
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			this.elem.setAttribute("name", name);
		}

		/**
		 * Returns the value of this StateObject as a String, or null if this
		 * StateObject has no value. If there are StateObjects nested beneath
		 * this one, then the entire tree beneath this StateObject is converted
		 * to a String and returned.
		 * 
		 * @return the value of this StateObject as a String.
		 */
		public String getValue()
		{
			return this.elem.getTextContent();
		}

		/**
		 * Sets the value of this StateObject to the specified String. If there
		 * are StateObjects nested beneath this one, then the entire tree
		 * beneath this StateObject is replaced with the specified value.
		 * 
		 * @param value
		 *            String value that will replace this StateObject's value.
		 * @throws IllegalArgumentException
		 *             If <code>value</code> is null.
		 */
		public void setValue(String value)
		{
			if (value == null)
			{
				String message = Logging.getMessage("nullValue.StringIsNull");
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			this.elem.setTextContent(value);
		}
	}

	/**
	 * Returns the String to be used for each state object's tag name. This tag
	 * name will be used as a search parameter to find a state object, and will
	 * be used as the tag name when a new state object is created. The default
	 * tag name is "stateObject".
	 * 
	 * @return String to be used for each state object's tag name
	 */
	public String getStateObjectTagName()
	{
		return this.stateObjectTagName;
	}

	/**
	 * Sets the String to be used for each state object's tag name. This tag
	 * name will be used as a search parameter to find a state object, and will
	 * be used as the tag name when a new state object is created. Setting this
	 * value will not retroactively set tag names for existing state objects.
	 * The default tag name is "stateObject".
	 * 
	 * @param stateObjectTagName
	 *            String to be used for each state object's tag name.
	 * @throws IllegalArgumentException
	 *             If <code>stateObjectTagName</code> is null.
	 */
	public void setStateObjectTagName(String stateObjectTagName)
	{
		if (stateObjectTagName == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.stateObjectTagName = stateObjectTagName;
	}

	private StateObject findStateObject(org.w3c.dom.Node context, String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Search for the state element with the specified name.
		String expression = String.format("%s[@name=\"%s\"]",
				getStateObjectTagName(), name);
		try
		{
			Object result = this.xpath.evaluate(
					expression,
					// If non-null, search from the specified context. Otherwise, search from the
					// document root element.
					(context != null ? context : getDocumentElement()),
					javax.xml.xpath.XPathConstants.NODE);
			if (result == null)
				return null;

			// If the result is an Element node, return a new StateObject with the result as its content.
			// Otherwise return null.
			return (result instanceof org.w3c.dom.Element) ? new StateObject(
					(org.w3c.dom.Element) result) : null;
		}
		catch (javax.xml.xpath.XPathExpressionException e)
		{
			return null;
		}
	}

	private StateObject[] findAllStateObjects(org.w3c.dom.Node context,
			String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Search for the state elements beneath the context with the specified name.
		String expression;
		if (name.length() != 0)
			expression = String.format("%s[@name=\"%s\"]",
					getStateObjectTagName(), name);
		else
			expression = String.format("%s//.", getStateObjectTagName());

		try
		{
			Object result = this.xpath.evaluate(
					expression,
					// If non-null, search from the specified context. Otherwise, search from the
					// document root element.
					(context != null ? context : getDocumentElement()),
					javax.xml.xpath.XPathConstants.NODESET);
			if (result == null || !(result instanceof org.w3c.dom.NodeList)
					|| ((org.w3c.dom.NodeList) result).getLength() == 0)
				return null;

			// If the result is a NodeList, return an array of StateObjects for each Element node in that list.
			org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) result;
			StateObject[] stateObjects = new StateObject[nodeList.getLength()];
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				org.w3c.dom.Node node = nodeList.item(i);
				if (node instanceof org.w3c.dom.Element)
					stateObjects[i] = new StateObject(
							(org.w3c.dom.Element) node);
			}
			return stateObjects;
		}
		catch (javax.xml.xpath.XPathExpressionException e)
		{
			return null;
		}
	}

	private StateObject[] extractStateObjects(org.w3c.dom.Element context)
	{
		org.w3c.dom.NodeList nodeList = (context != null ? context
				: getDocumentElement()).getChildNodes();

		StateObject[] stateObjects = new StateObject[0];
		if (nodeList != null)
		{
			stateObjects = new StateObject[nodeList.getLength()];
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				org.w3c.dom.Node node = nodeList.item(i);
				if (node instanceof org.w3c.dom.Element
						&& node.getNodeName() != null
						&& node.getNodeName().equals(getStateObjectTagName()))
				{
					stateObjects[i] = new StateObject(
							(org.w3c.dom.Element) node);
				}
			}
		}
		return stateObjects;
	}

	private StateObject createStateObject(org.w3c.dom.Element context,
			String name, String value)
	{
		return createStateObject(context, name, value, false);
	}

	private StateObject createStateObject(org.w3c.dom.Element context,
			String name, String value, boolean escapeValue)
	{
		org.w3c.dom.Element elem = this.doc
				.createElement(getStateObjectTagName());

		// If non-null, name goes in an attribute entitled "name".
		if (name != null)
			elem.setAttribute("name", name);

		// If non-null, value goes in the element text content.
		if (value != null)
		{
			// If escapeValue is true, we place value in a CDATA node beneath elem.
			if (escapeValue)
				elem.appendChild(this.doc.createCDATASection(value));
			// Otherwise, just set the text value of elem normally.
			else
				elem.setTextContent(value);
		}

		// If non-null, add the StateObject element to the specified context. Otherwise, add it to the
		// document root element.
		(context != null ? context : getDocumentElement()).appendChild(elem);

		return new StateObject(elem);
	}

	private boolean containsElement(org.w3c.dom.Element elem)
	{
		if (elem == null)
		{
			String message = Logging.getMessage("nullValue.ElementIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return elem.getOwnerDocument().equals(this.doc);
	}

	/**
	 * Returns the StateObject with the specified <code>name</code>. This will
	 * search the StateObjects directly beneath the document root. If no
	 * StateObject with that name exists, this will return null.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return the StateObject instance, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public StateObject getStateObject(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getStateObject(null, name);
	}

	/**
	 * Returns the StateObject with the specified <code>name</code>. If context
	 * is not null, this will search the StateObjects directly below the
	 * specified <code>context</code>. Otherwise, this will search the
	 * StateObjects directly beneath the document root. If no StateObject with
	 * that name exists, this will return null.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return the StateObject instance, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public StateObject getStateObject(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return findStateObject(context != null ? context.elem : null, name);
	}

	/**
	 * Returns all StateObjects directly beneath the a context StateObject. If
	 * context is not null, this will return all the StateObjects directly below
	 * the specified <code>context</code>. Otherwise, this will return all the
	 * StateObjects directly beneath the document root.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @return an array of the StateObject instances, which will have zero
	 *         length if none exist.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public StateObject[] getAllStateObjects(StateObject context)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return extractStateObjects(context != null ? context.elem : null);
	}

	/**
	 * Returns any StateObjects directly beneath the document root that have the
	 * specified <code>name</code>. If no StateObjects with that name exist,
	 * this will return a valid StateObject array with zero length.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return an array of the StateObject instances, which will have zero
	 *         length if none exist.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public StateObject[] getAllStateObjects(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getAllStateObjects(null, name);
	}

	/**
	 * Returns all StateObjects with the specified <code>name</code>. If context
	 * is not null, this will search the StateObjects directly below the
	 * specified <code>context</code>. Otherwise, this will search the
	 * StateObjects directly beneath the document root. If no StateObjects with
	 * that name exist, this will return a valid StateObject array with zero
	 * length.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return an array of the StateObject instances, which will have zero
	 *         length if none exist.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public StateObject[] getAllStateObjects(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return findAllStateObjects(context != null ? context.elem : null, name);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code>. The new
	 * StateObject will be placed directly beneath the document root. If a
	 * StateObject with this name already exists, a new one is still created.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @return the new StateObject instance.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public StateObject addStateObject(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return addStateObject(null, name);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code>. If
	 * <code>context</code> is not null, the new StateObject will be nested
	 * directly beneath the specified <code>context</code>. Otherwise, the new
	 * StateObject will be placed directly beneath the document root. If a
	 * StateObject with this name already exists, a new one is still created.
	 * 
	 * @param context
	 *            the StateObject under which the new StateObject will be
	 *            created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @return the new StateObject instance.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public StateObject addStateObject(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Create the state object with no value.
		return createStateObject(context != null ? context.elem : null, name,
				null);
	}

	/*************************************************************************************************************/
	/** Convenience methods for adding and querying state values. **/
	/*************************************************************************************************************/

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a String. This will search the StateObjects directly beneath the
	 * document root. If no StateObject with that name exists, or if the value
	 * of that StateObject is not a String, this will return null.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a String, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public String getStateValueAsString(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getStateValueAsString(null, name);
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a String. If context is not null, this will search the StateObjects
	 * directly below the specified <code>context</code>. Otherwise, this will
	 * search the StateObjects directly beneath the document root. If no
	 * StateObject with that name exists, or if the value of that StateObject is
	 * not a String, this will return null.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a String, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public String getStateValueAsString(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		StateObject stateObject = findStateObject(
				context != null ? context.elem : null, name);
		return stateObject != null ? stateObject.getValue() : null;
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as an Integer. This will search the StateObjects directly beneath the
	 * document root. If no StateObject with that name exists, or if the value
	 * of that StateObject is not an Integer, this will return null.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as an Integer, or null if none
	 *         exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public Integer getStateValueAsInteger(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getStateValueAsInteger(null, name);
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as an Integer. If context is not null, this will search the StateObjects
	 * directly below the specified <code>context</code>. Otherwise, this will
	 * search the StateObjects directly beneath the document root. If no
	 * StateObject with that name exists, or if the value of that StateObject is
	 * not an Integer, this will return null.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as an Integer, or null if none
	 *         exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public Integer getStateValueAsInteger(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String stringValue = getStateValueAsString(context, name);
		if (stringValue == null)
			return null;

		try
		{
			return Integer.valueOf(stringValue);
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ConversionError", stringValue);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a Double. This will search the StateObjects directly beneath the
	 * document root. If no StateObject with that name exists, or if the value
	 * of that StateObject is not a Double, this will return null.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a Double, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public Double getStateValueAsDouble(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getStateValueAsDouble(null, name);
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a Double. If context is not null, this will search the StateObjects
	 * directly below the specified <code>context</code>. Otherwise, this will
	 * search the StateObjects directly beneath the document root. If no
	 * StateObject with that name exists, or if the value of that StateObject is
	 * not a Double, this will return null.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a Double, or null if none exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public Double getStateValueAsDouble(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String stringValue = getStateValueAsString(context, name);
		if (stringValue == null)
			return null;

		try
		{
			return Double.valueOf(stringValue);
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ConversionError", stringValue);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a Boolean. This will search the StateObjects directly beneath the
	 * document root. If no StateObject with that name exists, this will return
	 * null. Otherwise, the Boolean value returned is equivalent to passing the
	 * StateObject's value to <code>Boolean.valueOf</code>.
	 * 
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a Boolean, or null if none
	 *         exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public Boolean getStateValueAsBoolean(String name)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return getStateValueAsBoolean(null, name);
	}

	/**
	 * Returns the value of the StateObject with the specified <code>name</code>
	 * as a Boolean. If context is not null, this will search the StateObjects
	 * directly below the specified <code>context</code>. Otherwise, this will
	 * search the StateObjects directly beneath the document root. If no
	 * StateObject with that name exists, this will return null. Otherwise, the
	 * Boolean value returned is equivalent to passing the StateObject's value
	 * to <code>Boolean.valueOf</code>.
	 * 
	 * @param context
	 *            StateObject context to search, or null to search the document
	 *            root.
	 * @param name
	 *            the StateObject name to search for.
	 * @return the value of the StateObject as a Boolean, or null if none
	 *         exists.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public Boolean getStateValueAsBoolean(StateObject context, String name)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		String stringValue = getStateValueAsString(context, name);
		if (stringValue == null)
			return null;

		try
		{
			return Boolean.valueOf(stringValue);
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ConversionError", stringValue);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and String
	 * <code>value</code>. The new StateObject will be placed beneath the
	 * document root. If a StateObject with this name already exists, a new one
	 * is still created.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @param value
	 *            the new StateObject's String value.
	 * @throws IllegalArgumentException
	 *             If either <code>name</code> or <code>value</code> is null.
	 */
	public void addStateValueAsString(String name, String value)
	{
		if (name == null || value == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(null, name, value, false);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and String
	 * <code>value</code>. The new StateObject will be placed beneath the
	 * document root. If a StateObject with this name already exists, a new one
	 * is still created. If <code>escapeValue</code> is true, the text in
	 * <code>value</code> will be escaped in a CDATA section. Otherwise, no
	 * special processing is performed on <code>value</code>. Once
	 * <code>value</code> has been escaped and added, it can be extracted
	 * exactly like any other String value.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @param value
	 *            the new StateObject's String value.
	 * @param escapeValue
	 *            whether to escape the String <code>value</code> or not.
	 * @throws IllegalArgumentException
	 *             If either <code>name</code> or <code>value</code> is null.
	 */
	public void addStateValueAsString(String name, String value,
			boolean escapeValue)
	{
		if (name == null || value == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(null, name, value, escapeValue);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and String
	 * <code>value</code>. If <code>context</code> is not null, the new
	 * StateObject will be nested directly beneath the specified
	 * <code>context</code>. Otherwise, the new StateObject will be placed
	 * directly beneath the document root. If a StateObject with this name
	 * already exists, a new one is still created.
	 * 
	 * @param context
	 *            the StateObject context under which the new StateObject will
	 *            be created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @param value
	 *            the new StateObject's String value.
	 * @throws IllegalArgumentException
	 *             If either <code>name</code> or <code>value</code> is null, or
	 *             if <code>context</code> is not null and does not belong to
	 *             this RestorableSupport.
	 */
	public void addStateValueAsString(StateObject context, String name,
			String value)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null || value == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(context, name, value, false);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and String
	 * <code>value</code>. If <code>context</code> is not null, the new
	 * StateObject will be nested directly beneath the specified
	 * <code>context</code>. Otherwise, the new StateObject will be placed
	 * directly beneath the document root. If a StateObject with this name
	 * already exists, a new one is still created. If <code>escapeValue</code>
	 * is true, the text in <code>value</code> will be escaped in a CDATA
	 * section. Otherwise, no special processing is performed on
	 * <code>value</code>. Once <code>value</code> has been escaped and added,
	 * it can be extracted exactly like any other String value.
	 * 
	 * @param context
	 *            the StateObject context under which the new StateObject will
	 *            be created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @param value
	 *            the new StateObject's String value.
	 * @param escapeValue
	 *            whether to escape the String <code>value</code> or not.
	 * @throws IllegalArgumentException
	 *             If either <code>name</code> or <code>value</code> is null, or
	 *             if <code>context</code> is not null and does not belong to
	 *             this RestorableSupport.
	 */
	public void addStateValueAsString(StateObject context, String name,
			String value, boolean escapeValue)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null || value == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		createStateObject(context != null ? context.elem : null, name, value,
				escapeValue);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Integer
	 * <code>value</code>. The new StateObject will be placed beneath the
	 * document root. If a StateObject with this name already exists, a new one
	 * is still created.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @param intValue
	 *            the new StateObject's Integer value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public void addStateValueAsInteger(String name, int intValue)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsInteger(null, name, intValue);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Integer
	 * <code>value</code>. If <code>context</code> is not null, the new
	 * StateObject will be nested directly beneath the specified
	 * <code>context</code>. Otherwise, the new StateObject will be placed
	 * directly beneath the document root. If a StateObject with this name
	 * already exists, a new one is still created.
	 * 
	 * @param context
	 *            the StateObject context under which the new StateObject will
	 *            be created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @param intValue
	 *            the new StateObject's Integer value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public void addStateValueAsInteger(StateObject context, String name,
			int intValue)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(context, name, Integer.toString(intValue));
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Double
	 * <code>value</code>. The new StateObject will be placed beneath the
	 * document root. If a StateObject with this name already exists, a new one
	 * is still created.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @param doubleValue
	 *            the new StateObject's Double value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public void addStateValueAsDouble(String name, double doubleValue)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsDouble(null, name, doubleValue);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Double
	 * <code>value</code>. If <code>context</code> is not null, the new
	 * StateObject will be nested directly beneath the specified
	 * <code>context</code>. Otherwise, the new StateObject will be placed
	 * directly beneath the document root. If a StateObject with this name
	 * already exists, a new one is still created.
	 * 
	 * @param context
	 *            the StateObject context under which the new StateObject will
	 *            be created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @param doubleValue
	 *            the new StateObject's Double value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public void addStateValueAsDouble(StateObject context, String name,
			double doubleValue)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(context, name, Double.toString(doubleValue));
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Boolean
	 * <code>value</code>. The new StateObject will be placed beneath the
	 * document root. If a StateObject with this name already exists, a new one
	 * is still created.
	 * 
	 * @param name
	 *            the new StateObject's name.
	 * @param booleanValue
	 *            the new StateObject's Boolean value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null.
	 */
	public void addStateValueAsBoolean(String name, boolean booleanValue)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsBoolean(null, name, booleanValue);
	}

	/**
	 * Adds a new StateObject with the specified <code>name</code> and Boolean
	 * <code>value</code>. If <code>context</code> is not null, the new
	 * StateObject will be nested directly beneath the specified
	 * <code>context</code>. Otherwise, the new StateObject will be placed
	 * directly beneath the document root. If a StateObject with this name
	 * already exists, a new one is still created.
	 * 
	 * @param context
	 *            the StateObject context under which the new StateObject will
	 *            be created, or null to place it under the document root.
	 * @param name
	 *            the new StateObject's name.
	 * @param booleanValue
	 *            the new StateObject's Boolean value.
	 * @throws IllegalArgumentException
	 *             If <code>name</code> is null, or if <code>context</code> is
	 *             not null and does not belong to this RestorableSupport.
	 */
	public void addStateValueAsBoolean(StateObject context, String name,
			boolean booleanValue)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsString(context, name, Boolean.toString(booleanValue));
	}

	public void addStateValueAsRestorable(String name, Restorable restorable)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		addStateValueAsRestorable(null, name, restorable);
	}

	public void addStateValueAsRestorable(StateObject context, String name,
			Restorable restorable)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		org.w3c.dom.Element elem = this.doc
				.createElement(getStateObjectTagName());

		// If non-null, name goes in an attribute entitled "name".
		if (name != null)
			elem.setAttribute("name", name);

		if (restorable != null)
		{
			RestorableSupport rs = parse(restorable.getRestorableState());
			org.w3c.dom.Node node = doc.importNode(rs.getDocumentElement(),
					true);
			elem.appendChild(node);
		}

		// If non-null, add the StateObject element to the specified context. Otherwise, add it to the
		// document root element.
		(context != null ? context.elem : getDocumentElement())
				.appendChild(elem);
	}

	public <E extends Restorable> E getStateValueAsRestorable(String name,
			E restorable)
	{
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		return getStateValueAsRestorable(null, name, restorable);
	}

	public <E extends Restorable> E getStateValueAsRestorable(
			StateObject context, String name, E restorable)
	{
		if (context != null && !containsElement(context.elem))
		{
			String message = Logging
					.getMessage("RestorableSupport.InvalidStateObject");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (name == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		StateObject stateObject = findStateObject(
				context != null ? context.elem : null, name);
		if (stateObject.elem != null && stateObject.elem.hasChildNodes())
		{
			javax.xml.parsers.DocumentBuilderFactory docBuilderFactory = javax.xml.parsers.DocumentBuilderFactory
					.newInstance();
			try
			{
				javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();
				org.w3c.dom.Document doc = docBuilder.newDocument();
				org.w3c.dom.Node node = doc.importNode(stateObject.elem
						.getChildNodes().item(0), true);
				doc.appendChild(node);
				RestorableSupport rs = new RestorableSupport(doc);
				restorable.restoreState(rs.getStateAsXml());
				return restorable;
			}
			catch (ParserConfigurationException e)
			{
			}
		}

		return null;
	}

	/*************************************************************************************************************/
	/** Convenience methods for adding and querying state values. **/
	/*************************************************************************************************************/

	/**
	 * Returns a String encoding of the specified <code>color</code>. The Color
	 * can be restored with a call to {@link #decodeColor(String)}.
	 * 
	 * @param color
	 *            Color to encode.
	 * @return String encoding of the specified <code>color</code>.
	 * @throws IllegalArgumentException
	 *             If <code>color</code> is null.
	 */
	public static String encodeColor(java.awt.Color color)
	{
		if (color == null)
		{
			String message = Logging.getMessage("nullValue.ColorIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// Encode the red, green, blue, and alpha components
		int rgba = (color.getRed() & 0xFF) << 24
				| (color.getGreen() & 0xFF) << 16
				| (color.getBlue() & 0xFF) << 8 | (color.getAlpha() & 0xFF);
		return String.format("%#08X", rgba);
	}

	/**
	 * Returns the Color described by the String <code>encodedString</code>.
	 * This understands Colors encoded with a call to
	 * {@link #encodeColor(java.awt.Color)}. If <code>encodedString</code>
	 * cannot be decoded, this will return null.
	 * 
	 * @param encodedString
	 *            String to decode.
	 * @return Color decoded from the specified <code>encodedString</code>, or
	 *         null if the String cannot be decoded.
	 * @throws IllegalArgumentException
	 *             If <code>encodedString</code> is null.
	 */
	public static java.awt.Color decodeColor(String encodedString)
	{
		if (encodedString == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// The hexadecimal representation for an RGBA color can result in a value larger than
		// Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
		// then keep only the lower four bytes.
		Long longValue;
		try
		{
			longValue = Long.decode(encodedString);
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage(
					"RestorableSupport.ConversionError", encodedString);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}

		int i = (int) (longValue & 0xFFFFFFFFL);
		return new java.awt.Color((i >> 24) & 0xFF, (i >> 16) & 0xFF,
				(i >> 8) & 0xFF, i & 0xFF);
	}
}
