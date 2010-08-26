package au.gov.ga.worldwind.animator.animation.io;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * An {@link AnimationWriter} that writes an {@link Animation} in the <em>version 2</em>
 * xml format.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class V2XmlAnimationWriter implements AnimationWriter
{

	@Override
	public void writeAnimation(String fileName, Animation animation) throws IOException
	{
		Validate.notBlank(fileName, "A filename must be provided");
		File outputFile = new File(fileName);
		writeAnimation(outputFile, animation);
	}

	@Override
	public void writeAnimation(File file, Animation animation) throws IOException
	{
		Document document = WWXML.createDocumentBuilder(false).newDocument();
		
		Element animationElement = document.createElement("animation");
		WWXML.setTextAttribute(animationElement, "version", "2.0");
		
		WWXML.saveDocumentToStream(document, new FileOutputStream(file));
	}


}
