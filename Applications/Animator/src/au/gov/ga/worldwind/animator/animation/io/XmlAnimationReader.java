package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.WWXML;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.WorldWindAnimationImpl;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An implementation of the {@link AnimationReader} interface that reads animations from
 * an XML file.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class XmlAnimationReader implements AnimationReader
{
	private static final String WORLD_WIND_ANIMATION_ELEMENT_NAME = AnimationFileVersion.VERSION020.getConstants().getWorldWindAnimationElementName();
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
		
		// Try to find a WorldWindAnimation element - if one isn't found assume it is a version 1 file
		AnimationFileVersion version = AnimationFileVersion.VERSION010;
		Element rootElement = xmlDocument.getDocumentElement();
		if (rootElement.getNodeName().equals(WORLD_WIND_ANIMATION_ELEMENT_NAME))
		{
			// V2 or above - extract the version from the element
			version = AnimationFileVersion.fromDisplayName(WWXML.getText(rootElement, XmlSerializable.ATTRIBUTE_PATH_PREFIX + WORLD_WIND_ANIMATION_VERSION, null));
		}
		
		switch (version)
		{
			case VERSION020:
			{
				Element animationElement = WWXML.getElement(rootElement, version.getConstants().getAnimationElementName(), null);
				AVList context = new AVListImpl();
				context.setValue(version.getConstants().getWorldWindowKey(), worldWindow);
				return new WorldWindAnimationImpl(worldWindow).fromXml(animationElement, version, context);
			}
		}
		
		return null;
	}

}
