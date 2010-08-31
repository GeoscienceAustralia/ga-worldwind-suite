package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.util.Validate;
import au.gov.ga.worldwind.common.util.XMLUtil;

/**
 * An implementation of the {@link AnimationReader} interface that reads animations from
 * an XML file.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class XmlAnimationReader implements AnimationReader
{
	private static final String WORLD_WIND_ANIMATION_VERSION = AnimationFileVersion.VERSION020.getConstants().getWorldWindAnimationAttributeVersion();
	
	@Override
	public Animation readAnimation(String fileName, WorldWindow worldWindow)
	{
		Validate.notBlank(fileName, "A file name must be provided");
		Validate.notNull(worldWindow, "A world window must be provided");
		
		File fileToRead = new File(fileName);
		return readAnimation(fileToRead, worldWindow);
	}

	@Override
	public Animation readAnimation(File file, WorldWindow worldWindow)
	{
		Validate.notNull(file, "A file must be provided");
		Validate.notNull(worldWindow, "A world window must be provided");
		
		Document xmlDocument = WWXML.openDocument(file);
		Validate.notNull(xmlDocument, "File " + file.getName() + " is not a valid XML animation file.");
		
		AnimationFileVersion version = getFileVersion(file);
		if (version == null)
		{
			throw new IllegalArgumentException("File " + file.getName() + " is not a valid XML animation file.");
		}
		
		AVList context = new AVListImpl();
		context.setValue(version.getConstants().getWorldWindowKey(), worldWindow);
		
		switch (version)
		{
			case VERSION010:
			{
				// Transform the V1 -> V2, then process as normal
				try
				{
					xmlDocument = getTransformedDocument(xmlDocument);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("File " + file.getName() + " is not a valid XML animation file.", e);
				}
				version = AnimationFileVersion.VERSION020;
				
			}
			default:
			{
				Element animationElement = WWXML.getElement(xmlDocument.getDocumentElement(), version.getConstants().getAnimationElementName(), null);
				return new WorldWindAnimationImpl(worldWindow).fromXml(animationElement, version, context);
			}
			
		}
	}

	@Override
	public AnimationFileVersion getFileVersion(String fileName)
	{
		Validate.notBlank(fileName, "A file name must be provided");
		return getFileVersion(new File(fileName));
	}
	
	@Override
	public AnimationFileVersion getFileVersion(File file)
	{
		Validate.notNull(file, "A file must be provided");
		
		Document xmlDocument = WWXML.openDocument(file);
		if (xmlDocument == null)
		{
			return null;
		}
		
		Element rootElement = xmlDocument.getDocumentElement();
		// If it's V2 or above, get the version from the root node attribute
		if (rootElement.getNodeName().equals(AnimationFileVersion.VERSION020.getConstants().getRootElementName()))
		{
			return AnimationFileVersion.fromDisplayName(WWXML.getText(rootElement, XmlSerializable.ATTRIBUTE_PATH_PREFIX + WORLD_WIND_ANIMATION_VERSION, null));
		}
		// Otherwise, check for a V1 file
		else if (rootElement.getNodeName().equals(AnimationFileVersion.VERSION010.getConstants().getRootElementName()))
		{
			return AnimationFileVersion.VERSION010;
		}
		return null;
	}
	
	/**
	 * Get a transformed version of the provided V1 XML document, transformed into a V2 document
	 * 
	 * @param v1Document The document to transform into V2
	 * 
	 * @return The transformed document
	 */
	private Document getTransformedDocument(Document v1Document) throws Exception
	{
		Source v1Source = new DOMSource(v1Document);
		Source xsltSource = new StreamSource(getClass().getResourceAsStream("v1ToV2Transformer.xsl"));
		
		ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
		Result transformationResult = new StreamResult(resultStream);
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
		transformer.transform(v1Source, transformationResult);
		
		return XMLUtil.openDocumentStream(new ByteArrayInputStream(resultStream.toByteArray()));
	}
}
